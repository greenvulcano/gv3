/*
 * Copyright (c) 2005 E@I Software - All right reserved
 *
 * Created on dd-mmm-yyyy
 *

 */
package max.documents;

import it.greenvulcano.configuration.XMLConfigException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import max.xml.XMLBuilder;

public class LocksSessionListener implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent se) {
        // Do Nothing
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();

        XMLBuilder.removeFromSession(session);
        try {
            LocksManager.unlockDocument(session.getId());
        }
        catch (XMLConfigException exc) {
            throw new RuntimeException(exc);
        }
    }

}
