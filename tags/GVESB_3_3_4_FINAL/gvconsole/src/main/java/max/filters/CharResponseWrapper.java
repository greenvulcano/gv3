/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.filters;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Bufferizza una risposta che utilizzi getWriter() per poter elaborarla
 * successivamente.
 */
public class CharResponseWrapper extends HttpServletResponseWrapper {
    private CharArrayWriter output;
    private PrintWriter     writer;

    @Override
    public String toString() {
        return output.toString();
    }

    public CharResponseWrapper(HttpServletResponse response) {
        super(response);

        output = new CharArrayWriter();
        writer = new PrintWriter(output);
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        writer.flush();
    }
}
