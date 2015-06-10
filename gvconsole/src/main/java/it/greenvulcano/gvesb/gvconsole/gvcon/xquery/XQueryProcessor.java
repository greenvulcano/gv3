/*
 * Copyright (c) 2005 Maxime Informatica s.n.c. - All right reserved
 *
 * Created on 3-giu-2005
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
package it.greenvulcano.gvesb.gvconsole.gvcon.xquery;

import it.greenvulcano.configuration.XMLConfig;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import max.xml.MaxEntityResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.JavaExtensionLibrary;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.XMLReader;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class XQueryProcessor
{

    private static Map<Thread, Configuration> threadToConfig                  = Collections.synchronizedMap(new HashMap<Thread, Configuration>());

    private XQueryBean                        xQueryBean;
    private ByteArrayOutputStream             xQueryResult;
    private int                               foundDocumentsNumber;
    private String                            fileConfig                      = "xml.xml";
    private Configuration                     config;
    private StaticQueryContext                sqc;
    /**
     *
     */
    public static final String                DEFAULT_NAMESPACE_FOR_FUNCTIONS = "urn:maxime/functions";
    /**
     *
     */
    public static final String                DEFAULT_PREFIX_FOR_FUNCTIONS    = "max";
    private URIResolverImpl                   uriResolver;

    /**
     * @param xQueryBean
     */
    public XQueryProcessor(XQueryBean xQueryBean)
    {
        try {
            this.xQueryBean = xQueryBean;
            init();
        }
        catch (Throwable exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Metodo per l'inizializzazione dei parametri di configurazione di
     * <code>XQueryProcessor<code>
     *
     * @throws Throwable
     */
    public void init() throws Throwable
    {
        uriResolver = new URIResolverImpl();

        config = new Configuration();
        config.setHostLanguage(Configuration.XQUERY);
        config.setValidation(false);
        config.setRetainDTDAttributeTypes(false);

        // setting entity resolver
        XMLReader reader = config.getSourceParser();
        reader.setEntityResolver(new MaxEntityResolver());
        config.reuseSourceParser(reader);

        // setting URI resolver
        config.setURIResolver(uriResolver);

        Node configNode = XMLConfig.getNode(fileConfig, "/xml");

        setFunctionLibrary(configNode);

        sqc = new StaticQueryContext(config);
        sqc.setBaseURI("file:///C:/Programmi/GreenVulcano/conf");

        setNamespaces(configNode);
    }

    /**
     * Legge i namespace dichiarati all'interno di <code>fileConfig<code>
     * e li imposta nell'ambiente di esecuzione delle XQuery
     *
     * @param configNode
     *        : nodo di partenza i parametri di configurazione
     * @throws Throwable
     */
    private void setNamespaces(Node configNode) throws Throwable
    {
        // load the namespaces
        String xpath = "./xpath/xpath-namespace";
        NodeList namespaceNodes = XMLConfig.getNodeList(configNode, xpath);
        for (int i = 0; i < namespaceNodes.getLength(); i++) {
            Node namespaceNode = namespaceNodes.item(i);
            String prefix = XMLConfig.get(namespaceNode, "@prefix", DEFAULT_PREFIX_FOR_FUNCTIONS);
            String namespace = XMLConfig.get(namespaceNode, "@namespace", DEFAULT_NAMESPACE_FOR_FUNCTIONS);
            // register namespace
            sqc.declareNamespace(prefix, namespace);
        }
        sqc.declareNamespace(DEFAULT_PREFIX_FOR_FUNCTIONS, DEFAULT_NAMESPACE_FOR_FUNCTIONS);
    }

    /**
     * Impostazione della libreria delle funzioni estese nell'ambiente di
     * esecuzione delle XQuery
     *
     * @param configNode
     *        : nodo di partenza i parametri di configurazione
     * @throws Throwable
     */
    private void setFunctionLibrary(Node configNode) throws Throwable
    {
        config.setAllowExternalFunctions(true);

        // load the functions
        String xpath = "./it.greenvulcano.gvesb.gvconsole.gvcon.xquery/it.greenvulcano.gvesb.gvconsole.gvcon.xquery-extension";
        NodeList functionNodes = XMLConfig.getNodeList(configNode, xpath);
        FunctionLibraryList functionLibrary = new FunctionLibraryList();
        for (int i = 0; i < functionNodes.getLength(); i++) {
            Node functionNode = functionNodes.item(i);
            String uri = XMLConfig.get(functionNode, "@uri", DEFAULT_NAMESPACE_FOR_FUNCTIONS);
            String className = XMLConfig.get(functionNode, "@class");
            Class<?> cls = Class.forName(className);
            // register function
            JavaExtensionLibrary jfl = new JavaExtensionLibrary(config);
            jfl.declareJavaClass(uri, cls);
            functionLibrary.addFunctionLibrary(jfl);
        }
        config.setExtensionBinder("", functionLibrary);
    }

    /**
     * @return the XQuery result
     */
    public ByteArrayOutputStream getXQueryResult()
    {
        return xQueryResult;
    }

    /**
     * @return the found documents number
     */
    public int getFoundDocumentsNumber()
    {
        return foundDocumentsNumber;
    }

    /**
     * @param request
     * @throws Exception
     */
    public void performXQueryDocuments(HttpServletRequest request) throws Exception
    {
        try {
            threadToConfig.put(Thread.currentThread(), config);

            xQueryResult = null;
            xQueryBean.setError(null);
            final XQueryExpression exp = sqc.compileQuery(xQueryBean.getText());
            final DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
            final Properties props = new Properties();
            props.setProperty(OutputKeys.INDENT, "yes");
            uriResolver.setRequest(request);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exp.run(dynamicContext, new StreamResult(baos), props);
            xQueryResult = baos;
        }
        catch (Exception e) {
            xQueryBean.setError(e.getMessage());
        }
        finally {
            threadToConfig.remove(Thread.currentThread());
        }
    }

    /**
     * @return the {@link Configuration} for the current <code>Thread</code>
     */
    public static Configuration getConfiguration()
    {
        return threadToConfig.get(Thread.currentThread());
    }
}