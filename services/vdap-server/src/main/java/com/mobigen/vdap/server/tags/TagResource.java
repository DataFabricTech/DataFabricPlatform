package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.api.AddTagToAssetsRequest;
import com.mobigen.vdap.schema.api.classification.CreateTag;
import com.mobigen.vdap.schema.api.data.RestoreEntity;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.ChangeEvent;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.Include;
import com.mobigen.vdap.schema.type.api.BulkOperationResult;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class TagResource {
    //  private final ClassificationMapper classificationMapper = new ClassificationMapper();
//  private final TagMapper mapper = new TagMapper();
//  public static final String TAG_COLLECTION_PATH = "/v1/tags/";
    static final String FIELDS = "children,usageCount";

    static class TagList extends ArrayList<Tag> {
        /* Required for serde */
    }

    @GetMapping
    @Valid
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
    public ArrayList<Tag> list(
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
            @Parameter(
                    description = "Filter Disabled Classifications",
                    schema = @Schema(type = "string"))
                @RequestParam(value = "disabled", defaultValue = "false", required = false)
                Boolean disabled,
            @Parameter(description = "page number of tags")
                @RequestParam(value = "page", defaultValue = "0", required = false)
                Integer page,
            @Parameter(description = "size the number tags returned. (1 to 50, default = 20)")
                @RequestParam(value = "size", defaultValue = "20", required = false)
                Integer size,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
                @RequestParam(value = "include", defaultValue = "non-deleted", required = false)
                Include include) {
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
    public Tag get(
            @Parameter(description = "Id of the tag", schema = @Schema(type = "UUID"))
                @PathVariable("id") UUID id,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
                @RequestParam(value = "fields", required = false)
                String fieldsParam,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
                @RequestParam(value = "include", defaultValue = "non-deleted", required = false)
                Include include) {
//        return getInternal(uriInfo, securityContext, id, fieldsParam, include);
        return null;
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
            @Valid CreateTag create) {
//        Tag tag = mapper.createToEntity(create, securityContext.getUserPrincipal().getName());
//        return create(uriInfo, securityContext, tag);
        return null;
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
    public Object delete(
            @Parameter(description = "Id of the tag", schema = @Schema(type = "UUID"))
                @PathVariable("id") UUID id,
            @Parameter(
                    description = "Recursively delete this entity and it's children. (Default `false`)")
                @RequestParam(value = "recursive", defaultValue = "false", required = false)
                boolean recursive,
            @Parameter(description = "Hard delete the entity. (Default = `false`)")
                @RequestParam(value = "hardDelete", defaultValue = "false", required = false)
                boolean hardDelete ) {
//        return delete(uriInfo, securityContext, id, recursive, hardDelete);
        return null;
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
            @Valid RestoreEntity restore) {
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
            @Valid AddTagToAssetsRequest request) {
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
            @Valid AddTagToAssetsRequest request) {
//        return bulkRemoveFromAssetsAsync(securityContext, id, request);
        return null;
    }

//    @Override
//    public Tag addHref(UriInfo uriInfo, Tag tag) {
//        super.addHref(uriInfo, tag);
//        Entity.withHref(uriInfo, tag.getClassification());
//        Entity.withHref(uriInfo, tag.getParent());
//        return tag;
//    }
    public Tag createToEntity(CreateTag request, String user) {
//        return copy(new Tag(), create, user)
//                .withParent(getEntityReference("tag", create.getParent()))
//                .withClassification(getEntityReference("classification", create.getClassification()))
//                .withProvider(create.getProvider())
//                // 이걸 이용해서 일반적인 태그로 사용(true), 카테고리(false)로 사용할 것 인지 설정
//                .withMutuallyExclusive(create.getMutuallyExclusive());
        return null;
    }
}
