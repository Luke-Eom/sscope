package com.sscope.sscope.login.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileResult {
    private Long id;
    private String connected_at;
    private Map<String, Object> kakao_account;

}
