/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *

 */
package max.taglib;

import it.greenvulcano.configuration.XMLConfigException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import max.core.ContentProvider;
import max.core.Contents;
import max.core.MaxException;

public class ContentTag extends TagSupport {
    /**
     *
     */
    private static final long serialVersionUID = -1390631439691963171L;
    private String            id               = null;

    @Override
    public void setId(String val) {
        id = val;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            LoopTag loop = LoopTag.findLoopTag(this, id);
            if (loop == null) {
                return SKIP_BODY;
            }

            String providerName = "" + loop.getField(ContentProvider.ATTR_PROVIDER);
            String category = "" + loop.getField(ContentProvider.ATTR_CATEGORY);
            String contentName = "" + loop.getField(ContentProvider.ATTR_NAME);

            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

            Contents contents = Contents.instance();
            ContentProvider provider = contents.getProvider(providerName);
            if (provider != null) {
                InputStream in = provider.get(category, contentName);
                InputStreamReader reader = new InputStreamReader(new BufferedInputStream(in, 4096));
                JspWriter out = pageContext.getOut();
                char buf[] = new char[4096];
                int l;
                while ((l = reader.read(buf, 0, 4096)) != -1) {
                    out.write(buf, 0, l);
                }
                reader.close();
            }
            return SKIP_BODY;
        }
        catch (IOException exc) {
            throw new JspException(exc);
        }
        catch (SQLException exc) {
            throw new JspException(exc);
        }
        catch (MaxException exc) {
            throw new JspException(exc);
        }
        catch (XMLConfigException exc) {
            throw new JspException(exc);
        }
    }
}
