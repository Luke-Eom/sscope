package com.sscope.sscope.login.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;

@Getter
@RequiredArgsConstructor
public class KakaoUserInfo {

    private final Long oid;
    private final String email;

}
