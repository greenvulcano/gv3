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
package it.greenvulcano.gvesb.gvconsole.deploy;

/**
 * DatiServizio class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class DatiServizio
{
    private String  nomeFile   = null;
    private String  nodoNew    = null;
    private String  nodoServer = null;
    private boolean exist      = false;
    private boolean equals     = false;

    public void setNomeFile(String nomeFile)
    {
        this.nomeFile = nomeFile;
    }

    public String getNomeFile()
    {
        return nomeFile;
    }

    public void setExist(boolean exist)
    {
        this.exist = exist;
    }

    public boolean getExist()
    {
        return exist;
    }

    public void setEquals(boolean equals)
    {
        this.equals = equals;
    }

    public boolean getEquals()
    {
        return equals;
    }

    public void setNodoNew(String nodoNew)
    {
        this.nodoNew = nodoNew;
    }

    public String getNodoNew()
    {
        return nodoNew;
    }

    public void setNodoServer(String nodoServer)
    {
        this.nodoServer = nodoServer;
    }

    public String getNodoServer()
    {
        return nodoServer;
    }

}
