package com.xs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xs.chat.pojo.DO.ChatRoomMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatRoomMemberMapper extends BaseMapper<ChatRoomMemberDO> {
    void BatchInsert(List<ChatRoomMemberDO> chatRoomMemberDOList);

    ChatRoomMemberDO selectByChatRoomIdAndUserId(Long chatRoomId, String userId);

    void updateLastReadMessageId(Long chatRoomId, String userId, Long lastReadMessageId);
}
