package org.amupoti.supermanager.acb.adapter.out.acbapi.dto;

import lombok.Value;

@Value
public class LoginRequest {

    String uuid ;
    String deviceId = "";
}
