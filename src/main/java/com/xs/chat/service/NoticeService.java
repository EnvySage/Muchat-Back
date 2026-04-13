package com.xs.chat.service;

import com.xs.chat.pojo.DO.SystemNoticeDO;
import com.xs.chat.pojo.DTO.NoticeHandleDTO;
import com.xs.chat.pojo.DTO.NoticeQueryDTO;
import com.xs.chat.pojo.VO.NoticeVO;

import java.util.List;
import java.util.Map;

public interface NoticeService {

    void sendNotice(SystemNoticeDO notice);

    List<NoticeVO> getNoticeList(NoticeQueryDTO queryDTO);

    NoticeVO getNoticeDetail(Long noticeId);

    void handleNotice(NoticeHandleDTO handleDTO);

    void markAsRead(Long noticeId);

    void markAllAsRead(String type);

    Map<String, Object> getUnreadCount();

    /**
     * 群解散时，将所有待处理的群邀请通知设为过期
     * @param chatRoomId 群聊ID
     */
    void expireGroupInvites(Long chatRoomId);
}
