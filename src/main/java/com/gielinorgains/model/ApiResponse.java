package com.gielinorgains.model;

import lombok.Data;
import java.util.List;

@Data
public class ApiResponse {
    private List<GainsItem> data;
    private int totalItems;
    private boolean success;
    private String error;
}