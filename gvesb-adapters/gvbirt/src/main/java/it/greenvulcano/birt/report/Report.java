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

import it.greenvulcano.birt.report.exception.BIRTException;
import it.greenvulcano.birt.report.internal.ReportRenderOptions;
import it.greenvulcano.birt.report.internal.param.AnyParameter;
import it.greenvulcano.birt.report.internal.param.BooleanParameter;
import it.greenvulcano.birt.report.internal.param.DateParameter;
import it.greenvulcano.birt.report.internal.param.DateTimeParameter;
import it.greenvulcano.birt.report.internal.param.DecimalParameter;
import it.greenvulcano.birt.report.internal.param.FloatParameter;
import it.greenvulcano.birt.report.internal.param.IntegerParameter;
import it.greenvulcano.birt.report.internal.param.StringParameter;
import it.greenvulcano.birt.report.internal.param.TimeParameter;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.1.0 19/dic/2010
 * @author GreenVulcano Developer Team
 */
public class Report
{
    private String                           reportName    = null;
    private String                           reportConfig  = null;
    private String                           defaultRender = null;
    private boolean                          isInitialized = false;
    private Map<String, ReportRenderOptions> renders       = new HashMap<String, ReportRenderOptions>();
    private Map<String, Parameter>           parameters    = new HashMap<String, Parameter>();
    private Node                             node          = null;
    private ArrayList<String>                paramNameList = new ArrayList<String>();

    /**
     *
     */
    public Report(Node node) throws BIRTException
    {
        try {
            reportName = XMLConfig.get(node, "@name");
            reportConfig = XMLConfig.get(node, "@config");
            this.node = node;
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing BIRT Report", exc);
        }
    }

    /**
     * Riceve in ingresso un nodo del file xml di configurazione e inizializza
     * il {@link Report} secondo i parametri di tale file. Crea la lista dei
     * {@link Parameter} e inizializza i singoli {@link Parameter}.
     * 
     * @param node
     *        Nodo del file xml. Utilizzato per configurare il {@link Report}
     * @throws Exception
     *         in caso di errori
     */
    public void init(Map<String, ReportRenderOptions> baseRenders, IReportEngine engine, IReportRunnable design,
            IGetParameterDefinitionTask task) throws Exception
    {
        try {
            if (isInitialized) {
                return;
            }

            defaultRender = XMLConfig.get(node, "@defaultRender", "pdf");

            // first copy renders from engine ...
            if (baseRenders != null && baseRenders.size() > 0)
                for (Entry<String, ReportRenderOptions> entry : baseRenders.entrySet()) {
                    renders.put(entry.getKey(), entry.getValue());
                }

            // ... then overwrites with custom eventually configured renders
            NodeList rnl = XMLConfig.getNodeList(node, "Renders/*[@type='report-render']");
            if ((rnl != null) && (rnl.getLength() > 0)) {
                for (int i = 0; i < rnl.getLength(); i++) {
                    Node n = rnl.item(i);
                    ReportRenderOptions render = (ReportRenderOptions) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                    render.init(n);
                    renders.put(render.getType(), render);
                }
            }

            // creo il task per l'estrazione delle definizioni dei parametri
            // estraggo i dettagli per ogni parametro
            // istanzio ogni parametro sovrascrivendo i dettagli descritti anche
            // nel file di configurazione
            Collection<IParameterDefnBase> params = task.getParameterDefns(true);
            String xPathString = null;

            // Iterate over each parameter
            for (IParameterDefnBase param : params) {
                IScalarParameterDefn scalar = (IScalarParameterDefn) param;
                Parameter p = null;
                Node n = null;

                switch (scalar.getDataType()) {
                    case IScalarParameterDefn.TYPE_INTEGER :
                        p = new IntegerParameter();
                        break;
                    case IScalarParameterDefn.TYPE_STRING :
                        p = new StringParameter();
                        break;
                    case IScalarParameterDefn.TYPE_FLOAT :
                        p = new FloatParameter();
                        break;
                    case IScalarParameterDefn.TYPE_DECIMAL :
                        p = new DecimalParameter();
                        break;
                    case IScalarParameterDefn.TYPE_DATE :
                        p = new DateParameter();
                        break;
                    case IScalarParameterDefn.TYPE_TIME :
                        p = new TimeParameter();
                        break;
                    case IScalarParameterDefn.TYPE_DATE_TIME :
                        p = new DateTimeParameter();
                        break;
                    case IScalarParameterDefn.TYPE_BOOLEAN :
                        p = new BooleanParameter();
                        break;
                    default :
                        p = new AnyParameter();
                        break;
                }
                paramNameList.add(scalar.getName());

                xPathString = "Parameters/Parameter[@name='" + scalar.getName() + "']";
                n = XMLConfig.getNode(node, xPathString);
                p.init(n, task, scalar, design);

                parameters.put(p.getName(), p);
            }

            System.out.println("Report [" + reportName + "]: " + parameters);
            isInitialized = true;
        }
        catch (BIRTException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new BIRTException("Error initializing BIRT Report", exc);
        }
    }

    public List<String> getParamsNames()
    {
        return paramNameList;
    }

    /**
     * @return
     */
    public String getName()
    {
        return reportName;
    }

    /**
     * @return the reportConfig
     */
    public String getReportConfig()
    {
        return this.reportConfig;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized()
    {
        return this.isInitialized;
    }

    /**
     * Ritorna una Map contenente i {@link Parameter} del {@link Report}
     * 
     * @return una Map contenente i {@link Parameter} che caratterizzano il
     *         report
     */
    public Map<String, Parameter> getParams()
    {
        return parameters;
    }

    public List<Parameter> getParamsList() throws Exception
    {
        List<Parameter> l = new ArrayList<Parameter>();

        for (String param : paramNameList) {
            l.add(parameters.get(param));
        }
        return l;
    }

    @Override
    public String toString()
    {
        if (isInitialized) {
            return "BIRTReport [" + reportName + "/" + reportConfig + "] - Parameters: " + parameters;
        }
        return "BIRTReport [" + reportName + " - Not Initialized]";
    }

    public synchronized void generate(OutputStream os, Map<String, Object> params, String type) throws BIRTException
    {
        try {
            Map<String, Object> lParams = params;
            if (lParams == null) {
                lParams = new HashMap<String, Object>();
            }
            ReportManager rm = ReportManager.instance();
            IRunAndRenderTask task = rm.getTask(reportConfig);

            if (!parameters.isEmpty()) {
                ExpressionEvaluatorHelper.startEvaluation();
                try {
                    ExpressionEvaluatorHelper.addToContext("params", lParams);
                    for (Parameter p : parameters.values()) {
                        if (p.isRequired() || lParams.containsKey(p.getName())) {
                            String expr = p.getExpression();
                            if (expr != null) {
                                ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE);
                                task.setParameterValue(p.getName(), expressionEvaluator.getValue(expr, params));
                            }
                            else {
                                task.setParameterValue(p.getName(), p.convertToValue("" + lParams.get(p.getName())));
                            }
                        }
                    }
                }
                finally {
                    ExpressionEvaluatorHelper.endEvaluation();
                }
                task.validateParameters();
            }

            if (type == null) {
                type = defaultRender;
            }

            ReportRenderOptions render = renders.get(type);
            if (render == null) {
                render = rm.getDefaultReportRender(type);
            }

            RenderOption opt = render.getOptions();
            opt.setOutputStream(os);

            task.setRenderOption(opt);
            task.run();
            task.close();
        }
        catch (Exception exc) {
            throw new BIRTException("Error generating BIRT Report [" + reportName + "]", exc);
        }
    }

    public byte[] generate(Map<String, Object> params, String type) throws BIRTException
    {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            generate(baos, params, type);
            baos.flush();
            return baos.toByteArray();
        }
        catch (Exception exc) {
            throw new BIRTException("Error generating BIRT Report [" + reportName + "]", exc);
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

}
