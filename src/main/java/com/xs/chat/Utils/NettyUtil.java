package com.xs.chat.Utils;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NettyUtil {
    public static AttributeKey<String> TOKEN = AttributeKey.valueOf("token");
    public static AttributeKey<String> ID = AttributeKey.valueOf("id");

    public static <T> void setAttr(Channel channel,AttributeKey<T> attributeKey,T data){
        Attribute<T> attribute = channel.attr(attributeKey);
        attribute.set(data);
    }
    public static <T> T getAttr(Channel channel,AttributeKey<T> attributeKey){
        return channel.attr(attributeKey).get();
    }
}
