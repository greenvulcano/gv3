/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Sourimport
 * it.greenvulcano.configuration.ConfigurationEvent;
 * import it.greenvulcano.configuration.ConfigurationListener;
 * import it.greenvulcano.event.util.shutdown.ShutdownEvent;
 * import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
 * import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
 * import it.greenvulcano.gvesb.rsh.RSHException;
 *
 * import java.util.HashMap;
 * import java.util.HashSet;
 * import java.util.Iterator;
 * import java.util.LinkedList;
 * import java.util.List;
 * import java.util.Map;
 *
 * import org.apache.log4j.Logger;
 * import org.w3c.dom.Node;
 * import org.w3c.dom.NodeList;
 * copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.rsh.client;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.log.GVLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Pool di oggetti <code>RSHServiceClient</code>.
 * <p/>
 * <b>Usage: </b> Per effettuare più invocazioni su RSHService, utilizzare il
 * seguente schema di codice:
 *
 * <pre>
 *
 *   ...
 *   RSHServiceClient svcClient = null;
 *   NMDC.push();
 *   try {
 *       svcClient = pool.getRSHServiceClient(nome, TIMEOUT);
 *       if(svcClient == null) {
 *           // Gestione del timeout
 *           ...
 *       }
 *       ...
 *   }
 *   finally {
 *       pool.releaseRSHServiceClient(svcClient);
 *       svcClient = null;
 *       NMDC.pop();
 *   }
 *   ...
 *
 * </pre>
 *
 *
 * @version 3.2.0 16/10/2011
 * @author GreenVulcano Developer Team
 */
public class RSHServiceClientManager implements ShutdownEventListener, ConfigurationListener
{
    /**
     * Logger.
     */
    private static Logger                  logger                    = GVLogger.getLogger(RSHServiceClientManager.class);

    public static final String             CONF_FILE_NAME            = "RSHServiceClient-Configuration.xml";

    /**
     * 20 seconds.
     */
    public static final long               DEFAULT_TIMEOUT           = 1000 * 20;
    public static final long               DEFAULT_SHRINK_DELAY_TIME = 1000 * 60 * 15;

    private Map<String, ClientPool>        pools                     = new HashMap<String, ClientPool>();
    /**
     * Timeout per ottenere un'istanza.
     */
    private long                           defaultTimeout            = DEFAULT_TIMEOUT;
    private long                           shrinkDelayTime           = DEFAULT_SHRINK_DELAY_TIME;
    /**
     * Next shrink time
     */
    private long                           nextShrinkTime            = System.currentTimeMillis() + shrinkDelayTime;

    /**
     * Se impostato è in corso lo shutdown del sistema.
     */
    private boolean                        shutdownFlag              = false;

    private boolean                        initialized               = false;

    private static RSHServiceClientManager instance                  = null;

    public class ClientPool
    {
        public static final int              DEFAULT_INITIAL_SIZE     = 1;
        public static final int              DEFAULT_MAXIMUM_SIZE     = 10;
        public static final int              DEFAULT_MAXIMUM_CREATION = 50;
        private int                          initialSize              = DEFAULT_INITIAL_SIZE;
        private int                          maximumSize              = DEFAULT_MAXIMUM_SIZE;
        private int                          maximumCreation          = DEFAULT_MAXIMUM_CREATION;
        private int                          created                  = 0;
        private String                       name                     = null;
        private String                       clazz                    = null;
        private Node                         cfgNode                  = null;
        /**
         * Pool di istanze RSHServiceClient.
         */
        private LinkedList<RSHServiceClient> pool                     = null;
        /**
         * Set di istanze, prelevate dal pool, assegnate.
         */
        private Set<RSHServiceClient>        assignedRSHC             = new HashSet<RSHServiceClient>();

        public ClientPool(Node node) throws Exception
        {
            cfgNode = node;
            name = XMLConfig.get(node, "@name");
            clazz = XMLConfig.get(node, "@class");
            int initialSizeL = XMLConfig.getInteger(node, "@initial-size", DEFAULT_INITIAL_SIZE);
            int maximumSizeL = XMLConfig.getInteger(node, "@maximum-size", DEFAULT_MAXIMUM_SIZE);
            int maximumCreationL = XMLConfig.getInteger(node, "@maximum-creation", DEFAULT_MAXIMUM_CREATION);

            if (initialSizeL < 0) {
                throw new IllegalArgumentException("initialSize < 0, client=" + name);
            }
            if ((maximumSizeL > 0) && (initialSizeL > maximumSizeL)) {
                throw new IllegalArgumentException("initialSize(" + initialSizeL + ") > maximumSize(" + maximumSizeL
                        + "), client=" + name);
            }
            if ((maximumCreationL > 0) && (maximumSizeL > maximumCreationL)) {
                throw new IllegalArgumentException("maximumSize(" + maximumSizeL + ") > maximumCreation("
                        + maximumCreationL + "), client=" + name);
            }
            this.initialSize = initialSizeL;
            this.maximumSize = maximumSizeL;
            this.maximumCreation = maximumCreationL;

            pool = new LinkedList<RSHServiceClient>();

            for (int i = 0; i < initialSize; i++) {
                RSHServiceClient rshClient = createRSHClient();
                pool.add(rshClient);
            }
        }

        public String getName()
        {
            return this.name;
        }

        public String getClazz()
        {
            return this.clazz;
        }

        public int getInitialSize()
        {
            return this.initialSize;
        }

        public int getMaximumSize()
        {
            return this.maximumSize;
        }

        public int getMaximumCreation()
        {
            return this.maximumCreation;
        }

        public int getCreated()
        {
            return this.created;
        }

        public int getPooledCount()
        {
            return this.pool.size();
        }

        public int getInUseCount()
        {
            return this.assignedRSHC.size();
        }

        public RSHServiceClient getRSHClient(long timeout) throws InterruptedException, RSHException
        {
            if (shutdownFlag) {
                throw new RSHException("RSHClient=" + name + " - ShutdownEvent received, pool disabled");
            }
            long endTime = System.currentTimeMillis() + timeout;
            while (true) {
                synchronized (this) {
                    if (pool == null) {
                        return null;
                    }

                    if (pool.size() > 0) {
                        RSHServiceClient sc = pool.removeFirst();
                        logger.debug("RSHClient=" + name + " - found instance in pool");
                        assignedRSHC.add(sc);
                        return sc;
                    }

                    if ((maximumCreation == -1) || (created < maximumCreation)) {
                        RSHServiceClient sc = createRSHClient();
                        logger.debug("RSHClient=" + name + " - not found instance in pool");
                        logger.debug("RSHClient=" + name + " - creating new instance(" + pool.size() + "/" + created
                                + "/" + maximumCreation + ")");
                        assignedRSHC.add(sc);
                        return sc;
                    }

                    long waitTime = Math.min(timeout, endTime - System.currentTimeMillis());
                    if (waitTime <= 0) {
                        logger.debug("RSHClient=" + name + " - timeout occurred(" + pool.size() + "/" + created + "/"
                                + maximumCreation + ")");
                        throw new RSHException("Timeout, RSHClient=" + name);
                    }
                    wait(waitTime);
                }
            }
        }

        public void releaseRSHClient(RSHServiceClient sc)
        {
            if (sc == null) {
                return;
            }

            logger.debug("RSHClient=" + name + " - releasing instance(" + pool.size() + "/" + created + "/"
                    + maximumCreation + ")");

            if (shutdownFlag) {
                logger.debug("RSHClient=" + name + " - ShutdownEvent received, destroying instance");
                sc.invalidate();
                return;
            }
            synchronized (this) {
                try {
                    if (assignedRSHC.remove(sc)) {
                        if (sc.isValid()) {
                            if ((maximumSize == -1) || ((pool != null) && (pool.size() < maximumSize))) {
                                pool.addFirst(sc);

                                long now = System.currentTimeMillis();
                                if ((shrinkDelayTime == -1) || (now < nextShrinkTime) || (pool.size() <= initialSize)) {
                                    return;
                                }
                                logger.debug("RSHClient=" + name + " - shrink time elapsed");
                                nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;
                                sc = pool.removeLast();
                            }
                            sc.invalidate();
                        }
                        if (created > 0) {
                            --created;
                        }
                        logger.debug("RSHClient=" + name + " - destroying instance(" + pool.size() + "/" + created
                                + "/" + maximumCreation + ")");
                    }
                    else {
                        logger.debug("RSHClient=" + name + " - instance not created by this pool, destroing it");
                        sc.invalidate();
                    }
                }
                finally {
                    notify();
                }
            }
        }

        public void destroy()
        {
            try {
                for (RSHServiceClient sc : pool) {
                    sc.invalidate();
                }
            }
            catch (Exception exc) {
                //do nothing
            }
            pool.clear();
        }

        private RSHServiceClient createRSHClient() throws RSHException
        {
            try {
                RSHServiceClient sc = (RSHServiceClient) Class.forName(clazz).newInstance();
                sc.init(cfgNode);
                created++;
                return sc;
            }
            catch (RSHException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new RSHException("Error creating instance of RSHClient[" + name + "]", exc);
            }
        }
    }

    public static synchronized RSHServiceClientManager instance() throws RSHException
    {
        if (instance == null) {
            instance = new RSHServiceClientManager();
        }
        return instance;
    }

    private RSHServiceClientManager() throws RSHException
    {
        init();
    }

    private synchronized void init() throws RSHException
    {
        if (initialized) {
            return;
        }

        XMLConfig.addConfigurationListener(this, CONF_FILE_NAME);

        try {
            Node cfgNode = XMLConfig.getNode(CONF_FILE_NAME, "/RSHServiceClientConfiguration");

            setDefaultTimeout(XMLConfig.getLong(cfgNode, "@default-timeout", DEFAULT_TIMEOUT));
            shrinkDelayTime = XMLConfig.getLong(cfgNode, "@shrink-timeout", DEFAULT_SHRINK_DELAY_TIME);
            if ((shrinkDelayTime != -1) && (shrinkDelayTime < 1)) {
                throw new IllegalArgumentException("shrinkDelayTime can be -1 or > 0");
            }
            nextShrinkTime = System.currentTimeMillis() + shrinkDelayTime;

            NodeList nl = XMLConfig.getNodeList(cfgNode, "*[@type='rshClient']");
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                ClientPool cp = new ClientPool(node);
                pools.put(cp.getName(), cp);
            }

            logger.debug("Initialized RSHServiceClientPool instance: defaultTimeout=" + defaultTimeout
                    + ", shrinkDelayTime=" + shrinkDelayTime);
            initialized = true;
        }
        catch (Exception exc) {
            throw new RSHException("Error initializing RSHServiceClientManager", exc);
        }

        try {
            ShutdownEventLauncher.addEventListener(this);
        }
        catch (Exception exc) {
            logger.warn("Unable to register ShutdownEventListener", exc);
        }
    }

    /**
     * @return Returns the defaultTimeout.
     */
    public long getDefaultTimeout()
    {
        return defaultTimeout;
    }

    /**
     * @param defaultTimeout
     *        The defaultTimeout to set.
     */
    public void setDefaultTimeout(long defaultTimeout)
    {
        if (defaultTimeout < 1) {
            throw new IllegalArgumentException("defaultTimeout < 1");
        }
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * @return Returns the pooled instance count.
     */
    public int getPooledCount()
    {
        int size = 0;
        Iterator<String> i = pools.keySet().iterator();
        while (i.hasNext()) {
            ClientPool pool = pools.get(i.next());
            size += pool.getPooledCount();
        }
        return size;
    }

    /**
     * @return Returns the used instance count.
     */
    public int getInUseCount()
    {
        int size = 0;
        Iterator<String> i = pools.keySet().iterator();
        while (i.hasNext()) {
            ClientPool pool = pools.get(i.next());
            size += pool.getInUseCount();
        }
        return size;
    }

    /**
     * Invoca <code>getRSHServiceClient(String, long)</code> con il timeout di
     * default.
     *
     * @return @throws
     *         InterruptedException
     * @throws RSHException
     *
     * @see #getRSHServiceClient(String,long)
     */
    public RSHServiceClient getRSHServiceClient(String name) throws InterruptedException, RSHException
    {
        return getRSHServiceClient(name, defaultTimeout);
    }

    /**
     * Se il pool contiene degli oggetti disponibili ritorna immediatamente,
     * altrimenti aspetta il timeout specificato.
     *
     * @return <code>null</code> se non si rende disponibile un
     *         <code>RSHServiceClient</code> prima del timeout
     * @exception InterruptedException
     *            se riceve un'interruzione mentre aspetta che si renda
     *            disponibile un RSHServiceClient
     */
    public RSHServiceClient getRSHServiceClient(String name, long timeout) throws InterruptedException, RSHException
    {
        if (shutdownFlag) {
            throw new RSHException("ShutdownEvent received, pool disabled");
        }
        if (!initialized) {
            init();
        }

        ClientPool pool = pools.get(name);
        if (pool == null) {
            logger.error("Invalid RSHClient name [" + name + "]");
            throw new RSHException("Invalid RSHClient name [" + name + "]");
        }

        return pool.getRSHClient(timeout);
    }

    /**
     *
     * @param rshClient
     */
    public void releaseRSHServiceClient(RSHServiceClient rshClient) throws RSHException
    {
        if (rshClient == null) {
            return;
        }

        if (shutdownFlag) {
            logger.debug("ShutdownEvent received, destroying instance");
            rshClient.invalidate();
            return;
        }

        if (!initialized) {
            init();
        }

        String name = rshClient.getName();
        ClientPool pool = pools.get(name);
        if (pool == null) {
            logger.error("Invalid RSHClient name [" + name + "]");
            throw new RSHException("Invalid RSHClient name [" + name + "]");
        }

        if (shutdownFlag) {
            logger.debug("ShutdownEvent received, destroying instance");
            rshClient.invalidate();
            return;
        }

        pool.releaseRSHClient(rshClient);
    }

    public synchronized void destroy()
    {
        if (pools == null) {
            return;
        }
        logger.debug("Begin destroying instances");
        Iterator<String> i = pools.keySet().iterator();
        while (i.hasNext()) {
            ClientPool pool = pools.get(i.next());
            pool.destroy();
        }
        logger.debug("End destroying instances");
        pools.clear();
    }

    /**
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        destroy();
    }

    /**
     * @see it.eai.utils.event.util.shutdown.ShutdownEventListener#shutdownStarted(it.eai.utils.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        shutdownFlag = true;
        destroy();
    }

    /* (non-Javadoc)
     * @see it.eai.utils.config.ConfigurationListener#configurationChanged(it.eai.utils.config.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        if (evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) {
            destroy();
            initialized = false;
        }
    }

}
