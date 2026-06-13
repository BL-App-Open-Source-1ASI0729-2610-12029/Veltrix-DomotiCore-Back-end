package com.domoticore.history.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.presentation.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history-insights")
@Tag(name = "History Insights")
public class HistoryInsightsController extends AbstractJsonCollectionController {

    public HistoryInsightsController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "history-insights");
    }
}
