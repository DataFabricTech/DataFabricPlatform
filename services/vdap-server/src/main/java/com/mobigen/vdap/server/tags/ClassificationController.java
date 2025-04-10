package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.api.classification.CreateClassification;
import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.server.annotations.CommonResponseAnnotation;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
public class ClassificationController {
    static final String FIELDS = "usageCount,termCount";

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

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
    @CommonResponseAnnotation
    public Object list(
            HttpServletRequest request,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fieldsParam,
            @Parameter(description = "Filter Disabled Classifications")
            @RequestParam(value = "disabled", required = false, defaultValue = "false")
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
            @RequestParam(value = "size", defaultValue = "20", required = false)
            Integer size) {
        log.info("[Classifications] Get List Fields[{}], page[{}], size[{}]", fieldsParam, page, size);
        return classificationService.list(Utilities.getBaseUri(request), fieldsParam, page, size);
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
    @CommonResponseAnnotation
    public Object getById(
            HttpServletRequest request,
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id")
            UUID id,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fieldsParam) {
        return classificationService.getById(Utilities.getBaseUri(request), fieldsParam, id);
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
    @CommonResponseAnnotation
    public Object getByName(
            HttpServletRequest request,
            @Parameter(description = "Name of the classification", schema = @Schema(type = "string"))
            @PathVariable("name")
            String name,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fieldsParam) {
        return classificationService.getByName(Utilities.getBaseUri(request), fieldsParam, name);
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
    @CommonResponseAnnotation
    public Object listVersions(
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id")
            UUID id) {
        return classificationService.listVersions(id);
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
    @CommonResponseAnnotation
    public Object getVersion(
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id")
            UUID id,
            @Parameter(
                    description = "classification version number in the form `major`.`minor`",
                    schema = @Schema(type = "string", example = "0.1 or 1.1"))
            @PathVariable("version")
            String version) {
        return classificationService.getVersion(id, version);
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
    @CommonResponseAnnotation
    public Object create(HttpServletRequest request,
                         @Parameter(
                                 name = "Create Classification",
                                 schema = @Schema(implementation = CreateClassification.class))
                         @RequestBody @Valid CreateClassification create) {
        // TODO : Get User Info
        log.info("[Classification] Create");
        log.debug("[Classification] CreateClassification Data -\n{}", JsonUtils.pojoToJson(create, true));
        Classification classification = createToEntity(create, "admin");
        return classificationService.create(Utilities.getBaseUri(request), classification);
    }

    @PostMapping("/{id}/update")
    @Operation(
            operationId = "updateClassification",
            summary = "Update a classification",
            description = "Update an existing category identify by category id")
    @CommonResponseAnnotation
    public Object update(
            HttpServletRequest request,
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @RequestBody @Valid
            CreateClassification create) {
        // TODO : Get UserInfo
        Classification classification = createToEntity(create, "admin");
        classification.setId(id);
        log.info("[Classification] Update");
        log.debug("[Classification] Update Classification Id[{}] Data - \n{}", id, JsonUtils.pojoToJson(create, true));
        return classificationService.update(Utilities.getBaseUri(request), classification);
    }

    @PostMapping("/{id}/delete")
    @Operation(
            operationId = "deleteClassification",
            summary = "Delete classification by id",
            description = "Delete a classification and all the tags under it.")
    @CommonResponseAnnotation
    public Object delete(
            @Parameter(description = "Id of the classification", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id) {
        log.info("[Classification] Delete By ID[{}]", id);
        classificationService.deleteById(id, "admin");
        return "success";
    }

    @PostMapping("/name/{name}/delete")
    @Operation(
            operationId = "deleteClassificationByName",
            summary = "Delete classification by name",
            description = "Delete a classification by `name` and all the tags under it.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
            })
    @CommonResponseAnnotation
    public Object delete(
            @Parameter(description = "Name of the classification", schema = @Schema(type = "string"))
            @PathVariable("name") String name) {
        log.info("[Classification] Delete By Name[{}]", name);
        classificationService.deleteByName(name, "admin");
        return "success";
    }

    public Classification createToEntity(CreateClassification request, String user) {
        Classification entity = new Classification();
        entity.setId(Utilities.generateUUID());
        entity.setName(request.getName());
        entity.setDisplayName(request.getDisplayName());
        entity.setDescription(request.getDescription());
        entity.setUpdatedBy(user);
        entity.setUpdatedAt(Utilities.getLocalDateTime());
        entity.setProvider(request.getProvider());
        entity.setMutuallyExclusive(request.getMutuallyExclusive());
        return entity;
    }

}
