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
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.integration.http.yandex.YandexClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class YandexClientTokenTest {

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
        registry.add("rest.yandex.oauth-url", wireMock::baseUrl);
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
                                          "token_type": "bearer",
                                          "access_token": "AQAAAACy1C6ZAAAAfa6vDLuItEy8pg-iIpnDxIs",
                                          "expires_in": 124234123534,
                                          "refresh_token": "1:GN686QVt0mmakDd9:A4pYuW9LGk0_UnlrMIWklkAuJkUWbq27loFekJVmSYrdfzdePBy7:A-2dHOmBxiXgajnD-kYOwQ",
                                          "scope": "login:info login:email login:avatar"
                                        }
                                        """
                        )
                ));

        AccessTokenResponseDTO result = yandexClient.getAccessToken(code);

        assertNotNull(result);
        assertEquals("AQAAAACy1C6ZAAAAfa6vDLuItEy8pg-iIpnDxIs", result.getAccessToken());
        assertEquals(
                "1:GN686QVt0mmakDd9:A4pYuW9LGk0_UnlrMIWklkAuJkUWbq27loFekJVmSYrdfzdePBy7:A-2dHOmBxiXgajnD-kYOwQ",
                result.getRefreshToken()
        );
        assertEquals(124234123534L, result.getExpiresIn());
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
                                        "access_token": "AQAAAACy1C6ZAAAAfa6vDLuItEy8pg-iIpnDxIs",
                                        "refresh_token": "1:GN686QVt0mmakDd9:A4pYuW9LGk0_UnlrMIWklkAuJkUWbq27loFekJVmSYrdfzdePBy7:A-2dHOmBxiXgajnD-kYOwQ",
                                        "token_type": "bearer",
                                        "expires_in": 124234123534
                                        }
                                        """
                        )
                ));

        RefreshTokenResponseDTO result = yandexClient.refreshAccessToken(refreshToken);

        assertNotNull(result);
        assertEquals("AQAAAACy1C6ZAAAAfa6vDLuItEy8pg-iIpnDxIs", result.getAccessToken());
        assertEquals(124234123534L, result.getExpiresIn());
        assertEquals(
                "1:GN686QVt0mmakDd9:A4pYuW9LGk0_UnlrMIWklkAuJkUWbq27loFekJVmSYrdfzdePBy7:A-2dHOmBxiXgajnD-kYOwQ",
                result.getRefreshToken()
        );
    }
}
