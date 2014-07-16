package max.xml;

import java.util.*;
import org.w3c.dom.*;
import java.lang.ref.WeakReference;

/**
 *
 */
public class WarningManager extends Thread
{
    //---------------------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------------------

    /**
     * Map(Warning, Element)
     */
    private Map               warningToElement;

    /**
     * Map(Element, Set(Warning))
     */
    private Map               elementToWarnings;

    /**
     * Map(String elementName, Set(Warning))
     */
    private Map               elementTypeToWarnings;


     /**
     * Map(Pair(Element,Check), Warning[])
     */
    private Map               pairsToWarning;


    /**
     * Map(String, Warning)
     */
    private Map               keyToWarning;

    /**
     * Map(Warning, String)
     */
    private Map               warningToKey;

    /**
     * Set(Check)
     */

    private Map                checks;

    private CheckDependencies checkDependecies;

    private ElementIndex      index;

    private int               numOfPerformedChecks;
    private long              startCheckTime;
    private boolean           restart;
    private boolean           started;
    private boolean           closed;
    private NodesToCheck      nodesToCheck = new NodesToCheck();
    private WeakReference     builder;


    /**
     * Elementi da controllare.
     * <br>
     * Set[String]
     */
    private Set               affectedElements;

    /**
     * Elementi che hanno causato il calcolo di <code>affectedElements</code>.
     * <br>
     * Set[Element]
     */
    private Set               affectingElements;


    private final boolean 	  STDOUT_LOGGING_ENABLED = false;

    //---------------------------------------------------------------------------
    // CONSTRUCTORS
    //---------------------------------------------------------------------------

    public WarningManager(ElementIndex index, XMLBuilder builder)
    {
        checks = new HashMap();

        checkDependecies = new CheckDependencies();
        this.index = index;
        pairsToWarning = new HashMap();
        //cleanWarnings();
        this.builder = new WeakReference(builder);
        started = false;
        warningToElement = new HashMap();
        elementToWarnings = new HashMap();
        elementTypeToWarnings = new HashMap();
        keyToWarning = new HashMap();
        warningToKey = new HashMap();
        closed = false;

    }

    //---------------------------------------------------------------------------
    // WARNING MANAGEMENT
    //---------------------------------------------------------------------------

    public void cleanWarnings()
    {
        if (affectedElements == null) {

            // Deve ricontrollare tutto il documento, quindi ripulisce
            // totalmente i warnings

            warningToElement = new HashMap();
            elementToWarnings = new HashMap();
            elementTypeToWarnings = new HashMap();
            keyToWarning = new HashMap();
            warningToKey = new HashMap();
        }
        else {

            // Ripulisce solo i warning degli elementi da ricontrollare

            if (affectedElements != null) {
                Iterator it = affectedElements.iterator();
                while (it.hasNext()) {
                    String elementName = (String) it.next();
                    removeWarnings(elementName);
                }
            }

            if (affectingElements != null) {
                Iterator it = affectingElements.iterator();
                while (it.hasNext()) {
                    Element element = (Element) it.next();
                    removeWarnings(element);
                }
            }
        }
    }

     /**
     * Only the first invocation has effects.
     */
    public synchronized void startChecks(Document document)
    {
        if (started) {
            return;
        }
        nodesToCheck.addTree(document.getDocumentElement());
        start();
        started = true;
    }

    //Aggiunta Renato
    /**
     * @return Returns the started.
     */
    public boolean isStarted()
    {
        return started;
    }


   public synchronized void addWarning(Warning warning)
    {
        addWarningPriv(warning);
    }

    public synchronized Warning getWarning(String key)
    {
        return (Warning) keyToWarning.get(key);
    }

    /**
     * Rimuove i warnings per un dato elemento
     *
     * @param element
     */

    public synchronized void removeWarnings(Element element)
    {
        removeWarningsPriv(element);
    }


    /**
     * Rimuove i warnings per tutti gli elementi di nome dato
     *
     * @param elementName
     */

    public synchronized void removeWarnings(String elementName)
    {
        Set elements = index.getElements(elementName);
        if (elements != null) {
            Iterator it = elements.iterator();
            while (it.hasNext()) {
                Element element = (Element) it.next();

                removeWarningsPriv(element);

            }
        }
    }


     public synchronized void removeWarning(Warning warning)
    {
        removeWarningPriv(warning);
    }

    public synchronized Collection getWarnings(Element element)
    {

         if (nodesToCheck.mustBeChecked(element)) {
            nodesToCheck.markAsChecked(element);
            checkElement(element);
        }

        return (Set) elementToWarnings.get(element);
    }

    /**
     * Return the element warnings and children warning recursively.
     * First the element warning and then the children warings.
     */

     public synchronized Collection getAllWarnings(Element element)
    {
        List result = new LinkedList();
        getAllWarningsPriv(element, result);
        return result;
    }

    /**
     * Return all document warnings.
     */

     public synchronized Collection getAllWarnings(Document document)
    {
        List result = new LinkedList();
        getAllWarningsPriv(document.getDocumentElement(), result);
        return result;
    }

    //---------------------------------------------------------------------------
    // SUPPORTS
    //---------------------------------------------------------------------------

    /**
     * An Element is in error if is in error itself or is in error any child element.
     */

    public synchronized boolean isInError(Element element)
    {
        return isInErrorPriv(element);
    }

    public boolean isDocumentInError()
    {
        if (keyToWarning == null)
            return false;
        return keyToWarning.size() > 0;
    }

    //---------------------------------------------------------------------------
    // CHECK
    //---------------------------------------------------------------------------

   public void run()
    {
        long startTime = log("Starting...");
        try {
            startCheckTime = -1;
            while ((builder.get() != null) && (!closed)) {
                synchronized (this) {
                    if (restart) {
                        if (startCheckTime != -1) {
                            endLog("Number of performed checks: " + numOfPerformedChecks, startCheckTime);
                        }
                        numOfPerformedChecks = 0;
                        restart = false;
                        startCheckTime = log("Starting performing checks...");
                    }
                    if (nodesToCheck.isEmpty()) {
                        if (startCheckTime != -1) {
                            endLog("Number of performed checks: " + numOfPerformedChecks, startCheckTime);
                            startCheckTime = -1;
                        }
                        notifyAll();
                        try {
                            wait(10000);
                        }
                        catch (InterruptedException exc) {
                            exc.printStackTrace();
                            return;
                        }
                    }
                    else {
                        Element element = nodesToCheck.next();
                        if (element != null) {
                            checkElement(element);
                        }
                    }
                }
            }
        }
        catch (Throwable exc) {
            exc.printStackTrace();
        }
        finally {
            synchronized (this) {
                notifyAll();
            }
            endLog("Closed.", startTime);
        }
    }

   public void close()
    {
        closed = true;
        synchronized (this) {
            notifyAll();
        }
    }





    public void calculateAffectedElements(Collection nodes, boolean recursive)
    {
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            Node node = (Node) it.next();
            calculateAffectedElements(node, recursive);
        }
    }

    public void calculateAffectedElements(NodeList nodeList, boolean recursive)
    {
        if (nodeList == null) {
            return;
        }
        if (affectedElements == null) {
            affectedElements = new HashSet();
        }
        for (int i = 0; i < nodeList.getLength(); ++i) {
            addAffectedElements(nodeList.item(i), recursive);
        }
    }

    public synchronized void calculateAffectedElements(Node node, boolean recursive)
    {
        if (node == null) {
            return;
        }
        addAffectedElements(node, recursive);
    }


    public synchronized void calculateAffectedElements(Node nodeList[], boolean recursive)
    {
        if (nodeList == null) {
            return;
        }
        for (int i = 0; i < nodeList.length; ++i) {
            addAffectedElements(nodeList[i], recursive);
        }
    }

    public synchronized void addCheck(Check check)
    {
        String elementName = check.getElementName();
        Set elementChecks = (Set) checks.get(elementName);
        if (elementChecks == null) {
            elementChecks = new HashSet();
            checks.put(elementName, elementChecks);
        }
        elementChecks.add(check);

        String[] xpaths = check.getXPaths();
        if (xpaths != null) {
            for (int i = 0; i < xpaths.length; ++i) {
                checkDependecies.addDependency(elementName, xpaths[i]);
            }
        }
    }

    public synchronized void check(boolean wait)
    {
        if (nodesToCheck.isEmpty()) {
            return;
        }

        restart = true;
        notifyAll();

        if (wait) {
            try {
                wait(1000L * 60 * 15);
            }
            catch (InterruptedException exc) {
                exc.printStackTrace();
            }
        }
    }


    protected void check(Element element)
    {
        checkElement(element);
        Node child = element.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                check((Element) child);
            }
            child = child.getNextSibling();
        }

    }



    protected void checkAffected()
    {
        Iterator it = affectedElements.iterator();
        while (it.hasNext()) {
            String elementName = (String) it.next();
            Set elements = index.getElements(elementName);
            if (elements != null) {
                Iterator el = elements.iterator();
                while (el.hasNext()) {
                    Element element = (Element) el.next();
                    checkElement(element);
                }
            }
        }
    }

    protected void checkAffecting()
    {
        if (affectingElements == null) {
            return;
        }

        Iterator it = affectingElements.iterator();
        while (it.hasNext()) {
            Element element = (Element) it.next();
            String elementName = element.getNodeName();
            if (!affectedElements.contains(elementName)) {
                checkElement(element);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------


     /**
     * @param warning
     */
    private void addWarningPriv(Warning warning)
    {
        String key = warning.getKey();
        Element element = warning.getElement();

        warningToElement.put(warning, element);
        Set set = (Set) elementToWarnings.get(element);
        if (set == null) {
            set = new HashSet();
            elementToWarnings.put(element, set);
        }
        set.add(warning);

        String elementName = element.getNodeName();
        set = (Set) elementTypeToWarnings.get(elementName);
        if (set == null) {
            set = new HashSet();
            elementTypeToWarnings.put(elementName, set);
        }
        set.add(warning);

        keyToWarning.put(key, warning);
        warningToKey.put(warning, key);
    }

    /**
     * @param warning
     */
    private void removeWarningPriv(Warning warning)
    {
        Element element = warning.getElement();
        String elementName = element.getNodeName();

        Set warnings = (Set) elementToWarnings.get(element);
        if (warnings != null) {
            warnings.remove(warning);
            if (warnings.size() == 0) {
                elementToWarnings.remove(element);
            }
        }

        warnings = (Set) elementTypeToWarnings.get(elementName);
        if (warnings != null) {
            warnings.remove(warning);
            if (warnings.size() == 0) {
                elementTypeToWarnings.remove(elementName);
            }
        }

        warningToElement.remove(warning);
        Object key = warningToKey.get(warning);

        keyToWarning.remove(key);
        warningToKey.remove(warning);
    }

    /**
     * @param element
     */
    private void removeWarningsPriv(Element element)
    {
        Set warnings = (Set) elementToWarnings.remove(element);
        if (warnings != null) {
            Set warningsForType = (Set) elementTypeToWarnings.get(element.getNodeName());
            warningsForType.removeAll(warnings);

            Iterator it = warnings.iterator();
            while (it.hasNext()) {
                Warning warning = (Warning) it.next();
                warningToElement.remove(warning);
                Object key = warningToKey.remove(warning);
                keyToWarning.remove(key);
            }
        }
    }

    /**
     * Return the current calculated element warnings and children warnings recursively.
     * First the element warning and then the children warnings.
     */
    private void getAllWarningsPriv(Element element, List result)
    {
        Collection warnings = (Collection) elementToWarnings.get(element);
        if (warnings != null) {
            result.addAll(warnings);
        }

        NodeList list = element.getChildNodes();
        int l = list.getLength();
        for (int i = 0; i < l; ++i) {
            Node node = list.item(i);
            if (node instanceof Element) {
                getAllWarningsPriv((Element) node, result);
            }
        }
    }

    private void checkElement(Element element)
    {
        String elementName = element.getNodeName();
        Set elementChecks = (Set) checks.get(elementName);
        if (elementChecks == null) {
            return;
        }

        Iterator i = elementChecks.iterator();
        while (i.hasNext()) {
            Check check = (Check) i.next();
            Pair pair = new Pair(element, check);
            removePair(pair);
            Warning warn[] = check.getWarning(element);

            ++numOfPerformedChecks;
            if (numOfPerformedChecks % 500 == 0) {
                endLog("Performed " + numOfPerformedChecks + " checks", startCheckTime);
            }

            if ((warn != null) && (warn.length > 0)) {
                pairsToWarning.put(pair, warn);
                for (int j = 0; j < warn.length; ++j) {
                    addWarningPriv(warn[j]);
                }
            }
        }
    }

    private void removePair(Pair pair)
    {
        Warning[] warnings = (Warning[]) pairsToWarning.remove(pair);

        if (warnings != null) {
            for (int i = 0; i < warnings.length; i++) {
                removeWarningPriv(warnings[i]);
            }
        }
    }

    /**
     * @param element
     * @return
     */
    private boolean isInErrorPriv(Element element)
    {
        Collection warnings = (Collection) elementToWarnings.get(element);
        if ((warnings != null) && (warnings.size() > 0)) {
            return true;
        }

        NodeList list = element.getChildNodes();
        int l = list.getLength();
        for (int i = 0; i < l; ++i) {
            Node node = list.item(i);
            if (node instanceof Element) {
                if (isInErrorPriv((Element) node)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addAffectedElements(Node node, boolean recursive)
    {
        if (node instanceof Element) {
            Element element = (Element) node;

            nodesToCheck.addElement(element);

            for (Iterator it = checkDependecies.elementsToCheck(element).iterator(); it.hasNext();) {
                Set elements = index.getElements((String) it.next());
                if (elements != null) {
                    nodesToCheck.addAllElements(elements);
                }
            }

            if (recursive) {
                Node child = node.getFirstChild();
                while (child != null) {
                    addAffectedElements(child, recursive);
                    child = child.getNextSibling();
                }
            }
        }
        else if (node instanceof DocumentFragment) {
            Node child = node.getFirstChild();
            while (child != null) {
                addAffectedElements(child, recursive);
                child = child.getNextSibling();
            }
        }
        else if (node instanceof Document) {
            Document document = (Document) node;
            addAffectedElements(document.getDocumentElement(), recursive);
        }
    }

    private long log(String message)
    {
        if(STDOUT_LOGGING_ENABLED) {
        	System.out.println("WarningManager # " + hashCode() + ": " + message);
        }
        return System.currentTimeMillis();
    }

    private long endLog(String message, long startTime)
    {
        long endTime = System.currentTimeMillis();
        if(STDOUT_LOGGING_ENABLED) {
        	System.out.println("WarningManager # " + hashCode() + ": " + message + " (" + (endTime - startTime) + " ms)");
        }
        return endTime;
    }
}
