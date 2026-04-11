package com.xs.chat.service.Impl;

import com.xs.chat.Configure.EmailConfig;
import com.xs.chat.Exception.BusinessException;
import com.xs.chat.Utils.CheckCodeUtil;
import com.xs.chat.Utils.StringUtil;
import com.xs.chat.constants.CheckCodeConstant;
import com.xs.chat.constants.RedisConstant;
import com.xs.chat.enumeration.CaptchaType;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.SysSettingDTO;
import com.xs.chat.pojo.Result;
import com.xs.chat.service.CheckCodeService;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static com.xs.chat.constants.CheckCodeConstant.*;
@Slf4j
@Service
public class CheckCodeImpl implements CheckCodeService {
    @Autowired
    private CheckCodeUtil checkCodeUtil;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailConfig emailConfig;
    @Override
    public String sendCheckCode(HttpServletResponse response, HttpSession session) throws IOException {
        Map<String,Object> result = checkCodeUtil.generateCaptcha(CaptchaType.LINE, null, null, null, null, null);
        String code = (String) result.get("code");
        session.setAttribute(CHECK_CODE_KEY, code);
        BufferedImage image = (BufferedImage) result.get("image");
        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
        return code;
    }

    @Override
    public String sendEmailCheckCode(HttpServletResponse response, HttpSession session) throws IOException {
        Map<String, Object> result = CheckCodeUtil.generateCaptcha(
                CaptchaType.CIRCLE,
                null,
                null,
                null,
                null,
                null
        );
        String code = (String) result.get("code");
        session.setAttribute(CHECK_CODE_EMAIL_KEY, code);
        BufferedImage image = (BufferedImage) result.get("image");
        response.setContentType("image/png");
        response.setHeader("Cache-Email-Key", code);
        ImageIO.write(image, "png", response.getOutputStream());
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendQQEmailCode(String email, HttpSession session,String type) {
        if (type.equals("0")){
            UserDO userInfoDO = userInfoMapper.selectByEmail(email);
            if (userInfoDO != null){
                Result.error("邮箱已存在");
            }
        }
        if (type.equals("1")) {
            UserDO userInfoDO = userInfoMapper.selectByEmail(email);
            if (userInfoDO == null) {
                Result.error("邮箱不存在");
            }
        }
        String RandomCode = StringUtil.getRandomString(DEFAULT_CHAR_COUNT);
        String redisKey = RedisConstant.QQEmail+email;
        redisTemplate.delete(redisKey);
        sendEmail(email,RandomCode);
        redisTemplate.opsForValue().set(redisKey,RandomCode);
    }
    private void sendEmail(String toEmail,String RandomCode){
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailConfig.getUsername());
            helper.setTo(toEmail);

            SysSettingDTO sysSettingDTO = new SysSettingDTO();
            helper.setSubject(sysSettingDTO.getRegisterEmailTitle());
            helper.setText(String.format(sysSettingDTO.getRegisterEmailContent(), RandomCode));
            helper.setSentDate(new Date());
            javaMailSender.send(message);
        } catch (Exception e) {
            log.warn("发送邮件失败");
            throw new BusinessException("发送邮件失败");
        }
    }

}
