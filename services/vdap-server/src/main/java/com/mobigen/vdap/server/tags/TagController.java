package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.api.AddTagToAssetsRequest;
import com.mobigen.vdap.schema.api.classification.CreateTag;
import com.mobigen.vdap.schema.api.data.RestoreEntity;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.ChangeEvent;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.api.BulkOperationResult;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.annotations.CommonResponseAnnotation;
import com.mobigen.vdap.server.util.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/tags")
@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Classifications",
        description =
                "These APIs are related to `Classification` and `Tags`. A `Classification`"
                        + " "
                        + "entity "
                        + "contains hierarchical"
                        + " terms called `Tags` used "
                        + "for categorizing and classifying data assets and other entities.")
public class TagController {
    static final String FIELDS = "children,usageCount";

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    static class TagList extends ArrayList<Tag> {
        /* Required for serde */
    }

    @GetMapping
    @Operation(
            operationId = "listTags",
            summary = "List tags",
            description =
                    "Get a list of tags. Use `fields` parameter to get only necessary fields. "
                            + " Use cursor-based pagination to limit the number "
                            + "entries in the list using `limit` and `before` or `after` query params.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of tags",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TagList.class)))
            })
    @CommonResponseAnnotation
    public Object list(
            @Parameter(
                    description =
                            "List tags filtered by children of tag identified by uuid given in `parent` parameter.",
                    schema = @Schema(type = "UUID"))
            @RequestParam(value = "parent", required = false)
            String parent,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fieldsParam,
            @Parameter(description = "page number of tags")
            @RequestParam(value = "page", defaultValue = "0", required = false)
            Integer page,
            @Parameter(description = "size the number tags returned. (1 to 50, default = 20)")
            @RequestParam(value = "size", defaultValue = "20", required = false)
            Integer size) {
//        ListFilter filter =
//                new ListFilter(include)
//                        .addRequestParam("parent", parent)
//                        .addRequestParam("classification.disabled", disabled);
//        return super.listInternal(
//                uriInfo, securityContext, fieldsParam, filter, limitParam, before, after);
        return null;
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "getTagByID",
            summary = "Get a tag by id",
            description = "Get a tag by `id`.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The tag",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Tag.class)))
            })
    @CommonResponseAnnotation
    public Object getById(
            HttpServletRequest request,
            @Parameter(description = "Id of the tag", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fieldsParam) {
        return tagService.getById(Utilities.getBaseUri(request), id, fieldsParam);
    }

    @GetMapping("/{id}/versions")
    @Operation(
            operationId = "listAllTagVersion",
            summary = "List tag versions",
            description = "Get a list of all the versions of a tag identified by `id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of tag versions",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EntityHistory.class)))
            })
    public EntityHistory listVersions(
            @Parameter(description = "Id of the tag", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id) {
//        return super.listVersionsInternal(securityContext, id);
        return null;
    }

    @GetMapping("/{id}/versions/{version}")
    @Operation(
            operationId = "getSpecificTagVersion",
            summary = "Get a version of the tags",
            description = "Get a version of the tag by given `id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "tags",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Tag.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Tag for instance {id} and version {version} is not found")
            })
    public Tag getVersion(
            @Parameter(description = "Id of the tag", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Parameter(
                    description = "tag version number in the form `major`.`minor`",
                    schema = @Schema(type = "string", example = "0.1 or 1.1"))
            @PathVariable("version") String version) {
//        return super.getVersionInternal(securityContext, id, version);
        return null;
    }

    @PostMapping
    @Operation(
            operationId = "createTag",
            summary = "Create a tag",
            description = "Create a new tag.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The tag",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Tag.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    public Object create(
            HttpServletRequest request,
            @Valid @RequestBody CreateTag create) {
        // TODO : Get User Info
        Tag tag = createToEntity(create, "admin");
        return tagService.create(Utilities.getBaseUri(request), tag);
    }

    /*
    @PostMapping("/{id}/patch")
    @Operation(
            operationId = "patchTag",
            summary = "Update a tag",
            description = "Update an existing tag using JsonPatch.",
            externalDocs =
            @ExternalDocumentation(
                    description = "JsonPatch RFC",
                    url = "https://tools.ietf.org/html/rfc6902"))
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response patch(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Id of the tag", schema = @Schema(type = "UUID")) @PathVariable("id")
            UUID id,
            @RequestBody(
                    description = "JsonPatch with array of operations",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                            examples = {
                                    @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                            }))
            JsonPatch patch) {
        return patchInternal(uriInfo, securityContext, id, patch);
    }
    */

    @PostMapping("/update")
    @Operation(
            operationId = "createOrUpdateTag",
            summary = "Create or update a tag",
            description = "Create a new tag, if it does not exist or update an existing tag.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The tag",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Tag.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    public Object createOrUpdate(
            @Valid CreateTag create) {
//        Tag tag = mapper.createToEntity(create, securityContext.getUserPrincipal().getName());
//        return createOrUpdate(uriInfo, securityContext, tag);
        return null;
    }

    @PostMapping("/{id}/delete")
    @Operation(
            operationId = "deleteTag",
            summary = "Delete a tag by id",
            description = "Delete a tag by `id`.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "tag for instance {id} is not found")
            })
    public Object deleteById(
            HttpServletRequest request,
            @Parameter(description = "Id of the tag", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id) {
        return tagService.deleteInternalById(, securityContext, id, recursive, hardDelete);
    }

    @PostMapping("/restore")
    @Operation(
            operationId = "restoreTag",
            summary = "Restore a soft deleted tag.",
            description = "Restore a soft deleted tag.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully restored the Tag ",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Tag.class)))
            })
    public Object restore(
            @Valid @RequestBody RestoreEntity restore) {
//        return restoreEntity(uriInfo, securityContext, restore.getId());
        return null;
    }

    @PostMapping("/{id}/assets/add")
    @Operation(
            operationId = "bulkAddTagToAssets",
            summary = "Bulk Add Classification Tag to Assets",
            description = "Bulk Add Classification Tag to Assets",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BulkOperationResult.class))),
                    @ApiResponse(responseCode = "404", description = "model for instance {id} is not found")
            })
    public Object bulkAddTagToAssets(
            @Parameter(description = "Id of the Entity", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Valid @RequestBody AddTagToAssetsRequest request) {
//        return bulkAddToAssetsAsync(securityContext, id, request);
        return null;
    }

    @PostMapping("/{id}/assets/remove")
    @Operation(
            operationId = "bulkRemoveTagFromAssets",
            summary = "Bulk Remove Tag from Assets",
            description = "Bulk Remove Tag from Assets",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ChangeEvent.class))),
                    @ApiResponse(responseCode = "404", description = "model for instance {id} is not found")
            })
    public Object bulkRemoveTagFromAssets(
            @Parameter(description = "Id of the Entity", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Valid @RequestBody AddTagToAssetsRequest request) {
//        return bulkRemoveFromAssetsAsync(securityContext, id, request);
        return null;
    }

    public Tag createToEntity(CreateTag request, String user) {
        Tag entity = new Tag();
        entity.setId(Utilities.generateUUID());
        entity.setName(request.getName());
        entity.setDisplayName(request.getDisplayName());
        entity.setDescription(request.getDescription());
        entity.setUpdatedBy(user);
        entity.setUpdatedAt(Utilities.getLocalDateTime());
        entity.withClassification(getEntityReference(Entity.CLASSIFICATION, request.getClassification()));
        entity.withProvider(request.getProvider());
        entity.withMutuallyExclusive(request.getMutuallyExclusive());
        return entity;
    }

    public EntityReference getEntityReference(String entityType, UUID id) {
//        switch (entityType) {
//            case Entity.TAG -> {
//                return tagService.getReferenceById(id);
//            }
//            case Entity.CLASSIFICATION -> {
//                return classificationService.getReferenceById(id);
//            }
//        }
        return null;
    }
}
