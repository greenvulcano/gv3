package it.greenvulcano.gvesb.virtual.social.twitter;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.social.SocialAdapterManager;
import it.greenvulcano.gvesb.social.SocialOperation;
import it.greenvulcano.gvesb.social.twitter.directcall.TwitterOperationUpdateStatus;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Class defining a call to updateStatus (Tweet) on Twitter.
 * 
 * @author mb
 */
public class TwitterUpdateStatusCallOperation extends TwitterSocialCallOperation {
    private static Logger logger = GVLogger.getLogger(TwitterUpdateStatusCallOperation.class);
	private String statusText;
    
    @Override
	public void init(Node node) throws InitializationException {
		try{
			super.init(node);
			statusText = XMLConfig.get(node, "@statusText");
		}
        catch (Exception exc) {
            logger.error("ERROR TwitterUpdateStatusCallOperation[" + getName() + "] initialization", exc);
            throw new InitializationException("GV_CONF_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
	}
	
	@Override
	public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException,
			CallException, InvalidDataException {
		SocialAdapterManager instance = SocialAdapterManager.getInstance();
		try {
			Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);

			SocialOperation op = new TwitterOperationUpdateStatus(
					PropertiesHandler.expand(getAccount(), params, gvBuffer),
					PropertiesHandler.expand(statusText, params, gvBuffer));
			instance.directExecute(op);
			op.updateResult(gvBuffer);
		}
        catch (Exception exc) {
        	logger.error("ERROR TwitterUpdateStatusCallOperation[" + getName() + "] execution", exc);
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"id", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
		return gvBuffer;
	}
}
