package com.domoticore.history.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.interfaces.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history-summary")
@Tag(name = "History Summary")
public class HistorySummaryController extends AbstractJsonCollectionController {

    public HistorySummaryController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "history-summary");
    }
}
