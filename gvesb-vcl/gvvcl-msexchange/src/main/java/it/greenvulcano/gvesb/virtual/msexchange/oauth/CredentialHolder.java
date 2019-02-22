/**
 *
 */
package it.greenvulcano.gvesb.virtual.msexchange.oauth;

import org.w3c.dom.Node;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;

import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.InitializationException;

/**
 * @author gianluca
 *
 */
public interface CredentialHolder {

	public void init(Node node) throws InitializationException;

	public String getId();

	public AuthenticationResult authenticate(AuthenticationContext context) throws CallException;

	public void destroy();
}
