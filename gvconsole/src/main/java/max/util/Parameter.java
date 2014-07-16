/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/Parameter.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: Parameter.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Date;

import javax.servlet.http.*;


/**
 * Classe utilizzata per prelevare i parametri passati ai plug-in o alle
 * pagine <code>jsp</code>. <p>
 *
 * I metodi della classe <code>Parameter</code> permettono di leggere i vari
 * tipi di dati da un <code>HttpServletRequest</code> o da un
 * <code>MultipartFormDataParser</code>, convertirli nel tipo
 * di dato giusto, fare dei check. <p>
 *
 * E' possibile specificare se i parametri sono necessari o opzionali. <br>
 * L'omissione di parametri necessari provoca un'eccezione di tipo
 * <code>ParameterException</code>. <br>
 * L'omissione di parametri non necessari non solleva alcuna eccezione, ed
 * � possibile utilizzare il metodo <code>exists</code> per verificare
 * se il parametro esiste o meno. <br>
 * Per parametri necessari si intende parametri che devono necessariamente
 * essere presenti nella query string. <p>
 *
 * L'errato formato di parametri presenti provoca un'eccezione di tipo
 * <code>ParameterException</code>. <p>
 *
 * E' possibile fare alcuni check sui parametri:
 * <ul>
 * <li> controllare che i valori siano in un dato insieme di possibili
 *		valori
 * <li> controllare che i valori siano compresi in un determinato range
 * </ul>
 * Un check fallito provoca un'eccezione di tipo <code>ParameterException</code>. <p>
 *
 * Nel caso di eccezione il messaggio dell'eccezione descrive il motivo
 * che l'ha causata. <p>
 *
 * Tipicamente il codice di un metodo di un plug-in avr� la forma:
 * <pre>
 *
 *           public void metodo( <i>parametri</i> )
 *           {
 *               try {
 *                   <i>// request � l'oggetto HttpServletRequest</i>
 *                   Parameter param = new Parameter(request);
 *
 *                   <i>// oppure:
 *                   // Parameter param = new Parameter(request, descriptions);</i>
 *
 *                   <i>// � possibile settare le descrizioni con:
 *                   // param.setDescriptions(descriptions);</i>
 *
 *                   ...
 *                   <i>// lettura di tutti i parametri, es:</i>
 *                   int i = param.getInt("param_name", true);
 *                   ...
 *                   <i>utilizzo parametri</i>
 *                   ...
 *               }
 *               catch(ParameterException pe) {
 *                  lout.writeError(pe.getMessage());
 *               }
 *           }
 *
 * </pre>
 * Non � obbligatorio utilizzare le descrizioni per i parametri, ma se si usano allora
 * devono avere la seguente forma:
 * <pre>
 *           String descriptions[][] = {
 *               {"nomeParam1", "descrizione parametro 1"},
 *               ...
 *               {"nomeParamN", "descrizione parametro N"}
 *           }
 * </pre>
 * <p>
 * Nei precedenti esempi � possibile sostituire la classe <code>MultipartFormDataParser</code>
 * alla classe <code>HttpServletRequest</code>.<br>
 * E' possibile anche utilizzare entrambe le classi, in questo caso la ricerca del parametro
 * � effettuata prima sulla request e poi sui dati in posting.
 */
public class Parameter
{
	protected HttpServletRequest req = null;
	protected MultipartFormDataParser multipart = null;
	protected String descriptions[][] = null;

	protected Vector dateFormats = new Vector();



	// MESSAGGI DI ERRORE
	protected String msg_missing_number = "Valore Numerico Mancante: ";
	protected String msg_wrong_number = "Valore Numerico Errato: ";

	protected String msg_missing_file = "File Mancante: ";

	protected String msg_missing_parameter = "Valore Mancante: ";
	protected String msg_wrong_parameter = "Valore Errato: ";

	protected String msg_missing_date = "Data Mancante: ";
	protected String msg_wrong_date = "Data Errata: ";



	/**
	 * Costruisce un oggetto <code>Parameter</code> senza descrizioni.
	 * E' possibile associare le descrizioni con il metodo <code>setDescriptions</code>.
	 */
	public Parameter(HttpServletRequest _req)
	{
		req = _req;
		dateFormats.add("d/M/y");
		dateFormats.add("d-M-y");
	}

	/**
	 * Costruisce un oggetto <code>Parameter</code> senza descrizioni.
	 * E' possibile associare le descrizioni con il metodo <code>setDescriptions</code>.
	 */
	public Parameter(MultipartFormDataParser _multipart)
	{
		multipart = _multipart;
		dateFormats.add("d/M/y");
		dateFormats.add("d-M-y");
	}

	/**
	 * Costruisce un oggetto <code>Parameter</code> senza descrizioni.
	 * E' possibile associare le descrizioni con il metodo <code>setDescriptions</code>.
	 */
	public Parameter(HttpServletRequest _req, MultipartFormDataParser _multipart)
	{
	    req = _req;
		multipart = _multipart;
		dateFormats.add("d/M/y");
		dateFormats.add("d-M-y");
	}


	/**
	 * Imposta le descrizioni per i parametri. <br>
	 * Le descrizioni devono avere la forma:
     * <pre>
	 *           String descriptions[][] = {
	 *               {"nomeParam1", "descrizione parametro 1"},
	 *               ...
	 *               {"nomeParamN", "descrizione parametro N"}
	 *           }
	 * </pre>
	 *
	 * @param _descriptions descrizioni dei parametri.
	 */
	public void setDescriptions(String _descriptions[][])
	{
		descriptions = _descriptions;
	}

	/**
	 * Restituisce la descrizione di un parametro.
	 *
	 * @param param nome del parametro.
	 */
	public String getDescription(String param)
	{
	  if(descriptions == null) return param;

		for(int i = 0; i < descriptions.length; ++i)
			if(descriptions[i][0].equals(param)) return descriptions[i][1];

		return "'" + param + "'";
	}

	/**
	 * Test sull'esistenza di un parametro.
	 *
	 * @return <code>true</code> se il parametro esiste, <code>false</code> altrimenti.
	 */
	public boolean exists(String paramName) throws ParameterException
	{
	    return get(paramName) != null;
	}


	/**
	 * Test per blank di un parametro.
	 *
	 * @return <code>true</code> se il parametro esiste ed � blank, <code>false</code> altrimenti.
	 */
	public boolean isBlank(String paramName) throws ParameterException
	{
		String s = get(paramName);
		if(s == null) return true;

		return  s.equals("");
	}


	public String get(String paramName) throws ParameterException
	{
	    if(req != null) {
	        String ret = req.getParameter(paramName);
	        if(ret != null) return ret;
	    }
	    try {
    	    if(multipart != null) {
    	        if(multipart.exists(paramName))
    	            return multipart.getString(paramName);
    	    }
    	}
    	catch(Exception exc) {
    	    throw new ParameterException("" + exc);
    	}
	    return null;
	}


	/**
	 * Restituisce un <code>InputStream</code> contenente il valore del
	 * parametro.
	 */
	public InputStream getInputStream(String paramName) throws ParameterException
	{
	    if(req != null) {
	        String ret = req.getParameter(paramName);
	        if(ret != null) {
	            return new ByteArrayInputStream(ret.getBytes());
	        }
	    }
	    try {
    	    if(multipart != null) {
    	        return multipart.getInputStream(paramName);
    	    }
    	}
    	catch(Exception exc) {
    	    throw new ParameterException("" + exc);
    	}
	    return null;
	}


    /**
     * Rstituisce il nome del file sottomesso dal client.
     *
     * @return <code>null</code> se il parametro non � un file inviato dal client.
     */
    public String getFileName(String paramName, boolean needed) throws ParameterException
    {
        String filename = null;
        try {
    	    if(multipart != null) {
    	        filename = multipart.getFileName(paramName);
    	    }
    	}
    	catch(Exception exc) {
    	    throw new ParameterException("" + exc);
    	}
    	if(needed && (filename == null)) {
            throw new ParameterException(msg_missing_file + getDescription(paramName));
    	}
    	return filename;
    }


	public String[] getValues(String paramName) throws ParameterException
	{
	    String values1[] = null;
	    String values2[] = null;
	    if(req != null) {
	        values1 = req.getParameterValues(paramName);
	    }
	    try {
    	    if(multipart != null) {
    	        values2 = multipart.getStrings(paramName);
    	    }
    	}
    	catch(Exception exc) {
    	    throw new ParameterException("" + exc);
    	}
	    String ret[] = new String[(values1 != null ? values1.length : 0) + (values2 != null ? values2.length : 0)];
	    int o = 0;
	    if(values1 != null) {
	        System.arraycopy(values1, 0, ret, 0, values1.length);
	        o = values1.length;
	    }
	    if(values2 != null) {
	        System.arraycopy(values2, 0, ret, o, values2.length);
	    }
        return ret;
	}


	/**
	 * Legge un'intero dalla richiesta. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il parametro non � presente
	 *		nella richiesta.
	 */
	public int getInt(String paramName) throws ParameterException
	{
		return getInt(paramName, true);
	}


	/**
	 * Legge un'intero dalla richiesta.
	 *
	 * @param paramName nome del parametro
	 * @param necessario se <code>true</code> allora il parametro � necessario
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il parametro � necessario e non � nella richiesta,
	 *		oppure se il formato � errato.
	 *		Se il parametro non � fornito e non � necessario � restituito 0.
	 */
	public int getInt(String paramName, boolean necessario) throws ParameterException
	{
		String val = get(paramName);
		if((val == null) && necessario)
			throw new ParameterException(msg_missing_number + getDescription(paramName));

		if(val == null) return 0;

		try {
			int ret = Integer.parseInt(val);
			return ret;
		}
		catch(NumberFormatException e) {
			throw new ParameterException(msg_wrong_number + getDescription(paramName));
		}
	}


	/**
	 * Legge un'intero dalla richiesta e controlla che i valori siano in un dato
	 * insieme. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 * @param accettabili i valori accettabili per il parametro
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il valore non � tra quelli
	 *		indicati.
	 */
	public int getInt(String paramName, int accettabili[]) throws ParameterException
	{
		int val = getInt(paramName, true);
		for(int i = 0; i < accettabili.length; ++i)
			if(val == accettabili[i]) return val;

		throw new ParameterException(msg_wrong_number + getDescription(paramName));
	}


	/**
	 * Legge un'intero dalla richiesta e controlla che i valori siano in un dato
	 * range, estremi compresi. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 * @param low estremo inferiore
	 * @param high estremo superiore
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il valore non � tra il
	 *		range indicato.
	 */
	public int getInt(String paramName, int  low, int high) throws ParameterException
	{
		int val = getInt(paramName, true);
		if((low <= val) && (val <= high)) return val;

		throw new ParameterException(msg_wrong_number + getDescription(paramName));
	}





	/**
	 * Legge un double dalla richiesta. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il parametro non � nella richiesta.
	 */
	public double getDouble(String paramName) throws ParameterException
	{
		return getDouble(paramName, true);
	}


	/**
	 * Legge un double dalla richiesta.
	 *
	 * @param paramName nome del parametro
	 * @param necessario se <code>true</code> allora il parametro � necessario
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il parametro � necessario e non � nella richiesta,
	 *		oppure se il formato � errato.
	 *		Se il parametro non � fornito e non � necessario � restituito 0.0.
	 */
	public double getDouble(String paramName, boolean necessario) throws ParameterException
	{
		String val = get(paramName);
		if((val == null) && necessario)
			throw new ParameterException(msg_missing_number + getDescription(paramName));

		if(val == null) return 0.0;

		try {
			double ret = Double.valueOf(val).doubleValue();
			return ret;
		}
		catch(NumberFormatException e) {
			throw new ParameterException(msg_wrong_number + getDescription(paramName));
		}
	}


	/**
	 * Legge un double dalla richiesta e controlla che i valori siano in un dato
	 * insieme. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 * @param accettabili i valori accettabili per il parametro
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il valore non � tra quelli
	 *		indicati.
	 */
	public double getDouble(String paramName, double accettabili[]) throws ParameterException
	{
		double val = getDouble(paramName, true);
		for(int i = 0; i < accettabili.length; ++i)
			if(val == accettabili[i]) return val;

		throw new ParameterException(msg_wrong_number + getDescription(paramName));
	}


	/**
	 * Legge un double dalla richiesta e controlla che i valori siano in un dato
	 * range, estremi compresi. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 * @param low estremo inferiore
	 * @param high estremo superiore
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il valore non � tra il
	 *		range indicato.
	 */
	public double getDouble(String paramName, double  low, double high) throws ParameterException
	{
		double val = getDouble(paramName, true);
		if((low <= val) && (val <= high)) return val;

		throw new ParameterException(msg_wrong_number + getDescription(paramName));
	}





	/**
	 * Legge una String dalla richiesta. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 * @param allowEmpty se <code>true</code> allora sono ammesse anche le stringhe vuote
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il parametro non � nella richiesta o � una stringa
	 *		vuota e <code>allowEmpty == false</code>.
	 */
	public String getString(String paramName, boolean allowEmpty) throws ParameterException
	{
		return getString(paramName, true, allowEmpty);
	}


	/**
	 * Legge una String dalla richiesta.
	 *
	 * @param paramName nome del parametro
	 * @param necessario se <code>true</code> allora il parametro � necessario
	 * @param allowEmpty se <code>true</code> allora sono ammesse anche le stringhe vuote
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il parametro � necessario e non � nella richiesta,
	 *		oppure se il formato � errato.
	 *		Se il parametro non � fornito e non � necessario � restituita "".
	 */
	public String getString(String paramName, boolean necessario, boolean allowEmpty) throws ParameterException
	{
		String val = get(paramName);
		if((val == null) && necessario)
			throw new ParameterException(msg_missing_parameter + getDescription(paramName));

		if(val == null) val = "";

		if( (val.trim().equals("")) && !allowEmpty)
			throw new ParameterException(msg_wrong_parameter + getDescription(paramName));

		return val;
	}


	/**
	 * Legge una String dalla richiesta e controlla che i valori siano in un dato
	 * insieme. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 * @param accettabili i valori accettabili per il parametro
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il valore non � tra quelli
	 *		indicati.
	 */
	public String getString(String paramName, String accettabili[]) throws ParameterException
	{
		String val = getString(paramName, true, true);
		for(int i = 0; i < accettabili.length; ++i)
			if(accettabili[i].equals(val)) return val;

		throw new ParameterException(msg_wrong_parameter + getDescription(paramName));
	}


	/**
	 * Legge un Array di String dalla richiesta. <br>
	 * Utile per leggere dati inseriti tramite checkbox.
	 *
	 * @param paramName nome del parametro
	 * @param necessario se <code>true</code> allora il parametro � necessario
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il parametro � necessario e non � nella richiesta.
	 *		Se il parametro non � fornito e non � necessario � restituita "".
	 */
	public String[] getStringArray(String paramName, boolean necessario) throws ParameterException
	{
		String val[] = getValues(paramName);
		if((val == null) && necessario)
			throw new ParameterException(msg_missing_parameter + getDescription(paramName));

		return val;
	}


	/**
	 * Legge una Date dalla richiesta. Il parametro � necessario.
	 * I formati predefiniti sono <code>DD/MM/YYYY</code> e <code>DD-MM-YYYY</code>.
	 * E' possibile cambiarli con le funzioni <code>addDateFormat</code> e
	 * <code>removeDateFormat</code>.
	 *
	 * @param paramName nome del parametro
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il parametro non � nella richiesta.
	 */
	public Date getDate(String paramName) throws ParameterException
	{
		return getDate(paramName, true);
	}


	/**
	 * Legge una Date dalla richiesta. <br>
	 * I formati predefiniti sono <code>DD/MM/YYYY</code> e <code>DD-MM-YYYY</code>.
	 * E' possibile cambiarli con le funzioni <code>addDateFormat</code> e
	 * <code>removeDateFormat</code>.
	 *
	 * @param paramName nome del parametro
	 * @param necessario se <code>true</code> allora il parametro � necessario
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il parametro � necessario e non � nella richiesta,
	 *		oppure se il formato � errato. <br>
	 *		Se il parametro non � fornito e non � necessario � restituita la data odierna.
	 */
	public Date getDate(String paramName, boolean necessario) throws ParameterException
	{
		String val = get(paramName);
		if((val == null) && necessario)
			throw new ParameterException(msg_missing_date + getDescription(paramName));

		if(val == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.AM_PM, Calendar.AM);
			return cal.getTime();
		}
		val = val.trim();
		if(val.equals("")) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.AM_PM, Calendar.AM);
			return cal.getTime();
		}

		for(Enumeration e = dateFormats.elements(); e.hasMoreElements();) {
			String frm = (String)e.nextElement();
			Date d = getDate(val, frm);
			if(d != null) return d;
		}

		/*
		Date d = getDate(val, "d/M/y");
		if(d != null) return d;
		d = getDate(val, "d-M-y");
		if(d != null) return d;
		*/

		throw new ParameterException(msg_wrong_date + getDescription(paramName));
	}


	/**
	 * Ottiene una data da una stringa formattata. Gestisce anche le date
	 * bisestili (cosa che non fa correttamente Java)
	 *
	 * @param dateStr
	 * @param format
	 */
	public static Date getDate(String dateStr, String format)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);

		ParsePosition pp = new ParsePosition(0);
		Date d = sdf.parse(dateStr, pp);

		if(d == null) {
			// proviamo a vedere se � un anno bisestile
			sdf.setLenient(true);
			pp = new ParsePosition(0);
			d = sdf.parse(dateStr, pp);
			if(d != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				if((cal.get(Calendar.DATE) != 29)
				   || (cal.get(Calendar.MONTH) != 1)) // febbraio � 1
					return null;
			}
			else return null;
		}

		int len = dateStr.length();
		if(len == pp.getIndex()) return d;
		return null;
	}


	/**
	 * Legge un double dalla richiesta e controlla che i valori siano in un dato
	 * insieme. Il parametro � necessario.
	 * I formati predefiniti sono <code>DD/MM/YYYY</code> e <code>DD-MM-YYYY</code>.
	 * E' possibile cambiarli con le funzioni <code>addDateFormat</code> e
	 * <code>removeDateFormat</code>.
	 *
	 * @param paramName nome del parametro
	 * @param accettabili i valori accettabili per il parametro
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il valore non � tra quelli
	 *		indicati.
	 */
	public Date getDate(String paramName, Date accettabili[]) throws ParameterException
	{
		Date val = getDate(paramName, true);
		for(int i = 0; i < accettabili.length; ++i)
			if(val.equals(accettabili[i])) return val;

		throw new ParameterException(msg_wrong_date + getDescription(paramName));
	}


	/**
	 * Legge una Date dalla richiesta e controlla che i valori siano in un dato
	 * range, estremi compresi. Il parametro � necessario.
	 *
	 * @param paramName nome del parametro
	 * @param low estremo inferiore
	 * @param high estremo superiore
	 *
	 * @return il valore del parametro
	 *
	 * @exception ParameterException se il formato � errato o il valore non � tra il
	 *		range indicato.
	 */
	public Date getDate(String paramName, Date low, Date high) throws ParameterException
	{
		Date val = getDate(paramName, true);
		if(low.before(val) && val.before(high)) return val;
		if(low.equals(val) || high.equals(val)) return val;

		throw new ParameterException(msg_wrong_date + getDescription(paramName));
	}


	/**
	 * Aggiunge un formato ammesso per le date.
	 *
	 * @param frm formato ammesso per la data.
	 */
	public void addDateFormat(String frm)
	{
		dateFormats.addElement(frm);
	}


	/**
	 * Rimuove un formato ammesso per le date.
	 *
	 * @param frm formato ammesso per la data.
	 */
	public void removeDateFormat(String frm)
	{
		dateFormats.removeElement(frm);
	}


	/**
	 * Rimuove tutti i  formati per le date.
	 */
	public void removeAllDateFormat()
	{
		dateFormats = new Vector();
	}
}
