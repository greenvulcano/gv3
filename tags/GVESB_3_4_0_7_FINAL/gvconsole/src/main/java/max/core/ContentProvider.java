/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 */
package max.core;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * Un content provider deve implementare questa interfaccia e fornire un
 * costruttore che prende in input una stringa: ricever� il nome della
 * sezione contenente la sua configurazione.<p>
 *
 * I contenuti hanno una serie di attributi: gli attributi permettono di
 * legare ai contenuti informazioni necessarie alla loro gestione.<br>
 * E' compito del content provider implementare la gestione degli attributi.<p>
 *
 * Il content provider deve dare l'opportunit� all'utente di aggiungere
 * attributi.<p>
 *
 * Attenzione all'implementazione: il provider pu� essere invocato
 * concorrentemente da pi� threads, eventuali sezioni sincronizzate
 * devono essere gestite nell'implementazione.<p>
 */
public interface ContentProvider {
    public static final String ATTR_PROVIDER   = "$PROVIDER";
    public static final String ATTR_CATEGORY   = "$CATEGORY";
    public static final String ATTR_NAME       = "$NAME";
    public static final String ATTR_LASTUPDATE = "$LASTUPDATE";

    /**
     * Inizializza il content provider.
     */
    public void init(Node node) throws MaxException;

    /**
     * Fornisce un contenuto.
     *
     * @return null se il contenuto non esiste.
     */
    public InputStream get(String category, String contentName) throws MaxException;

    /**
     * Restituisce tutte le categorie di contenuti.
     */
    public String[] getCategories() throws MaxException;

    /**
     * Resituisce tutti i nomi dei contenuti nella categoria data,
     * indipendentemente dallo stato di abilitazione.<br>
     */
    public String[] getContentNames(String category) throws MaxException;

    /**
     * Restituisce una collezione di mappe con gli attributi dei contenuti
     * specificati in input.
     *
     * @param category categoria di interesse
     * @param contentNames contenuti all'interno della categoria di cui
     *        interessano gli attributi.
     *
     * @return un array di Map contenente gli attributi per i contenuti
     *         selezionati.
     *
     * @see #getContentAttributes(java.lang.String, java.lang.String)
     */
    public Map[] getContentsAttributes(String category, String[] contentNames) throws MaxException;

    /**
     * Restituisce gli attributi per il contenuto dato.<p>
     *
     * @return Una mappa contenente gli attributi del contenuto.<br>
     * La mappa ritornata deve contenere i seguenti attributi:<br><br>
     * <table>
     * 	<tr valign=top>
     * 		<td><b>Attributo</b></td>
     * 		<td><b>Symbolo</b></td>
     * 		<td><b>Tipo</b></td>
     * 		<td><b>Descrizione</b></td>
     * 	</tr>
     * 	<tr><td colspan=4><hr></td></tr>
     * 	<tr valign=top>
     * 		<td>$PROVODER</td>
     * 		<td>ATTR_PROVIDER</td>
     * 		<td>java.lang.String</td>
     * 		<td>provider del contenuto</td>
     * 	</tr>
     * 	<tr valign=top>
     * 		<td>$CATEGORY</td>
     * 		<td>ATTR_CATEGORY</td>
     * 		<td>java.lang.String</td>
     * 		<td>categoria del contenuto</td>
     * 	</tr>
     * 	<tr valign=top>
     * 		<td>$NAME</td>
     * 		<td>ATTR_NAME</td>
     * 		<td>java.lang.String</td>
     * 		<td>nome del contenuto</td>
     * 	</tr>
     * 	<tr valign=top>
     * 		<td>$LASTUPDATE</td>
     * 		<td>ATTR_LASTUPDATE</td>
     * 		<td>java.util.Date</td>
     * 		<td>inserimento o ultima modifica</td>
     * 	</tr>
     * 	<tr valign=top>
     * 		<td>Attributi provider-specific</td>
     * 		<td>-</td>
     * 		<td>-</td>
     * 		<td>-</td>
     * 	</tr>
     * 	<tr valign=top>
     * 		<td>Attributi dell'utente</td>
     * 		<td>-</td>
     * 		<td>-</td>
     * 		<td>-</td>
     * 	</tr>
     * </table>
     */
    public Map getContentAttributes(String category, String contentName) throws MaxException;

    /**
     * Imposta un attributo di un contenuto.
     */
    public void setContentAttribute(String category, String contentName, String attribute, Serializable value)
            throws MaxException;

    /**
     * Restituisce true se il contenuto esiste.
     */
    public boolean exists(String category, String contentName) throws MaxException;

    /**
     * I nuovi contenuti devono risultare non abilitati, definiti in ogni
     * periodo ed essere accessibili ad utenti autenticati.<p>
     *
     * @see #update(java.lang.String, java.lang.String, java.io.InputStream)
     *
     * @exception MaxException se il contenuto � gia esistente
     */
    public void insert(String category, String contentName, InputStream content) throws MaxException;

    /**
     * Gli aggiornamenti non modificano gli attributi.<p>
     *
     * @see #insert(java.lang.String, java.lang.String, java.io.InputStream)
     *
     * @exception MaxException se il contenuto non esiste
     */
    public void update(String category, String contentName, InputStream content) throws MaxException;

    /**
     * Rimuove un contenuto.
     */
    public void remove(String category, String contentName) throws MaxException;
}
