package org.amupoti.supermanager.parser.acb.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SigninResponse {

    String AccessToken;
    String code;
}
