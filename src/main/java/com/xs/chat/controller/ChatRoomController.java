package com.xs.chat.controller;

import com.xs.chat.Annotation.GlobalInterceptor;
import com.xs.chat.Annotation.RequirePermission;
import com.xs.chat.context.BaseContext;
import com.xs.chat.enumeration.permission.GroupPermissionEnum;
import com.xs.chat.pojo.DO.ChatRoomMemberDO;
import com.xs.chat.pojo.DTO.ChatRoomDTO;
import com.xs.chat.pojo.DTO.ChatRoomMemberDTO;
import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.ChatRoomMemberVO;
import com.xs.chat.pojo.VO.ChatRoomVO;
import com.xs.chat.service.ChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatRoom")
@Slf4j
public class ChatRoomController {
    @Autowired
    private ChatRoomService chatRoomService;
    @GlobalInterceptor(checkParams = true)
    @GetMapping("/getAllRoom")
    public Result<List<ChatRoomVO>> getAllRoom(){
        List<ChatRoomVO> chatRoomVOList = chatRoomService.getAllRoom();
        if (chatRoomVOList == null) return Result.error("获取所有群聊失败");
        log.info("获取所有群聊：{}", chatRoomVOList);
        return Result.success(chatRoomVOList);
    }
    @GlobalInterceptor(checkParams = true)
    @PostMapping("/createRoom")
    public Result<ChatRoomVO> createRoom(@RequestBody ChatRoomDTO chatRoomDTO){
        ChatRoomVO chatRoomVO = chatRoomService.createRoom(chatRoomDTO);
        if (chatRoomVO == null) return Result.error("创建群聊失败");
        return Result.success(chatRoomVO);
    }
    @RequirePermission(GroupPermissionEnum.SEND_MESSAGE)
    @GlobalInterceptor(checkParams = true)
    @PostMapping("/JoinChatRoom")
    public Result<String> joinRoom(@RequestBody ChatRoomMemberDTO chatRoomMemberDTO){
        chatRoomService.joinRoom(chatRoomMemberDTO);
        log.info("加入群聊成功");
        return Result.success("群聊加入成功");
    }

    @DeleteMapping("/exitRoom/{chatRoomId}")
    public Result<String> exitRoom(@PathVariable Long chatRoomId){
        ChatRoomMemberDTO chatRoomMemberDTO = new ChatRoomMemberDTO();
        chatRoomMemberDTO.setChatRoomId(chatRoomId);
        chatRoomMemberDTO.setUserId(BaseContext.getCurrentId());
        boolean flag =chatRoomService.exitRoom(chatRoomMemberDTO);
        if (!flag) return Result.error("退出群聊失败");
        return Result.success("退出群聊成功");
    }

    @GlobalInterceptor(checkParams = true)
    @PutMapping("/read")
    public Result<ChatRoomMemberVO> unreadMessage(@RequestBody ChatRoomMemberDTO chatRoomMemberDTO){
        ChatRoomMemberVO chatRoomMemberVO = chatRoomService.unreadMessage(chatRoomMemberDTO);
        log.info("用户{}-房间{}-未读消息数:{}",chatRoomMemberVO.getUserId(),chatRoomMemberVO.getChatRoomId(),chatRoomMemberVO.getUnreadCount());
        return Result.success(chatRoomMemberVO);
    }

    @RequirePermission(GroupPermissionEnum.UPDATE_GROUP_INFO)
    @GlobalInterceptor(checkParams = true)
    @PutMapping("/updateGroupInfo")
    public Result<String> updateRoomInfo(@RequestBody ChatRoomDTO chatRoomDTO){
        boolean flag =chatRoomService.updateRoom(chatRoomDTO);
        log.info("更新群聊信息:{}",chatRoomDTO);
        if (flag ==false) return Result.error("更新群聊失败");
        return Result.success("更新群聊成功");
    }


    @RequirePermission(GroupPermissionEnum.UPDATE_SELF_NICKNAME)
    @GlobalInterceptor(checkParams = true)
    @PutMapping("/updateGroupNickname")
    public Result<String> updateGroupNickname(@RequestBody ChatRoomMemberDTO chatRoomMemberDTO){
        chatRoomService.updateGroupNickname(chatRoomMemberDTO);
        return Result.success("更新群昵称成功");
    }

    @RequirePermission(GroupPermissionEnum.INVITE_MEMBER)
    @GlobalInterceptor(checkParams = true)
    @PostMapping("/inviteGroup")
    public Result<String> inviteGroup(@RequestBody ChatRoomMemberDTO chatRoomMemberDTO){
        chatRoomService.inviteGroup(chatRoomMemberDTO);
        return Result.success("邀请成功{}",chatRoomMemberDTO.getUserId());
    }
    @RequirePermission(GroupPermissionEnum.KICK_MEMBER)
    @GlobalInterceptor(checkParams = true)
    @PostMapping("/kickGroup")
    public Result<String> kickGroup(@RequestBody ChatRoomMemberDTO chatRoomMemberDTO){
        boolean flag =chatRoomService.kickGroup(chatRoomMemberDTO);
        if (!flag) return Result.error("踢出失败");
        return Result.success("踢出成功{}",chatRoomMemberDTO.getUserId());
    }

    @RequirePermission(GroupPermissionEnum.MUTE_MEMBER)
    @GlobalInterceptor(checkParams = true)
    @PutMapping("/batchMute")
    public Result<String> batchMute(@RequestBody ChatRoomMemberDTO chatRoomMemberDTO) {
        boolean flag = chatRoomService.batchMute(chatRoomMemberDTO);
        if (!flag) return Result.error("批量禁言操作失败");
        return Result.success(chatRoomMemberDTO.getIsMuted() == 1 ? "禁言成功" : "解禁成功");
    }
    @RequirePermission(GroupPermissionEnum.DISSOLVE_GROUP)
    @GlobalInterceptor(checkParams = true)
    @DeleteMapping("/dismissRoom/{chatRoomId}")
    public Result<String> dismissRoom(@PathVariable Long chatRoomId){
        boolean flag =chatRoomService.dismissRoom(chatRoomId);
        if (!flag) return Result.error("解散群聊失败");
        return Result.success("解散群聊成功");
    }

    @RequirePermission(GroupPermissionEnum.MANAGE_ADMIN)
    @GlobalInterceptor(checkParams = true)
    @PutMapping("/changeAdmin")
    public Result<String> changeAdmin(@RequestBody ChatRoomMemberDTO chatRoomMemberDTO){
        log.info("changeAdmin:{}",chatRoomMemberDTO);
        boolean flag = chatRoomService.changeAdmin(chatRoomMemberDTO);
        if (!flag) return Result.error(chatRoomMemberDTO.getIsAdmin() == 1 ? "设置管理员失败" : "取消管理员失败");
        return Result.success(chatRoomMemberDTO.getIsAdmin() == 1 ? "设置管理员成功" : "取消管理员成功");
    }

    @GlobalInterceptor(checkParams = true)
    @GetMapping("/search")
    public Result<List<ChatRoomVO>> searchPublicRooms(@RequestParam String keyword) {
        List<ChatRoomVO> chatRoomVOList = chatRoomService.searchPublicRooms(keyword);
        return Result.success(chatRoomVOList);
    }
}
