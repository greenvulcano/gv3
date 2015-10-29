/*
 * Copyright (c) 2005 E@I Software - All right reserved
 *
 * Created on dd-mmm-yyyy
 *
 */
package max.documents;

/**
 * The locks info.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class LockInfo implements Comparable {
    private String sessionId  = "";
    private String name       = "";
    private String label      = "";
    private String user       = "";
    private String dateString = "";
    private String ipAddress  = "";
    private String hostName   = "";

    void setSessionId(String sessionId) {
        if (sessionId != null) {
            this.sessionId = sessionId;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public String getName() {
        return name;
    }

    void setLabel(String label) {
        if (label != null) {
            this.label = label;
        }
    }

    public String getLabel() {
        return label;
    }

    void setUser(String user) {
        if (user != null) {
            this.user = user;
        }
    }

    public String getUser() {
        return user;
    }

    void setDateString(String dateString) {
        if (dateString != null) {
            this.dateString = dateString;
        }
    }

    public String getDateString() {
        return dateString;
    }

    void setIpAddress(String ipAddress) {
        if (ipAddress != null) {
            this.ipAddress = ipAddress;
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    void setHostName(String hostName) {
        if (hostName != null) {
            this.hostName = hostName;
        }
    }

    public String getHostName() {
        return hostName;
    }

    public int compareTo(Object o) {
        LockInfo lockInfo = (LockInfo) o;
        return lockInfo.getLabel().compareTo(label);
    }
}
