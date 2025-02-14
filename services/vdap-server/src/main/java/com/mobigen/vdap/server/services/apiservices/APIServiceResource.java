/*
 *  Copyright 2021 Collate
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mobigen.vdap.server.services.apiservices;

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
import com.mobigen.vdap.schema.api.services.CreateApiService;
import com.mobigen.vdap.schema.entity.services.ApiService;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.schema.entity.services.connections.TestConnectionResult;
import com.mobigen.vdap.schema.type.ApiConnection;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.Include;
import com.mobigen.vdap.schema.type.MetadataOperation;
import com.mobigen.vdap.schema.utils.EntityInterfaceUtil;
import com.mobigen.vdap.service.Entity;
import com.mobigen.vdap.service.jdbi3.APIServiceRepository;
import com.mobigen.vdap.service.limits.Limits;
import com.mobigen.vdap.service.resources.Collection;
import com.mobigen.vdap.service.resources.services.ServiceEntityResource;
import com.mobigen.vdap.service.security.Authorizer;
import com.mobigen.vdap.service.security.policyevaluator.OperationContext;
import com.mobigen.vdap.service.util.JsonUtils;
import com.mobigen.vdap.service.util.ResultList;

import javax.json.JsonPatch;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Path("/v1/services/apiServices")
@Tag(
    name = "API Services",
    description = "APIs related `API Service` entities, such as REST or MicroService.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "apiServices")
public class APIServiceResource
    extends ServiceEntityResource<ApiService, APIServiceRepository, ApiConnection> {
  private final APIServiceMapper mapper = new APIServiceMapper();
  public static final String COLLECTION_PATH = "v1/services/apiServices/";
  static final String FIELDS = "pipelines,owners,tags,domain";

  @Override
  public ApiService addHref(UriInfo uriInfo, ApiService service) {
    super.addHref(uriInfo, service);
    Entity.withHref(uriInfo, service.getPipelines());
    return service;
  }

  public APIServiceResource(Authorizer authorizer, Limits limits) {
    super(Entity.API_SERVICE, authorizer, limits, ServiceType.API);
  }

  @Override
  protected List<MetadataOperation> getEntitySpecificOperations() {
    addViewOperation("pipelines", MetadataOperation.VIEW_BASIC);
    return null;
  }

  public static class APIServiceList extends ResultList<ApiService> {
    /* Required for serde */
  }

  @GET
  @Operation(
      operationId = "listAPIServices",
      summary = "List API services",
      description = "Get a list of API services.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of API service instances",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            implementation =
                                org.openmetadata.service.resources.services.apiservices
                                    .APIServiceResource.APIServiceList.class)))
      })
  public ResultList<ApiService> list(
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
              description = "Returns list of API services before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of API services after this cursor",
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
      operationId = "getAPIServiceByID",
      summary = "Get an API service",
      description = "Get an API service by `id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "API service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "API service for instance {id} is not found")
      })
  public ApiService get(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @PathParam("id") UUID id,
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
    ApiService apiService = getInternal(uriInfo, securityContext, id, fieldsParam, include);
    return decryptOrNullify(securityContext, apiService);
  }

  @GET
  @Path("/name/{name}")
  @Operation(
      operationId = "getAPIServiceByFQN",
      summary = "Get API service by name",
      description = "Get a API service by the service `name`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "API service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "API service for instance {id} is not found")
      })
  public ApiService getByName(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @PathParam("name") String name,
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
    ApiService apiService =
        getByNameInternal(
            uriInfo, securityContext, EntityInterfaceUtil.quoteName(name), fieldsParam, include);
    return decryptOrNullify(securityContext, apiService);
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
                    schema = @Schema(implementation = ApiService.class)))
      })
  public ApiService addTestConnectionResult(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Valid TestConnectionResult testConnectionResult) {
    OperationContext operationContext = new OperationContext(entityType, MetadataOperation.CREATE);
    authorizer.authorize(securityContext, operationContext, getResourceContextById(id));
    ApiService service = repository.addTestConnectionResult(id, testConnectionResult);
    return decryptOrNullify(securityContext, service);
  }

  @GET
  @Path("/{id}/versions")
  @Operation(
      operationId = "listAllAPIServiceVersion",
      summary = "List API service versions",
      description = "Get a list of all the versions of an API service identified by `id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of API service versions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntityHistory.class)))
      })
  public EntityHistory listVersions(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "API service Id", schema = @Schema(type = "string")) @PathParam("id")
          UUID id) {
    EntityHistory entityHistory = super.listVersionsInternal(securityContext, id);

    List<Object> versions =
        entityHistory.getVersions().stream()
            .map(
                json -> {
                  try {
                    ApiService apiService = JsonUtils.readValue((String) json, ApiService.class);
                    return JsonUtils.pojoToJson(decryptOrNullify(securityContext, apiService));
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
      operationId = "getSpecificAPIServiceVersion",
      summary = "Get a version of the API service",
      description = "Get a version of the API service by given `id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "API service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "API service for instance {id} and version {version} is not found")
      })
  public ApiService getVersion(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "API service Id", schema = @Schema(type = "string")) @PathParam("id")
          UUID id,
      @Parameter(
              description = "API service version number in the form `major`.`minor`",
              schema = @Schema(type = "string", example = "0.1 or 1.1"))
          @PathParam("version")
          String version) {
    ApiService apiService = super.getVersionInternal(securityContext, id, version);
    return decryptOrNullify(securityContext, apiService);
  }

  @POST
  @Operation(
      operationId = "createApiService",
      summary = "Create API service",
      description = "Create a new API service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "API service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiService.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response create(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateApiService create) {
    ApiService service =
        mapper.createToEntity(create, securityContext.getUserPrincipal().getName());
    Response response = create(uriInfo, securityContext, service);
    decryptOrNullify(securityContext, (ApiService) response.getEntity());
    return response;
  }

  @PUT
  @Operation(
      operationId = "createOrUpdateAPIService",
      summary = "Update API service",
      description = "Update an existing or create a new API service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Object store service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiService.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response createOrUpdate(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateApiService update) {
    ApiService service =
        mapper.createToEntity(update, securityContext.getUserPrincipal().getName());
    Response response = createOrUpdate(uriInfo, securityContext, unmask(service));
    decryptOrNullify(securityContext, (ApiService) response.getEntity());
    return response;
  }

  @PATCH
  @Path("/{id}")
  @Operation(
      operationId = "patchAPIService",
      summary = "Update an API service",
      description = "Update an existing API service using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @PathParam("id") UUID id,
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
      operationId = "patchAPIService",
      summary = "Update an API service using name.",
      description = "Update an existing API service using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @PathParam("fqn") String fqn,
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

  @DELETE
  @Path("/{id}")
  @Operation(
      operationId = "deleteAPIService",
      summary = "Delete an API service",
      description = "Delete an API services.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "API service for instance {id} is not found")
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
      @Parameter(description = "Id of the API service", schema = @Schema(type = "string"))
          @PathParam("id")
          UUID id) {
    return delete(uriInfo, securityContext, id, recursive, hardDelete);
  }

  @DELETE
  @Path("/name/{fqn}")
  @Operation(
      operationId = "deleteAPIServiceByFQN",
      summary = "Delete an APIService by fully qualified name",
      description = "Delete an APIService by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "APIService for instance {fqn} is not found")
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
      @Parameter(description = "Name of the APIService", schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn) {
    return deleteByName(
        uriInfo, securityContext, EntityInterfaceUtil.quoteName(fqn), recursive, hardDelete);
  }

  @PUT
  @Path("/restore")
  @Operation(
      operationId = "restore",
      summary = "Restore a soft deleted API Service.",
      description = "Restore a soft deleted API Service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully restored the API Service.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiService.class)))
      })
  public Response restoreAPIService(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid RestoreEntity restore) {
    return restoreEntity(uriInfo, securityContext, restore.getId());
  }

  @Override
  protected ApiService nullifyConnection(ApiService service) {
    return service.withConnection(null);
  }

  @Override
  protected String extractServiceType(ApiService service) {
    return service.getServiceType().value();
  }
}
