package org.amupoti.supermanager.acb.adapter.out.acbapi;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.LoginRequest;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.LoginResponse;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.SigninResponse;
import org.amupoti.supermanager.acb.application.port.out.AuthenticationPort;
import org.amupoti.supermanager.acb.exception.ErrorCode;
import org.amupoti.supermanager.acb.exception.InfrastructureException;
import org.amupoti.supermanager.acb.exception.SmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Out-adapter: authenticates users via the ACB API.
 */
@Component
@Slf4j
public class AcbAuthenticationAdapter implements AuthenticationPort {

    @Value("${acb.url.signin:https://id.acb.com/api/signIn}")
    private String preLoginUrl;

    @Value("${acb.url.token:https://supermanager.acb.com/oauth/V2/open/accounttoken/getTokens}")
    private String loginUrl;

    private final RestTemplate restTemplate;

    public AcbAuthenticationAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String authenticate(String user, String password) {
        LoginResponse response = checkUserLogin(user, password);
        if (response == null || response.getJwt() == null) {
            log.warn("ACB auth response missing JWT — API may have changed. Response: {}", response);
            throw new SmException(ErrorCode.INVALID_CREDENTIALS);
        }
        return response.getJwt();
    }

    @Retryable(maxAttempts = 5, value = InfrastructureException.class,
            backoff = @Backoff(delay = 2000, multiplier = 2))
    public LoginResponse checkUserLogin(String user, String password) {
        log.info("Trying to log user in...");
        try {
            ResponseEntity<SigninResponse> signinResponse = restTemplate.postForEntity(
                    preLoginUrl, buildSigninRequest(user, password), SigninResponse.class);
            ResponseEntity<LoginResponse> responseEntity = restTemplate.postForEntity(
                    loginUrl, new LoginRequest(signinResponse.getBody().getCode()), LoginResponse.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            throw new SmException(ErrorCode.INVALID_CREDENTIALS, e);
        } catch (Exception e) {
            throw new InfrastructureException("Ha ocurrido un problema al intentar recuperar la información", e);
        }
    }

    private HttpEntity<?> buildSigninRequest(String user, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", user);
        map.add("password", password);
        return new HttpEntity<>(map, headers);
    }
}
