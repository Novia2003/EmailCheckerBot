package ru.tbank.emailcheckerbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tbank.emailcheckerbot.service.AuthenticationService;

@Controller
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @GetMapping("/receiver")
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
