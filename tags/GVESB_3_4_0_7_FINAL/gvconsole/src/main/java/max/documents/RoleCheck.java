/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

/**
 * Checks if the current user play the given roles.
 *
 */
public interface RoleCheck {
    /**
     * @return <code>true</code> if the user play the given role
     */
    public boolean isUserInRole(String role);

    /**
     * @return <code>true</code> if the user play the given roles
     */
    public boolean isUserInSomeRole(String[] roles);
}
