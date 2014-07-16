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
package it.greenvulcano.birt.report.internal.param;

import it.greenvulcano.birt.report.Parameter;
import it.greenvulcano.birt.report.internal.field.DHSource;
import it.greenvulcano.birt.report.internal.field.FixedSource;
import it.greenvulcano.birt.report.internal.field.Source;
import it.greenvulcano.birt.report.internal.field.StringSource;
import it.greenvulcano.configuration.XMLConfig;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.w3c.dom.Node;

/**
 * @version 3.1.0 03/feb/2011
 * @author GreenVulcano Developer Team
 */
public abstract class BaseParameter implements Parameter
{
    private String  name           = null;
    private String  controlType    = null;
    private String  label          = null;
    private boolean isRequired     = false;
    private String  defaultValue   = null;
    private boolean isDefaultParam = true;
    private String  sourceType     = SOURCE_TYPE_NONE;
    private Source  source         = null;
    private String  expression     = null;

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.birtreport.config.Parameter#init(org.eclipse.birt.report
     * .engine.api.IGetParameterDefinitionTask,
     * org.eclipse.birt.report.engine.api.IScalarParameterDefn,
     * org.eclipse.birt.report.engine.api.IReportRunnable)
     */
    @Override
    public void init(Node node, IGetParameterDefinitionTask task, IScalarParameterDefn scalar, IReportRunnable report)
            throws Exception
    {
        this.name = scalar.getName();
        this.label = scalar.getDisplayName();
        if ((this.label == null) || ("".equals(this.label))) {
            this.label = this.name;
        }
        this.isRequired = scalar.isRequired();
        switch (scalar.getControlType()) {
            case IScalarParameterDefn.TEXT_BOX :
                this.controlType = CONTROL_TYPE_TEXT;
                break;
            case IScalarParameterDefn.LIST_BOX :
                this.controlType = CONTROL_TYPE_SELECT;
                break;
            case IScalarParameterDefn.RADIO_BUTTON :
                this.controlType = CONTROL_TYPE_RADIO;
                break;
            case IScalarParameterDefn.CHECK_BOX :
                this.controlType = CONTROL_TYPE_CHECK;
                break;
            default :
                this.controlType = CONTROL_TYPE_TEXT;
                break;
        }
        /*
         * ReportDesignHandle reportHandle = (ReportDesignHandle)
         * report.getDesignHandle(); ScalarParameterHandle parameterHandle =
         * (ScalarParameterHandle) reportHandle.findParameter(scalar.getName());
         * List ldv = parameterHandle.getDefaultValueList(); if (!ldv.isEmpty())
         * { this.defaultValue = ldv.get(0).toString(); }
         */
        Object dv = task.getDefaultValue(scalar);
        this.defaultValue = (dv == null) ? "" : dv.toString();

        if (node != null) {
            if (XMLConfig.exists(node, "@label")) {
                label = XMLConfig.get(node, "@label", label);
            }
            if (XMLConfig.exists(node, "@control-type")) {
                isDefaultParam = false;
                controlType = XMLConfig.get(node, "@control-type");
            }
/*            if (XMLConfig.exists(node, "@is-required")) {
                isRequired = Boolean.parseBoolean(XMLConfig.get(node, "@is-required"));
            }
*/          if (XMLConfig.exists(node, "@default-value")) {
                isDefaultParam = false;
                defaultValue = XMLConfig.get(node, "@default-value");
            }

            Node sourceNode = XMLConfig.getNode(node, "*[@type='source']");
            if (sourceNode != null) {
                isDefaultParam = false;
                source = (Source) Class.forName(XMLConfig.get(sourceNode, "@class")).newInstance();
                source.init(sourceNode);
                sourceType = source.getType();
            }
        }

        expression = XMLConfig.get(node, "@expression", null);
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.birtreport.config.Parameter#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.birtreport.config.Parameter#getLabel()
     */
    @Override
    public String getLabel()
    {
        return label;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.birtreport.config.Parameter#getControlType()
     */
    @Override
    public String getControlType()
    {
        return controlType;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.birtreport.config.Parameter#getDefValue()
     */
    @Override
    public String getDefValue()
    {
        return defaultValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.birtreport.config.Parameter#getValueList()
     */
    @Override
    public List<String> getValueList()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.birt.report.Parameter#getExpression()
     */
    @Override
    public String getExpression()
    {
        return expression;
    }

    public void setData(HttpSession session, Map<String, String> params) throws Exception
    {
        if (!SOURCE_TYPE_NONE.equals(sourceType)) {
            session.setAttribute(name, source.getData(params));
            if (SOURCE_TYPE_STRING.equals(sourceType)) {
                defaultValue = "" + source.getData(params);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.birtreport.config.Parameter#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        return isRequired;
    }

    @Override
    public boolean isDefaultParam()
    {
        return isDefaultParam;
    }

}
