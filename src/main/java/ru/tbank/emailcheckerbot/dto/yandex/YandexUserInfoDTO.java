package ru.tbank.emailcheckerbot.dto.yandex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YandexUserInfoDTO {

    private String id;

    private String login;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("default_email")
    private String defaultEmail;

    private List<String> emails;

    private String psuid;
}

