package org.amupoti.supermanager.parser.acb.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginResponse {

    String type;
    String jwt;

}
