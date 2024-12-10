package ru.tbank.emailcheckerbot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tbank.emailcheckerbot.service.authentication.AuthenticationService;

@Controller
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
@Tag(
        name = "AuthenticationController",
        description = "Functions for OAuth authentication"
)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @GetMapping("/receiver")
    @Operation(
            description = "Getting the code needed for exchanging to access_token during OAuth"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.TEXT_HTML_VALUE,
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input",
                            content = @Content(
                                    mediaType = MediaType.TEXT_HTML_VALUE,
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User record not found",
                            content = @Content(
                                    mediaType = MediaType.TEXT_HTML_VALUE,
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "Service unavailable",
                            content = @Content(
                                    mediaType = MediaType.TEXT_HTML_VALUE,
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public String getCode(
            @RequestParam String state,
            @RequestParam String code,
            Model model
    ) {
        String content = authenticationService.authenticate(state, code);
        model.addAttribute("content", content);
        return "oauth";
    }
}
