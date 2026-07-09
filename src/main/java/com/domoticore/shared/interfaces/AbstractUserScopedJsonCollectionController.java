package com.domoticore.shared.interfaces;

import com.domoticore.shared.application.UserCollectionAccessService;
import com.domoticore.shared.config.openapi.ApiAuthenticatedGetResponses;
import com.domoticore.shared.config.openapi.ApiDeleteResponses;
import com.domoticore.shared.config.openapi.ApiGetByIdResponses;
import com.domoticore.shared.config.openapi.ApiGetListResponses;
import com.domoticore.shared.config.openapi.ApiPatchMutationResponses;
import com.domoticore.shared.config.openapi.ApiPostCreateResponses;
import com.domoticore.shared.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

public abstract class AbstractUserScopedJsonCollectionController {

    private final UserCollectionAccessService userCollectionAccessService;
    protected final CurrentUserProvider currentUserProvider;
    private final String collectionName;

    protected AbstractUserScopedJsonCollectionController(
            UserCollectionAccessService userCollectionAccessService,
            CurrentUserProvider currentUserProvider,
            String collectionName) {
        this.userCollectionAccessService = userCollectionAccessService;
        this.currentUserProvider = currentUserProvider;
        this.collectionName = collectionName;
    }

    @GetMapping
    @ApiAuthenticatedGetResponses
    @ApiGetListResponses
    @Operation(summary = "List user-scoped resources in collection")
    public List<JsonNode> list() {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.list(user, currentUserProvider.requireSegment(), collectionName);
    }

    @GetMapping("/{id}")
    @ApiAuthenticatedGetResponses
    @ApiGetByIdResponses
    @Operation(summary = "Get user-scoped resource by id")
    public JsonNode getById(@PathVariable String id) {
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.getById(user, currentUserProvider.requireSegment(), collectionName, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiPostCreateResponses
    @Operation(summary = "Create user-scoped resource")
    public JsonNode create(@RequestBody JsonNode body) {
        beforeCreate();
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.create(user, currentUserProvider.requireSegment(), collectionName, body);
    }

    @PatchMapping("/{id}")
    @ApiPatchMutationResponses
    @Operation(summary = "Partially update user-scoped resource")
    public JsonNode patch(@PathVariable String id, @RequestBody JsonNode body) {
        beforePatch();
        var user = currentUserProvider.requireUser();
        return userCollectionAccessService.patch(user, currentUserProvider.requireSegment(), collectionName, id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiDeleteResponses
    @Operation(summary = "Delete user-scoped resource")
    public void delete(@PathVariable String id) {
        beforeDelete();
        var user = currentUserProvider.requireUser();
        userCollectionAccessService.delete(user, currentUserProvider.requireSegment(), collectionName, id);
    }

    protected void beforeCreate() {
    }

    protected void beforePatch() {
    }

    protected void beforeDelete() {
    }
}
