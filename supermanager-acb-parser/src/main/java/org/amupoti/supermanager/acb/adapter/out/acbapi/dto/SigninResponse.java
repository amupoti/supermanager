package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SigninResponse {

    String AccessToken;
    String code;
}
