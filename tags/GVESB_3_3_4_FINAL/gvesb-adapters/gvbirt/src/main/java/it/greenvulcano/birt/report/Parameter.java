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
package it.greenvulcano.birt.report;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.w3c.dom.Node;

/**
 * E' l'interfaccia che rappresenta i parametri di un report. Le classi che la
 * implementano servono per specificare le caratteristiche logiche di un report.
 *
 * @version 3.1.0 03/feb/2011
 * @author GreenVulcano Developer Team
 */
public interface Parameter {
    static String TYPE_STRING         = "STRING";
    static String TYPE_INTEGER        = "INTEGER";
    static String TYPE_DATE           = "DATE";
    static String TYPE_DATE_TIME      = "DATE_TIME";
    static String TYPE_TIME           = "TIME";
    static String TYPE_DECIMAL        = "DECIMAL";
    static String TYPE_FLOAT          = "FLOAT";
    static String TYPE_BOOLEAN        = "BOOLEAN";
    static String TYPE_ANY            = "ANY";
    static String CONTROL_TYPE_TEXT   = "TEXT";
    static String CONTROL_TYPE_SELECT = "SELECT";
    static String CONTROL_TYPE_RADIO  = "RADIO";
    static String CONTROL_TYPE_CHECK  = "CHECK";

    static String SOURCE_TYPE_NONE    = "NONE";
    static String SOURCE_TYPE_FIXED   = "FIXED";
    static String SOURCE_TYPE_STRING  = "STRING";
    static String SOURCE_TYPE_DH      = "DATA_HANDLER";


    /**
     * Riceve in ingresso un nodo del file xml di configurazione e inizializza
     * il {@link Parameter} secondo i parametri di tale file. Il comportamento
     * può differenziarsi a seconda dell'implementazione di questa interfaccia.
     *
     * @param node
     *            Nodo del file xml. Utilizzato per configurare il
     *            {@link Report}
     * @throws Exception
     *             in caso di errori
     */
    void init(Node node, IGetParameterDefinitionTask task, IScalarParameterDefn scalar, IReportRunnable report)
            throws Exception;

    /**
     * Ritorna il parametro name dell'oggetto che implementa l'interfaccia
     * {@link Parameter}
     *
     * @return La Stringa contenente il nome del parametro
     */
    String getName();

    String getLabel();

    String getType();

    String getControlType();

    String getDefValue();

    String getFormat();

    List<String> getValueList();

    void setData(HttpSession session, Map<String, String> params) throws Exception;

    boolean isRequired();

    boolean isDefaultParam();

    String getExpression();

    Object convertToValue(String val) throws Exception;

    String convertFromValue(Object val) throws Exception;
}
