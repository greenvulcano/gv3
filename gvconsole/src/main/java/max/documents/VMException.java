/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.documents;

/**
 * @deprecated replaced with MaxException
 * @see max.core.MaxException
 */
@Deprecated
public class VMException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 2641697956275654885L;
    private Exception         cause;

    public VMException(String msg) {
        super(msg);
        cause = null;
    }

    public VMException(Exception exc) {
        cause = exc;
    }

    public VMException(String msg, Exception exc) {
        super(msg);
        cause = exc;
    }

    public Exception getNestedException() {
        return cause;
    }

    @Override
    public String toString() {
        String msg = super.toString();
        if (cause == null) {
            return msg;
        }
        return msg + ". Cause: " + cause;
    }
}