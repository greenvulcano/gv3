/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.core;

public class MaxException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -6951172109255390353L;
    private Exception         cause;

    public MaxException(String msg) {
        super(msg);
        cause = null;
    }

    public MaxException(Exception exc) {
        super(exc);

        cause = exc;
    }

    public MaxException(String msg, Exception exc) {
        super(msg, exc);

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
