package com.xs.chat.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.extra.spring.SpringUtil;
import com.xs.chat.Utils.JWTUtils;
import com.xs.chat.Utils.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest){
            FullHttpRequest request = (FullHttpRequest) msg;
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.uri());

            String token = Optional.ofNullable(urlBuilder.getQuery()).map(query -> query.get("token"))
                    .map(CharSequence::toString).orElse("");
            NettyUtil.setAttr(ctx.channel(),NettyUtil.TOKEN,token);
            ctx.fireChannelRead(msg);
        }else {
            ctx.fireChannelRead(msg);
        }
    }
}
