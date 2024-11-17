package ru.tbank.emailcheckerbot.dto.mailru;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MailRuUserInfoDTO {

    private String id;

    @JsonProperty("client_id")
    private String clientId;

    private String gender;

    private String name;

    private String nickname;

    private String locale;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private String image;
}
