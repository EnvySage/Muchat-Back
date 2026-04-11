package com.xs.chat.websocket;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.xs.chat.Utils.NettyUtil;
import com.xs.chat.context.BaseContext;
import com.xs.chat.pojo.DTO.AuthDTO;
import com.xs.chat.pojo.DTO.AuthResultDTO;
import com.xs.chat.pojo.DTO.MessageDTO;
import com.xs.chat.pojo.DTO.SystemMsgDTO;
import com.xs.chat.service.WebSocketService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


@ChannelHandler.Sharable
@Slf4j
public class NettyWebsocketHandle extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 用户ID 与 Channel 的映射


    private WebSocketService webSocketService;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        webSocketService = SpringUtil.getBean(WebSocketService.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String content = msg.text();
        log.info("接收到消息：{}", content);
        SystemMsgDTO systemMsg = JSONUtil.toBean(content, SystemMsgDTO.class);
        switch (systemMsg.getType()) {
            case "AUTH" -> handleAuth(ctx, content);
            case "HEARTBEAT" -> log.debug("心跳检测",content);
            default -> webSocketService.handleMessage(ctx.channel(), content);
        }
    }
    private void handleAuth(ChannelHandlerContext ctx, String content) {
        AuthDTO authDTO = JSONUtil.toBean(content, AuthDTO.class);
        String token = authDTO.getToken();
        String userId = webSocketService.authenticate(ctx.channel(), token);

        AuthResultDTO result = new AuthResultDTO();
        result.setType("AUTH_RESULT");

        if (userId != null) {
            result.setCode(0);
            result.setMessage("认证成功");
            result.setUserId(userId);
            log.info("WebSocket认证成功 - userId: {}", userId);
        } else {
            result.setCode(1);
            result.setMessage("认证失败");
            log.info("WebSocket认证失败");
        }

        ctx.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(result)));
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        webSocketService.disconnect(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常：{}", cause.getMessage());
        webSocketService.disconnect(ctx.channel());
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleEvent) {
            if (idleEvent.state() == IdleState.READER_IDLE) {
                log.info("读空闲超时，断开连接");
                webSocketService.disconnect(ctx.channel());
            }
        } else if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            webSocketService.onConnect(ctx.channel());
            log.info("WebSocket握手完成");
        }
        super.userEventTriggered(ctx, evt);
    }

}





















