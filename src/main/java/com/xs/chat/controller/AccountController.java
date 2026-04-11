package com.xs.chat.controller;

import com.xs.chat.Annotation.GlobalInterceptor;
import com.xs.chat.Utils.CheckCodeUtil;
import com.xs.chat.Utils.JWTUtils;
import com.xs.chat.constants.CheckCodeConstant;
import com.xs.chat.pojo.DTO.EmailCodeDTO;
import com.xs.chat.pojo.DTO.UserDTO;
import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.UserVO;
import com.xs.chat.service.CheckCodeService;
import com.xs.chat.service.UserInfoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    @Autowired
    private CheckCodeService checkCodeService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private CheckCodeUtil checkCodeUtil;
    @Autowired
    private JWTUtils jwtUtils;

    /**
     * @param response
     * @param session
     * @param type 0 注册验证码 1 邮箱验证码
     * @throws IOException
     */
    @GlobalInterceptor(checkParams = true)
    @GetMapping("/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, @RequestParam String type)throws IOException {
        if (type.equals("0")&&type!=null){
            String code = checkCodeService.sendCheckCode(response,session);
            log.info("注册验证码：{}",code);
        }else if (type!=null&&type.equals("1")){
            String emailCode = checkCodeService.sendEmailCheckCode(response,session);
            log.info("邮箱发送验证码：{}",emailCode);
        }else {
            log.info("验证码发送失败，请稍后再试");
        }
    }

    @GlobalInterceptor(checkParams = true)
    @GetMapping("/verifyCode")
    public Result<Boolean> verifyCode(HttpSession session, @RequestParam String checkCode,@RequestParam String type) {
        if (checkCode== null||checkCode.equals("")||checkCode.length()<CheckCodeConstant.DEFAULT_CHAR_COUNT){
            return Result.error("验证码错误");
        }
        //登录验证码
        if (type.equals("0")){
            if (checkCodeUtil.verifyCheckCode(session,CheckCodeConstant.CHECK_CODE_KEY,checkCode)){
                log.info("登录验证码验证成功{}", checkCode);
                return Result.success(true);
            }
        }else if (type.equals("1")){
            if (checkCodeUtil.verifyCheckCode(session,CheckCodeConstant.CHECK_CODE_EMAIL_KEY,checkCode)){
                log.info("邮箱验证码验证成功{}", checkCode);
                return Result.success(true);
            }
        }
        return Result.error("验证码错误");
    }


    /**0 注册验证码 1 找回邮箱验证码
     * @param emailCodeDTO
     * @return
     */
    @GlobalInterceptor(checkParams = true)
    @PostMapping("/sendEmailCode")
    public Result<String> sendEmailCode(HttpSession session,@RequestBody EmailCodeDTO emailCodeDTO) {
        if (session.getAttribute(CheckCodeConstant.CHECK_CODE_EMAIL_KEY)!=null&&checkCodeUtil.verifyCheckCode(session,CheckCodeConstant.CHECK_CODE_EMAIL_KEY,emailCodeDTO.getCheckCode())){
            if (emailCodeDTO.getEmail()!=null&&emailCodeDTO.getType().equals("0")){
                //注册的邮箱验证码
                checkCodeService.sendQQEmailCode(emailCodeDTO.getEmail(),session,emailCodeDTO.getType());
                return Result.success("发送注册验证码成功"+emailCodeDTO.getEmail());
            }else if (emailCodeDTO.getEmail()!=null&&emailCodeDTO.getType().equals("1")){
                //找回密码的邮箱验证码
                checkCodeService.sendQQEmailCode(emailCodeDTO.getEmail(),session,emailCodeDTO.getType());
                return Result.success("发送找回密码验证码成功"+emailCodeDTO.getEmail());
            }
        }
        return Result.error("验证码错误!");
    }

    @GlobalInterceptor(checkParams = true)
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        if (checkCodeUtil.verifyEmailCode(userDTO.getEmail(), userDTO.getEmailCode())) {
            userInfoService.register(userDTO);
            log.info("用户注册成功：{}", userDTO.getNickname());
            return Result.success("注册成功");
        }
        log.info("用户注册失败：{}", userDTO.getNickname());
        return Result.error("注册失败");
    }
    @GlobalInterceptor(checkParams = true)
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserDTO userDTO, HttpSession session) {
        if (!checkCodeUtil.verifyCheckCode(session,CheckCodeConstant.CHECK_CODE_KEY,userDTO.getLoginCode())){
            log.info("用户登录失败：{}", userDTO.getNickname());
            return Result.error("验证码错误");
        }
        if (userDTO== null){
            return Result.error("用户登录失败或不存在");
        }
        UserVO userVO = userInfoService.login(userDTO);
        if (userVO.getNickname() == null||userVO.getNickname().equals("")){
            return Result.error("用户密码错误");
        }
        log.info("用户登录成功：{}", userVO.getNickname());
        return Result.success(userVO);
    }
    @GlobalInterceptor(checkParams = true)
    @PostMapping("/verifyToken")
    public Result<UserVO> verifyToken(@RequestBody Map<String, String> map) {
        String token = map.get("token");
        if (token == null || token.equals("")) {
            return Result.error("token为空 失败");
        }
        if (!jwtUtils.validateToken(token)) {
            return Result.error("token验证失败");
        }
        UserVO userVO = userInfoService.SelectUserById(jwtUtils.getIdFromToken(token));
        log.info("用户验证成功：{}", userVO.getNickname());
        return Result.success(userVO);
    }
}
