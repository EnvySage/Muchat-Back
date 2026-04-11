package com.xs.chat.service.Impl;

import com.xs.chat.Exception.BusinessException;
import com.xs.chat.constants.RedisConstant;
import com.xs.chat.enumeration.ResponseCodeEnum;
import com.xs.chat.enumeration.permission.GroupPermissionEnum;
import com.xs.chat.enumeration.permission.GroupRoleEnum;
import com.xs.chat.mapper.ChatRoomMemberMapper;
import com.xs.chat.pojo.DO.ChatRoomMemberDO;
import com.xs.chat.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
@Slf4j
@Service
public class PermissionImpl implements PermissionService {
    private static final long CACHE_EXPIRE_TIME = 100000;
    @Autowired
    private ChatRoomMemberMapper chatRoomMemberMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void checkPermission(Long chatRoomId, String userId, GroupPermissionEnum permission) {
        GroupRoleEnum role = getRole(chatRoomId, userId);
        if (role == null){
            throw new BusinessException(ResponseCodeEnum.CODE_403,"不在该群聊");
        }
        if(permission== GroupPermissionEnum.SEND_MESSAGE){
            ChatRoomMemberDO memberDO = chatRoomMemberMapper.selectByChatRoomIdAndUserId(chatRoomId,userId);
            if (memberDO!=null && memberDO.getIsMuted() != null &&memberDO.getIsMuted()==1){
                throw new BusinessException(ResponseCodeEnum.CODE_403,"您已被禁言");
            }
        }
        if (!role.hasPermission(permission)){
            throw new BusinessException(ResponseCodeEnum.CODE_403,"没有权限"+permission.getDesc());
        }
    }

    @Override
    public GroupRoleEnum getRole(Long chatRoomId, String userId) {
        String cacheKey = RedisConstant.GroupRole + chatRoomId + ":" + userId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null){
            return GroupRoleEnum.fromCode(cached);
        }

        ChatRoomMemberDO memberDO = chatRoomMemberMapper.selectByChatRoomIdAndUserId(chatRoomId,userId);
        if (memberDO == null){
            return null;
        }
        stringRedisTemplate.opsForValue().set(cacheKey, memberDO.getRole(), Duration.ofSeconds(CACHE_EXPIRE_TIME));
        return GroupRoleEnum.fromCode(memberDO.getRole());
    }

    @Override
    public void cleanRoleCache(Long chatRoomId, String userId) {
        String cacheKey = RedisConstant.GroupRole + chatRoomId + ":" + userId;
        stringRedisTemplate.delete(cacheKey);
        log.info("清除角色缓存: chatRoomId={}, userId={}", chatRoomId, userId);
    }

    @Override
    public void cleanRoomCache(Long chatRoomId) {
        String cacheKey = RedisConstant.GroupRole + chatRoomId + ":*";
        Set<String> keys = stringRedisTemplate.keys(cacheKey);
        if (keys != null && !keys.isEmpty()){
            stringRedisTemplate.delete(keys);
            log.info("清除群聊角色缓存: chatRoomId={}, 删除{}条", chatRoomId, keys.size());
        }
    }
}
