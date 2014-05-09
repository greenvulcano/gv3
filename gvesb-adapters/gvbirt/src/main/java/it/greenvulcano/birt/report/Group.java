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

import it.greenvulcano.configuration.XMLConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * E' la classe che rappresenta i gruppi logici di report. Serve per raggruppare insiemi di report che hanno caratteristiche logiche affini.
 *
 * @version 3.1.0 03/feb/2011
 * @author GreenVulcano Developer Team
 */
public class Group {
    private String              name    = null;
    private Map<String, Report> reports = new HashMap<String, Report>();

    /**
     * Riceve in ingresso un nodo del file xml di configurazione e inizializza il {@link Group} secondo i parametri di tale file.
     * Crea la lista dei {@link Report} e inizializza i singoli report.
     * @param node Nodo del file xml. Utilizzato per configurare il {@link Group}
     * @throws Exception in caso di errori
     */
    public void init(Node node) throws Exception {
        name = XMLConfig.get(node, "@name");
        NodeList nl = XMLConfig.getNodeList(node, "Report");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            Report r = new Report(n);

            reports.put(r.getName(), r);
        }

    }

    /**
     * Ritorna il parametro name dell'oggetto {@link Group}
     * @return La Stringa contenente il nome del gruppo
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce il {@link Report} del gruppo corrispondente al nome in ingresso
     * @param report Stringa con il nome del {@link Report} da restituire
     * @return Restituisce il {@link Report} del gruppo corrispondente al nome in ingresso
     */
    public Report getReport(String report) {
        return reports.get(report);
    }

    /**
     * Ritorna una Map contenente i {@link Report} del {@link Group}
     * @return Una Map contenente i {@link Report} che fanno parte del gruppo
     */
    public Map<String, Report> getReports() {
        if (reports.isEmpty())
            return null;
        return reports;
    }

    public List<String> getReportsNames(){
        List<String> l = new ArrayList<String>(0);
        String name = null;
        if(reports == null) return null;

        for(Iterator<String> it = reports.keySet().iterator(); it.hasNext();) {
            name = it.next();
            l.add(name);
        }
        if(l.isEmpty()) return null;
        return l;
    }

    @Override
    public String toString() {
        return "Group [" + name + "] - Reports: " + reports;
    }

}