package com.atguigu.java.ai.langchain4j.utils;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliyunTokenUtil {
    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;

    public String getToken() throws Exception {
        // 创建配置并获取客户端
        DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        // 构建请求
        CommonRequest request = new CommonRequest();
        request.setSysDomain("nls-meta.cn-shanghai.aliyuncs.com");
        request.setSysVersion("2019-02-28");
        request.setSysAction("CreateToken");

        CommonResponse response = client.getCommonResponse(request);
        if (response.getHttpStatus() == 200) {
            return JSON.parseObject(response.getData()).getJSONObject("Token").getString("Id");
        }
        throw new RuntimeException("获取阿里语音Token失败");
    }
}