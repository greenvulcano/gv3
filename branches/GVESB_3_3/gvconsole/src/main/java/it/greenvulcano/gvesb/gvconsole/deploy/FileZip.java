/**
 *
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
package it.greenvulcano.gvesb.gvconsole.deploy;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.zip.ZipHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * FileZip class
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class FileZip
{
    private String              nomeFile     = null;
    private File                fileToCreate = null;
    private static final int    BUFFER       = 1024;
    private String[]            listaFile    = null;
    private static final Logger logger       = GVLogger.getLogger(FileZip.class);

    /**
     * @throws IOException
     *
     */
    public FileZip(String nomeFile, byte[] data) throws IOException
    {
        logger.debug("Init FileZip");
        setNomeFile(nomeFile);
        saveFile(data);
        leggeFileZip();
    }

    public String getNomeFile()
    {
        return nomeFile;
    }

    /*public String[] getListaFile()
    {
        return listaFile;
    }*/

    private void setNomeFile(String nomeFile)
    {
        this.nomeFile = nomeFile;
    }

    private void saveFile(byte[] data) throws IOException
    {
        String path = java.lang.System.getProperty("java.io.tmpdir");
        if (!nomeFile.equals("")) {
            fileToCreate = new File(path, nomeFile);
            FileOutputStream fileOutStream = new FileOutputStream(fileToCreate);
            fileOutStream.write(data);
            fileOutStream.flush();
            fileOutStream.close();
        }
    }

    private void leggeFileZip() throws IOException
    {
        // carica il file zip
        String path = java.lang.System.getProperty("java.io.tmpdir");
        File dest = new File(path, "conf");

        FileUtils.deleteDirectory(dest);
        FileUtils.forceMkdir(dest);

        ZipHelper zh = new ZipHelper();
        zh.unzipFile(path, nomeFile, path);
    }

    private void deleteDirectory(File path) throws IOException
    {
        FileUtils.forceDelete(path);
    }


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        // TODO Auto-generated method stub
        System.out.println(java.lang.System.getProperty("java.io.tmpdir"));

        byte[] dati = BinaryUtils.readFileAsBytes("/home/gianluca/applicazioni/Vulcon/GV-Ent-OCTO/GV_001.zip");
        FileZip readFileZip = new FileZip("GV_001.zip", dati);
        /*String[] listaServizi = readFileZip.getListaFile();
        System.out.println(listaServizi[0]);
        System.out.println(listaServizi[1]);*/
    }

}
