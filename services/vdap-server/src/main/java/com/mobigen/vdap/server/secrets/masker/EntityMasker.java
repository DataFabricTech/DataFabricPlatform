package com.mobigen.vdap.server.secrets.masker;

import com.mobigen.vdap.schema.entity.services.ServiceType;

public abstract class EntityMasker {

    public abstract Object maskServiceConnectionConfig(
            Object connectionConfig, String connectionType, ServiceType serviceType);

    public abstract Object unmaskServiceConnectionConfig(
            Object connectionConfig,
            Object originalConnectionConfig,
            String connectionType,
            ServiceType serviceType);

//  public abstract void maskAuthenticationMechanism(
//      String name, AuthenticationMechanism authenticationMechanism);

//  public abstract void maskIngestionPipeline(IngestionPipeline ingestionPipeline);

//  public abstract Workflow maskWorkflow(Workflow workflow);

//  public abstract void unmaskIngestionPipeline(
//      IngestionPipeline ingestionPipeline, IngestionPipeline originalIngestionPipeline);

//  public abstract void unmaskAuthenticationMechanism(
//      String name,
//      AuthenticationMechanism authenticationMechanism,
//      AuthenticationMechanism originalAuthenticationMechanism);

//  public abstract Workflow unmaskWorkflow(Workflow workflow, Workflow originalWorkflow);
}
