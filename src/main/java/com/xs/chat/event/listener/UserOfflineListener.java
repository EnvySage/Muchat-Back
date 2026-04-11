package com.xs.chat.event.listener;

import com.xs.chat.event.UserOfflineEvent;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.service.Impl.WebSocketImpl;
import com.xs.chat.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class UserOfflineListener{
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private WebSocketImpl webSocket;

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void updateUserInfo(UserOfflineEvent event){
        UserDO user = event.getUserDO();
        user.setLastLoginAt(LocalDate.now());
        user.setOnlineStatus(0);
        userInfoService.updateUserInfo(user);
        webSocket.updateOnlineBroadcast();
    }
}
