package org.amupoti.supermanager.parser.acb.dto;

import lombok.Value;

@Value
public class LoginRequest {

    String uuid ;
    String deviceId = "";
}
