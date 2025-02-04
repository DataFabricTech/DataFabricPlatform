package com.mobigen.datafabric.relationship.data;

import lombok.Getter;

@Getter
public enum InteractionType {
    FOLLOW(0),
    RECOMMEND(1);
    private final int value;

    InteractionType(int value) {
        this.value = value;
    }
}
