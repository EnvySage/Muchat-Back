package com.xs.chat.controller;

import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.MessageVO;
import com.xs.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("msg")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @GetMapping("/list/{id}")
    public Result<List<MessageVO>> getMessageById(@PathVariable Long id,@RequestParam(defaultValue = "50") Integer size,@RequestParam(required = false) Long beforeTime){
        List<MessageVO> messageVOList = messageService.getMessageByRoomId(id,size,beforeTime);
        return Result.success(messageVOList);
    }
}
