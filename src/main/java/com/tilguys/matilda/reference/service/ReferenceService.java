package com.tilguys.matilda.reference.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.reference.domain.TilReferenceGenerator;
import com.tilguys.matilda.reference.domain.TilReferenceParser;
import com.tilguys.matilda.reference.event.ReferenceCreateEvent;
import com.tilguys.matilda.reference.repository.ReferenceRepository;
import com.tilguys.matilda.til.domain.Reference;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.ReferencesResponse;
import com.tilguys.matilda.til.repository.TilRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferenceService {

    private final OpenAIClient openAIClient;
    private final TilReferenceGenerator tilReferenceGenerator;
    private final TilReferenceParser tilReferenceParser;
    private final ReferenceRepository referenceRepository;
    private final TilRepository tilRepository;

    public ReferenceService(OpenAIClient openAIClient,
                            ReferenceRepository referenceRepository,
                            ObjectMapper objectMapper,
                            TilRepository tilRepository) {
        this.openAIClient = openAIClient;
        this.tilReferenceGenerator = new TilReferenceGenerator();
        this.tilReferenceParser = new TilReferenceParser(objectMapper);
        this.referenceRepository = referenceRepository;
        this.tilRepository = tilRepository;
    }

    public ReferencesResponse getReferencesByTilId(Long tilId) {
        List<Reference> references = referenceRepository.getAllByTil_TilId(tilId);
        return new ReferencesResponse(references);
    }

    @Transactional
    public void createReference(ReferenceCreateEvent event) {
        try {
            List<Reference> references = extractTilReference(event.tilContent());

            Til til = tilRepository.findById(event.tilId())
                    .orElseThrow(() -> new IllegalArgumentException("TIL을 찾을 수 없습니다"));

            references.forEach(reference -> reference.setTil(til));

            referenceRepository.saveAll(references);

        } catch (Exception e) {
            throw new RuntimeException("TIL Reference 저장 실패: " + e.getMessage());
        }
    }

    public List<Reference> extractTilReference(String tilContent) {
        String responseJson = openAIClient.callOpenAI(
                tilReferenceGenerator.createPrompt(tilContent),
                tilReferenceGenerator.createFunctionDefinition()
        );
        return tilReferenceParser.parseReferences(responseJson);
    }
}
