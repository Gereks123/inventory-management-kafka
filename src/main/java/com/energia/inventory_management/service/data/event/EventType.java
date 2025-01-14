package com.energia.inventory_management.service.data.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {
    ITEM_CREATED,
    ITEM_UPDATED,
    ITEM_DELETED,
    ITEM_SOLD,
    ITEM_FETCHED,
    SALES_HISTORY_QUERIED;


    @JsonCreator
    public static EventType fromString(String value) {
        return value == null ? null : EventType.valueOf(value);
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
}