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

import java.util.StringTokenizer;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class XQueriesElement
{

    private String nome;

    private String descrizione;

    private String xqueryString;

    /**
     *
     */
    public XQueriesElement()
    {
        nome = "";
        descrizione = "";
        xqueryString = "";
    }

    /**
     * @return
     */
    public String getDescrizione()
    {
        return toHTML(descrizione);
    }

    /**
     * @param descrizione
     */
    public void setDescrizione(String descrizione)
    {
        this.descrizione = descrizione;
    }

    /**
     * @return
     */
    public String getNome()
    {
        return nome;
    }

    /**
     * @param nome
     */
    public void setNome(String nome)
    {
        this.nome = nome;
    }

    /**
     * @return
     */
    public String getXqueryString()
    {
        return toHTML(xqueryString);
    }

    /**
     * @param xqueryString
     */
    public void setXqueryString(String xqueryString)
    {
        this.xqueryString = xqueryString;
    }

    /**
     * @param nome
     * @param descrizione
     * @param xQueryString
     */
    public void setFields(String nome, String descrizione, String xQueryString)
    {
        this.nome = nome;
        this.descrizione = descrizione;
        xqueryString = xQueryString;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String s = "Nome: " + getNome() + " Descrizione: " + getDescrizione() + " XQueryString: " + getXqueryString();
        return s;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object elem)
    {
        return nome.equals(((XQueriesElement) elem).getNome());
    }

    /**
     * @param str
     * @return
     */
    public String toHTML(String str)
    {
        StringTokenizer tokenizer = new StringTokenizer(str, "<>\"", true);
        StringBuffer buffer = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("<")) {
                buffer.append("&lt;");
            }
            else if (token.equals(">")) {
                buffer.append("&gt;");
            }
            else if (token.equals("\"")) {
                buffer.append("&quot;");
            }
            else {
                buffer.append(token);
            }
        }
        return buffer.toString();
    }
}
