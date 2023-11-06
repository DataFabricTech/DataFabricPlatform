package com.mobigen.datafabric.core.worker.queue;

public interface Queue<E> {
    boolean add(E e) throws Exception;
    E poll() throws Exception;
}

// Queue 는 2가지 작업을 향 후 진행할 필요가 있음.
// 1. 큐에 들어갈 내용을 제네릭 타입으로 구성
// 2. 큐를 여러개 사용(다중큐사용)에 대비 큐 아이디, 싱글톤 구성과 관리.
//    마찬가지로 MQTT 호환을 고려하여 MQTT 큐 이름(아이디)? 구성을 유사하게 구성
// 3. 큐 설정 옵션에 따라 MQTT, Memory Queue 로 내부가 구성될 수 있도록 구성
