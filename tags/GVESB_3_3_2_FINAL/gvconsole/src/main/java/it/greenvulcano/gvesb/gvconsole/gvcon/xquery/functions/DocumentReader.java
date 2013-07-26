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
package it.greenvulcano.gvesb.gvconsole.gvcon.xquery.functions;

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvconsole.gvcon.xquery.XQueryProcessor;
import max.core.MaxException;
import max.documents.DocumentRepository;
import net.sf.saxon.dom.DocumentWrapper;

import org.w3c.dom.Document;

/**
 * Classe di estensione delle funzioni XQuery. L'invocazione dei metodi da
 * XQuery viene effettuata utilizzando il reale nome del metodo anteponendo il
 * prefix o l'uri del namespace associato alla classe, es:
 * max:document("nome doc").
 *
 * @version 3.0.0 Apr 6, 2010
 * @author GreenVulcano Developer Team
 */
public class DocumentReader
{
    /**
     * Funzione che carica un documento XML mediante DocumentRepository. Il
     * metodo restituisce il root node del documento richiesto. I possibili doc
     * caricabili sono tutti quelli registrati sul DocumentRepository
     * specificando il nome simbolico con il quale e' stato registrato.
     *
     * @param documentName
     *        nome del doc da caricare
     * @return the <code>DocumentWrapper</code>
     */
    public static DocumentWrapper document(String documentName)
    {
        try {
            DocumentRepository repository = DocumentRepository.instance();
            Document doc = repository.getDocument(documentName);
            return new DocumentWrapper(doc, "", XQueryProcessor.getConfiguration());
        }
        catch (MaxException e) {
            e.printStackTrace();
            return null;
        }
        catch (XMLConfigException exc) {
            exc.printStackTrace();
            return null;
        }
    }
}
