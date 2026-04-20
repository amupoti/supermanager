package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginResponse {

    String type;
    String jwt;

}
