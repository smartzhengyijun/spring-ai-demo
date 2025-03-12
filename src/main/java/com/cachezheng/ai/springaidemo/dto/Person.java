package com.cachezheng.ai.springaidemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Person {
    @JsonProperty(required = true, value = "姓名")
    private String name;
    @JsonProperty(required = true, value = "年龄")
    private Integer age;
}
