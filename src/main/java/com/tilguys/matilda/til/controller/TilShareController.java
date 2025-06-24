package com.tilguys.matilda.til.controller;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilWithUserResponse;
import com.tilguys.matilda.til.service.TilService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/share")
@Controller
@RequiredArgsConstructor
public class TilShareController {

    private final TilService tilService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/{id}")
    public String shareTil(@PathVariable Long id,
                           HttpServletRequest request,
                           Model model) {

        String userAgent = request.getHeader("User-Agent");

        if (isSocialMediaBot(userAgent)) {
            Til til = tilService.getTilByTilId(id);
            TilWithUserResponse response = new TilWithUserResponse(til);

            model.addAttribute("til", response);
            model.addAttribute("frontendUrl", frontendUrl);
            model.addAttribute("description", createDescription(response.content()));
            model.addAttribute("imageUrl", getImageUrl());

            return "share/og-template";
        } else {
            return "redirect:" + frontendUrl + "/all-tils?tilId=" + id;
        }
    }

    private boolean isSocialMediaBot(String userAgent) {
        if (userAgent == null) {
            return false;
        }

        String[] botPatterns = {
                "kakaotalk",
                "slackbot",
                "discordbot",
                "linkedinbot"
        };

        String lowerUserAgent = userAgent.toLowerCase();
        return Arrays.stream(botPatterns)
                .anyMatch(lowerUserAgent::contains);
    }

    private String createDescription(String content) {
        return content.length() > 150
                ? content.substring(0, 150) + "..."
                : content;
    }

    private String getImageUrl() {
        return frontendUrl + "/assets/matilda-default-og.png";
    }
}
