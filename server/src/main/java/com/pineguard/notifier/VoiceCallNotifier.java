package com.pineguard.notifier;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.dyvmsapi.model.v20170525.SingleCallByTtsRequest;
import com.aliyuncs.dyvmsapi.model.v20170525.SingleCallByTtsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class VoiceCallNotifier implements Notifier {
    @Value("${notify.voice.regionId:cn-hangzhou}")
    private String regionId;
    @Value("${notify.voice.accessKeyId:}")
    private String accessKeyId;
    @Value("${notify.voice.accessKeySecret:}")
    private String accessKeySecret;
    @Value("${notify.voice.calledShowNumber:}")
    private String calledShowNumber;
    @Value("${notify.voice.ttsCode:}")
    private String ttsCode;
    @Value("${notify.voice.toPhones:}")
    private String toPhones;

    @Override
    public void notify(String deviceId, String message) {
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty() || calledShowNumber.isEmpty() || ttsCode.isEmpty()) {
            System.out.println("[VOICE] 配置不完整，跳过拨打");
            return;
        }
        try {
            DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);
            for (String phone : Arrays.stream(toPhones.split(",")).map(String::trim).filter(p -> !p.isEmpty()).toList()) {
                SingleCallByTtsRequest request = new SingleCallByTtsRequest();
                request.setCalledNumber(phone);
                request.setCalledShowNumber(calledShowNumber);
                request.setTtsCode(ttsCode);
                request.setTtsParam("{\\"deviceId\\":\\"" + deviceId + "\\",\\"msg\\":\\"" + message + "\\"}");
                SingleCallByTtsResponse resp = client.getAcsResponse(request);
                System.out.println("[VOICE] called -> " + phone + ", code=" + resp.getCode());
            }
        } catch (ClientException e) {
            System.out.println("[VOICE] 拨打失败: " + e.getErrMsg());
        }
    }
}
