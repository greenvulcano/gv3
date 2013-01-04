package it.greenvulcano.gvesb.social;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

/**
 * Interface for all classes implementing a method call on a social platform.
 * 
 * @author mb
 *
 */
public interface SocialOperation {

	public String getSocialName();
	
	public String getAccountName();
	
	public void execute(SocialAdapterAccount account) throws SocialAdapterException;
	
	public void updateResult(GVBuffer buffer) throws GVException;
}
