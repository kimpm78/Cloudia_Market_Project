package com.cloudia.backend.CM_03_1000.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryItem {
    private String code;
    private String name;
}