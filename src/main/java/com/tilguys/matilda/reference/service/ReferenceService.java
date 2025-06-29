package com.tilguys.matilda.reference.service;

import com.tilguys.matilda.reference.repository.ReferenceRepository;
import com.tilguys.matilda.til.domain.Reference;
import com.tilguys.matilda.til.dto.ReferencesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReferenceService {

    private final ReferenceRepository referenceRepository;

    public ReferencesResponse getReferencesByTilId(Long tilId) {
        List<Reference> references = referenceRepository.getAllByTil_TilId(tilId);
        return new ReferencesResponse(references);
    }
}
