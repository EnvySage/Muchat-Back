package com.xs.chat.event.listener;

import com.xs.chat.Annotation.RequirePermission;
import com.xs.chat.enumeration.permission.GroupPermissionEnum;
import com.xs.chat.event.MessageSentEvent;
import com.xs.chat.mapper.MessageMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.MessageDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.MessageDTO;
import com.xs.chat.pojo.VO.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MessageSentListener {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Async
    @EventListener(classes = MessageSentEvent.class)
    public void saveMessage(MessageSentEvent event){
        MessageDTO messageDTO = event.getMessageDTO();
        if (messageDTO == null || messageDTO.getChatRoomId() == null || messageDTO.getSenderId() == null) {
            log.warn("消息落库跳过，参数不完整: {}", messageDTO);
            return;
        }
        MessageDO messageDO = new MessageDO();
        BeanUtils.copyProperties(messageDTO, messageDO);
        messageMapper.insert(messageDO);

        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(messageDO, messageVO);
        UserDO userDO = userInfoMapper.selectById(messageDO.getSenderId());
        if (userDO != null) {
            messageVO.setSenderName(userDO.getNickname());
            messageVO.setSenderAvatar(userDO.getAvatar());
        }

        String redisKey = "chat:room:" + messageDO.getChatRoomId() + ":recent";
        redisTemplate.opsForList().rightPush(redisKey, messageVO);
        redisTemplate.opsForList().trim(redisKey, 0, 199);
        redisTemplate.expire(redisKey, Duration.ofHours(2));

        log.info("保存消息并写入缓存，chatRoomId:{}, senderId:{}", messageDO.getChatRoomId(), messageDO.getSenderId());
    }
}
