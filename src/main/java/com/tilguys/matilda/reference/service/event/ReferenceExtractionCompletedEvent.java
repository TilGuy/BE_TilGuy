package com.tilguys.matilda.reference.service.event;

import com.tilguys.matilda.til.domain.Reference;
import java.util.List;


public record ReferenceExtractionCompletedEvent(Long tilId, List<Reference> references) {

}
