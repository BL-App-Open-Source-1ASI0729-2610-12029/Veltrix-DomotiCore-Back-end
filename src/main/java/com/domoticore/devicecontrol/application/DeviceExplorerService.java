package com.domoticore.devicecontrol.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.shared.application.UserCollectionAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class DeviceExplorerService {

    private static final String COLLECTION = "device-explorer";
    private static final String DEFAULT_TEMPLATE = "default";

    private final UserCollectionAccessService userCollectionAccessService;

    public DeviceExplorerService(UserCollectionAccessService userCollectionAccessService) {
        this.userCollectionAccessService = userCollectionAccessService;
    }

    public JsonNode getDeviceExplorer(User user, String segment) {
        return userCollectionAccessService.getSingleton(user, segment, COLLECTION, DEFAULT_TEMPLATE);
    }
}
