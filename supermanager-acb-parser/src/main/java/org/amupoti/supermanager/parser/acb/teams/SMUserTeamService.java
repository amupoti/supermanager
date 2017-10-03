package org.amupoti.supermanager.parser.acb.teams;

import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.htmlcleaner.XPatherException;

import java.util.List;

/**
 * Created by Marcel on 02/01/2016.
 */
public interface SMUserTeamService {

    List<SmTeam> getTeamsByCredentials(String user, String password) throws XPatherException;
}
