package it.greenvulcano.gvesb.http.jmx;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.http.ProtocolFactory;
import it.greenvulcano.util.thread.BaseThread;

import java.util.Vector;

import org.apache.commons.httpclient.protocol.Protocol;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Jul 26, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HttpClientProtocolProxy implements ConfigurationListener
{

    private static final String            GVSUPPORT_CONF = "GVSupport.xml";

    /**
     *
     */
    static final String                    JMX_KEY_NAME   = "Component";
    /**
     *
     */
    static final String                    JMX_KEY_VALUE  = "HttpClientProtocolProxy";
    /**
     *
     */
    static final String                    JMX_KEY        = JMX_KEY_NAME + "=" + JMX_KEY_VALUE;
    /**
     *
     */
    static final String                    JMX_FILTER     = "*:*," + JMX_KEY;

    private static HttpClientProtocolProxy _instance      = null;

    private Vector<String>                 protocolIDs    = new Vector<String>();

    /**
     * @throws XMLConfigException
     */
    private HttpClientProtocolProxy() throws XMLConfigException
    {
        XMLConfig.addConfigurationListener(this, GVSUPPORT_CONF);
        init();
    }

    /**
     * @return the instance of this singleton
     * @throws XMLConfigException
     */
    public synchronized static HttpClientProtocolProxy instance() throws XMLConfigException
    {
        if (_instance == null) {
            _instance = new HttpClientProtocolProxy();
        }
        return _instance;
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(GVSUPPORT_CONF)) {
            release();

            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    init();
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for HttpClientProtocolProxy");
            bt.setDaemon(true);
            bt.start();
        }
    }

    /**
     *
     */
    private void init()
    {
        try {
            NodeList protocolHandlerConfigs = XMLConfig.getNodeList(GVSUPPORT_CONF,
                    "/GVSupport/GVHTTPClientProtocolConfig/CustomProtocol");
            if (protocolHandlerConfigs != null) {
                for (int i = 0; i < protocolHandlerConfigs.getLength(); ++i) {
                    Node node = protocolHandlerConfigs.item(i);
                    String scheme = XMLConfig.get(node, "@protocol-scheme");
                    String virtualScheme = XMLConfig.get(node, "@protocol-virtual-scheme", scheme);

                    Protocol.registerProtocol(virtualScheme, ProtocolFactory.create(node));
                    protocolIDs.add(virtualScheme);
                    System.out.println("Registered protocol scheme: " + scheme + " - vscheme: " + virtualScheme);
                }
            }
        }
        catch (Exception exc) {
            System.out.println("Error reloading HttpClient protocol handlers configuration: " + exc);
            exc.printStackTrace();
        }
    }

    /**
     * Destroys the singleton instance.
     */
    public synchronized static void destroy()
    {
        if (_instance != null) {
            _instance.release();
            _instance = null;
        }
    }

    private void release()
    {
        for (String protocolID : protocolIDs) {
            try {
                Protocol.unregisterProtocol(protocolID);
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    /**
     * @return the protocolIDs
     */
    public Vector<String> getProtocolIDs()
    {
        return protocolIDs;
    }

}
