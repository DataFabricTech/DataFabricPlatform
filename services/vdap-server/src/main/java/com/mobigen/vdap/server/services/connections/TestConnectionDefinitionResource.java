package com.mobigen.vdap.server.services.connections;

import com.mobigen.vdap.server.util.ResultList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import com.mobigen.vdap.schema.entity.services.connections.TestConnectionDefinition;
import com.mobigen.vdap.schema.type.Include;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@Tag(name = "Test Connection Definitions")
@RestController
@RequestMapping(value = "/v1/services/testConnectionDefinitions",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class TestConnectionDefinitionResource {
  public static final String COLLECTION_PATH = "/v1/services/testConnectionDefinitions";

  public static class TestConnectionDefinitionList extends ResultList<TestConnectionDefinition> {
        /* Required for serde */
 }

  @GetMapping
  @Operation(
      operationId = "listTestConnectionDefinitions",
      summary = "List test connection definitions",
      description =
          "Get a list of test connection definitions. Use cursor-based pagination to limit the number "
              + "entries in the list using `limit` and `before` or `after` query params.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of test connection definitions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            implementation =
                                    TestConnectionDefinitionResource.TestConnectionDefinitionList.class)))
      })
  public ResultList<TestConnectionDefinition> list(
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @RequestParam("fields")
          String fieldsParam,
      @Parameter(
              description =
                  "Limit the number test connection definitions returned. (1 to 1000000, default = 10)")
          @DefaultValue("10")
          @RequestParam("limit")
          @Min(0)
          @Max(1000000)
          int limitParam,
      @Parameter(
              description = "Returns list of test connection definitions before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of test connection definitions after this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("after")
          String after,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    ListFilter filter = new ListFilter(include);

    return super.listInternal(
        uriInfo, securityContext, fieldsParam, filter, limitParam, before, after);
  }

  @GET
  @Path("/{id}")
  @Operation(
      summary = "Get a test connection definition by Id",
      description = "Get a Test Connection Definition by `Id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The Test Connection definition",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TestConnectionDefinition.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Test Connection Definition for instance {id} is not found")
      })
  public TestConnectionDefinition get(
      @Context UriInfo uriInfo,
      @Parameter(
              description = "Id of the test connection definition",
              schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Context SecurityContext securityContext,
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
    return getInternal(uriInfo, securityContext, id, fieldsParam, include);
  }

  @GET
  @Path("/name/{name}")
  @Operation(
      operationId = "getTestConnectionDefinitionByName",
      summary = "Get a test connection definition by name",
      description = "Get a test connection definition by `name`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The test connection definition",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TestConnectionDefinition.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Test Connection Definition for instance {name} is not found")
      })
  public TestConnectionDefinition getByName(
      @Context UriInfo uriInfo,
      @Parameter(description = "Name of the test definition", schema = @Schema(type = "string"))
          @PathParam("name")
          String name,
      @Context SecurityContext securityContext,
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
    return getByNameInternal(uriInfo, securityContext, name, fieldsParam, include);
  }
}
