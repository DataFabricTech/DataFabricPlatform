package com.mobigen.monitoring.service.k8s;

import com.mobigen.monitoring.utils.Client;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class K8SService {
    private final CoreV1Api coreV1Api;
    private final String namespace;

    public K8SService(@Value("${k8s.namespace}") String namespace) {
        this.namespace = namespace;
        Client k8sClient = new Client();
        ApiClient client = k8sClient.getClient();
        this.coreV1Api = new CoreV1Api(client);
    }

    public Integer getNodePort(String serviceName, int targetPort) {
        // Get service details
        V1Service service = null;
        try {
            service = coreV1Api.readNamespacedService(serviceName, namespace, null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        if (service != null && service.getSpec() != null && service.getSpec().getPorts() != null) {
            return service.getSpec().getPorts().stream()
                    .filter(port -> Objects.requireNonNull(port.getTargetPort()).getIntValue() == targetPort)
                    .map(V1ServicePort::getNodePort)
                    .findFirst()
                    .orElse(null); // Return null if no matching NodePort is found
        }
        return null;
    }
}
