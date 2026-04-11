package com.xs.chat.event.listener;

import com.xs.chat.event.UserOfflineEvent;
import com.xs.chat.event.UserOnlineEvent;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.service.UserInfoService;
import com.xs.chat.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class UserOnlineListener {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private WebSocketService webSocketService;
    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void updateUserInfo(UserOnlineEvent event){
        UserDO user = event.getUserDO();
        user.setOnlineStatus(1);
        userInfoService.updateUserInfo(user);
        webSocketService.updateOnlineBroadcast();
    }
}
