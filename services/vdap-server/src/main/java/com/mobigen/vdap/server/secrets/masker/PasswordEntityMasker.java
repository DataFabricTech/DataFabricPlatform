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

package com.mobigen.vdap.server.secrets.masker;

import com.mobigen.vdap.annotator.PasswordField;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.secrets.SecretsManager;
import com.mobigen.vdap.server.secrets.SecretsUtil;
import com.mobigen.vdap.server.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PasswordEntityMasker extends EntityMasker {
    public static final String PASSWORD_MASK = "*********";
    private static final String NEW_KEY = "";

    protected PasswordEntityMasker() {
    }

    public Object maskServiceConnectionConfig(
            Object connectionConfig, String connectionType, ServiceType serviceType) {
        if (connectionConfig != null) {
            try {
                Object convertedConnectionConfig =
                        SecretsUtil.convert(connectionConfig, connectionType, null, serviceType);
                maskPasswordFields(convertedConnectionConfig);
                return convertedConnectionConfig;
            } catch (Exception e) {
                String message =
                        SecretsUtil.buildExceptionMessageConnectionMask(e.getMessage(), connectionType, true);
                if (message != null) {
                    throw new CustomException(message, e, null);
                }
                throw new CustomException(
                        String.format("Failed to mask connection instance of %s", connectionType), e, null);
            }
        }
        return null;
    }

//  public void maskAuthenticationMechanism(
//      String name, AuthenticationMechanism authenticationMechanism) {
//    if (authenticationMechanism != null) {
//      AuthenticationMechanismBuilder.addDefinedConfig(authenticationMechanism);
//      try {
//        maskPasswordFields(authenticationMechanism);
//      } catch (Exception e) {
//        throw new EntityMaskException(String.format("Failed to mask user bot instance [%s]", name));
//      }
//    }
//  }
//
//  public void maskIngestionPipeline(IngestionPipeline ingestionPipeline) {
//    if (ingestionPipeline != null) {
//      IngestionPipelineBuilder.addDefinedConfig(ingestionPipeline);
//      try {
//        maskPasswordFields(ingestionPipeline);
//      } catch (Exception e) {
//        throw new EntityMaskException(
//            String.format(
//                "Failed to mask ingestion pipeline instance [%s]", ingestionPipeline.getName()));
//      }
//    }
//  }
//
//  @Override
//  public Workflow maskWorkflow(Workflow workflow) {
//    if (workflow != null) {
//      Workflow workflowConverted =
//          (Workflow) ClassConverterFactory.getConverter(Workflow.class).convert(workflow);
//      try {
//        maskPasswordFields(workflowConverted);
//      } catch (Exception e) {
//        throw new EntityMaskException(
//            String.format("Failed to mask workflow instance [%s]", workflow.getName()));
//      }
//      return workflowConverted;
//    }
//    return null;
//  }

    public Object unmaskServiceConnectionConfig(
            Object connectionConfig,
            Object originalConnectionConfig,
            String connectionType,
            ServiceType serviceType) {
        if (originalConnectionConfig != null && connectionConfig != null) {
            try {
                Object toUnmaskConfig =
                        SecretsUtil.convert(connectionConfig, connectionType, null, serviceType);
                Object originalConvertedConfig =
                        SecretsUtil.convert(originalConnectionConfig, connectionType, null, serviceType);
                Map<String, String> passwordsMap = new HashMap<>();
                buildPasswordsMap(originalConvertedConfig, NEW_KEY, passwordsMap);
                unmaskPasswordFields(toUnmaskConfig, NEW_KEY, passwordsMap);
                return toUnmaskConfig;
            } catch (Exception e) {
                String message =
                        SecretsUtil.buildExceptionMessageConnectionMask(e.getMessage(), connectionType, false);
                if (message != null) {
                    throw new CustomException(message, e, null);
                }
                throw new CustomException(
                        String.format("Failed to unmask connection instance of %s", connectionType), e, null);
            }
        }
        return connectionConfig;
    }

//  public void unmaskIngestionPipeline(
//      IngestionPipeline ingestionPipeline, IngestionPipeline originalIngestionPipeline) {
//    if (ingestionPipeline != null && originalIngestionPipeline != null) {
//      IngestionPipelineBuilder.addDefinedConfig(ingestionPipeline);
//      IngestionPipelineBuilder.addDefinedConfig(originalIngestionPipeline);
//      try {
//        Map<String, String> passwordsMap = new HashMap<>();
//        buildPasswordsMap(originalIngestionPipeline, NEW_KEY, passwordsMap);
//        unmaskPasswordFields(ingestionPipeline, NEW_KEY, passwordsMap);
//      } catch (Exception e) {
//        throw new EntityMaskException(
//            String.format(
//                "Failed to unmask ingestion pipeline instance [%s]", ingestionPipeline.getName()));
//      }
//    }
//  }
//
//  public void unmaskAuthenticationMechanism(
//      String name,
//      AuthenticationMechanism authenticationMechanism,
//      AuthenticationMechanism originalAuthenticationMechanism) {
//    if (authenticationMechanism != null && originalAuthenticationMechanism != null) {
//      AuthenticationMechanismBuilder.addDefinedConfig(authenticationMechanism);
//      AuthenticationMechanismBuilder.addDefinedConfig(originalAuthenticationMechanism);
//      try {
//        Map<String, String> passwordsMap = new HashMap<>();
//        buildPasswordsMap(originalAuthenticationMechanism, NEW_KEY, passwordsMap);
//        unmaskPasswordFields(authenticationMechanism, NEW_KEY, passwordsMap);
//      } catch (Exception e) {
//        throw new EntityMaskException(
//            String.format("Failed to unmask auth mechanism instance [%s]", name));
//      }
//    }
//  }
//
//  @Override
//  public Workflow unmaskWorkflow(Workflow workflow, Workflow originalWorkflow) {
//    if (workflow != null && originalWorkflow != null) {
//      Workflow workflowConverted =
//          (Workflow) ClassConverterFactory.getConverter(Workflow.class).convert(workflow);
//      Workflow origWorkflowConverted =
//          (Workflow) ClassConverterFactory.getConverter(Workflow.class).convert(originalWorkflow);
//      try {
//        Map<String, String> passwordsMap = new HashMap<>();
//        buildPasswordsMap(origWorkflowConverted, NEW_KEY, passwordsMap);
//        unmaskPasswordFields(workflowConverted, NEW_KEY, passwordsMap);
//        return workflowConverted;
//      } catch (Exception e) {
//        throw new EntityMaskException(
//            String.format("Failed to unmask workflow instance [%s]", workflow.getName()));
//      }
//    }
//    return workflow;
//  }

    private void maskPasswordFields(Object toMaskObject) {
        // for each get method
        Arrays.stream(toMaskObject.getClass().getMethods())
                .filter(ReflectionUtil::isGetMethodOfObject)
                .forEach(
                        method -> {
                            Object obj = ReflectionUtil.getObjectFromMethod(method, toMaskObject);
                            String fieldName = method.getName().replaceFirst("get", "");
                            // if the object matches the package of user define
                            if (obj != null && obj.getClass().getPackageName().startsWith("com.mobigen")) {
                                // maskPasswordFields
                                maskPasswordFields(obj);
                                // check if it has PasswordField annotation
                            } else if (obj != null && method.getAnnotation(PasswordField.class) != null) {
                                // get setMethod
                                Method toSet = ReflectionUtil.getToSetMethod(toMaskObject, obj, fieldName);
                                // set new value
                                ReflectionUtil.setValueInMethod(toMaskObject, PASSWORD_MASK, toSet);
                            }
                        });
    }

    private void unmaskPasswordFields(
            Object toUnmaskObject, String key, Map<String, String> passwordsMap) {
        // for each get method
        Arrays.stream(toUnmaskObject.getClass().getMethods())
                .filter(ReflectionUtil::isGetMethodOfObject)
                .forEach(
                        method -> {
                            Object obj = ReflectionUtil.getObjectFromMethod(method, toUnmaskObject);
                            String fieldName = method.getName().replaceFirst("get", "");
                            // if the object matches the package of user define
                            if (obj != null && obj.getClass().getPackageName().startsWith("com.mobigen")) {
                                // maskPasswordFields
                                unmaskPasswordFields(obj, createKey(key, fieldName), passwordsMap);
                                // check if it has PasswordField annotation
                            } else if (obj != null && method.getAnnotation(PasswordField.class) != null) {
                                String valueToSet = null;
                                try {
                                    valueToSet = PASSWORD_MASK.equals(obj)
                                            ? passwordsMap.getOrDefault(createKey(key, fieldName), PASSWORD_MASK)
                                            : SecretsManager.decrypt((String) obj);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                // get setMethod
                                Method toSet = ReflectionUtil.getToSetMethod(toUnmaskObject, obj, fieldName);
                                // set new value
                                ReflectionUtil.setValueInMethod(toUnmaskObject, valueToSet, toSet);
                            }
                        });
    }

    private void buildPasswordsMap(Object toMapObject, String key, Map<String, String> passwordsMap) {
        // for each get method
        Arrays.stream(toMapObject.getClass().getMethods())
                .filter(ReflectionUtil::isGetMethodOfObject)
                .forEach(
                        method -> {
                            Object obj = ReflectionUtil.getObjectFromMethod(method, toMapObject);
                            String fieldName = method.getName().replaceFirst("get", "");
                            // if the object matches the package of user define
                            if (obj != null && obj.getClass().getPackageName().startsWith("com.mobigen")) {
                                // maskPasswordFields
                                buildPasswordsMap(obj, createKey(key, fieldName), passwordsMap);
                                // check if it has PasswordField annotation
                            } else if (obj != null && method.getAnnotation(PasswordField.class) != null) {
                                try {
                                    passwordsMap.put(createKey(key, fieldName), SecretsManager.decrypt((String) obj));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
    }

    private String createKey(String previousKey, String key) {
        return NEW_KEY.equals(previousKey) ? key : previousKey + "." + key;
    }
}
