package com.xs.chat.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xs.chat.constants.RedisConstant;
import com.xs.chat.mapper.MessageMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.MessageDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.VO.MessageVO;
import com.xs.chat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;
    @Override
    public List<MessageVO> getMessageByRoomId(Long id, Integer size, Long beforeTime) {
        log.info("getMessageByRoomId: id={}, size={}, beforeTime={}", id, size, beforeTime);
        //redis 缓存
        int pageSize = (size == null || size <= 0) ? 50 : Math.min(size,100);
        String redisKey = RedisConstant.CHATROOM + id + ":recent";
        if (beforeTime == null){
            List<Object> cached = redisTemplate.opsForList().range(redisKey, 0, -1);
            if (cached != null && !cached.isEmpty()){
                return cached.stream().map(
                        item->(MessageVO)item
                ).collect(Collectors.toList());
            }
        }

        LambdaQueryWrapper<MessageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageDO::getChatRoomId, id).lt(beforeTime != null,MessageDO::getSentAt,beforeTime).orderByDesc(MessageDO::getId).last("LIMIT " + pageSize);
        List<MessageDO> messageDOList = messageMapper.selectList(queryWrapper);
        if (messageDOList == null || messageDOList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, UserDO> userMap = new HashMap<>();
        List<MessageVO> messageVOList = messageDOList.stream().map(item -> {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(item, messageVO);
            messageVO.setId(item.getId());
            UserDO userDO = userMap.computeIfAbsent(item.getSenderId(), userInfoMapper::selectById);
            if (userDO != null) {
                messageVO.setSenderName(userDO.getNickname());
                messageVO.setSenderAvatar(userDO.getAvatar());
            }
            return messageVO;
        }).sorted(Comparator.comparing(MessageVO::getSentAt)).collect(Collectors.toList());
        if(messageVOList.size() <=1){
            return messageVOList;
        }
        if (beforeTime == null) {
            redisTemplate.delete(redisKey);
            redisTemplate.opsForList().rightPushAll(redisKey, new ArrayList<>(messageVOList));
            redisTemplate.opsForList().trim(redisKey, 0, 199);
            redisTemplate.expire(redisKey, Duration.ofHours(2));
        }
        return messageVOList;
    }
}
