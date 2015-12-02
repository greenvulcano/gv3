/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2004 - All right reserved
 */
public abstract class MaxBodyTagSupport extends BodyTagSupport {
    private static final boolean VERBOSE = false;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS
    //----------------------------------------------------------------------------------------------

    /**
     * Invoca il metodo reset
     */
    public MaxBodyTagSupport() {
        super();
        reset();
    }

    //----------------------------------------------------------------------------------------------
    // FINAL METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Invoca startTag(). Se startTag() ritorna true, allora valuta il corpo,
     * altrimenti salta il corpo.
     *
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public final int doStartTag() throws JspException {
        log("doStartTag, calling startTag()...");
        if (startTag()) {
            log("...evaluating body.");
            return EVAL_BODY_BUFFERED;
        }
        else {
            log("...skipping body.");
            return SKIP_BODY;
        }
    }

    /**
     * Invoca initBody().
     *
     * @see javax.servlet.jsp.tagext.BodyTag#doInitBody()
     */
    @Override
    public final void doInitBody() throws JspException {
        log("doInitBody, calling super.doInitBody()");
        super.doInitBody();
        log("doInitBody, calling initBody()");
        initBody();
    }

    /**
     * Invoca afterBody(). Se afterBody() ritorna true, scrive il contenuto
     * del buffer sulla JSP, resetta il buffer e ripete il body.
     * Se afterBody() ritorna false, non esegua alcuna operazione e termina
     * l'esecuzione del tag. In questo caso ci penser� doEndTag a scrivere
     * sulla JSP.
     *
     * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
     */
    @Override
    public final int doAfterBody() throws JspException {
        log("doAfterBody, calling afterBody()");
        boolean mustRepeat = afterBody();
        if (mustRepeat) {
            writeOut();
            log("doAfterBody, avluate again");
            return EVAL_BODY_BUFFERED;
        }
        else {
            log("doAfterBody, stop evaluating body");
            return SKIP_BODY;
        }
    }

    /**
     * Invoca endTag().
     * Se sul bodyContent ci sono informazioni le invia alla JSP e ripulisce il
     * buffer.
     * Invoca reset().
     * Se endTag() ritorna true, allora continua la valutazione della pagina,
     * altrimenti causa il completamento della pagina contenente questo tag.
     *
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public final int doEndTag() throws JspException {
        boolean mustContinue = true;
        try {
            try {
                log("doEndTag, calling endTag()");
                mustContinue = endTag();
            }
            catch (JspException exc) {
                log("doEndTag, clearing bodyContent due exception: " + exc);
                bodyContent.clearBody();
                throw exc;
            }
            writeOut();
        }
        finally {
            log("doEndTag, resetting tag with reset()");
            reset();
        }

        log("doEndTag, continue evaluating page? " + mustContinue);

        return mustContinue ? EVAL_PAGE : SKIP_PAGE;
    }

    //----------------------------------------------------------------------------------------------
    // ABSTRACT METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Reinizializza i campi del tag come se il tag fosse stato appena
     * costruito. Non si deve preoccupare di resettare il bodyContent.
     */
    protected abstract void reset();

    /**
     * Invocato a seguito di doStartTag()
     * @return true se si deve valutare il corpo del tag, false altrimenti
     * @throws JspException
     */
    protected abstract boolean startTag() throws JspException;

    /**
     * Invocato a seguito di doInitBody()
     * @throws JspException
     */
    protected abstract void initBody() throws JspException;

    /**
     * Invocato a seguito di doAfterBody().
     * @return return true se deve valutare ancora il corpo del tag, false altrimenti
     * @throws JspException
     */
    protected abstract boolean afterBody() throws JspException;

    /**
     * Invocato a seguito di doEndTag()
     * @return true se deve continuare la valutazione della pagina, false altrimenti
     * @throws JspException
     */
    protected abstract boolean endTag() throws JspException;

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Scrive il bodyContent sulla JSP e resetta il bodyContent.
     */
    private void writeOut() throws JspException {
        log("writing out the body content to the JSP");
        if (bodyContent != null) {
            try {
                JspWriter out = bodyContent.getEnclosingWriter();
                bodyContent.writeOut(out);
            }
            catch (IOException exc) {
                log("exception: " + exc);
                throw new JspException("" + exc, exc);
            }
            finally {
                log("clearing bodyContent");
                bodyContent.clearBody();
            }
        }
    }

    private void log(Object log) {
        if (VERBOSE) {
            System.out.println("TAG: " + this + ": " + log);
        }
    }
}