package com.mobigen.vdap.server.secrets.converter;

import com.mobigen.vdap.schema.entity.automations.TestServiceConnectionRequest;
import com.mobigen.vdap.schema.entity.automations.Workflow;
import com.mobigen.vdap.schema.services.connections.metadata.VdapServerConnection;
import com.mobigen.vdap.server.util.JsonUtils;

import java.util.List;

/** Converter class to get an `Workflow` object. */
public class WorkflowClassConverter extends ClassConverter {

  public WorkflowClassConverter() {
    super(Workflow.class);
  }

  @Override
  public Object convert(Object object) {
    Workflow workflow = (Workflow) JsonUtils.convertValue(object, this.clazz);

    tryToConvertOrFail(workflow.getRequest(), List.of(TestServiceConnectionRequest.class))
        .ifPresent(workflow::setRequest);

    if (workflow.getServerConnection() != null) {
      workflow.setServerConnection(
          (VdapServerConnection)
              ClassConverterFactory.getConverter(VdapServerConnection.class)
                  .convert(workflow.getServerConnection()));
    }

    return workflow;
  }
}
