package com.xs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xs.chat.pojo.DO.MessageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<MessageDO> {

    /**
     * 统计未读消息数量
     * @param chatRoomId 聊天室ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 未读消息数量
     */
    Integer countUnreadMessages(Long chatRoomId, Long lastReadMessageId);

    /**
     * 获取聊天室最新一条消息
     * @param chatRoomId 聊天室ID
     * @return 最新消息
     */
    MessageDO selectLatestMessage(Long chatRoomId);
}
