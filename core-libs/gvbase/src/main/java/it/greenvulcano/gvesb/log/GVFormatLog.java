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
package it.greenvulcano.gvesb.log;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVFormatLog
{
    public final static String FMT_BEGIN                 = "BEGIN";
    public final static String FMT_END                   = "END";
    public final static String FMT_INPUT                 = "INPUT";
    public final static String FMT_OUTPUT                = "OUTPUT";
    public final static String FMT_OPERATION             = "Operation";
    public final static String FMT_SUBFLOW               = "Subflow";
    public final static String FMT_SYSTEM                = "System";
    public final static String FMT_SERVICE               = "Service";
    public final static String FMT_ID                    = "Id";
    public final static String FMT_RETCODE               = "RetCode";
    public final static String FMT_PLUG_IN               = "Perform External Call";
    protected final static int MSG_NONE                  = 0;
    protected final static int MSG_BEGIN1                = 1;
    protected final static int MSG_BEGIN2                = 2;
    protected final static int MSG_END                   = 3;
    protected final static int MSG_INPUT                 = 4;
    protected final static int MSG_OUTPUT                = 5;
    protected final static int MSG_BEGIN_PLUGIN          = 6;
    protected final static int MSG_END_PLUGIN            = 7;
    protected final static int MSG_BEGINOperation        = 8;
    protected final static int MSG_ENDOperation          = 9;
    protected final static int MSG_ENDOperationExc       = 10;
    protected final static int MSG_ENDOperationException = 11;
    protected final static int MSG_BEGINSubflow          = 12;
    protected final static int MSG_ENDSubflow            = 13;
    protected final static int MSG_ENDSubflowExc         = 14;
    protected final static int MSG_ENDSubflowException   = 15;

    protected int              type                      = MSG_NONE;
    protected String           operation                 = "";
    protected String           otherFieldName            = "";
    protected String           otherField                = "";
    protected String           result                    = "";
    protected String           moduleName                = "";
    protected boolean          forceDump                 = true;
    protected boolean          onlyData                  = false;
    protected GVBuffer         gvBuffer                  = null;
    protected GVException      gvException               = null;
    protected Throwable        exception                 = null;
    protected long             executionTime             = 0;

    /**
     * @param operation
     * @param gvBuffer
     * @return the formatted log
     */
    public static GVFormatLog formatBEGIN(String operation, GVBuffer gvBuffer)
    {
        return new GVFormatLog(true, operation, gvBuffer);
    }

    /**
     * @param otherFieldName
     * @param otherField
     * @param operation
     * @param result
     * @return the formatted log
     */
    public static GVFormatLog formatBEGIN(String otherFieldName, String otherField, String operation, String result)
    {
        return new GVFormatLog(otherFieldName, otherField, operation, result);
    }

    /**
     * @param operation
     * @param gvBuffer
     * @return the formatted log
     */
    public static GVFormatLog formatEND(String operation, GVBuffer gvBuffer)
    {
        return new GVFormatLog(false, operation, gvBuffer);
    }

    /**
     *
     * @param gvBuffer
     * @param forceDump
     * @param onlyData
     * @return the formatted log
     */
    public static GVFormatLog formatINPUT(GVBuffer gvBuffer, boolean forceDump, boolean onlyData)
    {
        return new GVFormatLog(true, gvBuffer, forceDump, onlyData);
    }

    /**
     *
     * @param gvBuffer
     * @param forceDump
     * @param onlyData
     * @return the formatted log
     */
    public static GVFormatLog formatOUTPUT(GVBuffer gvBuffer, boolean forceDump, boolean onlyData)
    {
        return new GVFormatLog(false, gvBuffer, forceDump, onlyData);
    }

    /**
     * @param moduleName
     * @param operation
     * @return the formatted log
     */
    public static GVFormatLog formatBEGINPlugin(String moduleName, String operation)
    {
        return new GVFormatLog(moduleName, operation);
    }

    /**
     * @param moduleName
     * @param operation
     * @param result
     * @return the formatted log
     */
    public static GVFormatLog formatENDPlugin(String moduleName, String operation, String result)
    {
        return new GVFormatLog(moduleName, operation, result);
    }

    /**
     * @param gvBuffer
     * @return the formatted log
     */
    public static GVFormatLog formatBEGINOperation(GVBuffer gvBuffer)
    {
        return new GVFormatLog(gvBuffer);
    }

    /**
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDOperation(long execTime)
    {
        return new GVFormatLog((GVBuffer) null, execTime);
    }

    /**
     * @param gvBuffer
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDOperation(GVBuffer gvBuffer, long execTime)
    {
        return new GVFormatLog(gvBuffer, execTime);
    }

    /**
     * @param exc
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDOperation(GVException exc, long execTime)
    {
        return new GVFormatLog(exc, execTime);
    }

    /**
     * @param exc
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDOperation(Throwable exc, long execTime)
    {
        return new GVFormatLog(exc, execTime);
    }



    /**
     * @param gvBuffer
     * @return the formatted log
     */
    public static GVFormatLog formatBEGINSubflow(String flowName, GVBuffer gvBuffer)
    {
        return new GVFormatLog(flowName, gvBuffer);
    }

    /**
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDSubflow(String flowName, long execTime)
    {
        return new GVFormatLog(flowName, (GVBuffer) null, execTime);
    }

    /**
     * @param gvBuffer
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDSubflow(String flowName, GVBuffer gvBuffer, long execTime)
    {
        return new GVFormatLog(flowName, gvBuffer, execTime);
    }

    /**
     * @param exc
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDSubflow(String flowName, GVException exc, long execTime)
    {
        return new GVFormatLog(flowName, exc, execTime);
    }

    /**
     * @param exc
     * @param execTime
     * @return the formatted log
     */
    public static GVFormatLog formatENDSubflow(String flowName, Throwable exc, long execTime)
    {
        return new GVFormatLog(flowName, exc, execTime);
    }

    /**
     * The constructor
     */
    protected GVFormatLog()
    {
        // do nothing
    }

    /**
     * To be used to format BEGIN Operation message.
     *
     * @param gvBuffer
     */
    protected GVFormatLog(GVBuffer gvBuffer)
    {
        this.gvBuffer = gvBuffer;
        this.type = MSG_BEGINOperation;
    }

    /**
     * To be used to format BEGIN Operation message.
     *
     * @param gvBuffer
     */
    protected GVFormatLog(String flowName, GVBuffer gvBuffer)
    {
        this.gvBuffer = gvBuffer;
        this.operation = flowName;
        this.type = MSG_BEGINSubflow;
    }

    /**
     * To be used to format END Operation message.
     *
     * @param gvBuffer
     * @param execTime
     */
    protected GVFormatLog(GVBuffer gvBuffer, long execTime)
    {
        this.gvBuffer = gvBuffer;
        this.executionTime = execTime;
        this.type = MSG_ENDOperation;
    }

    /**
     * To be used to format END Subflow message.
     *
     * @param gvBuffer
     * @param execTime
     */
    protected GVFormatLog(String flowName, GVBuffer gvBuffer, long execTime)
    {
        this.gvBuffer = gvBuffer;
        this.operation = flowName;
        this.executionTime = execTime;
        this.type = MSG_ENDSubflow;
    }

    /**
     * To be used to format END Operation message with exception.
     *
     * @param exc
     * @param execTime
     */
    protected GVFormatLog(GVException exc, long execTime)
    {
        this.gvException = exc;
        this.executionTime = execTime;
        this.type = MSG_ENDOperationExc;
    }

    /**
     * To be used to format END Subflow message with exception.
     *
     * @param exc
     * @param execTime
     */
    protected GVFormatLog(String flowName, GVException exc, long execTime)
    {
        this.gvException = exc;
        this.operation = flowName;
        this.executionTime = execTime;
        this.type = MSG_ENDSubflowExc;
    }

    /**
     * To be used to format END Operation message with exception.
     *
     * @param exc
     * @param execTime
     */
    protected GVFormatLog(Throwable exc, long execTime)
    {
        this.exception = exc;
        this.executionTime = execTime;
        this.type = MSG_ENDOperationException;
    }

    /**
     * To be used to format END Subflow message with exception.
     *
     * @param exc
     * @param execTime
     */
    protected GVFormatLog(String flowName, Throwable exc, long execTime)
    {
        this.exception = exc;
        this.operation = flowName;
        this.executionTime = execTime;
        this.type = MSG_ENDSubflowException;
    }

    /**
     * To be used to format BEGIN/END Operation message.
     *
     * @param isBegin
     * @param operation
     * @param gvBuffer
     */
    protected GVFormatLog(boolean isBegin, String operation, GVBuffer gvBuffer)
    {
        this.operation = operation;
        this.gvBuffer = gvBuffer;
        if (isBegin) {
            this.type = MSG_BEGIN1;
        }
        else {
            this.type = MSG_END;
        }
    }

    /**
     * To be used to format BEGIN Operation message.
     *
     * @param otherFieldName
     * @param otherField
     * @param operation
     * @param result
     */
    protected GVFormatLog(String otherFieldName, String otherField, String operation, String result)
    {
        this.otherFieldName = otherFieldName;
        this.otherField = otherField;
        this.operation = operation;
        this.result = result;
        this.type = MSG_BEGIN2;
    }

    /**
     * To be used to format INPUT/OUTPUT message.
     *
     * @param isInput
     * @param gvBuffer
     * @param forceDump
     * @param onlyData
     */
    protected GVFormatLog(boolean isInput, GVBuffer gvBuffer, boolean forceDump, boolean onlyData)
    {
        this.gvBuffer = gvBuffer;
        this.forceDump = forceDump;
        this.onlyData = onlyData;
        if (isInput) {
            this.type = MSG_INPUT;
        }
        else {
            this.type = MSG_OUTPUT;
        }
    }

    /**
     * To be used to format BEGIN plug-in message.
     *
     * @param moduleName
     * @param operation
     */
    protected GVFormatLog(String moduleName, String operation)
    {
        this.moduleName = moduleName;
        this.operation = operation;
        this.type = MSG_BEGIN_PLUGIN;
    }

    /**
     * To be used to format END plug-in message.
     *
     * @param moduleName
     * @param operation
     * @param result
     */
    protected GVFormatLog(String moduleName, String operation, String result)
    {
        this.moduleName = moduleName;
        this.operation = operation;
        this.result = result;
        this.type = MSG_END_PLUGIN;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        switch (this.type) {
            case MSG_BEGIN1 :
                return formatBEGIN1();
            case MSG_BEGIN2 :
                return formatBEGIN2();
            case MSG_END :
                return formatEND();
            case MSG_INPUT :
                return formatINPUT();
            case MSG_OUTPUT :
                return formatOUTPUT();
            case MSG_BEGIN_PLUGIN :
                return formatBEGINPlugin();
            case MSG_END_PLUGIN :
                return formatENDPlugin();
            case MSG_BEGINOperation :
                return formatBEGINOperation();
            case MSG_ENDOperation :
                return formatENDOperation();
            case MSG_ENDOperationExc :
                return formatENDOperationGVExc();
            case MSG_ENDOperationException :
                return formatENDOperationExc();
            case MSG_BEGINSubflow :
                return formatBEGINSubflow();
            case MSG_ENDSubflow :
                return formatENDSubflow();
            case MSG_ENDSubflowExc :
                return formatENDSubflowGVExc();
            case MSG_ENDSubflowException :
                return formatENDSubflowExc();
            default :
                return "Invalid log context";
        }
    }

    /**
     * @return the formatted begin string
     */
    protected String formatBEGIN1()
    {
        StringBuilder message = new StringBuilder(FMT_BEGIN);

        if (this.gvBuffer == null) {
            message.append(" null GVBuffer");
            return message.toString();
        }

        message.append(" - ").append(FMT_OPERATION).append("(").append(this.operation).append(")");
        message.append(" - ").append(FMT_SYSTEM).append("(").append(this.gvBuffer.getSystem()).append(")");
        message.append(" - ").append(FMT_SERVICE).append("(").append(this.gvBuffer.getService()).append(")");
        message.append(" - ").append(FMT_ID).append("(").append(this.gvBuffer.getId().toString()).append(")");
        message.append(" - ").append(FMT_RETCODE).append("(").append(this.gvBuffer.getRetCode()).append(")");

        return message.toString();
    }

    /**
     * @return the formatted begin string
     */
    protected String formatBEGIN2()
    {
        StringBuilder message = new StringBuilder(FMT_BEGIN);

        message.append(" - ").append(this.otherFieldName).append("(").append(this.otherField).append(")");
        message.append(" - ").append(FMT_OPERATION).append("(").append(this.operation).append(") ");
        message.append(" : ").append(this.result);
        return message.toString();
    }

    /**
     * @return the formatted end string
     */
    protected String formatEND()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        if (this.gvBuffer == null) {
            message.append(" null GVBuffer");
            return message.toString();
        }

        message.append(" - ").append(FMT_OPERATION).append("(").append(this.operation).append(")");
        message.append(" - ").append(FMT_SYSTEM).append("(").append(this.gvBuffer.getSystem()).append(")");
        message.append(" - ").append(FMT_SERVICE).append("(").append(this.gvBuffer.getService()).append(")");
        message.append(" - ").append(FMT_ID).append("(").append(this.gvBuffer.getId().toString()).append(")");
        message.append(" - ").append(FMT_RETCODE).append("(").append(this.gvBuffer.getRetCode()).append(")");

        return message.toString();
    }

    /**
     * @return the formatted input string
     */
    protected String formatINPUT()
    {
        StringBuilder message = new StringBuilder(FMT_INPUT);

        if (this.gvBuffer == null) {
            message.append(" null GVBuffer");
            return message.toString();
        }

        message.append(" - GVBuffer: \n");
        message.append(new GVBufferDump(this.gvBuffer, this.forceDump, this.onlyData));

        return message.toString();
    }

    /**
     * @return the formatted output string
     */
    protected String formatOUTPUT()
    {
        StringBuilder message = new StringBuilder(FMT_OUTPUT);

        if (this.gvBuffer == null) {
            message.append(" null GVBuffer");
            return message.toString();
        }

        message.append(" - GVBuffer: \n");
        message.append(new GVBufferDump(this.gvBuffer, this.forceDump, this.onlyData));

        return message.toString();
    }

    /**
     * @return the formatted begin plug-in string
     */
    protected String formatBEGINPlugin()
    {
        StringBuilder message = new StringBuilder(FMT_BEGIN);

        message.append(" - ").append(FMT_PLUG_IN).append("(").append(this.moduleName).append(")");
        message.append(" - ").append(FMT_OPERATION).append("(").append(this.operation).append(")");

        return message.toString();
    }

    /**
     * @return the formatted end plug-in string
     */
    protected String formatENDPlugin()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        message.append(" - ").append(FMT_PLUG_IN).append("(").append(this.moduleName).append(")");
        message.append(" - ").append(FMT_OPERATION).append("(").append(this.operation).append(")");
        message.append(" : ").append(this.result);

        return message.toString();
    }

    /**
     * @return the formatted begin operation string
     */
    protected String formatBEGINOperation()
    {
        StringBuilder message = new StringBuilder(FMT_BEGIN);

        message.append(" Operation - RetCode (").append(this.gvBuffer.getRetCode()).append(")");

        return message.toString();
    }

    /**
     * @return the formatted end operation string
     */
    protected String formatENDOperation()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        if (this.gvBuffer != null) {
            message.append(" Operation - RetCode (").append(this.gvBuffer.getRetCode()).append(") - ExecutionTime (").append(
                    this.executionTime).append(")");
        }
        else {
            message.append(" Operation - RetCode (0) - ExecutionTime (").append(this.executionTime).append(")");
        }

        return message.toString();
    }

    /**
     * @return the formatted end operation string with GV exception
     */
    protected String formatENDOperationGVExc()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        message.append(" Operation - RetCode (").append(this.gvException.getErrorCode()).append(") - ExecutionTime (").append(
                this.executionTime).append(") - Error performing service call: ").append(this.gvException);

        return message.toString();
    }

    /**
     * @return the formatted end operation string with exception
     */
    protected String formatENDOperationExc()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        message.append(" Operation - RetCode (-1) - ExecutionTime (").append(this.executionTime).append(
                ") - Error performing service call: ").append(this.exception);

        return message.toString();
    }

    /**
     * @return the formatted begin subflow string
     */
    protected String formatBEGINSubflow()
    {
        StringBuilder message = new StringBuilder(FMT_BEGIN);

        message.append(" Subflow").append("(").append(this.operation).append(")").append(" - RetCode (").append(this.gvBuffer.getRetCode()).append(")");

        return message.toString();
    }

    /**
     * @return the formatted end subflow string
     */
    protected String formatENDSubflow()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        if (this.gvBuffer != null) {
            message.append(" Subflow").append("(").append(this.operation).append(")").append(" - RetCode (").append(this.gvBuffer.getRetCode())
                .append(") - ExecutionTime (").append(this.executionTime).append(")");
        }
        else {
            message.append(" Subflow").append("(").append(this.operation).append(")").append(" - RetCode (0) - ExecutionTime (").append(this.executionTime).append(")");
        }

        return message.toString();
    }

    /**
     * @return the formatted end subflow string with GV exception
     */
    protected String formatENDSubflowGVExc()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        message.append(" Subflow").append("(").append(this.operation).append(")").append(" - RetCode (").append(this.gvException.getErrorCode())
            .append(") - ExecutionTime (").append(this.executionTime).append(") - Error performing service call: ").append(this.gvException);

        return message.toString();
    }

    /**
     * @return the formatted end subflow string with exception
     */
    protected String formatENDSubflowExc()
    {
        StringBuilder message = new StringBuilder(FMT_END);

        message.append(" Subflow").append("(").append(this.operation).append(")").append(" - RetCode (-1) - ExecutionTime (")
            .append(this.executionTime).append(") - Error performing service call: ").append(this.exception);

        return message.toString();
    }

}
