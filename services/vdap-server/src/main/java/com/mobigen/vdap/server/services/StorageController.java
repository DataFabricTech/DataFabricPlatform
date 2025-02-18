package com.mobigen.vdap.server.services;

import com.mobigen.vdap.schema.api.services.CreateStorageService;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.schema.entity.services.StorageService;
import com.mobigen.vdap.schema.entity.services.connections.TestConnectionResult;
import com.mobigen.vdap.schema.system.EntityError;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.Include;
import com.mobigen.vdap.server.util.ResultList;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Tag(
        name = "Storage Services",
        description = "Storage Service is `Database, Storage, Api, Search Service`. such as MySQL, MariaDB, Postgres, MinIO, Elasticsearch, etc.")
@RestController
@RequestMapping(value = "/v1/services")
public class StorageController {
    static final String FIELDS = "pipelines,owners,tags";

    public static class StorageServiceList extends ResultList<StorageService> {
        /* For RestAPI Doc */
    }

    @GetMapping
    @Operation(
            operationId = "getAllServices",
            summary = "Get All Services",
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
    public Object getAllServices(
            @Parameter(
                    description = "Kind Of StorageService(database, storage, search, api).",
                    schema = @Schema(type = "string", example = "database"))
            @RequestParam("kind_of_service")
            ServiceType kindOfService,
            @Parameter(
                    description = "StorageService Type(Mysql, Mariadb, Minio, etc ...)",
                    schema = @Schema(type = "string", example = "Mysql"))
            @RequestParam("service_type")
            CreateStorageService.StorageServiceType service_type,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam("fields")
            String fields,
            @Parameter(
                    description = "Select Page Number",
                    schema = @Schema(type = "Integer"))
            @RequestParam("page") Integer page,
            @Parameter(
                    description = "Offset",
                    schema = @Schema(type = "Integer"))
            @RequestParam("offset") Integer offset,
            @RequestParam("size") Integer size,
            @RequestParam("limit") Integer limit,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @RequestParam("include")
            @DefaultValue("non-deleted")
            Include include
    ) {
        return "get all services";
//        return service.getAllServices(kindOfService, service_type, page, offset, size, limit, include);
    }

    // GET List
    // GET  kind - List
    // GET  kind - List
    // GET /{id}
    // GET name/{name}
    // GET /{id}/versions
    // GET /{id}/versions/{version}
    // POST Create
    // PUT CreateOrUpdate
    // PUT /{id}/testConnectionResult
    // PATCH /{id} -> /{id}/update
    // PATCH /{fqn} -> /{id}/update
    // DELETE /{id} -> POST /{id}/delete
    // DELETE /{fqn} -> POST /{id}/delete
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
    public Object getById(
            @Parameter(description = "Id of the storage service", schema = @Schema(type = "UUID"))
            @PathVariable UUID id,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @RequestParam("fields")
            String fieldsParam,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @RequestParam("include")
            @DefaultValue("non-deleted")
            Include include) {
        return "get by id storage service";
//        DatabaseService databaseService =
//                getInternal(uriInfo, securityContext, id, fieldsParam, include);
//        return decryptOrNullify(securityContext, databaseService);
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
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Not found storage service instance",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EntityError.class)
                            )
                    )
            })
    public Object getByName(
            @Parameter(description = "Name of the storage service", schema = @Schema(type = "string"))
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
            @QueryParam("include")
            @DefaultValue("non-deleted")
            Include include) {
//        DatabaseService databaseService =
//                getByNameInternal(uriInfo, securityContext, name, fieldsParam, include);
//        return decryptOrNullify(securityContext, databaseService);
        return "get by name storage service";
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
    public Object addTestConnectionResult(
            @Parameter(description = "Id of the service", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id,
            @Valid TestConnectionResult testConnectionResult) {
//        DatabaseService service = repository.addTestConnectionResult(id, testConnectionResult);
//        return decryptOrNullify(securityContext, service);
        return "add test connection result";
    }

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
            @Parameter(description = "Id of the database service", schema = @Schema(type = "UUID"))
            @PathVariable("id") UUID id) {
//        EntityHistory entityHistory = super.listVersionsInternal(securityContext, id);
//
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
                                    schema = @Schema(implementation = Response.class))),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EntityError.class)
                            )
                    )
            })
    public Response create(
            @Valid CreateStorageService create) {
//        DatabaseService service =
//                mapper.createToEntity(create, securityContext.getUserPrincipal().getName());
//        Response response = service.create(createService);
//        decryptOrNullify(securityContext, (DatabaseService) response.getEntity());
        return response;
    }

    @PUT
    @Operation(
            operationId = "createOrUpdateDatabaseService",
            summary = "Update database service",
            description = "Update an existing or create a new database service.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Database service instance",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DatabaseService.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    public Response createOrUpdate(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Valid CreateDatabaseService update) {
        DatabaseService service =
                mapper.createToEntity(update, securityContext.getUserPrincipal().getName());
        Response response = createOrUpdate(uriInfo, securityContext, unmask(service));
        decryptOrNullify(securityContext, (DatabaseService) response.getEntity());
        return response;
    }

    @PATCH
    @Path("/{id}")
    @Operation(
            operationId = "patchDatabaseService",
            summary = "Update a database service",
            description = "Update an existing database service using JsonPatch.",
            externalDocs =
            @ExternalDocumentation(
                    description = "JsonPatch RFC",
                    url = "https://tools.ietf.org/html/rfc6902"))
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response patch(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Id of the database service", schema = @Schema(type = "UUID"))
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

    @PATCH
    @Path("/name/{fqn}")
    @Operation(
            operationId = "patchDatabaseService",
            summary = "Update a database service using name.",
            description = "Update an existing database service using JsonPatch.",
            externalDocs =
            @ExternalDocumentation(
                    description = "JsonPatch RFC",
                    url = "https://tools.ietf.org/html/rfc6902"))
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response patch(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Name of the database service", schema = @Schema(type = "string"))
            @PathParam("fqn")
            String fqn,
            @RequestBody(
                    description = "JsonPatch with array of operations",
                    content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                            examples = {
                                    @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                            }))
            JsonPatch patch) {
        return patchInternal(uriInfo, securityContext, fqn, patch);
    }

    @GET
    @Path("/name/{name}/export")
    @Produces(MediaType.TEXT_PLAIN)
    @Valid
    @Operation(
            operationId = "exportDatabaseServices",
            summary = "Export database service in CSV format",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Exported csv with services from the database services",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = String.class)))
            })
    public String exportCsv(
            @Context SecurityContext securityContext,
            @Parameter(description = "Name of the Database Service", schema = @Schema(type = "string"))
            @PathParam("name")
            String name)
            throws IOException {
        return exportCsvInternal(securityContext, name);
    }

    @GET
    @Path("/name/{name}/exportAsync")
    @Produces(MediaType.APPLICATION_JSON)
    @Valid
    @Operation(
            operationId = "exportDatabaseService",
            summary = "Export database service in CSV format",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Exported csv with database schemas",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CSVExportResponse.class)))
            })
    public Response exportCsvAsync(
            @Context SecurityContext securityContext,
            @Parameter(description = "Name of the Database", schema = @Schema(type = "string"))
            @PathParam("name")
            String name) {
        return exportCsvInternalAsync(securityContext, name);
    }

    @PUT
    @Path("/name/{name}/import")
    @Consumes(MediaType.TEXT_PLAIN)
    @Valid
    @Operation(
            operationId = "importDatabaseService",
            summary = "Import service from CSV to update database service (no creation allowed)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Import result",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CsvImportResult.class)))
            })
    public CsvImportResult importCsv(
            @Context SecurityContext securityContext,
            @Parameter(description = "Name of the Database Service", schema = @Schema(type = "string"))
            @PathParam("name")
            String name,
            @Parameter(
                    description =
                            "Dry-run when true is used for validating the CSV without really importing it. (default=true)",
                    schema = @Schema(type = "boolean"))
            @DefaultValue("true")
            @QueryParam("dryRun")
            boolean dryRun,
            String csv)
            throws IOException {
        return importCsvInternal(securityContext, name, csv, dryRun);
    }

    @PUT
    @Path("/name/{name}/importAsync")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Valid
    @Operation(
            operationId = "importDatabaseServiceAsync",
            summary =
                    "Import service from CSV to update database service asynchronously (no creation allowed)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Import initiated successfully",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CsvImportResult.class)))
            })
    public Response importCsvAsync(
            @Context SecurityContext securityContext,
            @Parameter(description = "Name of the Database Service", schema = @Schema(type = "string"))
            @PathParam("name")
            String name,
            @Parameter(
                    description =
                            "Dry-run when true is used for validating the CSV without really importing it. (default=true)",
                    schema = @Schema(type = "boolean"))
            @DefaultValue("true")
            @QueryParam("dryRun")
            boolean dryRun,
            String csv) {
        return importCsvInternalAsync(securityContext, name, csv, dryRun);
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            operationId = "deleteDatabaseService",
            summary = "Delete a database service by Id",
            description =
                    "Delete a database services. If databases (and tables) belong the service, it can't be deleted.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "DatabaseService service for instance {id} is not found")
            })
    public Response delete(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(
                    description = "Recursively delete this entity and it's children. (Default `false`)")
            @DefaultValue("false")
            @QueryParam("recursive")
            boolean recursive,
            @Parameter(description = "Hard delete the entity. (Default = `false`)")
            @QueryParam("hardDelete")
            @DefaultValue("false")
            boolean hardDelete,
            @Parameter(description = "Id of the database service", schema = @Schema(type = "UUID"))
            @PathParam("id")
            UUID id) {
        return delete(uriInfo, securityContext, id, recursive, hardDelete);
    }

    @DELETE
    @Path("/name/{name}")
    @Operation(
            operationId = "deleteDatabaseServiceByName",
            summary = "Delete a database service by name",
            description =
                    "Delete a database services by `name`. If databases (and tables) belong the service, it can't be "
                            + "deleted.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "DatabaseService service for instance {name} is not found")
            })
    public Response delete(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Hard delete the entity. (Default = `false`)")
            @QueryParam("hardDelete")
            @DefaultValue("false")
            boolean hardDelete,
            @Parameter(
                    description = "Recursively delete this entity and it's children. (Default `false`)")
            @QueryParam("recursive")
            @DefaultValue("false")
            boolean recursive,
            @Parameter(description = "Name of the database service", schema = @Schema(type = "string"))
            @PathParam("name")
            String name) {
        return deleteByName(uriInfo, securityContext, name, recursive, hardDelete);
    }

    @PUT
    @Path("/restore")
    @Operation(
            operationId = "restore",
            summary = "Restore a soft deleted database service",
            description = "Restore a soft deleted database service.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully restored the DatabaseService.",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DatabaseService.class)))
            })
    public Response restoreDatabaseService(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Valid RestoreEntity restore) {
        return restoreEntity(uriInfo, securityContext, restore.getId());
    }

    @Override
    protected DatabaseService nullifyConnection(DatabaseService service) {
        return service.withConnection(null);
    }

    @Override
    protected String extractServiceType(DatabaseService service) {
        return service.getServiceType().value();
    }
}
