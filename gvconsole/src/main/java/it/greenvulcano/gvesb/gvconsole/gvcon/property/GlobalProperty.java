/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.gvconsole.gvcon.property;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * GlobalProperty class
 *
 * @version 3.0.0 Dec 12, 2013
 * @author GreenVulcano Developer Team
 */
public class GlobalProperty
{
    private String name         = "";
    private String value        = "";
    private boolean present     = false;
    private List<String> usedIn = new ArrayList<String>();
    private String strUsedIn    = "";
    private String description  = "";
    private boolean encrypted   = false;


    /**
     * @param value
     *        the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }
    
    public boolean isEmpty() {
		return "".equals(value);
	}

    public void setEmpty() {
		//empty
	}

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
	 * @return the present
	 */
	public boolean isPresent() {
		return present;
	}

	/**
	 * @param present the present to set
	 */
	public void setPresent(boolean present) {
		this.present = present;
	}

	public boolean isUsed() {
		return !usedIn.isEmpty();
	}
	
	public void setUsed(){
		//empty
	}

	/**
	 * @return the usedIn
	 */
	public List<String> getUsedIn() {
		return usedIn;
	}

	/**
	 * @param usedIn the usedIn to set
	 */
	public void setUsedIn(List<String> usedIn) {
		this.usedIn = usedIn;
		valorizeStrUsedIn();
	}

	/**
     * @param description
     *        the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

	/**
	 * @return the encrypted
	 */
	public boolean isEncrypted() {
		return encrypted;
	}

	/**
	 * @param encrypted the encrypted to set
	 */
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	private void valorizeStrUsedIn() {
		String usedInStr = "";
		for (String fName : usedIn) {
			usedInStr += fName + ",";
		}
		if(usedInStr.endsWith(",")){
			usedInStr = usedInStr.substring(0, usedInStr.length()-1);
		}
		this.strUsedIn = usedInStr;
	}
	
	private void valorizeUsedIn() {
		usedIn.clear();
		for (String fName : strUsedIn.split(",")) {
			if (!"".equals(fName)) {
				usedIn.add(fName);
			}
		}
	}

	/**
	 * @return the strUsedIn
	 */
	public String getStrUsedIn() {
		return strUsedIn;
	}

    /**
	 * @param strUsedIn the strUsedIn to set
	 */
	public void setStrUsedIn(String strUsedIn) {
		this.strUsedIn = strUsedIn;
		valorizeUsedIn();
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GlobalProperty [name=" + name + ", value=" + value + ", empty="
				+ isEmpty() + ", present=" + present + ", used=" + isUsed()
				+ ", usedIn=" + usedIn + ", strUsedIn=" + strUsedIn 
				+ ", description=" + description + ", encrypted=" + encrypted + "]";
	}
}
