package com.xs.chat.controller;

import com.xs.chat.Annotation.GlobalInterceptor;
import com.xs.chat.context.BaseContext;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.UserDTO;
import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.UserVO;
import com.xs.chat.service.UserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    @GlobalInterceptor(checkParams = true)
    @PutMapping("/updateUserInfo")
    public Result<String> updateUserInfo(@RequestBody UserDTO userDTO){
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDTO,userDO);
        userDO.setId(BaseContext.getCurrentId());
        boolean flag = userInfoService.updateUserInfo(userDO);
        if (!flag) return Result.error("更新失败");
        return Result.success("更新成功");
    }

}
