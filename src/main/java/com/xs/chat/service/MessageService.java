package com.xs.chat.service;

import com.xs.chat.pojo.DTO.ChatRoomMemberDTO;
import com.xs.chat.pojo.VO.MessageVO;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {
    List<MessageVO> getMessageByRoomId(Long id, Integer size, Long beforeTime);


}
