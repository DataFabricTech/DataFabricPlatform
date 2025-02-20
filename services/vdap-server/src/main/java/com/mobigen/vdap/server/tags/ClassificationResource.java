package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.api.classification.CreateClassification;
import com.mobigen.vdap.schema.api.data.RestoreEntity;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.Include;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/classifications")
@Tag(
        name = "Classifications",
        description =
                "These APIs are related to `Classification` and `Tags`. A `Classification` "
                        + "entity "
                        + "contains hierarchical"
                        + " terms called `Tags` used "
                        + "for categorizing and classifying data assets and other entities.")
public class ClassificationResource {
    //  public static final String TAG_COLLECTION_PATH = "/v1/classifications/";
    static final String FIELDS = "usageCount,termCount";

    static class ClassificationList extends ArrayList<Classification> {
        /* Required for serde */
    }

    @GetMapping
    @Operation(
            operationId = "listClassifications",
            summary = "List classifications",
            description = "Get a list of classifications.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The user ",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ClassificationList.class)))
            })
    public Object list(
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fieldsParam,
            @Parameter(description = "Filter Disabled Classifications")
            @RequestParam("disabled")
            String disabled,
            @Parameter(
                    description = "select page number of classifications",
                    schema = @Schema(type = "integer", minimum = "0"))
            @RequestParam(value = "page", defaultValue = "0", required = false)
            Integer page,
            @Parameter(
                    description =
                            "size the number classifications returned. (1 to 50, default = 20) ",
                    schema = @Schema(type = "integer", minimum = "0", maximum = "50"))
            @RequestParam(value = "size", required = false, defaultValue = "20")
            Integer size,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @RequestParam(value = "include", defaultValue = "non-deleted", required = false)
            Include include) {
        return "classification list";
//        ListFilter filter = new ListFilter(include);
//        return super.listInternal(
//                uriInfo, securityContext, fieldsParam, filter, limitParam, before, after);
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "getClassificationByID",
            summary = "Get a classification by id",
            description = "Get a classification by `id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "classification",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Classification.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Classification for instance {id} is not found")
            })
    public Classification get(
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id")
            UUID id,
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
        return null;
//        return getInternal(uriInfo, securityContext, id, fieldsParam, include);
    }

    @GetMapping("name/{name}")
    @Operation(
            operationId = "getClassificationByName",
            summary = "Get a classification by name",
            description =
                    "Get a classification identified by name. The response includes classification information along "
                            + "with the entire hierarchy of all the children tags.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The user ",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Classification.class)))
            })
    public Classification getByName(
            @Parameter(description = "Name of the classification", schema = @Schema(type = "string"))
            @PathVariable("name")
            String name,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fieldsParam,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @RequestParam(value = "include", required = false, defaultValue = "non-deleted")
            Include include) {
        return null;
//        return getByNameInternal(uriInfo, securityContext, name, fieldsParam, include);
    }

    @GetMapping("/{id}/versions")
    @Operation(
            operationId = "listAllClassificationVersion",
            summary = "List classification versions",
            description = "Get a list of all the versions of a classification identified by `id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of classification versions",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EntityHistory.class)))
            })
    public EntityHistory listVersions(
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id")
            UUID id) {
        return null;
//        return super.listVersionsInternal(securityContext, id);
    }

    @GetMapping("/{id}/versions/{version}")
    @Operation(
            operationId = "getSpecificClassificationVersion",
            summary = "Get a version of the classification",
            description = "Get a version of the classification by given `id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "glossaries",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Classification.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Classification for instance {id} and version {version} is not found")
            })
    public Classification getVersion(
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id")
            UUID id,
            @Parameter(
                    description = "classification version number in the form `major`.`minor`",
                    schema = @Schema(type = "string", example = "0.1 or 1.1"))
            @PathVariable("version")
            String version) {
        return null;
//        return super.getVersionInternal(securityContext, id, version);
    }

    @PostMapping
    @Operation(
            operationId = "createClassification",
            summary = "Create a classification",
            description =
                    "Create a new classification. The request can include the children tags to be created along "
                            + "with the classification.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The user ",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Classification.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    public Object create(
            @Valid CreateClassification create) {
//        Classification category =
//                mapper.createToEntity(create, securityContext.getUserPrincipal().getName());
//        return create(uriInfo, securityContext, category);
        return null;
    }

    @PostMapping("/update")
    @Operation(
            operationId = "createOrUpdateClassification",
            summary = "Update a classification",
            description = "Update an existing category identify by category name")
    public Object createOrUpdate(
            @Valid CreateClassification create) {
//        Classification category =
//                mapper.createToEntity(create, securityContext.getUserPrincipal().getName());
//        return createOrUpdate(uriInfo, securityContext, category);
        return null;
    }

    /*
    @PATCH
    @Path("/{id}")
    @Operation(
            operationId = "patchClassification",
            summary = "Update a classification",
            description = "Update an existing classification using JsonPatch.",
            externalDocs =
            @ExternalDocumentation(
                    description = "JsonPatch RFC",
                    url = "https://tools.ietf.org/html/rfc6902"))
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response patch(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathParam("id")
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
    @PostMapping("/{id}/delete")
    @Operation(
            operationId = "deleteClassification",
            summary = "Delete classification by id",
            description = "Delete a classification and all the tags under it.")
    public Object delete(
            @Parameter(
                    description = "Recursively delete this entity and it's children. (Default `false`)")
            @RequestParam(value = "recursive", defaultValue = "false", required = false)
            boolean recursive,
            @Parameter(description = "Hard delete the entity. (Default = `false`)")
            @RequestParam(value = "hardDelete", defaultValue = "false", required = false)
            boolean hardDelete,
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathParam("id")
            UUID id) {
//        return delete(uriInfo, securityContext, id, recursive, hardDelete);
        return null;
    }

    @PostMapping("/name/{name}/delete")
    @Operation(
            operationId = "deleteClassificationByName",
            summary = "Delete classification by name",
            description = "Delete a classification by `name` and all the tags under it.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
            })
    public Object delete(
            @Parameter(description = "Hard delete the entity. (Default = `false`)")
            @RequestParam(value = "hardDelete", defaultValue = "false", required = false)
            boolean hardDelete,
            @Parameter(description = "Name of the classification", schema = @Schema(type = "string"))
            @PathVariable("name")
            String name) {
//        return deleteByName(uriInfo, securityContext, name, false, hardDelete);
        return null;
    }

    @PostMapping("/restore")
    @Operation(
            operationId = "restoreClassification",
            summary = "Restore a soft deleted classification",
            description = "Restore a soft deleted classification.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully restored the Table ",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Classification.class)))
            })
    public Object restore(
            @Valid RestoreEntity restore) {
//        return restoreEntity(uriInfo, securityContext, restore.getId());
        return null;
    }

    public Classification createToEntity(CreateClassification request, String user) {
//        List<EntityReference> owners = validateOwners(request.getOwners());
//        validateReviewers(request.getReviewers());
        Classification entity = new Classification();
        entity.setId(UUID.randomUUID());
        entity.setName(request.getName());
        entity.setDisplayName(request.getDisplayName());
        entity.setDescription(request.getDescription());
//        entity.setOwners(owners);
        entity.setUpdatedBy(user);
        entity.setUpdatedAt(System.currentTimeMillis());
//        entity.setReviewers(request.getReviewers());
        entity.setProvider(request.getProvider());
        entity.setMutuallyExclusive(request.getMutuallyExclusive());
        return entity;
    }
}
