/**
 *
 */
package it.greenvulcano.birt.report;

import it.greenvulcano.birt.report.exception.BIRTException;
import it.greenvulcano.birt.report.internal.ReportRenderOptions;
import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.1.0 19 Gen 2011
 * @author GreenVulcano Developer Team
 */
public class ReportManager implements ShutdownEventListener, ConfigurationListener
{
    private static ReportManager             instance         = null;
    public static String                     BIRT_CFG_FILE    = "GVBIRTReport-Configuration.xml";

    private IReportEngine                    engine           = null;
    private String                           reportEngineHome = null;
    private String                           logLevel         = null;
    private Map<String, ReportRenderOptions> renders          = new HashMap<String, ReportRenderOptions>();

    private Map<String, Group>               groups           = new HashMap<String, Group>();
    private boolean                          isReportsInit    = false;

    /**
     * Legge il file xml di configrazione dei report e inizializza il
     * {@link ReportManager} secondo i parametri di tale file. Crea la lista dei
     * {@link Group} e inizializza i singoli gruppi.
     * 
     * @throws Exception
     *         in caso di errori
     */
    private ReportManager() throws BIRTException
    {
        init();
    }

    public static synchronized ReportManager instance() throws BIRTException
    {
        if (instance == null) {
            instance = new ReportManager();
            ShutdownEventLauncher.addEventListener(instance);
            XMLConfig.addConfigurationListener(instance, BIRT_CFG_FILE);
        }
        return instance;
    }

    private void init() throws BIRTException
    {
        try { // inizializzo l'engine
            Node engNode = XMLConfig.getNode(BIRT_CFG_FILE, "/GVBIRTReportConfiguration/Engine");

            reportEngineHome = PropertiesHandler.expand(XMLConfig.get(engNode, "@reportEngineHome",
                    "sp{{gv.app.home}}/BIRTReportEngine"));
            logLevel = XMLConfig.get(engNode, "@logLevel", "FINEST");
            NodeList rnl = XMLConfig.getNodeList(engNode, "Renders/*[@type='report-render']");
            if ((rnl != null) && (rnl.getLength() > 0)) {
                for (int i = 0; i < rnl.getLength(); i++) {
                    Node n = rnl.item(i);
                    ReportRenderOptions opt = (ReportRenderOptions) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                    opt.init(n);
                    renders.put(opt.getType(), opt);
                }
            }

            EngineConfig config = new EngineConfig();
            //config.setBIRTHome(reportEngineHome);
            config.setLogConfig(reportEngineHome + File.separator + "log", Level.parse(logLevel));
            Platform.startup(config);
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            engine = factory.createReportEngine(config);
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing BIRT Engine", exc);
        }

        initReport();
    }

    private void initReport() throws BIRTException
    {
        if (isReportsInit) {
            return;
        }

        try {// inizializzo i gruppi e di conseguenza i report e i parametri
            NodeList nl = XMLConfig.getNodeList(BIRT_CFG_FILE, "/GVBIRTReportConfiguration/ReportGroups/ReportGroup");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                Group g = new Group();
                g.init(n);

                groups.put(g.getName(), g);
            }
            isReportsInit = true;
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing reports", exc);
        }
    }

    /**
     * Restituisce una lista (List) di String contenente i nomi dei
     * {@link Group} contenuti nel {@link ReportManager}, null se non ci sono
     * gruppi
     * 
     * @return La List dei nomi (String) degli oggetti {@link Group}, null se
     *         non ci sono gruppi
     */
    public List<String> getGroupsName() throws Exception
    {
        initReport();

        List<String> l = new ArrayList<String>(0);
        String key = null;

        for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(key);
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Restituisce una lista (List) degli oggetti {@link Group} contenuti nel
     * {@link ReportManager}, null se non ci sono gruppi
     * 
     * @return La List degli oggetti {@link Group} contenuti nel
     *         {@link ReportManager}, null se non ci sono gruppi
     */
    public List<Group> getGroups() throws Exception
    {
        initReport();

        List<Group> l = new ArrayList<Group>(0);
        String key = null;

        for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(groups.get(key));
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Restituisce una lista (List) dei nomi dei {@link Report} contenuti nel
     * {@link Group} di nome <i>group</i>, null se non ci sono gruppi con quel
     * nome o se il gruppo non ha report
     * 
     * @return La List degli oggetti {@link Group} contenuti nel
     *         {@link ReportManager}, null se non ci sono gruppi con quel nome o
     *         se il gruppo non ha report
     * @param group
     *        (String): nome del {@link Group} dal quale si vogliono estrarre i
     *        nomi dei Report
     */
    public List<String> getReportsName(String group) throws Exception
    {
        initReport();

        List<String> l = new ArrayList<String>(0);
        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        l = g.getReportsNames();

        return l;
    }

    /**
     * Restituisce una lista (List) di String contenente i nomi degli oggetti
     * {@link Report} contenuti nel {@link Group} in ingresso, null se non ci
     * sono report nel gruppo
     * 
     * @return La List dei nomi (String) degli oggetti {@link Report} contenuti
     *         nel {@link Group} in ingresso, null se non ci sono report nel
     *         gruppo
     * @param g
     *        {@link Group} dal quale si vuole estrarre la lista di nomi dei
     *        report
     */
    public List<String> getReportsName(Group g)
    {
        List<String> l = new ArrayList<String>(0);
        String name = null;
        Map<String, Report> reports = g.getReports();
        if (reports == null) {
            return null;
        }

        for (Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            name = it.next();
            l.add(name);
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Restituisce una lista (List) degli oggetti {@link Report} del
     * {@link Group} con nome corrispondente al parametro in ingresso, null se
     * non ci sono gruppi con quel nome o se il gruppo non ha report
     * 
     * @return La List degli oggetti {@link Group} contenuti nel
     *         {@link ReportManager}, null se non ci sono gruppi con quel nome o
     *         se il gruppo non ha report
     * @param group
     *        String con il nome del {@link Group} dal quale si vogliono
     *        estrarre i Report
     */
    public List<Report> getReports(String group) throws Exception
    {
        initReport();

        List<Report> l = new ArrayList<Report>(0);
        String key = null;
        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Map<String, Report> reports = g.getReports();
        if (reports == null) {
            return null;
        }

        for (Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(reports.get(key));
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    /**
     * Restituisce una lista (List) degli oggetti {@link Report} contenuti nel
     * {@link Group} in ingresso, null se non ci sono report nel gruppo
     * 
     * @return La List degli oggetti {@link Report} contenuti nel {@link Group}
     *         in ingresso, null se non ci sono report nel gruppo
     * @param g
     *        {@link Group} dal quale si vogliono estrarre i Report
     */
    public List<Report> getReports(Group g)
    {
        List<Report> l = new ArrayList<Report>(0);
        String key = null;
        Map<String, Report> reports = g.getReports();
        if (reports == null) {
            return null;
        }

        for (Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            key = it.next();
            l.add(reports.get(key));
        }
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    public Report getReport(String group, String report) throws Exception
    {
        initReport();

        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Report r = g.getReport(report);
        reportChosen(r);
        return r;
    }

    /**
     * Restituisce una lista (List) degli oggetti {@link Parameter} contenuti
     * nel {@link Report} di nome <i>report</i> che appartiene al {@link Group}
     * di nome <i>group</i>, null se il report non ha parametri, se il gruppo
     * non ha report, o se non esistono un gruppo o un report con nome uguale a
     * quello in ingresso
     * 
     * @return La List degli oggetti {@link Parameter} contenuti nel
     *         {@link Report} di nome <i>report</i> che appartiene al
     *         {@link Group} di nome <i>group</i>, null se il report non ha
     *         parametri, se il gruppo non ha report, o se non esistono un
     *         gruppo o un report con nome uguale a quello in ingresso
     * @param group
     *        String con il nome del {@link Group} nel quale si cerca il report
     *        di nome <i>report</i>
     * @param report
     *        String con il nome del {@link Report} dal quale si vogliono
     *        estrarre i Parameter
     */
    public List<Parameter> getParams(String group, String report) throws Exception
    {
        initReport();

        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Report r = g.getReport(report);
        if (r == null) {
            return null;
        }
        return getParams(r);
    }

    /**
     * Restituisce una lista (List) degli oggetti {@link Parameter} contenuti
     * nel {@link Report} in ingresso, null se il report non ha parametri
     * 
     * @return La List degli oggetti {@link Parameter} contenuti nel Report in
     *         ingresso, null se il report non ha parametri
     * @param r
     *        {@link Report} dal quale si vogliono estrarre i Parameter
     */
    public List<Parameter> getParams(Report r) throws Exception
    {
        reportChosen(r);
        return r.getParamsList();
    }

    /**
     * Restituisce una lista (List) di String contenente i nomi degli oggetti
     * {@link Parameter} contenuti nel {@link Report} di nome <i>report</i> che
     * appartiene al {@link Group} di nome <i>group</i>, null se il report non
     * ha parametri, se il gruppo non ha report, o se non esistono un gruppo o
     * un report con nome uguale a quello in ingresso
     * 
     * @return La List dei nomi (String) degli oggetti {@link Parameter}
     *         contenuti nel {@link Report} di nome <i>report</i> che appartiene
     *         al {@link Group} di nome <i>group</i>, null se il report non ha
     *         parametri, se il gruppo non ha report, o se non esistono un
     *         gruppo o un report con nome uguale a quello in ingresso
     * @param group
     *        String con il nome del {@link Group} nel quale si cerca il report
     *        di nome <i>report</i>
     * @param report
     *        String con il nome del {@link Report} dal quale si vuole estrarre
     *        la lista di nomi dei {@link Parameter}
     */
    public List<String> getParamsNames(String group, String report) throws Exception
    {
        initReport();

        Group g = groups.get(group);
        if (g == null) {
            return null;
        }
        Report r = g.getReport(report);
        if (r == null) {
            return null;
        }
        return getParamsNames(r);
    }

    /**
     * Restituisce una lista (List) di String contenente i nomi degli oggetti
     * {@link Parameter} contenuti nel {@link Report} in ingresso, null se il
     * report non ha parametri
     * 
     * @return La List dei nomi (String) degli oggetti {@link Parameter}
     *         contenuti nel {@link Report} in ingresso, null se il report non
     *         ha parametri
     * @param r
     *        {@link Report} dal quale si vuole estrarre la lista di nomi dei
     *        {@link Parameter}
     */
    public List<String> getParamsNames(Report r) throws Exception
    {
        reportChosen(r);
        return r.getParamsNames();
    }

    /**
     * Restituisce una lista (List) oridnata alfabeticamente di String
     * contenente i nomi degli oggetti {@link Parameter} contenuti nel
     * {@link Report} di nome <i>report</i> che appartiene al {@link Group} di
     * nome <i>group</i>, null se il report non ha parametri, se il gruppo non
     * ha report, o se non esistono un gruppo o un report con nome uguale a
     * quello in ingresso
     * 
     * @return La List dei nomi (String), oridnata alfabeticamente, degli
     *         oggetti {@link Parameter} contenuti nel {@link Report} di nome
     *         <i>report</i> che appartiene al {@link Group} di nome
     *         <i>group</i>, null se il report non ha parametri, se il gruppo
     *         non ha report, o se non esistono un gruppo o un report con nome
     *         uguale a quello in ingresso
     * @param group
     *        String con il nome del {@link Group} nel quale si cerca il report
     *        di nome <i>report</i>
     * @param report
     *        String con il nome del {@link Report} dal quale si vuole estrarre
     *        la lista di nomi dei {@link Parameter}
     */
    public List<String> getOrderedParamsNames(String group, String report) throws Exception
    {
        List<String> l = getParamsNames(group, report);
        if (l == null) {
            return null;
        }
        Collections.sort(l);

        return l;
    }

    /**
     * Restituisce una lista (List) oridnata alfabeticamente di String
     * contenente i nomi degli oggetti {@link Parameter} contenuti nel
     * {@link Report} in ingresso, null se il report non ha parametri
     * 
     * @return La List dei nomi (String) degli oggetti {@link Parameter}
     *         contenuti nel {@link Report} in ingresso, null se il report non
     *         ha parametri
     * @param r
     *        {@link Report} dal quale si vuole estrarre la lista di nomi dei
     *        parametri
     */
    public List<String> getOrderedParamsNames(Report r) throws Exception
    {
        List<String> l = getParamsNames(r);
        if (l == null) {
            return null;
        }
        Collections.sort(l);

        return l;
    }

    public IRunAndRenderTask getTask(String reportConfig) throws BIRTException
    {
        try {
            IReportRunnable design = engine.openReportDesign(reportEngineHome + File.separator + "reports"
                    + File.separator + reportConfig);
            return engine.createRunAndRenderTask(design);
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing BIRT ReportRender for [" + reportConfig + "]", exc);
        }
    }

    /**
     * @param type
     * @return
     */
    public ReportRenderOptions getDefaultReportRender(String type)
    {
        return renders.get(type);
    }

    private synchronized void reportChosen(Report report) throws Exception
    {
        if (report == null) {
            return;// da aggiustare
        }

        if (!report.isInitialized()) {
            IReportRunnable design = null;
            try {
                // Open a report design
                design = engine.openReportDesign(reportEngineHome + File.separator + "reports" + File.separator
                        + report.getReportConfig());
            }
            catch (Exception exc) {
                throw new BIRTException("Error initializing BIRT ReportDesign for [" + report.getReportConfig() + "]",
                        exc);
            }
            IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask(design);

            report.init(renders, engine, design, task);
        }

        return;
    }

    private void destroy()
    {
        renders.clear();
        groups.clear();
        try {
            if (engine != null) {
                engine.destroy();
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        finally {
            engine = null;
        }
        try {
            Platform.shutdown();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        ShutdownEventLauncher.removeEventListener(instance);
        XMLConfig.removeConfigurationListener(instance);
        instance = null;
    }

    /**
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted
     *      (it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        destroy();
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     *      (it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(BIRT_CFG_FILE)) {
            groups.clear();
            isReportsInit = false;
        }
    }
}