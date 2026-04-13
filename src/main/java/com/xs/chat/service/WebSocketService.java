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

    /**
     * 群权限/成员变更广播，推送给群内在线成员
     * @param chatRoomId 群聊ID
     * @param action 变更类型（如 KICK、MUTE、UNMUTE、ADMIN_ADD、ADMIN_REMOVE、GROUP_INFO_UPDATE、DISMISS）
     * @param data 变更数据（JSON字符串）
     */
    void groupPermissionBroadcast(Long chatRoomId, String action, String data);
}
