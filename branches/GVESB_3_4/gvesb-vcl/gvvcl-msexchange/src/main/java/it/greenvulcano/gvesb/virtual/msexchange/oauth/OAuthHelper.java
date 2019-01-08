/**
 *
 */
package it.greenvulcano.gvesb.virtual.msexchange.oauth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.log.GVLogger;

/**
 * @author gianluca
 *
 */
public class OAuthHelper implements ConfigurationListener, ShutdownEventListener {
    private static final Logger logger            = GVLogger.getLogger(OAuthHelper.class);

    private static OAuthHelper instance = null;

    private final Map<String, CredentialHolder>     credentials = new HashMap<String, CredentialHolder>();
    private final Map<String, AuthenticationResult> tokens      = new HashMap<String, AuthenticationResult>();
    private ExecutorService               service = null;
    private AuthenticationContext         context = null;


    private OAuthHelper() {
        // do nothing
    }


    public static synchronized OAuthHelper instance() throws InitializationException {
        if (instance == null) {
            try {
                instance = new OAuthHelper();
                instance.init();
            }
            catch(InitializationException exc) {
                if (instance != null) {
                    instance.destroy();
                }
                instance = null;
                throw exc;
            }
            XMLConfig.addConfigurationListener(instance, "MSOAuthHelper.xml");
            ShutdownEventLauncher.addEventListener(instance);
        }
        return instance;
    }


    public String getToken(String id) throws CallException {
    	AuthenticationResult result = this.tokens.get(id);
    	if (result != null) {
    		Date now = new Date();
            now.setTime(now.getTime() + (600 * 1000)); // check 10' before expiration
    		if (result.getExpiresOnDate().before(now)) {
    			logger.debug("Refreshing token for[" + id + "]");
    			this.tokens.remove(id);
    			result = null;
    		}
    	}
    	if (result == null) {
    		CredentialHolder ch = this.credentials.get(id);
    		if (ch == null) {
    			throw new CallException("CredentialHolder[" + id + "] not found!");
    		}
    		result = ch.authenticate(this.context);
    		logger.debug("New token for[" + id + "][" + result.getExpiresOnDate() + "]: " + result.getAccessToken());
    		this.tokens.put(id, result);
    	}
    	return result.getAccessToken();
    }


    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        logger.debug("BEGIN - Operation(reload Configuration)");
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals("MSOAuthHelper.xml")) {
            destroy();
            try {
            	init();
            }
            catch(Exception exc) {
            	logger.error("Error reloading configuration", exc);
            }
        }
        logger.debug("END - Operation(reload Configuration)");
    }


    @Override
    public void shutdownStarted(ShutdownEvent event) {
    	ShutdownEventLauncher.removeEventListener(this);
        destroy();
    }


    private void init() throws InitializationException {
        logger.info("Initializing MSOAuthHelper");
        try {
            Node nc = XMLConfig.getNode("MSOAuthHelper.xml", "/MSOAuthHelper/Context/*[@type='auth-context' and @enabled='true']");

            String authority = XMLConfig.get(nc, "@authority", "https://login.microsoftonline.com/invitalia.onmicrosoft.com/oauth2/v2.0/token");
            this.service = Executors.newFixedThreadPool(1);
            this.context = new AuthenticationContext(authority, true, this.service);
            logger.debug("Initialized AuthenticationContext[" + authority + "]");

            NodeList nl = XMLConfig.getNodeList("MSOAuthHelper.xml", "/MSOAuthHelper/Credentials/*[@type='credential-holder']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                CredentialHolder ch = (CredentialHolder) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                ch.init(n);
                logger.debug("Initialized CredentialHolder: " + ch);
                this.credentials.put(ch.getId(), ch);
            }
        }
        catch (Exception exc) {
            logger.error("Error initializing OAuthHelper", exc);
            throw new InitializationException("Error initializing OAuthHelper", exc);
        }
    }


    private void destroy() {
        logger.info("Destroying MSOAuthHelper");
        if (this.service != null) {
            this.service.shutdownNow();
        }
        this.service = null;
        this.context = null;
        for (CredentialHolder ch : this.credentials.values()) {
            ch.destroy();
        }
        this.credentials.clear();
        this.tokens.clear();
    }

}
