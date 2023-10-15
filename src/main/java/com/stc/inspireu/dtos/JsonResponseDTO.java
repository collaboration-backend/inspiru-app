package com.stc.inspireu.dtos;

import lombok.Data;

@Data
public class JsonResponseDTO<T> {

    private boolean success = false;

    private String message = "";

    private T data;

    public JsonResponseDTO(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public JsonResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public JsonResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public JsonResponseDTO() {
    }
}
