package com.mobigen.vdap.server.services;

import com.github.fge.jsonpatch.JsonPatch;
import com.mobigen.vdap.schema.api.services.CreateStorageService;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.schema.entity.services.StorageService;
import com.mobigen.vdap.schema.entity.services.connections.TestConnectionResult;
import com.mobigen.vdap.schema.type.CommonResponse;
import com.mobigen.vdap.schema.type.Include;
import com.mobigen.vdap.server.annotations.CommonResponseAnnotation;
import com.mobigen.vdap.server.users.UserService;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.Utilities;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Tag(
        name = "Storage Services",
        description = "Storage Service is `Database, Storage, Api, Search Service`. such as MySQL, MariaDB, Postgres, MinIO, Elasticsearch, etc.")
@RestController
@RequestMapping(value = "/v1/services")
public class StorageServiceController {
    private static final String FIELDS = "pipelines,owners,tags";
    private final StorageServiceApp service;
    private final UserService userService;

    public StorageServiceController(StorageServiceApp service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    public static class StorageServiceList extends ArrayList<StorageService> {
        /* For RestAPI Doc */
    }

    @GetMapping
    @Operation(
            operationId = "listStorageServices",
            summary = "List Storage(Database, Storage, Search, Api) Services",
            description = "Get a list of all services. Page, Offset And Filter(kind_of_service, service_type)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of all services instances",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageServiceList.class)
                            )
                    )
            }
    )
    @CommonResponseAnnotation
    public Object list(
            HttpServletRequest request,
            @Parameter(
                    description = "Kind Of StorageService(database, storage, search, api).",
                    schema = @Schema(implementation = ServiceType.class, example = "database"))
            @RequestParam(name = "kind_of_service", required = false)
            ServiceType kindOfService,
            @Parameter(
                    description = "StorageService Type(Mysql, Mariadb, Minio, etc ...)",
                    schema = @Schema(implementation = StorageService.StorageServiceType.class, example = "Mysql"))
            @RequestParam(value = "service_type", required = false)
            StorageService.StorageServiceType serviceType,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam(value = "fields", required = false)
            String fields,
            @Parameter(
                    name = "page",
                    description = "Select Page Number",
                    schema = @Schema(type = "Integer", defaultValue = "0"))
            @RequestParam(value = "page", required = false)
            Integer page,
            @Parameter(
                    description = "Page Size",
                    schema = @Schema(type = "Integer", defaultValue = "20"))
            @RequestParam(value = "size", required = false)
            Integer size,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class, defaultValue = "non-deleted"))
            @RequestParam(value = "include", required = false, defaultValue = "non-deleted")
            Include include) {
        log.info("[StorageService] Get List");
        log.debug("[StorageService] Get List Kind[{}], ServiceType[{}], Fields[{}], Page[{}], Size[{}]",
                kindOfService, serviceType, fields, page, size);
        return service.list(Utilities.getBaseUri(request), kindOfService, serviceType, fields, page, size, include);
    }

    // GET /{id}/versions
    // GET /{id}/versions/{version}
    // PUT /{id}/testConnectionResult

    // PUT /restore -> POST /restore

    @GetMapping("/{id}")
    @Operation(
            operationId = "getStorageServiceByID",
            summary = "Get a storage service",
            description = "Get a storage service(database, storage, search, api) by `Id`.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Storage service(Database, Storage, Search, Api) instance",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageService.class))
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Storage service for instance {id} is not found")
            })
    @CommonResponseAnnotation
    public Object getById(
            HttpServletRequest request,
            @Parameter(
                    description = "Id of the storage service",
                    schema = @Schema(type = "UUID"))
            @PathVariable
            UUID id,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam("fields")
            String fieldsParam,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @RequestParam(value = "include", defaultValue = "non-deleted")
            Include include) {
        log.info("[StorageService] Get By ID[{}]", id);
        return service.getById(Utilities.getBaseUri(request), id, fieldsParam, include);
    }

    @GetMapping("/name/{name}")
    @Operation(
            operationId = "getStorageServiceByName",
            summary = "Get storage service(database, storage, search, api) by name",
            description = "Get a storage service by the service `name`.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Storage service instance",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageService.class)
                            )
                    )
            })
    @CommonResponseAnnotation
    public Object getByName(
            HttpServletRequest request,
            @Parameter(
                    description = "Name of the storage service",
                    schema = @Schema(type = "string"))
            @PathVariable("name")
            String name,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam("fields")
            String fields,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @RequestParam(value = "include", defaultValue = "non-deleted")
            Include include) {
        log.info("[StorageService] Get By Name[{}]", name);
        return service.getByName(Utilities.getBaseUri(request), name, fields, include);
    }

    @PostMapping("/{id}/testConnectionResult")
    @Operation(
            operationId = "addTestConnectionResult",
            summary = "Add test connection result",
            description = "Add test connection result to the service.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated the service",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageService.class)))
            })
    @CommonResponseAnnotation
    public Object addTestConnectionResult(
            @Parameter(
                    description = "Id of the service",
                    schema = @Schema(type = "UUID"))
            @PathVariable("id")
            UUID id,
            @Valid TestConnectionResult testConnectionResult) {
//        DatabaseService service = repository.addTestConnectionResult(id, testConnectionResult);
//        return decryptOrNullify(securityContext, service);
        return "add test connection result";
    }
/*
    @GetMapping("/{id}/versions")
    @Operation(
            operationId = "listAllStorageServiceVersion",
            summary = "List storage service versions",
            description = "Get a list of all the versions of a storage service identified by `Id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of storage service versions",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EntityHistory.class)))
            })
    public EntityHistory listVersions(
            @Parameter(
                    description = "Id of the database service",
                    schema = @Schema(type = "UUID"))
                @PathVariable("id") UUID id) {
//        EntityHistory entityHistory = super.listVersionsInternal(securityContext, id);
//        List<Object> versions =
//                entityHistory.getVersions().stream()
//                        .map(
//                                json -> {
//                                    try {
//                                        DatabaseService databaseService =
//                                                JsonUtils.readValue((String) json, DatabaseService.class);
//                                        return JsonUtils.pojoToJson(decryptOrNullify(securityContext, databaseService));
//                                    } catch (Exception e) {
//                                        return json;
//                                    }
//                                })
//                        .collect(Collectors.toList());
//        entityHistory.setVersions(versions);
        EntityHistory entityHistory = new EntityHistory();
        entityHistory.setEntityType(StorageService.class.getSimpleName());
        return entityHistory;
    }

    @GetMapping("/{id}/versions/{version}")
    @Operation(
            operationId = "getSpecificStorageServiceVersion",
            summary = "Get a version of the storage service",
            description = "Get a version of the storage service by given `Id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "storage service",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageService.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "storage service for instance {id} and version {version} is not found",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EntityError.class)
                            )
                    )
            })
    public Object getVersion(
            @Parameter(description = "Id of the storage service", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Parameter(
                    description = "storage service version number in the form `major`.`minor`",
                    schema = @Schema(type = "string", example = "0.1 or 1.1"))
            @PathVariable("version")
            String version) {
//        DatabaseService databaseService = super.getVersionInternal(securityContext, id, version);
//        return decryptOrNullify(securityContext, databaseService);
        return "get specific version storage service";
    }
    */

    @PostMapping
    @Operation(
            operationId = "createStorageService",
            summary = "Create storage service",
            description = "Create a new storage service.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Storage service instance",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StorageService.class))),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            })
    @CommonResponseAnnotation
    public Object create(
            HttpServletRequest request,
            @Valid @RequestBody CreateStorageService create) {

        log.info("[StorageService] Create");
        log.debug("[StorageService] CreateStorageService Data -\n{}", JsonUtils.pojoToJson(create, true));

        // TODO : 요청 사용자 정보 처리
        // String userId = header.get("X-VDAP-User-Id");
        // String userName = header.get("X-VDAP-User-Name");
        // User user = getUser(id, name)
        StorageService storage = createToEntity(create, "admin");
        return service.create(Utilities.getBaseUri(request), storage);
    }

    @PostMapping("/{id}/update")
    @Operation(
            operationId = "UpdateStorageService",
            summary = "Update data storage service",
            description = "Update an existing data storage service.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Storage service instance",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CreateStorageService.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    @CommonResponseAnnotation
    public Object update(
            HttpServletRequest request,
            @Parameter(
                    description = "Id of the storage service",
                    schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Valid @RequestBody CreateStorageService update) {
        log.info("[StorageService] Update");
        log.debug("[StorageService] UpdateStorageService Data -\n{}", JsonUtils.pojoToJson(update, true));
        // TODO : 요청 사용자 정보 처리
        StorageService storageService = createToEntity(update, "admin");
        storageService.withId(id);
        return service.update(Utilities.getBaseUri(request), storageService);
    }

    @PostMapping("/{id}/patch")
    @Operation(
            operationId = "patchStorageService",
            summary = "Update a data storage service",
            description = "Update an existing data storage service using JsonPatch.",
            externalDocs =
            @ExternalDocumentation(
                    description = "JsonPatch RFC",
                    url = "https://tools.ietf.org/html/rfc6902")
    )
    public Object patch(
            HttpServletRequest request,
            @Parameter(
                    description = "Id of the storage service",
                    schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Parameter(
                    description = "JsonPatch with array of operations",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                            examples = {
                                    @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                            }
                    )
            )
            @RequestBody JsonPatch patch) {
        // TODO : 요청 사용자 정보 처리
        return service.patch(Utilities.getBaseUri(request), id, patch, "admin");
    }

    @PostMapping("/{id}/delete")
    @Operation(
            operationId = "deleteStorageServiceById",
            summary = "Delete a data storage service by Id",
            description =
                    "Delete a data storage services. If child(database(tables), bucket(file)) belong the service, it can't be deleted.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "StorageService instance is not found form {id}")
            })
    @CommonResponseAnnotation
    public Object deleteById(
            HttpServletRequest request,
            @Parameter(description = "Recursively delete this entity and it's children. (Default `false`)")
            @RequestParam(name = "recursive", defaultValue = "false")
            boolean recursive,
            @Parameter(description = "Hard delete the entity. (Default = `false`)")
            @RequestParam(name = "hardDelete", defaultValue = "false")
            boolean hardDelete,
            @Parameter(description = "Id of the data storage service", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id) {
        service.deleteById(id, recursive, hardDelete, "admin");
        return "success";
    }

    @PostMapping("/name/{name}/delete")
    @Operation(
            operationId = "deleteStorageServiceByName",
            summary = "Delete a storage service by name",
            description =
                    "Delete a data storage services by `name`. If child(databases(tables), bucket(file)) belong the service, it can't be "
                            + "deleted.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "StorageService instance {name} is not found")
            })
    @CommonResponseAnnotation
    public Object delete(
            HttpServletRequest request,
            @Parameter(description = "Hard delete the entity. (Default = `false`)")
            @RequestParam(name = "hardDelete", defaultValue = "false")
            boolean hardDelete,
            @Parameter(description = "Recursively delete this entity and it's children. (Default `false`)")
            @RequestParam(name = "recursive", defaultValue = "false")
            boolean recursive,
            @Parameter(description = "Name of the database service", schema = @Schema(type = "string"))
            @PathVariable("name") String name) {
        service.deleteByName(name, recursive, hardDelete, "admin");
        return "success";
    }

//    @PostMapping("/{id}/restore")
//    @Operation(
//            operationId = "restore",
//            summary = "Restore a soft deleted database service",
//            description = "Restore a soft deleted database service.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Successfully restored the DatabaseService.",
//                            content =
//                            @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = DatabaseService.class)))
//            })
//    public Response restoreDatabaseService(
//            @Context UriInfo uriInfo,
//            @Context SecurityContext securityContext,
//            @Valid RestoreEntity restore) {
//        return restoreEntity(uriInfo, securityContext, restore.getId());
//    }

    public StorageService createToEntity(CreateStorageService request, String user) {
        StorageService entity = new StorageService();
        entity.withId(Utilities.generateUUID());
        entity.withKindOfService(request.getKindOfService());
        entity.withServiceType(request.getServiceType());
        entity.withName(request.getName());
        entity.withDisplayName(request.getDisplayName());
        entity.withDescription(request.getDescription());
        entity.withConnection(request.getConnection());
        entity.withOwners(request.getOwners() == null ? Collections.emptyList() : request.getOwners());
        entity.withTags(request.getTags() == null ? Collections.emptyList() : request.getTags());
        entity.withPipelines(Collections.emptyList());
//        entity.withTestConnectionResult()
        entity.withUpdatedAt(Utilities.getLocalDateTime());
        entity.withUpdatedBy(user);
        return entity;
    }
}
