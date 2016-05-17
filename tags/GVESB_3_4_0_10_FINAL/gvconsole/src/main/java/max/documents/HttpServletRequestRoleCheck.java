/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

import javax.servlet.http.*;



/**
 * This class realizes the RoleCheck interface in order to implement
 * the role check mechanism in an interface involving servlets.
 * It's delegates to the enclosed HttpServletRequest object.
 *
 */
public class HttpServletRequestRoleCheck implements RoleCheck {

    /**
     * Wrapped http request.
     */
    private HttpServletRequest request = null;

    /**
     * Login of the user that is performing the request.
     */
    private String remoteUser = null;


    /**
    * Public constructor.
    * @param HttpServletRequest is the request object of the caller Servlet
    */
    public HttpServletRequestRoleCheck(HttpServletRequest request) {
        this.request = request;
        this.remoteUser = request.getRemoteUser();
    }


    /**
     * This method checks if the user belongs to a determinate role or has a
     * given user name.
     *
     * @param role the user role or the user name
     *
     * @return <false>If the user is not authenticated;
     *         <true> If the user is authenticated.
     */
    public boolean isUserInRole(String role) {

        // Checks for role
        //
        if(request.isUserInRole(role)) {
            return true;
        }

        // Checking for user name
        //
        if(remoteUser != null) {
            return remoteUser.equals(role);
        }

        return false;
    }

    /**
     * This method checks if the user belongs to a collection's role.
     *
     * @param role The user Role;
     *
     * @return <false>If the user is not authenticated;
     *         <true> If the user is authenticated.
     */
    public boolean isUserInSomeRole(String[] roles) {

        for (int i=0; i < roles.length; i++) {

            if(isUserInRole(roles[i]))
                return true;
        }
        return false;
    }
}