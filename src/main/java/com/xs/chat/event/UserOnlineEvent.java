package com.xs.chat.event;

import com.xs.chat.pojo.DO.UserDO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class UserOnlineEvent extends ApplicationEvent {
    private final UserDO userDO;
    public UserOnlineEvent(Object source,UserDO userDO) {
        super(source);
        this.userDO = userDO;
    }
}
