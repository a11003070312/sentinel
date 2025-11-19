package com.pineguard.notifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatNotifier implements Notifier {
    @Value("${notify.wechat.appId:}")
    private String appId;
    @Value("${notify.wechat.appSecret:}")
    private String appSecret;
    @Value("${notify.wechat.templateId:}")
    private String templateId;
    @Value("${notify.wechat.toOpenIds:}")
    private String toOpenIds;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void notify(String deviceId, String message) {
        if (appId.isEmpty() || appSecret.isEmpty() || templateId.isEmpty()) {
            System.out.println("[WeChat] 配置不完整，跳过推送");
            return;
        }
        try {
            // 获取 access_token
            String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
            Map tokenResp = restTemplate.getForObject(tokenUrl, Map.class);
            if (tokenResp == null || tokenResp.get("access_token") == null) {
                System.out.println("[WeChat] 获取token失败: " + tokenResp);
                return;
            }
            String accessToken = (String) tokenResp.get("access_token");

            String apiUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
            for (String openId : Arrays.stream(toOpenIds.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("touser", openId);
                payload.put("template_id", templateId);
                Map<String, Object> data = new HashMap<>();
                Map<String, String> first = new HashMap<>(); first.put("value", "检测到跌倒报警");
                Map<String, String> keyword1 = new HashMap<>(); keyword1.put("value", deviceId);
                Map<String, String> keyword2 = new HashMap<>(); keyword2.put("value", message);
                data.put("first", first); data.put("keyword1", keyword1); data.put("keyword2", keyword2);
                payload.put("data", data);
                HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                Map resp = restTemplate.postForObject(apiUrl, entity, Map.class);
                System.out.println("[WeChat] push -> " + openId + ", resp=" + resp);
            }
        } catch (Exception e) {
            System.out.println("[WeChat] 推送失败: " + e.getMessage());
        }
    }
}
