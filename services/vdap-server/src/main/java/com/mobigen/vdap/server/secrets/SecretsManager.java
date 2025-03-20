package com.mobigen.vdap.server.secrets;

import com.mobigen.vdap.annotator.PasswordField;
import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.util.ReflectionUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;


@Slf4j
@NoArgsConstructor
public class SecretsManager {

    private static final String ALGORITHM = "AES";

    private static final String SECRET = "j45DJWkJxdv+oxKv5zkuyXhGvi58Ycvt1tagcFbLfHQ=";

    public static SecretKey decodeSecretKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }

    // AES 암호화
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = decodeSecretKey(SECRET);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES 복호화
    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = decodeSecretKey(SECRET);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public Object encryptServiceConnectionConfig(
            Object connectionConfig,
            String connectionType,
            String connectionName,
            ServiceType serviceType) {
        try {
            Object newConnectionConfig =
                    SecretsUtil.convert(connectionConfig, connectionType, connectionName, serviceType);
            return encryptPasswordFields(newConnectionConfig);
        } catch (Exception e) {
            String message =
                    SecretsUtil.buildExceptionMessageConnection(e.getMessage(), connectionType, true);
            if (message != null) {
                throw new CustomException(message, e, connectionConfig);
            }
            throw new CustomException(
                    String.format(
                            "Failed to encrypt connection instance of %s. Did the Fernet Key change?",
                            connectionType), e, connectionConfig);
        }
    }

    public Object decryptServiceConnectionConfig(
            Object connectionConfig, String connectionType, ServiceType serviceType) {
        try {
            Object newConnectionConfig =
                    SecretsUtil.convert(connectionConfig, connectionType, null, serviceType);
            return decryptPasswordFields(newConnectionConfig);
        } catch (Exception e) {
            String message =
                    SecretsUtil.buildExceptionMessageConnection(e.getMessage(), connectionType, false);
            if (message != null) {
                throw new CustomException(message, e, connectionConfig);
            }
            throw new CustomException(
                    String.format(
                            "Failed to decrypt connection instance of %s. Did the Fernet Key change?",
                            connectionType), e, connectionConfig);
        }
    }

    //    public Object encryptAuthenticationMechanism(
//            String name, AuthenticationMechanism authenticationMechanism) {
//        if (authenticationMechanism != null) {
//            AuthenticationMechanismBuilder.addDefinedConfig(authenticationMechanism);
//            try {
//                return encryptPasswordFields(
//                        authenticationMechanism, buildSecretId(true, "bot", name), true);
//            } catch (Exception e) {
//                throw new SecretsManagerException(
//                        Response.Status.BAD_REQUEST,
//                        String.format("Failed to encrypt user bot instance [%s]", name));
//            }
//        }
//        return null;
//    }
//
//    /**
//     * This is used to handle the JWT Token internally, in the JWTFilter, when
//     * calling for the auth-mechanism in the UI, etc.
//     * If using SM, we need to decrypt and GET the secret to ensure we are comparing
//     * the right values.
//     */
//    public AuthenticationMechanism decryptAuthenticationMechanism(
//            String name, AuthenticationMechanism authenticationMechanism) {
//        if (authenticationMechanism != null) {
//            AuthenticationMechanismBuilder.addDefinedConfig(authenticationMechanism);
//            try {
//                AuthenticationMechanism fernetDecrypted =
//                        (AuthenticationMechanism) decryptPasswordFields(authenticationMechanism);
//                return (AuthenticationMechanism) getSecretFields(fernetDecrypted);
//            } catch (Exception e) {
//                throw new SecretsManagerException(
//                        Response.Status.BAD_REQUEST,
//                        String.format("Failed to decrypt user bot instance [%s]", name));
//            }
//        }
//        return null;
//    }
//
//    public OpenMetadataJWTClientConfig decryptJWTConfig(OpenMetadataJWTClientConfig jwtConfig) {
//        if (jwtConfig != null) {
//            try {
//                OpenMetadataJWTClientConfig decrypted =
//                        (OpenMetadataJWTClientConfig) decryptPasswordFields(jwtConfig);
//                return (OpenMetadataJWTClientConfig) getSecretFields(decrypted);
//            } catch (Exception e) {
//                throw new SecretsManagerException(
//                        Response.Status.BAD_REQUEST, "Failed to decrypt JWT Client Config instance.");
//            }
//        }
//        return null;
//    }
//
//    public void encryptIngestionPipeline(IngestionPipeline ingestionPipeline) {
//        OpenMetadataConnection openMetadataConnection =
//                encryptOpenMetadataConnection(ingestionPipeline.getOpenMetadataServerConnection(), true);
//        ingestionPipeline.setOpenMetadataServerConnection(null);
//        // we don't store OM conn sensitive data
//        IngestionPipelineBuilder.addDefinedConfig(ingestionPipeline);
//        try {
//            encryptPasswordFields(
//                    ingestionPipeline, buildSecretId(true, "pipeline", ingestionPipeline.getName()), true);
//        } catch (Exception e) {
//            throw new SecretsManagerException(
//                    Response.Status.BAD_REQUEST,
//                    String.format(
//                            "Failed to encrypt ingestion pipeline instance [%s]", ingestionPipeline.getName()));
//        }
//        ingestionPipeline.setOpenMetadataServerConnection(openMetadataConnection);
//    }
//
//    public void decryptIngestionPipeline(IngestionPipeline ingestionPipeline) {
//        OpenMetadataConnection openMetadataConnection =
//                decryptOpenMetadataConnection(ingestionPipeline.getOpenMetadataServerConnection());
//        ingestionPipeline.setOpenMetadataServerConnection(null);
//        // we don't store OM conn sensitive data
//        IngestionPipelineBuilder.addDefinedConfig(ingestionPipeline);
//        try {
//            decryptPasswordFields(ingestionPipeline);
//        } catch (Exception e) {
//            throw new SecretsManagerException(
//                    Response.Status.BAD_REQUEST,
//                    String.format(
//                            "Failed to decrypt ingestion pipeline instance [%s]", ingestionPipeline.getName()));
//        }
//        ingestionPipeline.setOpenMetadataServerConnection(openMetadataConnection);
//    }
//
//    public Workflow encryptWorkflow(Workflow workflow) {
//        OpenMetadataConnection openMetadataConnection =
//                encryptOpenMetadataConnection(workflow.getOpenMetadataServerConnection(), true);
//        Workflow workflowConverted =
//                (Workflow) ClassConverterFactory.getConverter(Workflow.class).convert(workflow);
//        // we don't store OM conn sensitive data
//        workflowConverted.setOpenMetadataServerConnection(null);
//        try {
//            encryptPasswordFields(
//                    workflowConverted, buildSecretId(true, "workflow", workflow.getName()), true);
//        } catch (Exception e) {
//            throw new SecretsManagerException(
//                    Response.Status.BAD_REQUEST,
//                    String.format("Failed to encrypt workflow instance [%s]", workflow.getName()));
//        }
//        workflowConverted.setOpenMetadataServerConnection(openMetadataConnection);
//        return workflowConverted;
//    }
//
//    public Workflow decryptWorkflow(Workflow workflow) {
//        OpenMetadataConnection openMetadataConnection =
//                decryptOpenMetadataConnection(workflow.getOpenMetadataServerConnection());
//        Workflow workflowConverted =
//                (Workflow) ClassConverterFactory.getConverter(Workflow.class).convert(workflow);
//        // we don't store OM conn sensitive data
//        workflowConverted.setOpenMetadataServerConnection(null);
//        try {
//            decryptPasswordFields(workflowConverted);
//        } catch (Exception e) {
//            throw new SecretsManagerException(
//                    Response.Status.BAD_REQUEST,
//                    String.format("Failed to decrypt workflow instance [%s]", workflow.getName()));
//        }
//        workflowConverted.setOpenMetadataServerConnection(openMetadataConnection);
//        return workflowConverted;
//    }
//
//    public OpenMetadataConnection encryptOpenMetadataConnection(
//            OpenMetadataConnection openMetadataConnection, boolean store) {
//        if (openMetadataConnection != null) {
//            OpenMetadataConnection openMetadataConnectionConverted =
//                    (OpenMetadataConnection)
//                            ClassConverterFactory.getConverter(OpenMetadataConnection.class)
//                                    .convert(openMetadataConnection);
//            try {
//                encryptPasswordFields(
//                        openMetadataConnectionConverted, buildSecretId(true, "serverconnection"), store);
//            } catch (Exception e) {
//                throw new SecretsManagerException(
//                        Response.Status.BAD_REQUEST, "Failed to encrypt OpenMetadataConnection instance.");
//            }
//            return openMetadataConnectionConverted;
//        }
//        return null;
//    }
//
//    public OpenMetadataConnection decryptOpenMetadataConnection(
//            OpenMetadataConnection openMetadataConnection) {
//        if (openMetadataConnection != null) {
//            OpenMetadataConnection openMetadataConnectionConverted =
//                    (OpenMetadataConnection)
//                            ClassConverterFactory.getConverter(OpenMetadataConnection.class)
//                                    .convert(openMetadataConnection);
//            try {
//                decryptPasswordFields(openMetadataConnectionConverted);
//            } catch (Exception e) {
//                throw new SecretsManagerException(
//                        Response.Status.BAD_REQUEST, "Failed to decrypt OpenMetadataConnection instance.");
//            }
//            return openMetadataConnectionConverted;
//        }
//        return null;
//    }
//
//    /**
//     * Used only in the OM Connection Builder, which sends the credentials to Ingestion Workflows
//     */
//    public JWTAuthMechanism decryptJWTAuthMechanism(JWTAuthMechanism authMechanism) {
//        if (authMechanism != null) {
//            try {
//                decryptPasswordFields(authMechanism);
//            } catch (Exception e) {
//                throw new SecretsManagerException(
//                        Response.Status.BAD_REQUEST, "Failed to decrypt OpenMetadataConnection instance.");
//            }
//            return authMechanism;
//        }
//        return null;
//    }
//
    private Object encryptPasswordFields(Object toEncryptObject) {
        try {
            // for each get method
            Arrays.stream(toEncryptObject.getClass().getMethods())
                    .filter(ReflectionUtil::isGetMethodOfObject)
                    .forEach(
                            method -> {
                                Object obj = ReflectionUtil.getObjectFromMethod(method, toEncryptObject);
                                String fieldName = method.getName().replaceFirst("get", "");
                                // if the object matches the package of user define object
                                if (Boolean.TRUE.equals(CommonUtil.isUserDefineObject(obj))) {
                                    // encryptPasswordFields
                                    encryptPasswordFields(obj);
                                    // check if it has annotation
                                } else if (obj != null && method.getAnnotation(PasswordField.class) != null) {
                                    // store value if proceed
                                    String newFieldValue = (String) obj;
                                    // get setMethod
                                    Method toSet = ReflectionUtil.getToSetMethod(toEncryptObject, obj, fieldName);
                                    // set new value
                                    try {
                                        ReflectionUtil.setValueInMethod(toEncryptObject, encrypt(newFieldValue), toSet);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
            return toEncryptObject;
        } catch (Exception e) {
            throw new CustomException(
                    String.format(
                            "Error trying to encrypt object with [%s]",
                            e.getMessage()), e, null);
        }
    }

    private Object decryptPasswordFields(Object toDecryptObject) {
        try {
            // for each get method
            Arrays.stream(toDecryptObject.getClass().getMethods())
                    .filter(ReflectionUtil::isGetMethodOfObject)
                    .forEach(
                            method -> {
                                Object obj = ReflectionUtil.getObjectFromMethod(method, toDecryptObject);
                                String fieldName = method.getName().replaceFirst("get", "");
                                // if the object matches the package of user define object
                                if (Boolean.TRUE.equals(CommonUtil.isUserDefineObject(obj))) {
                                    // encryptPasswordFields
                                    decryptPasswordFields(obj);
                                    // check if it has annotation
                                } else if (obj != null && method.getAnnotation(PasswordField.class) != null) {
                                    String fieldValue = (String) obj;
                                    // get setMethod
                                    Method toSet = ReflectionUtil.getToSetMethod(toDecryptObject, obj, fieldName);
                                    // set new value
                                    try {
                                        ReflectionUtil.setValueInMethod(toDecryptObject, decrypt(fieldValue), toSet);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
            return toDecryptObject;
        } catch (Exception e) {
            throw new CustomException(
                    String.format(
                            "Error trying to decrypt object [%s] due to [%s]",
                            toDecryptObject.toString(), e.getMessage()), e, toDecryptObject);
        }
    }
//
//    /**
//     * Get the object and use the secrets manager to get the right value to show
//     */
//    private Object getSecretFields(Object toDecryptObject) {
//        try {
//            // for each get method
//            Arrays.stream(toDecryptObject.getClass().getMethods())
//                    .filter(ReflectionUtil::isGetMethodOfObject)
//                    .forEach(
//                            method -> {
//                                Object obj = ReflectionUtil.getObjectFromMethod(method, toDecryptObject);
//                                String fieldName = method.getName().replaceFirst("get", "");
//                                // if the object matches the package of openmetadata
//                                if (Boolean.TRUE.equals(CommonUtil.isOpenMetadataObject(obj))) {
//                                    // encryptPasswordFields
//                                    getSecretFields(obj);
//                                    // check if it has annotation
//                                } else if (obj != null && method.getAnnotation(PasswordField.class) != null) {
//                                    String fieldValue = (String) obj;
//                                    // get setMethod
//                                    Method toSet = ReflectionUtil.getToSetMethod(toDecryptObject, obj, fieldName);
//                                    // set new value
//                                    ReflectionUtil.setValueInMethod(
//                                            toDecryptObject,
//                                            Boolean.TRUE.equals(isSecret(fieldValue))
//                                                    ? getSecretValue(fieldValue)
//                                                    : fieldValue,
//                                            toSet);
//                                }
//                            });
//            return toDecryptObject;
//        } catch (Exception e) {
//            throw new SecretsManagerException(
//                    String.format(
//                            "Error trying to GET secret [%s] due to [%s]",
//                            toDecryptObject.toString(), e.getMessage()));
//        }
//    }
//

//    @VisibleForTesting
//    void setFernet(Fernet fernet) {
//        this.fernet = fernet;
//    }
//
//    protected abstract void deleteSecretInternal(String secretName);
//
//    public void deleteSecretsFromServiceConnectionConfig(
//            Object connectionConfig,
//            String connectionType,
//            String connectionName,
//            ServiceType serviceType) {
//
//        try {
//            Object newConnectionConfig =
//                    SecretsUtil.convert(connectionConfig, connectionType, connectionName, serviceType);
//            deleteSecrets(newConnectionConfig, buildSecretId(true, serviceType.value(), connectionName));
//        } catch (Exception e) {
//            String message =
//                    SecretsUtil.buildExceptionMessageConnection(e.getMessage(), connectionType, true);
//            if (message != null) {
//                throw new InvalidServiceConnectionException(message);
//            }
//            throw InvalidServiceConnectionException.byMessage(
//                    connectionType,
//                    String.format("Failed to delete secrets from connection instance of %s", connectionType));
//        }
//    }
//
//    public void deleteSecretsFromWorkflow(Workflow workflow) {
//        Workflow workflowConverted =
//                (Workflow) ClassConverterFactory.getConverter(Workflow.class).convert(workflow);
//        // we don't store OM conn sensitive data
//        workflowConverted.setOpenMetadataServerConnection(null);
//        try {
//            deleteSecrets(workflowConverted, buildSecretId(true, "workflow", workflow.getName()));
//        } catch (Exception e) {
//            throw new SecretsManagerException(
//                    Response.Status.BAD_REQUEST,
//                    String.format(
//                            "Failed to delete secrets from workflow instance [%s]", workflow.getName()));
//        }
//    }
//
//    private void deleteSecrets(Object toDeleteSecretsFrom, String secretId) {
//        if (!DO_NOT_ENCRYPT_CLASSES.contains(toDeleteSecretsFrom.getClass())) {
//            Arrays.stream(toDeleteSecretsFrom.getClass().getMethods())
//                    .filter(ReflectionUtil::isGetMethodOfObject)
//                    .forEach(
//                            method -> {
//                                Object obj = ReflectionUtil.getObjectFromMethod(method, toDeleteSecretsFrom);
//                                String fieldName = method.getName().replaceFirst("get", "");
//                                // check if it has annotation:
//                                // We are replicating the logic that we use for storing the fields we need to
//                                // encrypt at encryptPasswordFields
//                                if (Boolean.TRUE.equals(CommonUtil.isOpenMetadataObject(obj))) {
//                                    deleteSecrets(
//                                            obj, buildSecretId(false, secretId, fieldName.toLowerCase(Locale.ROOT)));
//                                } else if (obj != null && method.getAnnotation(PasswordField.class) != null) {
//                                    deleteSecretInternal(
//                                            buildSecretId(false, secretId, fieldName.toLowerCase(Locale.ROOT)));
//                                }
//                            });
//        }
//    }
//
//    public static Map<String, String> getTags(SecretsConfig secretsConfig) {
//        Map<String, String> tags = new HashMap<>();
//        secretsConfig.tags.forEach(
//                keyValue -> {
//                    try {
//                        tags.put(keyValue.split(":")[0], keyValue.split(":")[1]);
//                    } catch (Exception e) {
//                        LOG.error(
//                                String.format(
//                                        "The SecretsConfig could not extract tag from [%s] due to [%s]",
//                                        keyValue, e.getMessage()));
//                    }
//                });
//        return tags;
//    }
}
