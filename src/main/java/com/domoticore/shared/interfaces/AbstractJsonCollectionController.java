package com.domoticore.shared.interfaces;

import com.domoticore.shared.application.JsonResourceService;
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

public abstract class AbstractJsonCollectionController {

    private final JsonResourceService jsonResourceService;
    private final String collectionName;

    protected AbstractJsonCollectionController(JsonResourceService jsonResourceService, String collectionName) {
        this.jsonResourceService = jsonResourceService;
        this.collectionName = collectionName;
    }

    @GetMapping
    @Operation(summary = "List all resources in collection")
    public List<JsonNode> list() {
        return jsonResourceService.findAll(collectionName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by id")
    public JsonNode getById(@PathVariable String id) {
        return jsonResourceService.findById(collectionName, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create resource")
    public JsonNode create(@RequestBody JsonNode body) {
        return jsonResourceService.create(collectionName, body);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update resource")
    public JsonNode patch(@PathVariable String id, @RequestBody JsonNode body) {
        return jsonResourceService.patch(collectionName, id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete resource")
    public void delete(@PathVariable String id) {
        jsonResourceService.delete(collectionName, id);
    }
}
