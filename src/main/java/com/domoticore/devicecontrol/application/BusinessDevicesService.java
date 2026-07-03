package com.domoticore.devicecontrol.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class BusinessDevicesService {

    private static final String COLLECTION = "business-devices-overview";
    private static final String DEFAULT_TEMPLATE = "default";

    private final UserCollectionAccessService userCollectionAccessService;

    public BusinessDevicesService(UserCollectionAccessService userCollectionAccessService) {
        this.userCollectionAccessService = userCollectionAccessService;
    }

    public JsonNode getOverview(User user, String segment) {
        return userCollectionAccessService.getSingleton(user, segment, COLLECTION, DEFAULT_TEMPLATE);
    }
}
