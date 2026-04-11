package com.xs.chat.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StsCredentialVO {
    /** 临时 AccessKeyId */
    private String accessKeyId;
    /** 临时 AccessKeySecret */
    private String accessKeySecret;
    /** 安全令牌 SecurityToken */
    private String securityToken;
    /** 凭证过期时间 */
    private String expiration;
    /** OSS endpoint */
    private String endpoint;
    /** OSS bucket名称 */
    private String bucket;
    /** 上传目标文件夹前缀 */
    private String folder;
}
