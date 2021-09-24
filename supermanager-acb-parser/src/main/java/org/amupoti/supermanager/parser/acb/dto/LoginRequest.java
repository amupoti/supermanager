package org.amupoti.supermanager.parser.acb.dto;

import lombok.Value;

@Value
public class LoginRequest {

    String client_id = "test";
    String client_secret = "1";
    String deviceId = " ";
    String grant_type = "password";
    String username;
    String password;
}
