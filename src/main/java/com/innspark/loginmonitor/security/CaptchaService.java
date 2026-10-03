package com.innspark.loginmonitor.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final ObjectMapper objectMapper;

    @Value("${captcha.secret-key}")
    private String secretKey;

    private static final String VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    public boolean verify(String captchaToken) {

        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }

        try {
            RestClient restClient = RestClient.create();

            String response = restClient.post()
                    .uri(VERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("secret=" + secretKey + "&response=" + captchaToken)
                    .retrieve()
                    .body(String.class);

            JsonNode json = objectMapper.readTree(response);

            return json.path("success").asBoolean(false);

        } catch (Exception ex) {
            return false;
        }
    }
}