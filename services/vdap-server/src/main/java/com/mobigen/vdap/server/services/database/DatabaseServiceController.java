package com.mobigen.vdap.server.services.database;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.mobigen.vdap.schema.api.data.RestoreEntity;
import com.mobigen.vdap.schema.api.services.CreateDatabaseService;
import com.mobigen.vdap.schema.entity.services.DatabaseService;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.schema.entity.services.connections.TestConnectionResult;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.Include;
import com.mobigen.vdap.schema.type.MetadataOperation;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.ResultList;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Tag(
        name = "Database Services",
        description = "`Database Service` is a service such as MySQL, MariaDB, Postgres.")
@RequestMapping(value = "/v1/services/databaseServices")
public class DatabaseServiceController {
    public static final String COLLECTION_PATH = "v1/services/databaseServices/";
    static final String FIELDS = "pipelines,owners,tags";

    public static class DatabaseServiceList extends ResultList<DatabaseService> {
        /* Required for serde */
    }

    @GetMapping
    @Operation(
            operationId = "listDatabaseServices",
            summary = "List database services",
            description = "Get a list of database services.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of database service instances",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DatabaseServiceList.class)))
            })
    public ResultList<DatabaseService> list(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @QueryParam("fields")
            String fieldsParam,
            @Parameter(
                    description = "Filter services by domain",
                    schema = @Schema(type = "string", example = "Marketing"))
            @QueryParam("domain")
            String domain,
            @DefaultValue("10") @Min(0) @Max(1000000) @QueryParam("limit") int limitParam,
            @Parameter(
                    description = "Returns list of database services before this cursor",
                    schema = @Schema(type = "string"))
            @QueryParam("before")
            String before,
            @Parameter(
                    description = "Returns list of database services after this cursor",
                    schema = @Schema(type = "string"))
            @QueryParam("after")
            String after,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @QueryParam("include")
            @DefaultValue("non-deleted")
            Include include) {
        return listInternal(
                uriInfo, securityContext, fieldsParam, include, domain, limitParam, before, after);
    }

    @GET
    @Path("/{id}")
    @Operation(
            operationId = "getDatabaseServiceByID",
            summary = "Get a database service",
            description = "Get a database service by `Id`.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Database service instance",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DatabaseService.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Database service for instance {id} is not found")
            })
    public DatabaseService get(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Id of the database service", schema = @Schema(type = "UUID"))
            @PathParam("id")
            UUID id,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @QueryParam("fields")
            String fieldsParam,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @QueryParam("include")
            @DefaultValue("non-deleted")
            Include include) {
        DatabaseService databaseService =
                getInternal(uriInfo, securityContext, id, fieldsParam, include);
        return decryptOrNullify(securityContext, databaseService);
    }

    @GET
    @Path("/name/{name}")
    @Operation(
            operationId = "getDatabaseServiceByFQN",
            summary = "Get database service by name",
            description = "Get a database service by the service `name`.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Database service instance",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DatabaseService.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Database service for instance {name} is not found")
            })
    public DatabaseService getByName(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Name of the database service", schema = @Schema(type = "string"))
            @PathParam("name")
            String name,
            @Parameter(
                    description = "Fields requested in the returned resource",
                    schema = @Schema(type = "string", example = FIELDS))
            @QueryParam("fields")
            String fieldsParam,
            @Parameter(
                    description = "Include all, deleted, or non-deleted entities.",
                    schema = @Schema(implementation = Include.class))
            @QueryParam("include")
            @DefaultValue("non-deleted")
            Include include) {
        DatabaseService databaseService =
                getByNameInternal(uriInfo, securityContext, name, fieldsParam, include);
        return decryptOrNullify(securityContext, databaseService);
    }

    @PUT
    @Path("/{id}/testConnectionResult")
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
                                    schema = @Schema(implementation = DatabaseService.class)))
            })
    public DatabaseService addTestConnectionResult(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Id of the service", schema = @Schema(type = "UUID"))
            @PathParam("id")
            UUID id,
            @Valid TestConnectionResult testConnectionResult) {
        OperationContext operationContext = new OperationContext(entityType, MetadataOperation.CREATE);
        authorizer.authorize(securityContext, operationContext, getResourceContextById(id));
        DatabaseService service = repository.addTestConnectionResult(id, testConnectionResult);
        return decryptOrNullify(securityContext, service);
    }

    @GET
    @Path("/{id}/versions")
    @Operation(
            operationId = "listAllDatabaseServiceVersion",
            summary = "List database service versions",
            description = "Get a list of all the versions of a database service identified by `Id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of database service versions",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = EntityHistory.class)))
            })
    public EntityHistory listVersions(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Id of the database service", schema = @Schema(type = "UUID"))
            @PathParam("id")
            UUID id) {
        EntityHistory entityHistory = super.listVersionsInternal(securityContext, id);

        List<Object> versions =
                entityHistory.getVersions().stream()
                        .map(
                                json -> {
                                    try {
                                        DatabaseService databaseService =
                                                JsonUtils.readValue((String) json, DatabaseService.class);
                                        return JsonUtils.pojoToJson(decryptOrNullify(securityContext, databaseService));
                                    } catch (Exception e) {
                                        return json;
                                    }
                                })
                        .collect(Collectors.toList());
        entityHistory.setVersions(versions);
        return entityHistory;
    }

    @GET
    @Path("/{id}/versions/{version}")
    @Operation(
            operationId = "getSpecificDatabaseServiceVersion",
            summary = "Get a version of the database service",
            description = "Get a version of the database service by given `Id`",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "database service",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DatabaseService.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Database service for instance {id} and version {version} is not found")
            })
    public DatabaseService getVersion(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Parameter(description = "Id of the database service", schema = @Schema(type = "UUID"))
            @PathParam("id")
            UUID id,
            @Parameter(
                    description = "database service version number in the form `major`.`minor`",
                    schema = @Schema(type = "string", example = "0.1 or 1.1"))
            @PathParam("version")
            String version) {
        DatabaseService databaseService = super.getVersionInternal(securityContext, id, version);
        return decryptOrNullify(securityContext, databaseService);
    }

    @POST
    @Operation(
            operationId = "createDatabaseService",
            summary = "Create database service",
            description = "Create a new database service.",
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
    public Response create(
            @Context UriInfo uriInfo,
            @Context SecurityContext securityContext,
            @Valid CreateDatabaseService create) {
        DatabaseService service =
                mapper.createToEntity(create, securityContext.getUserPrincipal().getName());
        Response response = create(uriInfo, securityContext, service);
        decryptOrNullify(securityContext, (DatabaseService) response.getEntity());
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
