/*
 * Copyright (c) 2009-2015 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvpushnot.publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @version 3.4.0 16/feb/2015
 * @author GreenVulcano Developer Team
 */
public class Notification {
	private String              notificationID;
	private String              engineName;
	private List<String>        registrationIds    = new ArrayList<String>();
    private Map<String, String> extra              = new LinkedHashMap<String, String>();
    private Map<String, String> registrationIdKeys = new HashMap<String, String>();
    private boolean             delayWhileIdle;
    private long                timeToLive;
    private String              title = null;
    private String              message = null;

	public Notification(String engineName, String notificationID, Object data) throws PushNotificationException {
		this.engineName = engineName;
		this.notificationID = notificationID;
		parseData(data);
	}
	
	public String getEngineName() {
		return engineName;
	}
	
	public String getNotificationID() {
		return this.notificationID;
	}

    public List<String> getRegistrationIds() {
        return registrationIds;
    }

    public Map<String, String> getRegistrationIdKeys() {
        return registrationIdKeys;
    }

    public void setRegistrationIds(List<String> registrationIds) {
        this.registrationIds = registrationIds;
    }

    public void addRegistrationId(String registrationId) {
        getRegistrationIds().add(registrationId);
    }
    
    public void addRegistrationId(String regKey, String registrationId) {
        getRegistrationIds().add(registrationId);
        getRegistrationIdKeys().put(registrationId, regKey);
    }

    public String getTitle() {
		return this.title;
	}

    public void setTitle(String title) {
		this.title = title;
	}

    public String getMessage() {
		return this.message;
	}

    public void setMessage(String message) {
		this.message = message;
	}

    public Map<String, String> getExtraData() {
        return extra;
    }

    public void setExtraData(Map<String, String> extra) {
        this.extra = extra;
    }

    public void setExtraData(String key, String value) {
    	if (value == null) {
    		this.extra.remove(key);
    		return;
    	}
    	this.extra.put(key, value);
    }

    public boolean isDelayWhileIdle() {
        return delayWhileIdle;
    }

    public void setDelayWhileIdle(boolean delayWhileIdle) {
        this.delayWhileIdle = delayWhileIdle;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

	private void parseData(Object data) throws PushNotificationException {
		JSONObject jo = null;
		if (data instanceof JSONObject) {
			jo = (JSONObject) data;
		}
		else if (data instanceof String) {
			jo = new JSONObject((String) data);
		}
		else {
			throw new PushNotificationException("Invalid notification format: " + data.getClass());
		}

		if (jo.has("destination_id")) {
			Object ids = jo.get("destination_id");
			if (ids instanceof JSONArray) {
				JSONArray list = (JSONArray) ids;
				for (int i = 0; i < list.length(); i++) {
					addRegistrationId(list.getString(i));
				}
			}
			else {
				addRegistrationId((String) ids);
			}
		}
		JSONObject joD = jo.getJSONObject("data");
		setTitle(joD.getString("title"));
		setMessage(joD.getString("message"));
		if (joD.has("extra")) {
			JSONObject joE = joD.getJSONObject("extra");
			Iterator<String> ik = joE.keys();
			while (ik.hasNext()) {
				String k = ik.next();
				setExtraData(k, joE.getString(k));
			}
		}
	}
}
