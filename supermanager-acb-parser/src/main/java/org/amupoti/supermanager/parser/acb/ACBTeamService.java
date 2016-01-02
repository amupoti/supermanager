package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.beans.ACBSupermanagerTeam;
import org.htmlcleaner.XPatherException;

import java.util.List;

/**
 * Created by Marcel on 02/01/2016.
 */
public interface ACBTeamService {

    List<ACBSupermanagerTeam> getTeamsByCredentials(String user, String password) throws XPatherException;
}
