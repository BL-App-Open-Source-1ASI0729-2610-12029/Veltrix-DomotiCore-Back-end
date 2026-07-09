package com.domoticore.settings.application;

import com.domoticore.iam.domain.model.aggregates.User;
import com.domoticore.iam.infrastructure.persistence.jpa.UserRepository;
import com.domoticore.shared.application.JsonResourceService;
import com.domoticore.shared.domain.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private static final String COLLECTION = "user-profile";

    private final JsonResourceService jsonResourceService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserProfileService(
            JsonResourceService jsonResourceService,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.jsonResourceService = jsonResourceService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public JsonNode getProfile(Long userId) {
        return stripInternalId(loadOrCreateProfile(userId));
    }

    @Transactional
    public JsonNode updateProfile(Long userId, JsonNode patch) {
        loadOrCreateProfile(userId);
        return stripInternalId(jsonResourceService.patch(COLLECTION, resourceId(userId), patch));
    }

    @Transactional
    public void ensureProfile(Long userId) {
        loadOrCreateProfile(userId);
    }

    private JsonNode loadOrCreateProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String resourceId = resourceId(userId);
        if (jsonResourceService.exists(COLLECTION, resourceId)) {
            return jsonResourceService.findById(COLLECTION, resourceId);
        }

        return jsonResourceService.create(COLLECTION, buildDefaultProfile(user));
    }

    private ObjectNode buildDefaultProfile(User user) {
        ObjectNode profile = objectMapper.createObjectNode();
        profile.put("id", user.getId());
        profile.put("fullName", user.getName());
        profile.put("email", user.getEmail());
        profile.put("homeZone", "North Wing / Primary Residence");
        profile.put("homeZoneKey", "settings.homeZones.northWing");
        profile.put("displayMode", "light");
        profile.put("profilePhoto", user.getAvatar() != null
                ? user.getAvatar()
                : "assets/icons/shared/profile-admin.jpg");
        profile.put("twoFactorEnabled", false);
        profile.put("googleConnected", false);
        profile.put("appleKitConnected", false);
        profile.put("nestConnected", false);
        profile.put("roleKey", "settings.administrator");
        profile.put("jobTitleKey", "settings.systemOwner");
        profile.put("dataRetentionDays", 365);
        return profile;
    }

    private String resourceId(Long userId) {
        return String.valueOf(userId);
    }

    private JsonNode stripInternalId(JsonNode payload) {
        if (payload instanceof ObjectNode objectNode) {
            ObjectNode copy = objectNode.deepCopy();
            copy.remove("id");
            return copy;
        }
        return payload;
    }
}
