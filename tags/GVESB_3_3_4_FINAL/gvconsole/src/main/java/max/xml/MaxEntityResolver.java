/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import max.core.ContentProvider;
import max.core.MaxException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Questa classe restituisce le entit� registrate sull'applicazione.
 * <p>
 * La chiave d'accesso per trovare un'entit� � l'URI dell'entit�.<br>
 * Se l'entit� non � contenuta nel content provider, allora sono innescati i
 * meccanismi di default per trovarla (tipicamente una connessione http verso il
 * server definito nell'URI).
 * <p>
 * Oltre alle entit� registrate, questa classe fornisce anche entit� presenti
 * nel classpath.
 * <p>
 * Se il systemId � nella forma
 * <code>URL?parametro=valore&parametro2=valore2...</code> si avvia un
 * meccanismo di sostituzione dei parametri che saranno sostituiti sull'entit�
 * prima che questa venga restituita al parser. I paceholder per i parametri
 * all'interno dell'entit� sono identificati dalla sintassi
 * <code>#${parametro}</code> se nessun parametro � fornito per il placeholder,
 * verr� sostituita la stringa vuota. Per inserire la sequenza di caratteri
 * <code>#$</code> nell'entit�, utilizzare la sintassi <code>#${#$}</code>. Al
 * momento l'encoding applicato � quello di default.
 *
 */
public class MaxEntityResolver extends DefaultHandler
{
    public static final String ENTITY_RESOLVER_PUBLIC        = "PUBLIC";
    public static final String ENTITY_RESOLVER_SYSTEM        = "SYSTEM";
    public static final String ENTITY_RESOLVER_DOCTYPE       = "DOCTYPE";
    public static final String ENTITY_RESOLVER_DESCRIPTION   = "DESCRIPTION";
    public static final String ENTITY_RESOLVER_NAMESPACE_URI = "NAMESPACE_URI";

    private String             categoryName;
    private String             prefix;

    /**
     * @throws MaxException
     * @throws XMLConfigException
     */
    public MaxEntityResolver() throws MaxException, XMLConfigException
    {
        Document xmlConfDocument = XMLConfig.getDocument(MaxXMLFactory.XML_CONF,
                MaxEntityResolver.class.getClassLoader(), true, false);
        Node node = XMLConfig.getNode(xmlConfDocument, "/xml/entity-resolver/*[@type='entity-resolver']");
        categoryName = XMLConfig.get(node, "@category");
        prefix = XMLConfig.get(node, "@prefix", "");
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException
    {
        try {
            if (systemId != null) {
                InputSource ret = null;

                int idx = systemId.indexOf('?');
                String params = null;
                String systemIdWithoutParams = null;
                if (idx != -1) {
                    params = systemId.substring(idx + 1);
                    systemIdWithoutParams = systemId.substring(0, idx);
                }
                else {
                    systemIdWithoutParams = systemId;
                }

                // Se e' nel content provider l'abbiamo trovato e fine.
                //
                ContentProvider contents = MaxXMLFactory.instance().getContentProvider();
                synchronized (contents) {
                    InputStream input = contents.get(categoryName, systemIdWithoutParams);
                    if (input == null) {
                        int lastIndexOfSeparator = systemIdWithoutParams.lastIndexOf("/");
                        if (lastIndexOfSeparator != -1) {
                            input = contents.get(categoryName,
                                    systemIdWithoutParams.substring(lastIndexOfSeparator + 1));
                        }
                    }
                    if (input != null) {
                        return applyParameters(params, input, params);
                    }
                }

                // Vediamo se e' un built-in DTD.
                // Per i built-in DTD rimuoviamo il prefisso se il systemId
                // inizia per tale prefisso.
                //
                String localSystemId = systemIdWithoutParams;
                if (systemId.startsWith(prefix)) {
                    localSystemId = systemId.substring(prefix.length());
                }
                ret = resolveBuiltinDTD(publicId, localSystemId, params);

                if (ret != null) {
                    return ret;
                }
            }

            // Se non abbiamo ancora trovato l'entita' inneschiamo il
            // meccanismo di default.
            //
            return super.resolveEntity(publicId, systemId);
        }
        catch (SAXException exc) {
            throw exc;
        }
        catch (MaxException exc) {
            Exception nexc = exc.getNestedException();
            if (nexc != null) {
                throw new SAXException(nexc);
            }
            throw new SAXException(exc.getMessage());
        }
        catch (Exception exc) {
            throw new SAXException(exc.getMessage());
        }
    }

    private InputSource resolveBuiltinDTD(String publicId, String systemId, String params) throws SAXException,
            IOException
    {
        ClassLoader loader = getClass().getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        InputStream stream = loader.getResourceAsStream(systemId);
        if (stream != null) {
            return applyParameters(systemId, stream, params);
        }
        return null;
    }

    private InputSource applyParameters(String systemId, InputStream entity, String paramStr) throws IOException
    {
        if (paramStr == null) {
            return new InputSource(entity);
        }

        // Prepara la mappa per i parametri
        //
        Map params = new HashMap();
        StringTokenizer stringTokenizer = new StringTokenizer(paramStr, "&", false);
        Pattern paramReader = Pattern.compile("([^=&]+)(?:=([^=&]*))?");
        while (stringTokenizer.hasMoreTokens()) {
            String paramValue = stringTokenizer.nextToken();
            Matcher matcher = paramReader.matcher(paramValue);
            if (matcher.find()) {
                String param = matcher.group(1);
                String value = matcher.group(2);
                if (value != null) {
                    param = URLDecoder.decode(param, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                    params.put(param.trim(), value);
                }
            }
        }

        // Legge l'entit�
        //
        StringBuffer buffer = new StringBuffer();
        InputStreamReader inputStreamReader = new InputStreamReader(entity);
        char[] buff = new char[2048];
        for (int len = 0; (len = inputStreamReader.read(buff)) != -1;) {
            buffer.append(buff, 0, len);
        }

        // Ricerca e sostituzione dei parametri
        //
        Pattern findParam = Pattern.compile("#\\$\\{(.*?)\\}");
        StringBuffer result = new StringBuffer();
        Matcher matcher = findParam.matcher(buffer);
        while (matcher.find()) {
            String param = matcher.group(1).trim();
            matcher.appendReplacement(result, "");
            if (param.equals("#$")) {
                result.append("#$");
            }
            else {
                String value = (String) params.get(param);
                if (value == null) {
                    value = "";
                }
                result.append(value);
            }
        }
        matcher.appendTail(result);

        return new InputSource(new ByteArrayInputStream(result.toString().getBytes()));
    }

    public void insert(String docType, String publicId, String systemId, InputStream dtd, String description,
            String namespace) throws MaxException, XMLConfigException
    {
        if (systemId == null) {
            throw new MaxException("MaxEntityResolver: systemId is null");
        }

        ContentProvider contents = MaxXMLFactory.instance().getContentProvider();
        synchronized (contents) {

            if (!contents.exists(categoryName, systemId)) {
                contents.insert(categoryName, systemId, dtd);
            }
            else {
                throw new MaxException("DTD already exists: URI=\"" + systemId + "\"");
            }

            contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_PUBLIC, publicId == null
                    ? ""
                    : publicId);
            contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_SYSTEM, systemId == null
                    ? ""
                    : systemId);
            contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_DOCTYPE, docType == null
                    ? ""
                    : docType);
            contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_DESCRIPTION, description == null
                    ? ""
                    : description);
            contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_NAMESPACE_URI, namespace == null
                    ? ""
                    : namespace);
        }
    }

    public void update(String systemId, InputStream dtd) throws MaxException, XMLConfigException
    {
        if (systemId == null) {
            throw new MaxException("MaxEntityResolver: systemId is null");
        }

        ContentProvider contents = MaxXMLFactory.instance().getContentProvider();
        synchronized (contents) {

            if (contents.exists(categoryName, systemId)) {
                contents.update(categoryName, systemId, dtd);
            }
            else {
                throw new MaxException("DTD does not exist: URI=\"" + systemId + "\"");
            }
        }
    }

    public void delete(String systemId) throws MaxException, XMLConfigException
    {
        if (systemId == null) {
            throw new MaxException("MaxEntityResolver: systemId is null");
        }

        ContentProvider contents = MaxXMLFactory.instance().getContentProvider();
        synchronized (contents) {
            contents.remove(categoryName, systemId);
        }
    }

    public void updateAttributes(String oldSystemId, String docType, String publicId, String systemId,
            String description, String namespace) throws MaxException, XMLConfigException
    {
        if (systemId == null) {
            throw new MaxException("MaxEntityResolver: systemId is null");
        }

        ContentProvider contents = MaxXMLFactory.instance().getContentProvider();
        synchronized (contents) {

            if (!contents.exists(categoryName, oldSystemId)) {
                throw new MaxException("No DTD found for update: " + systemId);
            }

            if (oldSystemId.equals(systemId)) {
                contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_PUBLIC, publicId == null
                        ? ""
                        : publicId);
                contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_SYSTEM, systemId == null
                        ? ""
                        : systemId);
                contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_DOCTYPE, docType == null
                        ? ""
                        : docType);
                contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_DESCRIPTION, description == null
                        ? ""
                        : description);
                contents.setContentAttribute(categoryName, systemId, ENTITY_RESOLVER_NAMESPACE_URI, namespace == null
                        ? ""
                        : namespace);
            }
            else {
                if (contents.exists(categoryName, systemId)) {
                    throw new MaxException("DTD already exists: URI=\"" + systemId + "\"");
                }

                insert(docType, publicId, systemId, contents.get(categoryName, oldSystemId), description, namespace);
                contents.remove(categoryName, oldSystemId);
            }
        }
    }
}