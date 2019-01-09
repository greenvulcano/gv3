/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:49 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/xml/DocumentDestination.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Id: DocumentDestination.java,v 1.1 2010-04-03 15:28:49 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.xml;

import max.core.MaxException;

import org.w3c.dom.Document;


/**
 * I documenti saranno salvati utilizzando questa interfaccia.
 * Le classi che implementano questa interfaccia possono scrivere
 * i documenti nei supporti pi� disparate: file, jar files, database
 * ecc. Sar� possibile salvare documenti non in formato
 * XML: le classi concrete possono urilizzare l'XML dato nel modo pi�
 * appropriato.
 */
public interface DocumentDestination
{
    public void init(String section, String prefix) throws MaxException;
    public void storeDocument(Document document) throws MaxException;
}
