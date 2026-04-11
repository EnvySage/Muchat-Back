package com.xs.chat.service.Impl;

import com.aliyun.oss.OSS;
import com.aliyun.sts20150401.Client;
import com.aliyun.sts20150401.models.AssumeRoleRequest;
import com.aliyun.sts20150401.models.AssumeRoleResponseBody;
import com.xs.chat.pojo.VO.StsCredentialVO;
import com.xs.chat.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class OssServiceImpl implements OssService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private Client stsClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.sts.role-arn}")
    private String roleArn;

    @Value("${aliyun.oss.sts.role-session-name}")
    private String roleSessionName;

    @Value("${aliyun.oss.sts.token-expiration}")
    private long tokenExpiration;

    @Override
    public StsCredentialVO getStsCredential(String folder) {
        try {
            // 构造 STS Policy，限制只能 PutObject 到指定目录
            String policy = """
                    {
                        "Version": "1",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Action": [
                                    "oss:PutObject"
                                ],
                                "Resource": [
                                    "acs:oss:*:*:%s/%s/*"
                                ]
                            }
                        ]
                    }
                    """.formatted(bucketName, folder);

            // 调用 AssumeRole 获取临时凭证
            AssumeRoleRequest request = new AssumeRoleRequest()
                    .setRoleArn(roleArn)
                    .setRoleSessionName(roleSessionName + "-" + UUID.randomUUID().toString().substring(0, 8))
                    .setPolicy(policy)
                    .setDurationSeconds(tokenExpiration);

            AssumeRoleResponseBody.AssumeRoleResponseBodyCredentials credentials = stsClient.assumeRole(request).getBody().getCredentials();

            log.info("获取 STS 凭证成功，folder: {}，过期时间: {}", folder, credentials.getExpiration());

            return new StsCredentialVO(
                    credentials.getAccessKeyId(),
                    credentials.getAccessKeySecret(),
                    credentials.getSecurityToken(),
                    credentials.getExpiration(),
                    endpoint,
                    bucketName,
                    folder
            );
        } catch (Exception e) {
            log.error("获取 STS 凭证失败", e);
            throw new RuntimeException("获取上传凭证失败，请稍后重试");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String path = fileUrl.replace("https://" + bucketName + "." + endpoint + "/", "");
            ossClient.deleteObject(bucketName, path);
            log.info("文件删除成功，文件路径：{}", path);
        } catch (Exception e) {
            log.error("文件删除失败，文件URL：{}", fileUrl, e);
        }
    }
}
