package com.xs.chat.service;

import com.xs.chat.pojo.DTO.ChatRoomDTO;
import com.xs.chat.pojo.DTO.ChatRoomMemberDTO;
import com.xs.chat.pojo.VO.ChatRoomMemberVO;
import com.xs.chat.pojo.VO.ChatRoomVO;

import java.util.List;

public interface ChatRoomService {
    void joinRoom(ChatRoomMemberDTO chatRoomMemberDTO);

    List<ChatRoomVO> getAllRoom();

    ChatRoomVO createRoom(ChatRoomDTO chatRoomDTO);

    ChatRoomMemberVO unreadMessage(ChatRoomMemberDTO chatRoomMemberDTO);

    void updateGroupNickname(ChatRoomMemberDTO chatRoomMemberDTO);

    void inviteGroup(ChatRoomMemberDTO chatRoomMemberDTO);

    boolean kickGroup(ChatRoomMemberDTO chatRoomMemberDTO);


    boolean exitRoom(ChatRoomMemberDTO chatRoomMemberDTO);

    boolean updateRoom(ChatRoomDTO chatRoomDTO);

    boolean batchMute(ChatRoomMemberDTO chatRoomMemberDTO);

    boolean dismissRoom(Long chatRoomId);

    boolean changeAdmin(ChatRoomMemberDTO chatRoomMemberDTO);

    List<ChatRoomVO> searchPublicRooms(String keyword);
}
