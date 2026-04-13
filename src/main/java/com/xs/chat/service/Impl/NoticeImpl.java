package com.xs.chat.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xs.chat.Exception.BusinessException;
import com.xs.chat.context.BaseContext;
import com.xs.chat.enumeration.NoticeStatusEnum;
import com.xs.chat.enumeration.NoticeTypeEnum;
import com.xs.chat.enumeration.ResponseCodeEnum;
import com.xs.chat.event.NoticeSentEvent;
import com.xs.chat.handler.NoticeHandler;
import com.xs.chat.handler.NoticeHandlerFactory;
import com.xs.chat.mapper.SystemNoticeMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.SystemNoticeDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.NoticeHandleDTO;
import com.xs.chat.pojo.DTO.NoticeQueryDTO;
import com.xs.chat.pojo.VO.NoticeVO;
import com.xs.chat.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoticeImpl implements NoticeService {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private NoticeHandlerFactory noticeHandlerFactory;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String UNREAD_KEY_PREFIX = "chat:notice:unread:";
    private static final long UNREAD_CACHE_SECONDS = 86400;

    @Override
    public void sendNotice(SystemNoticeDO notice) {
        if (notice.getStatus() == null) {
            notice.setStatus(NoticeStatusEnum.UNREAD.getCode());
        }
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        systemNoticeMapper.insert(notice);

        // Redis 未读数 +1
        incrementUnreadCount(notice.getReceiverId(), notice.getType());

        // 发布事件 -> WS推送
        applicationEventPublisher.publishEvent(
                new NoticeSentEvent(this, notice.getId(), notice.getReceiverId(), notice.getType(), notice.getTitle()));

        log.info("发送通知: id={}, receiverId={}, type={}", notice.getId(), notice.getReceiverId(), notice.getType());
    }

    @Override
    public List<NoticeVO> getNoticeList(NoticeQueryDTO queryDTO) {
        String currentUserId = BaseContext.getCurrentId();
        LambdaQueryWrapper<SystemNoticeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNoticeDO::getReceiverId, currentUserId);

        if (queryDTO.getType() != null && !queryDTO.getType().isEmpty()) {
            wrapper.eq(SystemNoticeDO::getType, queryDTO.getType());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(SystemNoticeDO::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(SystemNoticeDO::getCreatedAt);

        // 分页
        int page = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
        int size = queryDTO.getSize() != null ? queryDTO.getSize() : 20;
        wrapper.last("LIMIT " + (page - 1) * size + "," + size);

        List<SystemNoticeDO> notices = systemNoticeMapper.selectList(wrapper);
        return notices.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public NoticeVO getNoticeDetail(Long noticeId) {
        SystemNoticeDO notice = systemNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "通知不存在");
        }
        // 只能查看自己的通知
        if (!notice.getReceiverId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_403, "无权查看此通知");
        }
        // 标记已读
        if (NoticeStatusEnum.UNREAD.getCode().equals(notice.getStatus())) {
            markAsRead(noticeId);
            notice.setStatus(NoticeStatusEnum.READ.getCode());
        }
        return convertToVO(notice);
    }

    @Override
    public void handleNotice(NoticeHandleDTO handleDTO) {
        SystemNoticeDO notice = systemNoticeMapper.selectById(handleDTO.getNoticeId());
        if (notice == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "通知不存在");
        }
        if (!notice.getReceiverId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_403, "无权操作此通知");
        }

        NoticeTypeEnum typeEnum = NoticeTypeEnum.fromCode(notice.getType());
        if (typeEnum == null || !typeEnum.isNeedAction()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "该通知无需处理");
        }

        NoticeStatusEnum currentStatus = NoticeStatusEnum.fromCode(notice.getStatus());
        if (currentStatus != NoticeStatusEnum.UNREAD && currentStatus != NoticeStatusEnum.READ) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "该通知已处理，当前状态: " + currentStatus.getDesc());
        }

        NoticeHandler handler = noticeHandlerFactory.getHandler(typeEnum);

        String action = handleDTO.getAction().toUpperCase();
        if ("ACCEPT".equals(action)) {
            handler.handleAccept(notice);
        } else if ("REJECT".equals(action)) {
            handler.handleReject(notice);
        } else {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "无效的操作: " + action);
        }

        // 处理后减少未读计数（如果之前是未读状态）
        if (currentStatus == NoticeStatusEnum.UNREAD) {
            decrementUnreadCount(notice.getReceiverId(), notice.getType());
        }
    }

    @Override
    public void markAsRead(Long noticeId) {
        SystemNoticeDO notice = systemNoticeMapper.selectById(noticeId);
        if (notice == null) {
            return;
        }
        if (!notice.getReceiverId().equals(BaseContext.getCurrentId())) {
            return;
        }
        if (NoticeStatusEnum.UNREAD.getCode().equals(notice.getStatus())) {
            SystemNoticeDO update = new SystemNoticeDO();
            update.setId(noticeId);
            update.setStatus(NoticeStatusEnum.READ.getCode());
            update.setUpdatedAt(LocalDateTime.now());
            systemNoticeMapper.updateById(update);
            decrementUnreadCount(notice.getReceiverId(), notice.getType());
        }
    }

    @Override
    public void markAllAsRead(String type) {
        String currentUserId = BaseContext.getCurrentId();
        LambdaUpdateWrapper<SystemNoticeDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNoticeDO::getReceiverId, currentUserId)
                .eq(SystemNoticeDO::getStatus, NoticeStatusEnum.UNREAD.getCode());
        if (type != null && !type.isEmpty()) {
            wrapper.eq(SystemNoticeDO::getType, type);
        }
        SystemNoticeDO update = new SystemNoticeDO();
        update.setStatus(NoticeStatusEnum.READ.getCode());
        update.setUpdatedAt(LocalDateTime.now());
        systemNoticeMapper.update(update, wrapper);

        // 清除未读数缓存
        clearUnreadCache(currentUserId, type);
    }

    @Override
    public Map<String, Object> getUnreadCount() {
        String currentUserId = BaseContext.getCurrentId();
        Map<String, Object> result = new HashMap<>();

        // 总未读数
        Long totalCount = getTotalUnreadFromDB(currentUserId);
        result.put("total", totalCount);

        // 按类型分组未读数
        Map<String, Long> typeCountMap = new HashMap<>();
        for (NoticeTypeEnum type : NoticeTypeEnum.values()) {
            LambdaQueryWrapper<SystemNoticeDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemNoticeDO::getReceiverId, currentUserId)
                    .eq(SystemNoticeDO::getStatus, NoticeStatusEnum.UNREAD.getCode())
                    .eq(SystemNoticeDO::getType, type.getCode());
            typeCountMap.put(type.getCode(), systemNoticeMapper.selectCount(wrapper));
        }
        result.put("byType", typeCountMap);
        return result;
    }

    @Override
    public void expireGroupInvites(Long chatRoomId) {
        LambdaUpdateWrapper<SystemNoticeDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemNoticeDO::getType, NoticeTypeEnum.GROUP_INVITE.getCode())
                .eq(SystemNoticeDO::getRelatedId, String.valueOf(chatRoomId))
                .in(SystemNoticeDO::getStatus, NoticeStatusEnum.UNREAD.getCode(), NoticeStatusEnum.READ.getCode());
        SystemNoticeDO update = new SystemNoticeDO();
        update.setStatus(NoticeStatusEnum.EXPIRED.getCode());
        update.setUpdatedAt(LocalDateTime.now());
        systemNoticeMapper.update(update, wrapper);
        log.info("群解散，过期群邀请通知: chatRoomId={}", chatRoomId);
    }

    // ========== Redis 缓存方法 ==========

    private void incrementUnreadCount(String receiverId, String type) {
        String totalKey = UNREAD_KEY_PREFIX + receiverId;
        String typeKey = UNREAD_KEY_PREFIX + receiverId + ":" + type;
        redisTemplate.opsForValue().increment(totalKey);
        redisTemplate.opsForValue().increment(typeKey);
        redisTemplate.expire(totalKey, Duration.ofSeconds(UNREAD_CACHE_SECONDS));
        redisTemplate.expire(typeKey, Duration.ofSeconds(UNREAD_CACHE_SECONDS));
    }

    private void decrementUnreadCount(String receiverId, String type) {
        String totalKey = UNREAD_KEY_PREFIX + receiverId;
        String typeKey = UNREAD_KEY_PREFIX + receiverId + ":" + type;
        Object totalObj = redisTemplate.opsForValue().get(totalKey);
        if (totalObj != null) {
            long total = Long.parseLong(totalObj.toString());
            if (total > 0) {
                redisTemplate.opsForValue().decrement(totalKey);
            }
        }
        Object typeObj = redisTemplate.opsForValue().get(typeKey);
        if (typeObj != null) {
            long count = Long.parseLong(typeObj.toString());
            if (count > 0) {
                redisTemplate.opsForValue().decrement(typeKey);
            }
        }
    }

    private void clearUnreadCache(String receiverId, String type) {
        String totalKey = UNREAD_KEY_PREFIX + receiverId;
        if (type != null && !type.isEmpty()) {
            redisTemplate.delete(UNREAD_KEY_PREFIX + receiverId + ":" + type);
        } else {
            // 清除所有类型的未读缓存
            Set<Object> keys = redisTemplate.keys(UNREAD_KEY_PREFIX + receiverId + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
        // 重建总未读数
        Long total = getTotalUnreadFromDB(receiverId);
        redisTemplate.opsForValue().set(totalKey, total, Duration.ofSeconds(UNREAD_CACHE_SECONDS));
    }

    private Long getTotalUnreadFromDB(String userId) {
        LambdaQueryWrapper<SystemNoticeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNoticeDO::getReceiverId, userId)
                .eq(SystemNoticeDO::getStatus, NoticeStatusEnum.UNREAD.getCode());
        return systemNoticeMapper.selectCount(wrapper);
    }

    // ========== VO 转换 ==========

    private NoticeVO convertToVO(SystemNoticeDO notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setReceiverId(notice.getReceiverId());
        vo.setSenderId(notice.getSenderId());
        vo.setType(notice.getType());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setRelatedId(notice.getRelatedId());
        vo.setExtraData(notice.getExtraData());
        vo.setStatus(notice.getStatus());
        vo.setExpiredAt(notice.getExpiredAt());
        vo.setCreatedAt(notice.getCreatedAt());

        // 枚举描述
        NoticeTypeEnum typeEnum = NoticeTypeEnum.fromCode(notice.getType());
        if (typeEnum != null) {
            vo.setTypeDesc(typeEnum.getDesc());
            vo.setNeedAction(typeEnum.isNeedAction());
        }
        NoticeStatusEnum statusEnum = NoticeStatusEnum.fromCode(notice.getStatus());
        if (statusEnum != null) {
            vo.setStatusDesc(statusEnum.getDesc());
        }

        // 发送者信息
        if (notice.getSenderId() != null) {
            UserDO sender = userInfoMapper.selectById(notice.getSenderId());
            if (sender != null) {
                vo.setSenderNickname(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }
        }
        return vo;
    }
}
