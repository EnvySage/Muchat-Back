package com.xs.chat.controller;

import com.xs.chat.Annotation.GlobalInterceptor;
import com.xs.chat.pojo.DTO.ContactsDTO;
import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.ContactsVO;
import com.xs.chat.service.ContactService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
public class ContactsController {
    @Resource
    private ContactService contactService;
    @GlobalInterceptor(checkParams = true)
    @GetMapping("/all")
    public Result<List<ContactsVO>> getAllContacts(){
        List<ContactsVO> contactsVOList = contactService.getAllById();
        return Result.success(contactsVOList);
    }

    @GlobalInterceptor(checkParams = true)
    @PostMapping("/request/{contactId}")
    public Result<String> sendFriendRequest(@PathVariable("contactId") String contactId){
        contactService.sendFriendRequest(contactId);
        return Result.success("好友邀请已发送");
    }

    @GlobalInterceptor(checkParams = true)
    @DeleteMapping("/delete/{contactId}")
    public Result<String> deleteContact(@PathVariable("contactId") String contactId){
        contactService.deleteContact(contactId);
        return Result.success("删除成功");
    }
    @GlobalInterceptor(checkParams = true)
    @PutMapping("/update")
    public Result<String> updateContact(@RequestBody ContactsDTO contactsDTO){
        boolean flag = contactService.updateContact(contactsDTO);
        if (!flag) return Result.error("更新失败");
        return Result.success("更新成功");
    }
}
