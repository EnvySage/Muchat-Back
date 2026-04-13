package com.xs.chat.controller;

import com.xs.chat.Annotation.GlobalInterceptor;
import com.xs.chat.enumeration.NoticeStatusEnum;
import com.xs.chat.pojo.DTO.NoticeHandleDTO;
import com.xs.chat.pojo.DTO.NoticeQueryDTO;
import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.NoticeVO;
import com.xs.chat.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notice")
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GlobalInterceptor(checkParams = true)
    @GetMapping("/list")
    public Result<List<NoticeVO>> getNoticeList(NoticeQueryDTO queryDTO) {
        List<NoticeVO> list = noticeService.getNoticeList(queryDTO);
        return Result.success(list);
    }

    @GlobalInterceptor(checkParams = true)
    @GetMapping("/detail/{id}")
    public Result<NoticeVO> getNoticeDetail(@PathVariable Long id) {
        NoticeVO vo = noticeService.getNoticeDetail(id);
        return Result.success(vo);
    }

    @GlobalInterceptor(checkParams = true)
    @PostMapping("/handle")
    public Result<String> handleNotice(@RequestBody NoticeHandleDTO handleDTO) {
        noticeService.handleNotice(handleDTO);
        return Result.success("ACCEPT".equalsIgnoreCase(handleDTO.getAction()) ? "已同意" : "已拒绝");
    }

    @GlobalInterceptor(checkParams = true)
    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        noticeService.markAsRead(id);
        return Result.success("已读");
    }

    @GlobalInterceptor(checkParams = true)
    @PutMapping("/readAll")
    public Result<String> markAllAsRead(@RequestParam(required = false) String type) {
        noticeService.markAllAsRead(type);
        return Result.success("全部已读");
    }

    @GlobalInterceptor(checkParams = true)
    @GetMapping("/unreadCount")
    public Result<Map<String, Object>> getUnreadCount() {
        Map<String, Object> count = noticeService.getUnreadCount();
        return Result.success(count);
    }
}
