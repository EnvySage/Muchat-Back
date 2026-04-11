package com.xs.chat.service;

import com.xs.chat.pojo.VO.StsCredentialVO;

public interface OssService {

    /**
     * 获取前端直传所需的 STS 临时凭证
     * @param folder 上传目标文件夹
     * @return STS 凭证信息
     */
    StsCredentialVO getStsCredential(String folder);

    /**
     * 删除 OSS 文件
     * @param fileUrl 文件 URL
     */
    void deleteFile(String fileUrl);
}
