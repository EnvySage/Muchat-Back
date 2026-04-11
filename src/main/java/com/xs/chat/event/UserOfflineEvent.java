package com.xs.chat.event;

import com.xs.chat.pojo.DO.UserDO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class UserOfflineEvent extends ApplicationEvent {
    private final UserDO userDO;
    public UserOfflineEvent(Object source, UserDO userDO) {
        super(source);
        this.userDO = userDO;
    }
}
