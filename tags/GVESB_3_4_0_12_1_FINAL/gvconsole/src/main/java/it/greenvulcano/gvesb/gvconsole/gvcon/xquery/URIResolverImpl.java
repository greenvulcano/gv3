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
package it.greenvulcano.gvesb.gvconsole.gvcon.xquery;

import it.greenvulcano.configuration.XMLConfigException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;

import max.documents.DocumentDescriptor;
import max.documents.DocumentRepository;
import max.documents.HttpServletRequestRoleCheck;
import max.documents.RoleCheck;

import org.w3c.dom.Document;

/**
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class URIResolverImpl implements URIResolver
{
    private DocumentRepository repository = null;
    private HttpServletRequest request;

    public URIResolverImpl() throws XMLConfigException
    {
        repository = DocumentRepository.instance();
    }

    /**
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
     *      java.lang.String)
     */
    public Source resolve(String href, String base) throws TransformerException
    {
        try {
            Document doc = getDocument(href);
            DOMSource source = new DOMSource(doc);
            return source;
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw new TransformerException(exc);
        }
    }

    /**
     * @param request
     */
    public void setRequest(HttpServletRequest request)
    {
        this.request = request;
    }

    private Document getDocument(String docName) throws Exception
    {
        HttpServletRequestRoleCheck roleCheck = new HttpServletRequestRoleCheck(request);
        DocumentDescriptor documentDescriptor = repository.getDocumentDescriptor(docName);
        Document document = null;
        if (checkRoles(documentDescriptor, roleCheck)) {
            document = repository.getDocument(docName);
        }
        else {
            throw new TransformerException("Utente non autorizzato");
        }
        return document;
    }

    private boolean checkRoles(DocumentDescriptor documentDescriptor, RoleCheck roleCheck)
    {
        if (roleCheck.isUserInSomeRole(documentDescriptor.getReadWriteRoles())) {
            return true;
        }
        if (roleCheck.isUserInSomeRole(documentDescriptor.getReadOnlyRoles())) {
            return true;
        }
        if (roleCheck.isUserInSomeRole(documentDescriptor.getExternalSystemRoles())) {
            return true;
        }
        return false;
    }
}
