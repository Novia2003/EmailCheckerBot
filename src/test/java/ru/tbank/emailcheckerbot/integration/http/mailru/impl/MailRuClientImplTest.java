package ru.tbank.emailcheckerbot.integration.http.mailru.impl;

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
import ru.tbank.emailcheckerbot.dto.mailru.MailRuUserInfoDTO;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.integration.http.mailru.MailRuClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class MailRuClientImplTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private MailRuClient mailRuClient;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension
            .newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("rest.mailru.url", wireMock::baseUrl);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void getAccessToken_shouldReturnAccessTokenResponse() {
        String code = "authCode";

        wireMock.stubFor(post(urlMatching("/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                """
                                        {
                                            "expires_in": 3600,
                                            "access_token": "6c3c7dcebeba73ba878439237b8cdc302031313737363830",
                                            "refresh_token": "b42019a80b7d76b388b504cc4366e98425fcbd3e37363830"
                                        }
                                        """
                        )
                ));

        AccessTokenResponseDTO result = mailRuClient.getAccessToken(code);

        assertNotNull(result);
        assertEquals("6c3c7dcebeba73ba878439237b8cdc302031313737363830", result.getAccessToken());
        assertEquals("b42019a80b7d76b388b504cc4366e98425fcbd3e37363830", result.getRefreshToken());
        assertEquals(3600, result.getExpiresIn());
    }

    @Test
    void refreshAccessToken_shouldReturnRefreshTokenResponse() {
        String refreshToken = "refreshToken";

        wireMock.stubFor(post(urlMatching("/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                """
                                                {
                                                	"expires_in":3600,
                                                	"access_token":"4ab90dfebeb475d6aff3702120e6fa4125fcbd3e37363830"
                                                }
                                        """
                        )
                ));

        RefreshTokenResponseDTO result = mailRuClient.refreshAccessToken(refreshToken);

        assertNotNull(result);
        assertEquals("4ab90dfebeb475d6aff3702120e6fa4125fcbd3e37363830", result.getAccessToken());
        assertEquals(3600, result.getExpiresIn());
    }

    @Test
    void getUserInfo_shouldReturnMailRuUserInfoDTO() {
        String token = "accessToken";

        wireMock.stubFor(get(urlMatching("/userinfo.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                """
                                        {
                                        	"id": "...",
                                        	"client_id": "...",
                                        	"gender": "m",
                                        	"name": "Алексей Иванов",
                                        	"nickname": "alex",
                                        	"locale": "ru_RU",
                                        	"first_name": "Алексей",
                                        	"last_name": "Иванов",
                                        	"email": "alex@ivanov.ru",
                                        	"image": "https://...."
                                        }
                                        """
                        )
                ));

        MailRuUserInfoDTO result = mailRuClient.getUserInfo(token);

        assertNotNull(result);
        assertEquals("alex@ivanov.ru", result.getEmail());
    }
}
