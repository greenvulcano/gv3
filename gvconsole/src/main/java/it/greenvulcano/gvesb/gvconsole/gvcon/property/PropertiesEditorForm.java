/**
 * 
 */
package it.greenvulcano.gvesb.gvconsole.gvcon.property;

import java.util.List;

/**
 * PropertiesEditor class
 * 
 * @version 3.4.0 Dec 17, 2010
 * @author GreenVulcano Developer Team
 */
public class PropertiesEditorForm {
	private List<GlobalProperty> properties = null;
	
	public List<GlobalProperty> getProperties() {
		return properties;
	}
	
	public void setProperties(List<GlobalProperty> properties) {
		this.properties = properties;
	}
	
	@Override
	public String toString() {
		return "PropertiesEditorForm: " + properties;
	}
}
