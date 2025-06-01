package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Reference;
import java.util.List;
import lombok.Getter;

@Getter
public class ReferencesResponse {

    private final List<String> words;

    private final List<String> infos;

    public ReferencesResponse(List<Reference> references) {
        this.words = references.stream()
                .map(Reference::getWord)
                .toList();

        this.infos = references.stream()
                .map(Reference::getInfo)
                .toList();
    }
}
