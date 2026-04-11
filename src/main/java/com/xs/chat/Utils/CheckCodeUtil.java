package com.xs.chat.Utils;

import cn.hutool.captcha.*;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.StrUtil;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.xs.chat.constants.RedisConstant;
import com.xs.chat.enumeration.CaptchaType;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.xs.chat.constants.CheckCodeConstant.*;


/**
 * Hutool 验证码生成工具类（封装多种验证码类型，支持自定义配置）
 */
@Slf4j
@Component
public class CheckCodeUtil {
    @Autowired
    private RedisTemplate redisTemplate;

    // ============================ 核心生成方法 ============================

    /**
     * 生成验证码（返回验证码文本和图片对象）
     *
     * @param type        验证码类型（LINE/CIRCLE/SHEAR/GIF）
     * @param width       图片宽度
     * @param height      图片高度
     * @param charCount   字符数量
     * @param lineCount   干扰线数量（仅线段/圆圈/扭曲干扰生效）
     * @param charSet     字符集（为空则用默认）
     * @return 包含验证码文本和图片的 Map（key: "code"-文本, "image"-BufferedImage）
     */
    public static Map<String, Object> generateCaptcha(
            CaptchaType type,
            Integer width,
            Integer height,
            Integer charCount,
            Integer lineCount,
            String charSet
    ) {
        // 1. 处理默认参数
        int imgWidth = width == null ? DEFAULT_WIDTH : width;
        int imgHeight = height == null ? DEFAULT_HEIGHT : height;
        int charNum = charCount == null ? DEFAULT_CHAR_COUNT : charCount;
        int lineNum = lineCount == null ? DEFAULT_LINE_COUNT : lineCount;
        String charset = StrUtil.isEmpty(charSet) ? DEFAULT_CHAR_SET : charSet;

        // 2. 创建验证码对象（根据类型选择 Hutool 实现）
        AbstractCaptcha captcha;
        switch (type) {
            case LINE:
                captcha = CaptchaUtil.createLineCaptcha(imgWidth, imgHeight, charNum, lineNum);
                break;
            case CIRCLE:
                captcha = CaptchaUtil.createCircleCaptcha(imgWidth, imgHeight, charNum, lineNum);
                break;
            case SHEAR:
                captcha = CaptchaUtil.createShearCaptcha(imgWidth, imgHeight, charNum, lineNum);
                break;
            default:
                throw new IllegalArgumentException("不支持的验证码类型：" + type);
        }

        // 3. 自定义字符集（Hutool 默认字符集可能包含易混淆字符，这里过滤）
        if (captcha instanceof LineCaptcha || captcha instanceof CircleCaptcha || captcha instanceof ShearCaptcha) {
            captcha.setGenerator(new CustomCodeGenerator(charset, charNum));
        }
        // 4. 获取验证码文本和图片
        String code = captcha.getCode();
        BufferedImage image = captcha.getImage();

        // 5. 返回结果（文本+图片对象）
        Map<String, Object> result = new HashMap<>(2);
        result.put("code", code);       // 验证码文本（正确答案）
        result.put("image", image);     // 图片对象（BufferedImage）
        return result;
    }

    public boolean verifyEmailCode(String email,String emailCode) {
        if (emailCode == null){
            return false;
        }
        if (emailCode.equalsIgnoreCase((String) redisTemplate.opsForValue().get(RedisConstant.QQEmail+email))){
            return true;
        }
        return false;
    }


    // ============================ 自定义字符生成器（过滤易混淆字符） ============================
    static class CustomCodeGenerator implements CodeGenerator {
        private final String charSet;
        private final int length;

        public CustomCodeGenerator(String charSet, int length) {
            // 过滤易混淆字符（0/O、1/I/l、2/Z、5/S、8/B等）
            this.charSet = filterConfusableChars(charSet);
            this.length = length;
        }

        @Override
        public String generate() {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(charSet.charAt(ThreadLocalRandom.current().nextInt(charSet.length())));
            }
            return sb.toString();
        }

        @Override
        public boolean verify(String s, String s1) {
            if (s == null || s1 == null){
                return false;
            }
            return s.equals(s1);
        }

        // 过滤易混淆字符
        private String filterConfusableChars(String original) {
            return original.replaceAll("[0Oo]", "")  // 移除 0/O/o
                    .replaceAll("[1Il]", "")       // 移除 1/I/l
                    .replaceAll("[2Zz]", "")       // 移除 2/Z/z
                    .replaceAll("[5Ss]", "")       // 移除 5/S/s
                    .replaceAll("[8Bb]", "");      // 移除 8/B/b
        }
    }

    public boolean verifyCheckCode(HttpSession session, String CodeKey, String code) {
        Object sessionCode = session.getAttribute(CodeKey);
        if (sessionCode != null) {
            if (sessionCode.toString().equalsIgnoreCase(code)){
                session.removeAttribute(CodeKey);
                log.info("验证码验证成功{}", code);
                return true;
            }else {
                session.removeAttribute(CodeKey);
                log.info("验证码验证失败，实际验证码为{}", sessionCode);
                return false;
            }
        }
        log.info("验证码已过期或不存在{}", code);
        return false;
    }

}