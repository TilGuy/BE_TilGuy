package com.tilguys.matilda.til.controller;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.ReferencesResponse;
import com.tilguys.matilda.til.dto.TagsResponse;
import com.tilguys.matilda.til.dto.TilWithUserResponse;
import com.tilguys.matilda.til.service.TilService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/til")
@Controller
@RequiredArgsConstructor
public class TilShareController {

    private final TilService tilService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/share/{id}")
    public String shareTil(@PathVariable Long id,
                           HttpServletRequest request,
                           Model model) {

        String userAgent = request.getHeader("User-Agent");

        if (isSocialMediaBot(userAgent)) {
            Til til = tilService.getTilByTilId(id);
            TilWithUserResponse response = new TilWithUserResponse(til);

            model.addAttribute("til", response);
            model.addAttribute("frontendUrl", frontendUrl);
            model.addAttribute("description", createDescription(response));

            return "share/og-template";
        } else {
            return "redirect:" + frontendUrl + "/all-tils/" + id;
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

    private String createDescription(TilWithUserResponse til) {
        String name = "✏️ 작성자: " + til.nickname();
        return Stream.of(
                        name,
                        formatTags(til.tags()),
                        formatReferences(til.references()),
                        truncateContent(til.content())
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    private String formatTags(TagsResponse tags) {
        return Optional.ofNullable(tags)
                .filter(t -> !t.getTags().isEmpty())
                .map(t -> "🏷️ 태그: #" + String.join(" #", t.getTags()))
                .orElse(null);
    }

    private String formatReferences(ReferencesResponse references) {
        return Optional.ofNullable(references)
                .filter(r -> !r.getWords().isEmpty())
                .map(r -> "📚 학습 단어: " + r.getWords())
                .orElse(null);
    }

    private String truncateContent(String content) {
        int maxLength = 100;
        return content.length() > maxLength
                ? "\n" + content.substring(0, maxLength) + "..."
                : "\n" + content;
    }
}
