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
package it.greenvulcano.excel.format;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.excel.exception.ExcelException;
import jxl.format.UnderlineStyle;
import jxl.write.WritableFont;
import jxl.write.WriteException;

import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class CellFont
{
    private static final int    DEF_FONT_SIZE = 10;
    private static final String DEF_FONT_TYPE = "ARIAL";

    private String              type          = null;
    private int                 size          = 0;
    private boolean             isBold        = false;
    private boolean             isItalic      = false;
    private boolean             isUnderlined  = false;

    public CellFont(Node node) throws ExcelException
    {
        try {
            type = XMLConfig.get(node, "@type", DEF_FONT_TYPE);
            size = XMLConfig.getInteger(node, "@size", DEF_FONT_SIZE);
            isBold = XMLConfig.getBoolean(node, "@isBold", false);
            isItalic = XMLConfig.getBoolean(node, "@isItalic", false);
            isUnderlined = XMLConfig.getBoolean(node, "@isUnderlined", false);
        }
        catch (Exception exc) {
            throw new ExcelException("Error initializing CellFont", exc);
        }
    }

    public CellFont(String type, int size, boolean isBold, boolean isItalic, boolean isUnderlined)
    {
        this.type = type;
        this.size = size;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderlined = isUnderlined;
    }

    public CellFont(CellFont font)
    {
        type = font.type;
        size = font.size;
        isBold = font.isBold;
        isItalic = font.isItalic;
        isUnderlined = font.isUnderlined;
    }

    public String getType()
    {
        return type;
    }

    public int getSize()
    {
        return size;
    }

    public boolean isBold()
    {
        return isBold;
    }

    public boolean isItalic()
    {
        return isItalic;
    }

    public boolean isUnderlined()
    {
        return isUnderlined;
    }

    public WritableFont getAsWritableFont()
    {
        return getWritableFont(type, size, isBold, isItalic, isUnderlined);
    }

    public static WritableFont getWritableFont(String type, int size, boolean isBold, boolean isItalic,
            boolean isUnderlined)
    {
        WritableFont writablefont = null;
        if (type.compareToIgnoreCase("ARIAL") == 0) {
            writablefont = new WritableFont(WritableFont.ARIAL, size);
        }
        else if (type.compareToIgnoreCase("COURIER") == 0) {
            writablefont = new WritableFont(WritableFont.COURIER, size);
        }
        else if (type.compareToIgnoreCase("TAHOMA") == 0) {
            writablefont = new WritableFont(WritableFont.TAHOMA, size);
        }
        else if (type.compareToIgnoreCase("TIMES") == 0) {
            writablefont = new WritableFont(WritableFont.TIMES, size);
        }
        else {
            writablefont = new WritableFont(WritableFont.ARIAL, size);
        }
        try {
            if (isBold) {
                writablefont.setBoldStyle(WritableFont.BOLD);
            }
            if (isItalic) {
                writablefont.setItalic(true);
            }
            if (isUnderlined) {
                writablefont.setUnderlineStyle(UnderlineStyle.SINGLE);
            }
        }
        catch (WriteException writeexception) {
            // do nothing
        }
        return writablefont;
    }

}
