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
package it.greenvulcano.gvesb.gvconsole.workbench.plugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;

/**
 * PagedQueueBrowser class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class PagedQueueBrowser implements Runnable
{

    private static final String DATE_TOSTRING_FORMAT = "dd.MM.yyyy HH:mm:ss.S";
    private static final String DATE_PARSER_FORMAT   = "y-M-d H:m:s";

    private static long         counter              = 0;

    private SimpleDateFormat    dateToStringFormat   = new SimpleDateFormat(DATE_TOSTRING_FORMAT);
    private SimpleDateFormat    dateParserFormat     = new SimpleDateFormat(DATE_PARSER_FORMAT);

    private int                 recordsPerPage;

    private int                 recordCount;

    private int                 pageCount;

    private long                id;

    private boolean             destroyed;

    private Vector              page;

    private int                 currPage;

    private Vector<Message>     buildingPage;

    private int                 pagesToWait;

    private boolean             building;

    private Enumeration         jmsRecordEnumeration = null;

    private String              tempDir              = "";

    private Map<String, String> properties           = null;

    private HashMap             additionalProperties = null;

    private HashMap             deletedMessage       = new HashMap();

    private Vector<Message>     deletedInPage        = null;

    private Integer             countProperties      = new Integer(0);

    /**
     *
     * @param qb
     * @param tempDir
     * @throws Throwable
     */
    public PagedQueueBrowser(QueueBrowser qb, String tempDir) throws Throwable
    {
        init(qb, 20, -1, tempDir);
    }

    /**
     *
     * @param qb
     * @param _recordsPerPage
     * @param tempDir
     * @throws Throwable
     */
    public PagedQueueBrowser(QueueBrowser qb, int _recordsPerPage, String tempDir) throws Throwable
    {
        if (_recordsPerPage < 1) {
            _recordsPerPage = 1;
        }

        init(qb, _recordsPerPage, -1, tempDir);
    }

    /**
     * Asynchronous version of the constructor. After <code>pagesToWait</code>
     * pages has been constructed, the constructor returns, while a thread build
     * the remaining pages.
     *
     * @param qb
     * @param _recordsPerPage
     * @param _pagesToWait
     * @param tempDir
     * @throws Throwable
     */
    public PagedQueueBrowser(QueueBrowser qb, int _recordsPerPage, int _pagesToWait, String tempDir) throws Throwable
    {
        if (_recordsPerPage < 1) {
            _recordsPerPage = 1;
        }

        if (_pagesToWait < 1) {
            _pagesToWait = 1;
        }

        init(qb, _recordsPerPage, _pagesToWait, tempDir);
    }

    /**
     *
     * @param _qb
     * @param _recordsPerPage
     * @param _pagesToWait
     * @param _tempDir
     * @throws Throwable
     */
    private void init(QueueBrowser _qb, int _recordsPerPage, int _pagesToWait, String _tempDir) throws Throwable
    {
        jmsRecordEnumeration = _qb.getEnumeration();
        Enumeration messageE = _qb.getEnumeration();
        int count = 0;
        while (messageE.hasMoreElements()) {
            Message message = ((Message) messageE.nextElement());
            Enumeration e = message.getPropertyNames();
            while (e.hasMoreElements()) {
                String properties = (String) e.nextElement();
                if (!properties.equals("")) {
                    count++;
                }
            }
            if (count > countProperties.intValue()) {
                countProperties = new Integer(count);
            }
            count = 0;
        }
        destroyed = false;
        pagesToWait = _pagesToWait;
        id = getId();
        recordsPerPage = _recordsPerPage;
        recordCount = 0;
        pageCount = 0;
        page = null;
        currPage = 0;
        tempDir = _tempDir;
        if (pagesToWait > 0) {
            Thread th = new Thread(this);
            th.start();
            synchronized (this) {
                try {
                    wait();
                }
                catch (InterruptedException e) {
                }
            }
        }
        else {
            run();
        }

        gotoPage(1);
    }

    /**
	 *
	 */
    public void run()
    {
        building = true;
        try {
            buildingPage = null;
            int i = 0;
            while (jmsRecordEnumeration.hasMoreElements()) {
                i++;
                if (i <= recordsPerPage) {
                    insertInBuildingPage((Message) jmsRecordEnumeration.nextElement());
                }
            }

            saveBuildingPage();
        }
        catch (IOException e) {

        }
        finally {
            buildingPage = null;
            building = false;
            synchronized (this) {
                notify();
            }
        }
    }

    /**
     *
     * @param message
     * @throws IOException
     */
    private void insertInBuildingPage(Message message) throws IOException
    {
        if (buildingPage == null) {
            buildingPage = new Vector<Message>();
        }

        buildingPage.addElement(message);
        if (buildingPage.size() >= recordsPerPage) {
            saveBuildingPage();
        }
    }

    /**
     *
     */
    public void rollBack()
    {
        deletedInPage = null;
        deletedMessage.clear();
    }

    /**
     *
     * @param queue
     * @param queueSession
     * @return
     * @throws Throwable
     */
    public String commit(Queue queue, QueueSession queueSession) throws Throwable
    {
        if (!isBuilding()) {
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            boolean exist = true;
            while (exist) {
                Message message = queueReceiver.receiveNoWait();
                if (message == null) {
                    exist = false;
                }
            }
            queueSession.commit();
            queueReceiver.close();
            updatePage();
            return "";
        }
        else {
            return "Attendere il caricamento di tutte le pagine e poi effettuare la cancellazione.";
        }
    }

    /**
     *
     * @param queue
     * @param queueSession
     * @param selected
     * @throws Throwable
     */
    public void commit(Queue queue, QueueSession queueSession, String[] selected) throws Throwable
    {

        Vector delete = new Vector();

        gotoPage(currPage);

        for (int i = 0; i < selected.length; i++) {
            if (selected[i].equals("on")) {

                // l'array di parametri selected parte da 1 mentre
                // il vector da 0 quindi aggiusto il tiro
                //
                Message m = (Message) page.get(i);

                delete.add(m);

                // gestione messaggio
                //
                StringBuffer id = new StringBuffer();
                id.append("JMSMessageID='");
                id.append(m.getJMSMessageID());
                id.append("'");

                QueueReceiver queueReceiver = queueSession.createReceiver(queue, id.toString());
                queueReceiver.receiveNoWait();
                queueSession.commit();
                queueReceiver.close();
            }
        }

        // remove from page
        //
        if (delete.size() > 0) {
            for (int j = 0; j < delete.size(); j++) {
                page.remove(delete.get(j));
            }
            updatePage(delete);
        }
    }

    /**
     *
     * @throws IOException
     */
    public void saveBuildingPage() throws IOException
    {
        if (buildingPage == null) {
            return;
        }

        ++pageCount;
        File file = getFileForPage(pageCount);
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file), 4192));

        out.writeObject(buildingPage);
        out.flush();
        out.close();

        recordCount = recordCount + buildingPage.size();
        if (pageCount == pagesToWait) {
            synchronized (this) {
                notify();
            }
        }

        buildingPage = null;
    }

    /**
     *
     * @param page
     * @return
     */
    public File getFileForPage(int page)
    {
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File ret = new File(tempDir, "PagedQueueBrowser_" + id + "_" + page + ".tmp");
        ret.deleteOnExit();
        return ret;
    }

    /**
     *
     * @param delete
     * @throws IOException
     */
    public synchronized void updatePage(Vector delete) throws IOException
    {
        File file = getFileForPage(currPage);
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file), 4192));

        out.writeObject(page);
        out.flush();
        out.close();

        if (delete != null) {
            recordCount = recordCount - delete.size();
        }
    }

    public synchronized void updatePage() throws IOException
    {

        int count = getPageCount();

        for (int i = 1; i < count; i++) {
            File file = getFileForPage(i);
            file.delete();
        }
    }

    /**
	 *
	 *
	 */
    public void destroy()
    {
        if (destroyed) {
            return;
        }

        for (int i = 1; i <= pageCount; ++i) {
            File file = getFileForPage(i);
        }

        destroyed = true;
    }

    /**
     *
     * @param pageNum
     * @throws IOException
     */
    public void gotoPage(int pageNum) throws IOException
    {
        if ((pageNum > pageCount) || (pageNum < 1)) {
            page = new Vector();
            currPage = pageNum;
            return;
        }

        ObjectInputStream in;
        File filename = getFileForPage(pageNum);
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename), 4192));
        }
        catch (StreamCorruptedException exc) {
            throw new IOException("" + exc);
        }
        catch (FileNotFoundException exc) {
            System.out.println("WARNING : File not found:  " + filename);
            return;
        }

        Object o;
        try {
            o = in.readObject();
        }
        catch (ClassNotFoundException exc) {
            throw new IOException("" + exc);
        }

        in.close();

        page = (Vector) o;

        currPage = pageNum;
    }

    /**
     *
     * @param messageToDel
     * @throws IOException
     */
    public void delFromPage(int messageToDel) throws IOException
    {
        page.remove(messageToDel);
    }

    /**
     *
     * @param index
     * @return the message properties
     * @throws Throwable
     */
    public Map<String, String> getMessageProperties(String index) throws Throwable
    {
        Date date = null;
        Message message = (Message) page.get(Integer.parseInt(index));
        properties = new HashMap<String, String>();
        properties.put("CORR_ID", message.getJMSCorrelationID());
        int delivery = message.getJMSDeliveryMode();
        if (delivery == 1) {
            properties.put("DELIVERY", "NON_PERSISTENT");
        }
        else if (delivery == 2) {
            properties.put("DELIVERY", "PERSISTENT");
        }

        date = new Date(message.getJMSExpiration());
        if (message.getJMSExpiration() == 0) {
            properties.put("EXPIRATION", "Never");
        }
        else {
            properties.put("EXPIRATION", dateToStringFormat.format(date));
        }
        properties.put("MSG_ID", message.getJMSMessageID());
        properties.put("PRIORITY", String.valueOf(message.getJMSPriority()));
        date = new Date(message.getJMSTimestamp());
        properties.put("TIME", dateToStringFormat.format(date));
        properties.put("TYPE", message.getJMSType());
        properties.put("REDELIVERY", String.valueOf(message.getJMSRedelivered()));

        if (message.getPropertyNames().hasMoreElements()) {
            getAdditionalProperties(index);
        }

        return properties;
    }

    /**
     *
     * @param index
     * @return @throws Throwable
     */
    public HashMap getAdditionalProperties(String index) throws Throwable
    {
        Message message = (Message) page.get(Integer.parseInt(index));
        Enumeration messagePropertyEnumeration = message.getPropertyNames();
        additionalProperties = new HashMap();
        while (messagePropertyEnumeration.hasMoreElements()) {
            String name = (String) messagePropertyEnumeration.nextElement();
            if (!name.equals("")) {
                Object value = message.getObjectProperty(name);
                additionalProperties.put(name, message.getObjectProperty(name));
            }
        }
        return additionalProperties;
    }

    /**
	 *
	 */
    @Override
    protected void finalize()
    {
        destroy();
    }

    /**
     * Restituisce il valore di un campo dato il suo nome, interpretato come
     * Date.
     *
     * @return null se non � possibile convertire la stringa in data
     */
    public Date getDate(String s)
    {
        return dateParserFormat.parse(s, new ParsePosition(0));
    }

    /**
     * Indica se un campo � null. Il campo � identificato can il suo nome.
     *
     * @return true se il campo � null.
     */
    public boolean isNull(String s)
    {
        return ((s.trim()).equals(""));
    }

    /**
     * @return
     */
    public int getPageRecordCount()
    {
        return page.size();
    }

    /**
     * @return
     */
    public int getPageCount()
    {
        return pageCount;
    }

    /**
     * @param pageCount
     */
    public void setPageCount(int pageCount)
    {
        this.pageCount = pageCount;
    }

    /**
     * @return
     */
    public int getRecordCount()
    {
        return recordCount;
    }

    /**
     * @return
     */
    public int getMaxRecordsPerPage()
    {
        return recordsPerPage;
    }

    /**
     * @return
     */
    public boolean isBuilding()
    {
        return building;
    }

    /**
     * @return Returns the page.
     */
    public Vector getPage()
    {
        if (page == null) {
            return null;
        }
        return page;
    }

    /**
     * @param page
     *        The page to set.
     */
    public void setPage(Vector page)
    {
        this.page = page;
    }

    /**
     * @return Returns the buildingPage.
     */
    public Vector getBuildingPage()
    {
        return buildingPage;
    }

    /**
     * @return Returns the buildingPage.
     */
    public Vector getBuildingPage(int page)
    {
        return buildingPage;
    }

    /**
     * @param buildingPage
     *        The buildingPage to set.
     */
    public void setBuildingPage(Vector buildingPage)
    {
        this.buildingPage = buildingPage;
    }

    /**
     * @return Returns the countProperties.
     */
    public Integer getCountProperties()
    {
        return countProperties;
    }

    /**
     * @param countProperties
     *        The countProperties to set.
     */
    public void setCountProperties(Integer countProperties)
    {
        this.countProperties = countProperties;
    }

    /**
     * @return Returns the deletedInPage.
     */
    public Vector<Message> getDeletedInPage()
    {
        return deletedInPage;
    }

    /**
     * @param deletedInPage
     *        The deletedInPage to set.
     */
    public void setDeletedInPage(Vector<Message> deletedInPage)
    {
        this.deletedInPage = deletedInPage;
    }

    /**
     * @return
     */
    public static synchronized long getId()
    {
        return ++counter;
    }

    /**
     * @param id
     */
    public void setId(long id)
    {
        counter = id;
    }
}