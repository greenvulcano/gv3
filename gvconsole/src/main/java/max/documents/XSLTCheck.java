/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import java.util.StringTokenizer;

/**
 * This class realizes the RoleCheck interface in order to implement
 * the role check mechanism in an interface involving servlets.
 * It's delegates to the enclosed HttpServletRequest object.
 *
 */
public class XSLTCheck {

    /**
    * Public constructor.
    */
    public XSLTCheck() {
    }

    public boolean grant(String roles, RoleCheck roleCheck) {
        StringTokenizer stRoles = new StringTokenizer(roles, ",");

        while (stRoles.hasMoreTokens()) {
            if (roleCheck.isUserInRole(stRoles.nextToken())) {
                return true;
            }
        }
        return false;
    }

}
