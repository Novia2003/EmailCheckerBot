package ru.tbank.emailcheckerbot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tbank.emailcheckerbot.service.EmailUIDService;

@Controller
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(
        name = "MessageController",
        description = "Functions for viewing email messages"
)
public class MessageController {

    private final EmailUIDService emailUIDService;

    @GetMapping
    @Operation(
            description = "Getting the full content of an email message by its UID"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = "text/html",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input",
                            content = @Content(
                                    mediaType = "text/html",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Message not found",
                            content = @Content(
                                    mediaType = "text/html",
                                    schema = @Schema(implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "Service unavailable",
                            content = @Content(
                                    mediaType = "text/html",
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public String getClients(
            @RequestParam Long userEmailId,
            @RequestParam Long messageUID,
            Model model
    ) {
        String content = emailUIDService.getMessageByUID(userEmailId, messageUID);
        model.addAttribute("content", content);
        return "message";
    }
}
