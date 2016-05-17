/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.expression.ExpressionEvaluator;
import it.greenvulcano.expression.ExpressionEvaluatorException;
import it.greenvulcano.expression.ExpressionEvaluatorHelper;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.js.initializer.JSInit;
import it.greenvulcano.js.initializer.JSInitManager;
import it.greenvulcano.js.util.JavaScriptHelper;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 21/nov/2013
 * @author GreenVulcano Developer Team
 * 
 */
public class ResultProcessor
{
    public enum ProcessorInput {
        PROCESS_ONLY_OBJECT("Process Only Object"), PROCESS_OBJECT_ERROR("Process Object and Error"), PROCESS_ONLY_GVBUFFER(
                "Process Only GVBuffer"), PROCESS_GVBUFFER_ERROR("Process GVBuffer and Error"), PROCESS_NOTHING("Process nothing");

        private String desc;

        private ProcessorInput(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        public static ProcessorInput fromString(String name) {
            if ((name == null) || "".equals(name)) {
                return null;
            }
            if ("only-object".equals(name)) {
                return PROCESS_ONLY_OBJECT;
            }
            if ("only-gvbuffer".equals(name)) {
                return PROCESS_ONLY_GVBUFFER;
            }
            if ("object-and-error".equals(name)) {
                return PROCESS_OBJECT_ERROR;
            }
            if ("gvbuffer-and-error".equals(name)) {
                return PROCESS_GVBUFFER_ERROR;
            }
            if ("nothing".equals(name)) {
                return PROCESS_NOTHING;
            }
            return null;
        }
    }

    private static final Logger logger             = GVLogger.getLogger(ResultProcessor.class);

    private ProcessorInput      processorInput     = null;
    private boolean             failOnError        = true;

    // XMLAggregate
    private String              aggregateRoot      = null;
    private String              aggregateNamespace = null;

    // OGNL
    private String              ognlScript         = null;

    // JavaScript
    private String              javaScript         = null;
    private String              jsScope            = null;

    public void init(Node node) throws GVCoreConfException {
        processorInput = ProcessorInput.fromString(XMLConfig.get(node, "@processor-input", ""));
        if (processorInput == null) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'processor-input'"}, {"node", XPathFinder.buildXPath(node)}});
        }

        failOnError = XMLConfig.getBoolean(node, "@fail-on-error", true);

        // XMLAggregate
        aggregateRoot = XMLConfig.get(node, "XMLAggregate/@root", null);
        aggregateNamespace = XMLConfig.get(node, "XMLAggregate/@namespace", null);
        // OGNL
        ognlScript = XMLConfig.get(node, "OGNLScript", null);
        // JavaScript
        javaScript = XMLConfig.get(node, "JavaScript", null);
        jsScope = XMLConfig.get(node, "JavaScript/@scope-name", "gvesb");


        if (processorInput == ProcessorInput.PROCESS_NOTHING) {
        	if ((aggregateRoot != null) || (ognlScript != null) || (javaScript != null)) {
        		throw new GVCoreConfException("GVCORE_BAD_CFG_ERROR", new String[][]{
                        {"name", "'processor-input'"}, {"message", "If processor-input=nothing cannot be configured a specific output processor"}});
        	}
        }
        
        logger.debug("Configured " + toString());
    }

    public GVBuffer process(GVBuffer input, List<Result> results) throws GVCoreException, InterruptedException {
        if ((aggregateRoot != null) && !"".equals(aggregateRoot)) {
            logger.debug("Using XMLAggregate Processor: root[" + aggregateNamespace + ":" + aggregateRoot + "]");
            return xmlProcessor(input, results);
        }
        if ((ognlScript != null) && !"".equals(ognlScript)) {
            logger.debug("Using OGNL Processor:\n" + ognlScript);
            return ognlProcessor(input, results);
        }
        if ((javaScript != null) && !"".equals(javaScript)) {
            logger.debug("Using JavaScript Processor:\n" + javaScript);
            return jsProcessor(input, results);
        }
        logger.debug("Using Default Processor");
        return defaultProcessor(input, results);
    }

    public boolean needsOutput() {
    	return processorInput != ProcessorInput.PROCESS_NOTHING;
    }

    @Override
    public String toString() {
        String desc = "ResultProcessor: failOnError[" + failOnError + "] - processorInput[" + processorInput + "]";
        if ((aggregateRoot != null) && !"".equals(aggregateRoot)) {
            desc += " - Use XMLAggregate Processor: root[" + aggregateNamespace + ":" + aggregateRoot + "]";
        }
        else if ((ognlScript != null) && !"".equals(ognlScript)) {
            desc += " - Use OGNL Processor:\n" + ognlScript;
        }
        else if ((javaScript != null) && !"".equals(javaScript)) {
            desc += " - Use JavaScript Processor:\n" + javaScript;
        }
        else {
            desc += " - Use Default Processor" + (processorInput == ProcessorInput.PROCESS_NOTHING ? " (No output)" : "");
        }
        return desc;
    }

    private GVBuffer defaultProcessor(GVBuffer input, List<Result> results) throws GVCoreException,
            InterruptedException {
        List<Object> toProcess = new ArrayList<Object>();

        Iterator<Result> itInput = results.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            Object d = currOutput.getOutput();
            if (failOnError && (currOutput.getState() != Result.State.STATE_OK)) {
                throw new GVCoreException("GVCORE_PARALLEL_EXEC_ERROR", new String[][]{{"message", "" + d}},
                        (Throwable) d);
            }
            switch (processorInput) {
                case PROCESS_GVBUFFER_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                case PROCESS_ONLY_GVBUFFER :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                    break;
                case PROCESS_OBJECT_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                case PROCESS_ONLY_OBJECT :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(((GVBuffer) d).getObject());
                        }
                    }
                    break;
                case PROCESS_NOTHING :
                    // do nothing
                    break;
            }
        }
        try {
            input.setObject(toProcess);
        }
        catch (Exception exc) {
            // do nothing
        }
        return input;
    }

    private GVBuffer xmlProcessor(GVBuffer input, List<Result> results) throws GVCoreException, InterruptedException {
        List<Object> toProcess = new ArrayList<Object>();

        Iterator<Result> itInput = results.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            Object d = currOutput.getOutput();
            if (failOnError && (currOutput.getState() != Result.State.STATE_OK)) {
                throw new GVCoreException("GVCORE_PARALLEL_EXEC_ERROR", new String[][]{{"message", "" + d}},
                        (Throwable) d);
            }
            if (currOutput.getState() == Result.State.STATE_OK) {
                if (d != null) {
                    toProcess.add(((GVBuffer) d).getObject());
                }
            }
        }
        try {
            input.setObject(XMLUtils.aggregateXML_S(aggregateRoot, aggregateNamespace, toProcess.toArray()));
            return input;
        }
        catch (Exception exc) {
            throw new GVCoreException("GVCORE_PARALLEL_XML_AGGREGATE_ERROR", new String[][]{{"message", "" + exc}}, exc);
        }
    }

    private GVBuffer ognlProcessor(GVBuffer input, List<Result> results) throws GVCoreException, InterruptedException {
        List<Object> toProcess = new ArrayList<Object>();

        Iterator<Result> itInput = results.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            Object d = currOutput.getOutput();
            if (failOnError && (currOutput.getState() != Result.State.STATE_OK)) {
                throw new GVCoreException("GVCORE_PARALLEL_EXEC_ERROR", new String[][]{{"message", "" + d}},
                        (Throwable) d);
            }
            switch (processorInput) {
                case PROCESS_GVBUFFER_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        toProcess.add(currOutput);
                    }
                case PROCESS_ONLY_GVBUFFER :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        toProcess.add(currOutput);
                    }
                    break;
                case PROCESS_OBJECT_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                case PROCESS_ONLY_OBJECT :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(((GVBuffer) d).getObject());
                        }
                    }
                    break;
            }
        }

        ExpressionEvaluatorHelper.startEvaluation();
        try {
            ExpressionEvaluatorHelper.addToContext("results", toProcess);
            ExpressionEvaluatorHelper.addToContext("data", input);
            ExpressionEvaluator expressionEvaluator = ExpressionEvaluatorHelper.getExpressionEvaluator(ExpressionEvaluatorHelper.OGNL_EXPRESSION_LANGUAGE);
            expressionEvaluator.getValue(ognlScript, input);
        }
        catch (ExpressionEvaluatorException exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new GVCoreException("GVCORE_PARALLEL_OGNL_AGGREGATE_ERROR", new String[][]{{"message", "" + exc}},
                    exc);
        }
        finally {
            ExpressionEvaluatorHelper.endEvaluation();
        }
        return input;
    }

    private GVBuffer jsProcessor(GVBuffer input, List<Result> results) throws GVCoreException, InterruptedException {
        List<Object> toProcess = new ArrayList<Object>();

        Iterator<Result> itInput = results.iterator();
        while (itInput.hasNext()) {
            Result currOutput = itInput.next();
            Object d = currOutput.getOutput();
            if (failOnError && (currOutput.getState() != Result.State.STATE_OK)) {
                throw new GVCoreException("GVCORE_PARALLEL_EXEC_ERROR", new String[][]{{"message", "" + d}},
                        (Throwable) d);
            }
            switch (processorInput) {
                case PROCESS_GVBUFFER_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        toProcess.add(currOutput);
                    }
                case PROCESS_ONLY_GVBUFFER :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        toProcess.add(currOutput);
                    }
                    break;
                case PROCESS_OBJECT_ERROR :
                    if (currOutput.getState() != Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(d);
                        }
                    }
                case PROCESS_ONLY_OBJECT :
                    if (currOutput.getState() == Result.State.STATE_OK) {
                        if (d != null) {
                            toProcess.add(((GVBuffer) d).getObject());
                        }
                    }
                    break;
            }
        }

        Context cx = ContextFactory.getGlobal().enterContext();
        Scriptable scope = null;
        try {
            scope = JSInitManager.instance().getJSInit(jsScope).getScope();
            scope = JSInit.setProperty(scope, "results", toProcess);
            scope = JSInit.setProperty(scope, "data", input);
            scope = JSInit.setProperty(scope, "logger", logger);
            JavaScriptHelper.executeScript(javaScript, "ResultProcessor", scope, cx);
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            throw new GVCoreException("GVCORE_PARALLEL_JS_AGGREGATE_ERROR", new String[][]{{"message", "" + exc}}, exc);
        }
        return input;
    }
}
