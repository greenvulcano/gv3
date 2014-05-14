/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.apache.commons.fileupload;

import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

/**
 * @version 3.2.0 24/ott/2012
 * @author GreenVulcano Developer Team
 */
public class MultipartParser
{
    public static class PartDescriptor
    {
        private String contentType             = null;
        private String contentTransferEncoding = null;
        private String contentID               = null;
        private String contentDisposition      = null;
        private byte[] data                    = null;

        /**
         * @return the contentType
         */
        public String getContentType()
        {
            return this.contentType;
        }

        /**
         * @param contentType
         *        the contentType to set
         */
        public void setContentType(String contentType)
        {
            this.contentType = contentType;
        }

        /**
         * @return the contentTransferEncoding
         */
        public String getContentTransferEncoding()
        {
            return this.contentTransferEncoding;
        }

        /**
         * @param contentTransferEncoding
         *        the contentTransferEncoding to set
         */
        public void setContentTransferEncoding(String contentTransferEncoding)
        {
            this.contentTransferEncoding = contentTransferEncoding;
        }

        /**
         * @return the contentID
         */
        public String getContentID()
        {
            return this.contentID;
        }

        /**
         * @param contentID
         *        the contentID to set
         */
        public void setContentID(String contentID)
        {
            this.contentID = contentID;
        }

        /**
         * @return the contentDisposition
         */
        public String getContentDisposition()
        {
            return this.contentDisposition;
        }

        /**
         * @param contentDisposition
         *        the contentDisposition to set
         */
        public void setContentDisposition(String contentDisposition)
        {
            this.contentDisposition = contentDisposition;
        }

        /**
         * @return the data
         */
        public byte[] getData()
        {
            return this.data;
        }

        /**
         * @param data
         *        the data to set
         */
        public void setData(byte[] data)
        {
            this.data = data;
        }

        @Override
        public String toString()
        {
            return "Content-Type: " + contentType + "\nContent-Transfer-Encoding: " + contentTransferEncoding
                    + "\nContent-ID: " + contentID + "\nContent-Disposition: " + contentDisposition;
        }
    }

    public static List<PartDescriptor> parseToPartList(String message) throws MalformedStreamException, IOException
    {
        List<PartDescriptor> result = new ArrayList<PartDescriptor>();

        int idxB = message.indexOf("--");
        int idxCR = message.indexOf("\r", idxB + 1);
        String boundary = message.substring(idxB + 2, idxCR);
        InputStream input = new ByteArrayInputStream(message.getBytes());

        MultipartStream.ProgressNotifier pNotifier = new MultipartStream.ProgressNotifier(new ProgressListener() {
            @Override
            public void update(long pBytesRead, long pContentLength, int pItems)
            {
                // do nothing
            }
        }, 1);
        MultipartStream multipartStream = new MultipartStream(input, boundary.getBytes(), pNotifier);
        boolean nextPart = multipartStream.skipPreamble();
        ByteArrayOutputStream output = null;
        System.out.println("------ START MULTIPART MESSAGE ------------");
        while (nextPart) {
            PartDescriptor pd = new PartDescriptor();
            String header = multipartStream.readHeaders();

            // process headers
            output = new ByteArrayOutputStream();
            // create some output stream
            multipartStream.readBodyData(output);
            pd.setData(output.toByteArray());
            result.add(pd);
            System.out.println("------------------");
            System.out.println(output.toString());
            System.out.println("------------------");
            nextPart = multipartStream.readBoundary();
        }
        System.out.println("------ END   MULTIPART MESSAGE ------------");

        return result;
    }


    public static SOAPMessage parseToSOAPMessage(String message) throws MalformedStreamException, IOException,
            SOAPException, XMLUtilsException
    {
        int idxB = message.indexOf("--");
        int idxCR = message.indexOf("\r", idxB + 2);
        String boundary = message.substring(idxB + 2, idxCR);
        int idxF = message.indexOf("Content-Id: <");
        int idxFe = message.indexOf(">", idxF + 13);
        String first = message.substring(idxF + 13, idxFe);

        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders hdrs = new MimeHeaders();
        hdrs.addHeader("MIME-Version", "1.0");
        hdrs.addHeader("Content-Type", "multipart/related; type=\"text/xml\"; start=\"<" + first + ">\"; boundary=\""
                + boundary + "\"");
        SOAPMessage soapM = messageFactory.createMessage(hdrs, new ByteArrayInputStream(message.getBytes()));
        System.out.println("------ START MULTIPART MESSAGE ------------");
        System.out.println(XMLUtils.serializeDOM_S(soapM.getSOAPPart().getEnvelope()));
        System.out.println("countAttachments: " + soapM.countAttachments());
        System.out.println("------ END   MULTIPART MESSAGE ------------");

        return soapM;
    }
}
