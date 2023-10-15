package com.stc.inspireu.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemNotFoundException extends RuntimeException {

    private String item;

    public static ItemNotFoundExceptionBuilder builder(String item) {
        return new ItemNotFoundExceptionBuilder().item(item);
    }

    @Override
    public String getMessage() {
        return item;
    }

    @Override
    public String toString() {
        return item;
    }
}
