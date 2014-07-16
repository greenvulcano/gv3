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
package it.greenvulcano.gvesb.gvconsole.savepoint;

import it.greenvulcano.gvesb.core.savepoint.SavePointController;
import it.greenvulcano.gvesb.datahandling.utils.dao.DataAccessObject;
import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.LookupDispatchAction;

/**
 *
 * @version 3.1.0 27 Feb 2011
 * @author GreenVulcano Developer Team
 */
public class SavePointAction extends LookupDispatchAction
{
    private static final Logger logger = GVLogger.getLogger(SavePointAction.class);

    private HashMap keyMethodMap = new HashMap();

    /**
     *
     */
    public SavePointAction()
    {
        keyMethodMap.put("savepoint.listSavePoint", "listSavePoint");
        keyMethodMap.put("savepoint.recoverSavePoint", "recoverSavePoint");
        keyMethodMap.put("savepoint.deleteSavePoint", "deleteSavePoint");
    }

    /* (non-Javadoc)
     * @see org.apache.struts.actions.LookupDispatchAction#getKeyMethodMap()
     */
    @Override
    protected Map getKeyMethodMap()
    {
        return keyMethodMap;
    }


    /* (non-Javadoc)
     * @see org.apache.struts.actions.LookupDispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward listSavePoint(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        DynaActionForm frm = (DynaActionForm) form;

        Map<String, String> params = new HashMap<String, String>();
        String id = frm.getString("id");
        String svc = frm.getString("service");
        String date = frm.getString("date");
        params.put("SERVICE", ((svc == null) || "".equals(svc)) ? "NULL" : svc);
        params.put("ID", ((id == null) || "".equals(id)) ? "NULL" : id);
        params.put("DATE", ((date == null) || "".equals(date)) ? "NULL" : date);

        byte[] list = null;

        try {
            list = DataAccessObject.getDataAsBytes("ListRecoveryPoint", params);
        }
        catch (Exception exc) {
            logger.error("Error reading recovery point list", exc);
            list = "{\"message\":\"Error reading recovery point list\", \"savepoint\":[]}".getBytes();
        }


        OutputStream resstream = response.getOutputStream();
        response.setContentType("text/text");
        response.setContentLength(list.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"listSavePoint_" + DateUtils.nowToString("yyyyMMhhHHmmSS") + ".txt\"");
        response.setHeader("Connection", "close");
        response.setHeader("Expires", "-1");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");

        resstream.write(list);
        resstream.flush();
        resstream.close();

        return mapping.findForward(null);
    }

    /* (non-Javadoc)
     * @see org.apache.struts.actions.LookupDispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward recoverSavePoint(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        String message = "";
        DynaActionForm frm = (DynaActionForm) form;

        Map<String, String> params = new HashMap<String, String>();
        String rec_id = frm.getString("rec_id");
        params.put("REC_ID", ((rec_id == null) || "".equals(rec_id)) ? "NULL" : rec_id);

        if (DataAccessObject.getSingleInt("CheckRecoveryPoint", params) == 1) {
            System.out.println("Start recovery SavePoint " + rec_id);
            try {
                SavePointController.instance().recover(Long.parseLong(rec_id));
                message = "{\"message\": \"Restarted SavePoint " + rec_id + "\"}";
            }
            catch (Exception exc) {
                System.out.println("Error recovering SavePoint: " + rec_id);
                exc.printStackTrace();
                message = "{\"message\": \"Error recovering SavePoint " + rec_id + " : " + TextUtils.replaceJSInvalidChars("" + exc) + "\"}";
            }
        }
        else {
            System.out.println("SavePoint " + rec_id + " is RUNNING. Unable to recover.");
            message = "{\"message\": \"SavePoint " + rec_id + " is RUNNING. Unable to recover.\"}";
        }

        OutputStream resstream = response.getOutputStream();
        response.setContentType("text/text");
        response.setContentLength(message.getBytes().length);
        response.setHeader("Content-Disposition", "attachment; filename=\"recoverSavePoint_" + DateUtils.nowToString("yyyyMMhhHHmmSS") + ".txt\"");
        response.setHeader("Connection", "close");
        response.setHeader("Expires", "-1");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");

        resstream.write(message.getBytes());
        resstream.flush();
        resstream.close();

        return mapping.findForward(null);
    }

    public ActionForward deleteSavePoint(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
    throws Exception
    {
    	String message = "";
    	DynaActionForm frm = (DynaActionForm)form;
    	Map params = new HashMap();
    	String rec_id = frm.getString("rec_id");
    	params.put("REC_ID", rec_id != null && !"".equals(rec_id) ? ((Object) (rec_id)) : "NULL");
    	if(DataAccessObject.getSingleInt("CheckRecoveryPoint", params) == 1)
    	{
    		System.out.println((new StringBuilder("Start recovery SavePoint ")).append(rec_id).toString());
    		try
    		{
    			SavePointController.instance().delete(Long.parseLong(rec_id));
    			message = (new StringBuilder("{\"message\": \"Deleted SavePoint ")).append(rec_id).append("\"}").toString();
    		}
    		catch(Exception exc)
    		{
    			System.out.println((new StringBuilder("Error deleting SavePoint: ")).append(rec_id).toString());
    			exc.printStackTrace();
    			message = (new StringBuilder("{\"message\": \"Error deleting SavePoint ")).append(rec_id).append(" : ").append(TextUtils.replaceJSInvalidChars((new StringBuilder()).append(exc).toString())).append("\"}").toString();
    		}
    	} else
    	{
    		System.out.println((new StringBuilder("SavePoint ")).append(rec_id).append(" is RUNNING. Unable to delete.").toString());
    		message = (new StringBuilder("{\"message\": \"SavePoint ")).append(rec_id).append(" is RUNNING. Unable to delete.\"}").toString();
    	}
    	OutputStream resstream = response.getOutputStream();
    	response.setContentType("text/text");
    	response.setContentLength(message.getBytes().length);
    	response.setHeader("Content-Disposition", (new StringBuilder("attachment; filename=\"recoverSavePoint_")).append(DateUtils.nowToString("yyyyMMhhHHmmSS")).append(".txt\"").toString());
    	response.setHeader("Connection", "close");
    	response.setHeader("Expires", "-1");
    	response.setHeader("Pragma", "no-cache");
    	response.setHeader("Cache-Control", "no-cache");
    	resstream.write(message.getBytes());
    	resstream.flush();
    	resstream.close();
    	return mapping.findForward(null);
    }

}
