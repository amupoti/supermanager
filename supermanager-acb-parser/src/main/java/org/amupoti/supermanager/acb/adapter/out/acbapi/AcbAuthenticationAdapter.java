package org.amupoti.supermanager.acb.adapter.out.acbapi;

import org.amupoti.supermanager.acb.application.port.out.AuthenticationPort;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.springframework.stereotype.Component;

/**
 * Out-adapter: authenticates users via the ACB API.
 */
@Component
public class AcbAuthenticationAdapter implements AuthenticationPort {

    private final SmContentProvider smContentProvider;

    public AcbAuthenticationAdapter(SmContentProvider smContentProvider) {
        this.smContentProvider = smContentProvider;
    }

    @Override
    public String authenticate(String user, String password) {
        return smContentProvider.authenticateUser(user, password).getJwt();
    }
}
