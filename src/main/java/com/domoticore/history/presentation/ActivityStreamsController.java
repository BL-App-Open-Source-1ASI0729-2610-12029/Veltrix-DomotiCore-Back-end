package com.domoticore.history.presentation;

import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.interfaces.AbstractJsonCollectionController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity-streams")
@Tag(name = "Activity Streams")
public class ActivityStreamsController extends AbstractJsonCollectionController {

    public ActivityStreamsController(JsonResourceService jsonResourceService) {
        super(jsonResourceService, "activity-streams");
    }
}
