package com.xs.chat.controller;

import com.xs.chat.context.BaseContext;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.Result;
import com.xs.chat.pojo.VO.StsCredentialVO;
import com.xs.chat.service.OssService;
import com.xs.chat.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/oss")
@Slf4j
public class OssController {

    @Autowired
    private OssService ossService;

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 获取OSS上传凭证（STS临时凭证，供前端直传）
     * 需要在请求头携带 Authorization: Bearer <token>
     * @param type 上传类型：user-avatar(用户头像)、group-avatar(群头像)
     * @param targetId 上传目标ID（type=group-avatar时传入群聊ID）
     * @return STS凭证信息
     */
    @GetMapping("/getOssToken")
    public Result<StsCredentialVO> getOssToken(
            @RequestParam(value = "type", defaultValue = "user-avatar") String type,
            @RequestParam(value = "targetId", required = false) String targetId) {
        String userId = BaseContext.getCurrentId();
        String folder;
        switch (type) {
            case "group-avatar" -> {
                if (targetId == null || targetId.isEmpty()) {
                    return Result.error("群头像上传需要提供群聊ID");
                }
                folder = "group-avatar/" + targetId;
            }
            case "user-avatar" -> {
                folder = "user-avatar/" + userId;
            }
            default -> {
                return Result.error("不支持的上传类型: " + type);
            }
        }
        try {
            StsCredentialVO credential = ossService.getStsCredential(folder);
            log.info("获取OSS上传凭证成功，userId: {}, type: {}, folder: {}", userId, type, folder);
            return Result.success(credential);
        } catch (Exception e) {
            log.error("获取OSS上传凭证失败", e);
            return Result.error("获取上传凭证失败，请稍后重试");
        }
    }

    /**
     * 更新用户头像URL（前端直传完成后调用）
     * @param map 包含 avatarUrl
     * @return 更新结果
     */
    @PostMapping("/updateAvatar")
    public Result<String> updateAvatar(@RequestBody Map<String, String> map) {
        String userId = BaseContext.getCurrentId();
        String avatarUrl = map.get("avatarUrl");

        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return Result.error("头像URL不能为空");
        }

        try {
            UserDO userDO = new UserDO();
            userDO.setId(userId);
            userDO.setAvatar(avatarUrl);
            userInfoService.updateUserInfo(userDO);
            log.info("头像更新成功，userId: {}", userId);
            return Result.success("头像更新成功");
        } catch (Exception e) {
            log.error("头像更新失败", e);
            return Result.error("头像更新失败，请稍后重试");
        }
    }
}
