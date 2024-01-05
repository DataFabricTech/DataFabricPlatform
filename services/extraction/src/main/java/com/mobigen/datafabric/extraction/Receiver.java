package com.mobigen.datafabric.extraction;

public class Receiver {
    public void Extract() {
        // TODO worker는 비동기적 + 멀티프로세싱 으로 작동할 수 있어야 한다.
        var id = getId();
        var worker = new Worker(id);
        worker.upsertMetadata();
    }

    /**
     * 주기적 / event 발생시 MessageQueue 에 등록된 id를 가져오는 함수
     *
     * @return dataModelId
     */
    public static String getId() {
        var id = "";
        return id;
    }
}
