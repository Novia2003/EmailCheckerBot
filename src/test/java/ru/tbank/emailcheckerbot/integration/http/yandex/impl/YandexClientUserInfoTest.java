package ru.tbank.emailcheckerbot.integration.http.yandex.impl;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tbank.emailcheckerbot.dto.yandex.YandexUserInfoDTO;
import ru.tbank.emailcheckerbot.integration.http.yandex.YandexClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class YandexClientUserInfoTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private YandexClient yandexClient;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension
            .newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("rest.yandex.user-info-url", wireMock::baseUrl);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void getUserInfo_shouldReturnYandexUserInfoDTO() {
        String token = "accessToken";

        wireMock.stubFor(get(urlMatching("/info.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                """
                                        {
                                            "login": "ivan",
                                            "old_social_login": "uid-mmzxrnry",
                                            "default_email": "test@yandex.ru",
                                            "id": "1000034426",
                                            "client_id": "4760187d81bc4b7799476b42b5103713",
                                            "emails": [
                                               "test@yandex.ru",
                                               "other-test@yandex.ru"
                                            ],
                                            "psuid": "1.AAceCw.tbHgw5DtJ9_zeqPrk-Ba2w.qPWSRC5v2t2IaksPJgnge"
                                         }
                                        """
                        )
                ));

        YandexUserInfoDTO result = yandexClient.getUserInfo(token);

        assertNotNull(result);
        assertEquals("test@yandex.ru", result.getDefaultEmail());
    }
}
