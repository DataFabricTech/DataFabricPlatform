package com.mobigen.monitoring.service.k8s;

import com.mobigen.monitoring.exception.CustomException;
import com.mobigen.monitoring.exception.ResponseCode;
import com.mobigen.monitoring.utils.Client;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
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

    /**
     * 연결 정보가 영어 (k8s service 이름)일 경우 해당 서버의 ip와 node port 로 바꾸기 위한 함수
     */
    public Integer getNodePort(String serviceName, int targetPort) {
        // Get service details
        V1Service service;

        try {
            // service 이름(예: mariadb-svc)으로 service 찾기
            service = coreV1Api.readNamespacedService(serviceName, namespace, null);
        } catch (ApiException e) {
            log.error("[K8S] API ERROR, check config file or permission");

            throw new CustomException(ResponseCode.DFM1000, "K8S api error");
        }

        // TODO node port 가 여러 개일 때 문제 발생 가능성 있음
        if (service != null && service.getSpec() != null && service.getSpec().getPorts() != null) {
            final Integer nodePort = service.getSpec().getPorts().stream()
                    .filter(port -> Objects.requireNonNull(port.getTargetPort()).getIntValue() == targetPort)
                    .map(V1ServicePort::getNodePort)
                    .findFirst()
                    .orElseThrow(null);

            if (nodePort != null) {
                return nodePort;
            }
        }

        throw new CustomException(ResponseCode.DFM1001, String.format("Not found service [ %s ] node port", serviceName));
    }
}
