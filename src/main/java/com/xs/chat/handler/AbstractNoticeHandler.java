package com.xs.chat.handler;

import com.xs.chat.Exception.BusinessException;
import com.xs.chat.enumeration.NoticeStatusEnum;
import com.xs.chat.enumeration.ResponseCodeEnum;
import com.xs.chat.event.NoticeHandledEvent;
import com.xs.chat.mapper.SystemNoticeMapper;
import com.xs.chat.pojo.DO.SystemNoticeDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public abstract class AbstractNoticeHandler implements NoticeHandler {

    @Autowired
    protected SystemNoticeMapper systemNoticeMapper;
    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void handleAccept(SystemNoticeDO notice) {
        if (!validate(notice)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600, "该邀请已失效");
        }
        doAccept(notice);
        updateNoticeStatus(notice.getId(), NoticeStatusEnum.ACCEPTED);
        // 发布通知处理事件 -> WS推送给发起方
        applicationEventPublisher.publishEvent(
                new NoticeHandledEvent(this, notice.getId(), notice.getSenderId(),
                        notice.getReceiverId(), notice.getType(), "ACCEPT", notice.getTitle()));
        log.info("通知已同意: id={}, type={}", notice.getId(), notice.getType());
    }

    @Override
    public void handleReject(SystemNoticeDO notice) {
        updateNoticeStatus(notice.getId(), NoticeStatusEnum.REJECTED);
        // 发布通知处理事件 -> WS推送给发起方
        applicationEventPublisher.publishEvent(
                new NoticeHandledEvent(this, notice.getId(), notice.getSenderId(),
                        notice.getReceiverId(), notice.getType(), "REJECT", notice.getTitle()));
        log.info("通知已拒绝: id={}, type={}", notice.getId(), notice.getType());
    }

    protected abstract void doAccept(SystemNoticeDO notice);

    protected void updateNoticeStatus(Long noticeId, NoticeStatusEnum status) {
        SystemNoticeDO update = new SystemNoticeDO();
        update.setId(noticeId);
        update.setStatus(status.getCode());
        systemNoticeMapper.updateById(update);
    }
}
