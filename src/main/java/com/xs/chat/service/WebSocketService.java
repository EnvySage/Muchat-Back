package com.xs.chat.service;

import com.xs.chat.pojo.DO.ChatRoomDO;
import com.xs.chat.pojo.VO.ChatRoomVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public interface WebSocketService {
    void onConnect(Channel channel);
    void disconnect(Channel channel);
    String authenticate(Channel channel, String token);
    void handleMessage(Channel channel, String content);

    void updateOnlineBroadcast();
    void groupCreatedBroadcast(ChatRoomVO chatRoomVO);

    /**
     * 群聊创建广播（含被邀请人）
     * @param chatRoomVO 群聊信息
     * @param invitedUserIds 被邀请人ID列表（收到邀请通知但尚未入群的用户）
     */
    void groupCreatedBroadcast(ChatRoomVO chatRoomVO, List<String> invitedUserIds);

    /**
     * 群权限/成员变更广播，推送给群内在线成员
     * @param chatRoomId 群聊ID
     * @param action 变更类型（如 KICK、MUTE、UNMUTE、ADMIN_ADD、ADMIN_REMOVE、GROUP_INFO_UPDATE、DISMISS）
     * @param data 变更数据（JSON字符串）
     */
    void groupPermissionBroadcast(Long chatRoomId, String action, String data);

    /**
     * 推送新通知给指定用户
     * @param receiverId 接收者用户ID
     * @param noticeId 通知ID
     * @param type 通知类型
     * @param title 通知标题
     */
    void sendNoticePush(String receiverId, Long noticeId, String type, String title);

    /**
     * 向指定在线用户推送原始JSON字符串消息
     * @param userId 目标用户ID
     * @param message JSON字符串消息
     */
    void sendToUser(String userId, String message);
}
