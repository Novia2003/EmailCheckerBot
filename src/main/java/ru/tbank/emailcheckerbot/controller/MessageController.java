package ru.tbank.emailcheckerbot.controller;

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
public class MessageController {

    private final EmailUIDService emailUIDService;

    @GetMapping
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
