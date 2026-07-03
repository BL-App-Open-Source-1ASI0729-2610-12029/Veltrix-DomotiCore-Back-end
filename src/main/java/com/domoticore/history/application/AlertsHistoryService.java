package com.domoticore.history.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class AlertsHistoryService {

    private static final String COLLECTION = "alerts-history";
    private static final String DEFAULT_TEMPLATE = "default";

    private final UserCollectionAccessService userCollectionAccessService;

    public AlertsHistoryService(UserCollectionAccessService userCollectionAccessService) {
        this.userCollectionAccessService = userCollectionAccessService;
    }

    public JsonNode getAlertsHistory(User user, String segment) {
        return userCollectionAccessService.getSingleton(user, segment, COLLECTION, DEFAULT_TEMPLATE);
    }
}
