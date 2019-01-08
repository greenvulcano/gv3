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
package it.greenvulcano.gvesb.gvconsole.webservice.utils;

import it.greenvulcano.gvesb.gvconsole.webservice.bean.BusinessWebServicesBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.FileBean;
import it.greenvulcano.gvesb.gvconsole.webservice.bean.WebServiceBean;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class FileUtility
{
    /**
     * @param path
     * @param fileFilter
     * @return the list of filename retrieved
     */
    public static final List<String> listFiles(String path, FileFilter fileFilter)
    {
        return listFiles(new File(path), fileFilter);
    }

    /**
     * @param ws
     * @param fileFilter
     * @return the list of file beans retrieved
     */
    public static final List<FileBean> listFilesBean(BusinessWebServicesBean ws, FileFilter fileFilter)
    {
        return listFilesBean(new File(ws.getWsdlDirectory()), fileFilter, ws.getWebServicesBeanMap(), null);
    }

    /**
     * @param ws
     * @param fileFilter
     * @param ll
     * @return the list of file beans retrieved
     */
    public static final List<FileBean> listFilesBean(BusinessWebServicesBean ws, FileFilter fileFilter,
            List<FileBean> ll)
    {
        return listFilesBean(new File(ws.getWsdlDirectory()), fileFilter, ws.getWebServicesBeanMap(), ll);
    }

    /**
     * @param folder
     * @param fileFilter
     * @return the list of filename retrieved
     */
    public static final List<String> listFiles(File folder, FileFilter fileFilter)
    {
        File[] file = folder.listFiles(fileFilter);
        List<String> files = new LinkedList<String>();
        if (file != null) {
            for (int i = 0; i < file.length; i++) {
                files.add(getFileName(file[i]));
            }
        }
        return files;
    }

    /**
     * @param folder
     * @param fileFilter
     * @param webServicesBeanMap
     * @param ll
     * @return the list of retrieved file beans.
     */
    public static final List<FileBean> listFilesBean(File folder, FileFilter fileFilter,
            Map<String, WebServiceBean> webServicesBeanMap, List<FileBean> ll)
    {
        File[] file = folder.listFiles(fileFilter);
        List<FileBean> files = ll;
        if (files == null) {
            files = new LinkedList<FileBean>();
        }
        FileBean fileBean;
        if (file != null) {
            for (int i = 0; i < file.length; i++) {
                File actFile = file[i];

                if (webServicesBeanMap.containsKey(getFileName(actFile)))
                    fileBean = new FileBean(getFileName(actFile), getExtension(actFile), actFile.lastModified(), true);
                else
                    fileBean = new FileBean(getFileName(actFile), getExtension(actFile), actFile.lastModified(), false);
                files.add(fileBean);
            }
        }
        return files;
    }

    /**
     * @param file
     * @return the file name
     */
    public static String getFileName(File file)
    {
        String fileName = file.getName();
        String ext = getExtension(file);
        fileName = fileName.substring(0, (fileName.length() - (ext.length() + 1)));
        return fileName;
    }

    /**
     * @param f
     * @return the file extension
     */
    public static final String getExtension(File f)
    {
        String ext = new String();
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * @param file
     * @param newFile
     */
    public static final void copy(File file, File newFile)
    {
        try {
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(newFile);
            copy(in, out);
            in.close();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final void copy(InputStream in, OutputStream out) throws IOException
    {
        synchronized (in) {
            synchronized (out) {
                byte[] buffer = new byte[256];
                while (true) {
                    int bytesread = in.read(buffer);
                    if (bytesread == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesread);
                }
            }
        }
    }
}
