package com.xs.chat.service;

import com.xs.chat.pojo.DO.ChatRoomDO;
import com.xs.chat.pojo.VO.ChatRoomVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface WebSocketService {
    void onConnect(Channel channel);
    void disconnect(Channel channel);
    String authenticate(Channel channel, String token);
    void handleMessage(Channel channel, String content);

    void updateOnlineBroadcast();
    void groupCreatedBroadcast(ChatRoomVO chatRoomVO);
}
