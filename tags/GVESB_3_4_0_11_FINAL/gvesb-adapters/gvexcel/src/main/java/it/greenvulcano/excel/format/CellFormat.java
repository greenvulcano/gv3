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

import java.text.Format;
import java.text.SimpleDateFormat;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WriteException;

import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class CellFormat
{
    private static final int     DEF_MIN_COL_WIDTH     = 1;
    private static final int     DEF_MAX_COL_WIDTH     = 30;
    private static final String  DEF_BACKGROUND_COLOUR = "";
    private static final String  DEF_ALIGNMENT         = "LEFT";
    private static final boolean DEF_HAS_BORDER        = false;
    private static final float   DEF_SCALE_FACTOR      = 1.0F;
    private static final String  DEF_DATE_PATTERN      = "dd/MM/yyyy hh:mm:ss";
    private static final String  DEF_NUMBER_PATTERN    = "@";

    public static final boolean  SHRINK_TO_FIT_ENABLED = false;
    public static int            STRING                = 0;
    public static int            NUMBER                = 1;
    public static int            DATE                  = 2;
    private String               backgroundColor       = null;
    private String               alignment             = null;
    private boolean              hasBorder             = false;
    private int                  minWidth              = 0;
    private int                  maxWidth              = 0;
    private float                scaleFactor           = 0;
    private int                  type                  = 0;
    private Format               dateTextFormat        = null;
    private CellFont             font                  = null;
    private WritableCellFormat   wcFormat              = null;

    public CellFormat(Node node, int type) throws ExcelException
    {
        try {
            wcFormat = null;
            minWidth = XMLConfig.getInteger(node, "@minWidth", DEF_MIN_COL_WIDTH);
            maxWidth = XMLConfig.getInteger(node, "@maxWidth", DEF_MAX_COL_WIDTH);
            backgroundColor = XMLConfig.get(node, "@backgroundColour", DEF_BACKGROUND_COLOUR);
            alignment = XMLConfig.get(node, "@alignment", DEF_ALIGNMENT);
            hasBorder = XMLConfig.getBoolean(node, "@hasBorder", DEF_HAS_BORDER);
            scaleFactor = XMLConfig.getFloat(node, "@scaleFactor", DEF_SCALE_FACTOR);
            font = new CellFont(XMLConfig.getNode(node, "Font"));

            this.type = type;
            if (type == STRING) {
                wcFormat = new WritableCellFormat(font.getAsWritableFont());
            }
            else if (type == DATE) {
                String pattern = XMLConfig.get(node, "@pattern", DEF_DATE_PATTERN);
                dateTextFormat = new SimpleDateFormat(pattern);
                DateFormat dateformat = new DateFormat(pattern);
                wcFormat = new WritableCellFormat(dateformat);
                wcFormat.setFont(font.getAsWritableFont());
            }
            else if (type == NUMBER) {
                String pattern = XMLConfig.get(node, "@pattern", DEF_NUMBER_PATTERN);
                NumberFormat numberformat = new NumberFormat(pattern, NumberFormat.COMPLEX_FORMAT);
                wcFormat = new WritableCellFormat(numberformat);
                wcFormat.setFont(font.getAsWritableFont());
            }
            configFormat(wcFormat);
        }
        catch (Exception exc) {
            throw new ExcelException("Error initializing CellFormat", exc);
        }
    }

    public CellFormat(int minWidth, int maxWidth, String backgroundColor, String alignment, boolean hasBorder,
            float scaleFactor, String format, CellFont font, int type)
    {
        this.type = -1;
        this.font = null;
        wcFormat = null;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.backgroundColor = backgroundColor;
        this.alignment = alignment;
        this.hasBorder = hasBorder;
        this.scaleFactor = scaleFactor;
        this.font = font;
        this.type = type;
        if (type == STRING) {
            wcFormat = new WritableCellFormat(font.getAsWritableFont());
        }
        else if (type == DATE) {
            dateTextFormat = new SimpleDateFormat(format);
            DateFormat dateformat = new DateFormat(format);
            wcFormat = new WritableCellFormat(dateformat);
            wcFormat.setFont(font.getAsWritableFont());
        }
        else if (type == NUMBER) {
            NumberFormat numberformat = new NumberFormat(format, NumberFormat.COMPLEX_FORMAT);
            wcFormat = new WritableCellFormat(numberformat);
            wcFormat.setFont(font.getAsWritableFont());
        }
        configFormat(wcFormat);
    }

    public CellFormat(CellFormat format)
    {
        type = -1;
        font = null;
        wcFormat = null;
        minWidth = format.minWidth;
        maxWidth = format.maxWidth;
        backgroundColor = format.backgroundColor;
        alignment = format.alignment;
        hasBorder = format.hasBorder;
        scaleFactor = format.scaleFactor;
        font = new CellFont(format.font);
        type = format.type;
        wcFormat = new WritableCellFormat(format.wcFormat);
        dateTextFormat = format.dateTextFormat;
    }

    public CellFont getFont()
    {
        return font;
    }

    public int getMinWidth()
    {
        return minWidth;
    }

    public int getMaxWidth()
    {
        return maxWidth;
    }

    public float getScaleFactor()
    {
        return scaleFactor;
    }

    public String getBackgroundColor()
    {
        return backgroundColor;
    }

    public String getAlignment()
    {
        return alignment;
    }

    public boolean hasBorder()
    {
        return hasBorder;
    }

    public int getType()
    {
        return type;
    }

    public void configFormat(WritableCellFormat writablecellformat)
    {
        configCellFormat(writablecellformat, backgroundColor, alignment, hasBorder);
    }

    public WritableCellFormat getAsWritableCellFormat()
    {
        return wcFormat;
    }

    public String applyPattern(Object obj)
    {
        if (type == DATE) {
            return dateTextFormat.format(obj);
        }
        else {
            return obj.toString();
        }
    }

    public static void configCellFormat(WritableCellFormat writablecellformat, String backgroundColor,
            String alignment, boolean hasBorder)
    {
        try {
            if (backgroundColor.compareToIgnoreCase("GREEN") == 0) {
                writablecellformat.setBackground(Colour.GREEN);
            }
            else if (backgroundColor.compareToIgnoreCase("GRAY_25") == 0) {
                writablecellformat.setBackground(Colour.GRAY_25);
            }
            else if (backgroundColor.compareToIgnoreCase("GRAY_50") == 0) {
                writablecellformat.setBackground(Colour.GRAY_50);
            }
            else if (backgroundColor.compareToIgnoreCase("GRAY_80") == 0) {
                writablecellformat.setBackground(Colour.GRAY_80);
            }
            else if (backgroundColor.compareToIgnoreCase("BLUE_GRAY") == 0) {
                writablecellformat.setBackground(Colour.BLUE_GREY);
            }
            else if (backgroundColor.compareToIgnoreCase("BLUE") == 0) {
                writablecellformat.setBackground(Colour.BLUE);
            }
            else if (backgroundColor.compareToIgnoreCase("BRIGHT_GREEN") == 0) {
                writablecellformat.setBackground(Colour.BRIGHT_GREEN);
            }
            else if (backgroundColor.compareToIgnoreCase("BROWN") == 0) {
                writablecellformat.setBackground(Colour.BROWN);
            }
            else if (backgroundColor.compareToIgnoreCase("RED") == 0) {
                writablecellformat.setBackground(Colour.RED);
            }
            else if (backgroundColor.compareToIgnoreCase("ROSE") == 0) {
                writablecellformat.setBackground(Colour.ROSE);
            }
            else if (backgroundColor.compareToIgnoreCase("YELLOW") == 0) {
                writablecellformat.setBackground(Colour.YELLOW);
            }
            else if (backgroundColor.compareToIgnoreCase("TURQUOISE") == 0) {
                writablecellformat.setBackground(Colour.TURQUOISE);
            }
            else if (backgroundColor.compareToIgnoreCase("DARK_BLUE") == 0) {
                writablecellformat.setBackground(Colour.DARK_BLUE);
            }
            else if (backgroundColor.compareToIgnoreCase("DARK_GREEN") == 0) {
                writablecellformat.setBackground(Colour.DARK_GREEN);
            }
            else if (backgroundColor.compareToIgnoreCase("DARK_PURPLE") == 0) {
                writablecellformat.setBackground(Colour.DARK_PURPLE);
            }
            else if (backgroundColor.compareToIgnoreCase("DARK_RED") == 0) {
                writablecellformat.setBackground(Colour.DARK_RED);
            }
            else if (backgroundColor.compareToIgnoreCase("DARK_YELLOW") == 0) {
                writablecellformat.setBackground(Colour.DARK_YELLOW);
            }
            else if (backgroundColor.compareToIgnoreCase("ICE_BLUE") == 0) {
                writablecellformat.setBackground(Colour.ICE_BLUE);
            }
            else if (backgroundColor.compareToIgnoreCase("LIGHT_BLUE") == 0) {
                writablecellformat.setBackground(Colour.LIGHT_BLUE);
            }
            else if (backgroundColor.compareToIgnoreCase("LIGHT_GREEN") == 0) {
                writablecellformat.setBackground(Colour.LIGHT_GREEN);
            }
            else if (backgroundColor.compareToIgnoreCase("LIGHT_ORANGE") == 0) {
                writablecellformat.setBackground(Colour.LIGHT_ORANGE);
            }
            else if (backgroundColor.compareToIgnoreCase("LIGHT_TURQUOISE") == 0) {
                writablecellformat.setBackground(Colour.LIGHT_TURQUOISE);
            }
            else if (backgroundColor.compareToIgnoreCase("ORANGE") == 0) {
                writablecellformat.setBackground(Colour.ORANGE);
            }
            else if (backgroundColor.compareToIgnoreCase("VIOLET") == 0) {
                writablecellformat.setBackground(Colour.VIOLET);
            }
            else if (backgroundColor.compareToIgnoreCase("SKY_BLUE") == 0) {
                writablecellformat.setBackground(Colour.SKY_BLUE);
            }
            else if (backgroundColor.compareToIgnoreCase("PINK") == 0) {
                writablecellformat.setBackground(Colour.PINK);
            }
            else if (backgroundColor.compareToIgnoreCase("GOLD") == 0) {
                writablecellformat.setBackground(Colour.GOLD);
            }
            else if (backgroundColor.compareToIgnoreCase("WHITE") == 0) {
                writablecellformat.setBackground(Colour.WHITE);
            }
            if (alignment.compareToIgnoreCase("CENTRE") == 0) {
                writablecellformat.setAlignment(Alignment.CENTRE);
            }
            else if (alignment.compareToIgnoreCase("LEFT") == 0) {
                writablecellformat.setAlignment(Alignment.LEFT);
            }
            else if (alignment.compareToIgnoreCase("RIGHT") == 0) {
                writablecellformat.setAlignment(Alignment.RIGHT);
            }
            else if (alignment.compareToIgnoreCase("JUSTIFY") == 0) {
                writablecellformat.setAlignment(Alignment.JUSTIFY);
            }
            else if (alignment.compareToIgnoreCase("FILL") == 0) {
                writablecellformat.setAlignment(Alignment.FILL);
            }
            else {
                writablecellformat.setAlignment(Alignment.GENERAL);
            }
            writablecellformat.setShrinkToFit(false);
            if (hasBorder) {
                writablecellformat.setBorder(Border.ALL, BorderLineStyle.THIN);
            }
        }
        catch (WriteException writeexception) {
            // do nothing
        }
    }

}
