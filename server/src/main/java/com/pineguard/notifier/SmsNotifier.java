package com.pineguard.notifier;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SmsNotifier implements Notifier {
    @Value("${notify.sms.regionId:cn-hangzhou}")
    private String regionId;
    @Value("${notify.sms.accessKeyId:}")
    private String accessKeyId;
    @Value("${notify.sms.accessKeySecret:}")
    private String accessKeySecret;
    @Value("${notify.sms.signName:}")
    private String signName;
    @Value("${notify.sms.templateCode:}")
    private String templateCode;
    @Value("${notify.sms.toPhones:}")
    private String toPhones;

    @Override
    public void notify(String deviceId, String message) {
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty() || templateCode.isEmpty() || signName.isEmpty()) {
            System.out.println("[SMS] 配置不完整，跳过发送");
            return;
        }
        try {
            DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);
            for (String phone : Arrays.stream(toPhones.split(",")).map(String::trim).filter(p -> !p.isEmpty()).toList()) {
                SendSmsRequest request = new SendSmsRequest();
                request.setRegionId(regionId);
                request.setPhoneNumbers(phone);
                request.setSignName(signName);
                request.setTemplateCode(templateCode);
                request.setTemplateParam("{\\"deviceId\\":\\"" + deviceId + "\\",\\"msg\\":\\"" + message + "\\"}");
                SendSmsResponse response = client.getAcsResponse(request);
                System.out.println("[SMS] sent -> " + phone + ", code=" + response.getCode());
            }
        } catch (ClientException e) {
            System.out.println("[SMS] 发送失败: " + e.getErrMsg());
        }
    }
}
