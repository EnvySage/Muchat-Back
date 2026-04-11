package com.xs.chat.Configure;

import com.aliyun.sts20150401.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StsConfig {

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.sts.endpoint}")
    private String stsEndpoint;

    @Bean
    public Client stsClient() throws Exception {
        log.info("初始化阿里云 STS 客户端，endpoint: {}", stsEndpoint);
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(stsEndpoint);
        return new Client(config);
    }
}
