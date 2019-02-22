/**
 *
 */
package it.greenvulcano.gvesb.virtual.msexchange.oauth.impl;

import java.io.FileInputStream;
import java.util.concurrent.Future;

import org.w3c.dom.Node;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.msexchange.oauth.CredentialHolder;
import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 * @author gianluca
 *
 */
public class AsymmetricKeyCredentialHolder implements CredentialHolder {

	private String id          = null;
	private String resource    = null;
	private String clientId    = null;
	private String keystoreURL = null;
	private String keystorePWD = null;
	private AsymmetricKeyCredential key = null;

	public AsymmetricKeyCredentialHolder() {
		// do nothing
	}

	/**
	 * @param resource
	 * @param clientId
	 * @param keystoreURL
	 * @param keystorePWD
	 */
	public AsymmetricKeyCredentialHolder(String id, String resource, String clientId, String keystoreURL, String keystorePWD) throws InitializationException {
		this.id = id;
		this.resource = resource;
		this.clientId = clientId;
		this.keystoreURL = keystoreURL;
		this.keystorePWD = keystorePWD;

		try {
			this.key = AsymmetricKeyCredential.create(this.clientId, new FileInputStream(this.keystoreURL), this.keystorePWD);
		} catch (Exception exc) {
			throw new InitializationException("Error generating AsymmetricKeyCredential[" + id + "][" + this.clientId + "][" + this.keystoreURL + "]", exc);
		}
	}

	@Override
	public String getId() {
		return this.id;
	}

	 @Override
    public void init(Node node) throws InitializationException
    {
        try {
        	this.id = XMLConfig.get(node, "@id");
            this.resource = XMLConfig.get(node, "@resource");
            this.clientId = XMLConfig.get(node, "@clientId");
            this.keystoreURL = XMLConfig.get(node, "@keystoreURL");
            this.keystorePWD = XMLConfig.getDecrypted(node, "@keystorePWD");

    		try {
    			this.key = AsymmetricKeyCredential.create(this.clientId, new FileInputStream(PropertiesHandler.expand(this.keystoreURL)), this.keystorePWD);
    		} catch (Exception exc) {
    			throw new InitializationException("Error generating AsymmetricKeyCredential[" + this.id + "][" + this.clientId + "][" + this.keystoreURL + "]", exc);
    		}
        }
        catch (InitializationException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new InitializationException("Error initializing AsymmetricKeyCredentialHolder[" + this.id + "]", exc);
        }
    }

	/* (non-Javadoc)
	 * @see it.greenvulcano.gvesb.virtual.msexchange.oauth.CredentialHolder#authenticate(com.microsoft.aad.adal4j.AuthenticationContext)
	 */
	@Override
	public AuthenticationResult authenticate(AuthenticationContext context) throws CallException {
		try {
			Future<AuthenticationResult> future = context.acquireToken(this.resource, this.key, null);
			AuthenticationResult result = future.get();
			return result;
		} catch (Exception exc) {
			throw new CallException("Error getting token [" + this.id + "][" + this.clientId + "][" + this.resource + "]", exc);
		}
	}

	@Override
    public void destroy()
    {
        // do nothing
    }

	@Override
	public String toString() {
		return "AsymmetricKeyCredential[" + this.id + "][" + this.clientId + "][" + this.resource + "][" + this.keystoreURL + "]";
	}
}
