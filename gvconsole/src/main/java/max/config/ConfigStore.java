/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 */
package max.config;

import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * Non � significativo l'ordine di specifica delle properties. <br>
 *
 * Ai valori delle properties � possibile applicare delle sostituzioni
 * utilizzando la sintassi <b><code>[[ide]]</code></b> oppure <b>
 * <code>[[ide::defaultVal]]</code></b> dove <code>ide</code> pu� essere una
 * property oppure <code>sezione!property</code>.
 * <p>
 *
 * La stringa <b><code>[[ide]]</code></b> � sostituita con il valore della
 * property <b>ide</b>.
 * <p>
 *
 * Se la property non esiste la stringa <b><code>[[ide::defaultVal]]</code> </b>
 * � sostituita dal valore di default <b><code>defaultVal</code></b> se
 * specificato, altrimenti resta invariata.
 * <p>
 *
 * Le sostituzioni sono abilitate per default, ma � possibile disabilitarle. <br>
 * Pu� essere utile disabilitare le sostituzioni quando si salvano le
 * properties.
 * <p>
 *
 * <u>Attenzione ai cicli quando le sostituzioni sono attivate!!</u>
 */
public class ConfigStore
{
    /**
     * Tabella per la memorizzazione delle properties.
     */
    protected Hashtable<String, Hashtable<String, String>> sections      = new Hashtable<String, Hashtable<String, String>>();

    /**
     * Se true <u>non</u> applica le sostituzioni.
     */
    protected boolean                                      notApplySubst = false;

    /**
     * Costruisce un <code>ConfigStore</code> inizialmente senza nessuna
     * propriet� definita.
     */
    public ConfigStore()
    {
    }

    /**
     * Abilita/disabilita le sostituzioni.
     *
     * @param enable
     */
    public void setSubstitutionsEnabled(boolean enable)
    {
        notApplySubst = !enable;
    }

    /**
     * Ritorna lo stato delle sostituzioni.
     *
     * @return the substitution state
     */
    public boolean isSubstitutionsEnabled()
    {
        return !notApplySubst;
    }

    /**
     * Legge una property. <br>
     *
     * @param sect
     *        sezione
     * @param key
     *        chiave
     *
     * @return il valore. <code>null</code> se la property non esiste.
     */
    public String get(String sect, String key)
    {
        return getDef(sect, key, null);
    }

    /**
     * Legge una property. <br>
     *
     * @param sect
     *        sezione
     * @param key
     *        chiave
     * @param defaultValue
     *        valore di default restituito se la chiave non esiste.
     *
     * @return il valore. <code>defaultValue</code> se la property non esiste.
     */
    public String getDef(String sect, String key, String defaultValue)
    {
        if (sect == null) {
            sect = "";
        }
        if (key == null) {
            key = "";
        }

        String ret = null;
        if (sect.equals("")) {
            try {
                ret = System.getProperty(key);
            }
            catch (SecurityException exc) {
            }
        }
        if (ret == null) {
            Hashtable<String, String> ht = sections.get(sect);
            if (ht != null) {
                ret = ht.get(key);
            }
        }
        if (ret == null) {
            ret = defaultValue;
        }
        // check for PropertiesHandler metadata
        if (!PropertiesHandler.isExpanded(ret)) {
            try {
                ret = PropertiesHandler.expand(ret, null);
            }
            catch (PropertiesHandlerException exc) {
                exc.printStackTrace();
            }
        }
        return applySubst(sect, ret);
    }

    /**
     * Imposta il valore di una property.
     *
     * @param sect
     *        sezione
     * @param key
     *        chiave
     * @param val
     *        valore
     */
    public void set(String sect, String key, String val)
    {
        if (sect == null) {
            sect = "";
        }
        if (key == null) {
            key = "";
        }
        if (val == null) {
            val = "";
        }

        Hashtable<String, String> ht = sections.get(sect);
        if (ht == null) {
            ht = new Hashtable<String, String>();
            sections.put(sect, ht);
        }

        ht.put(key, val);
    }

    /**
     * Concatena un valore al valore precedente di una property.
     *
     * @param sect
     *        sezione
     * @param key
     *        chiave
     * @param val
     *        valore da concatenare al precedente
     */
    public void concat(String sect, String key, String val)
    {
        if (sect == null) {
            sect = "";
        }
        if (key == null) {
            key = "";
        }
        if (val == null) {
            val = "";
        }

        Hashtable<String, String> ht = sections.get(sect);
        if (ht == null) {
            ht = new Hashtable<String, String>();
            sections.put(sect, ht);
        }

        String old = ht.get(key);
        if (old == null) {
            ht.put(key, val);
        }
        else {
            ht.put(key, old + val);
        }
    }

    /**
     * Rimuove una property.
     *
     * @param sect
     *        sezione
     * @param key
     *        property da rimuovere
     */
    public void remove(String sect, String key)
    {
        if (sect == null) {
            sect = "";
        }
        if (key == null) {
            key = "";
        }

        Hashtable<String, String> ht = sections.get(sect);
        if (ht == null) {
            return;
        }

        ht.remove(key);
    }

    /**
     * Rimuove tutte le properties.
     */
    public void removeAll()
    {
        sections = new Hashtable<String, Hashtable<String, String>>();
    }

    /**
     * @return the sections
     */
    public Enumeration<String> sections()
    {
        Vector<String> v = new Vector<String>();
        v.addElement("");

        for (Enumeration<String> e = sections.keys(); e.hasMoreElements();) {
            String k = e.nextElement();
            if (!k.equals("")) {
                v.addElement(k);
            }
        }

        return v.elements();
    }

    /**
     * Restituisce un <code>Enumeration</code> contenente tutte le chiavi nelle
     * properties di una sezione. <br>
     * L'enumerazione contiene tutti oggetti <code>String</code>.
     *
     * @param sect
     *        the section
     * @return the keys corresponding to the section
     */
    public Enumeration<String> keys(String sect)
    {
        if (sect == null) {
            sect = "";
        }

        Hashtable<Object, Object> h = new Hashtable<Object, Object>();
        Vector<String> v = new Vector<String>();

        if (sect.equals("")) {
            Properties props = System.getProperties();
            for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
                String k = (String) e.nextElement();
                v.addElement(k);
                h.put(k, h);
            }
        }

        Hashtable<String, String> ht = sections.get(sect);
        if (ht == null) {
            return v.elements();
        }

        for (Enumeration<String> e = ht.keys(); e.hasMoreElements();) {
            String k = e.nextElement();
            if (h.get(k) == null) {
                v.addElement(k);
            }
        }

        return v.elements();
    }

    /**
     * Applica le sostituzioni al valore della property.
     *
     * @param defSect
     *        default section
     * @param val
     *        valore al quale devono essere applicate le sostituzioni.
     *
     * @return il valore finale.
     */
    protected final String applySubst(String defSect, String val)
    {
        if (val == null) {
            return null;
        }
        if (notApplySubst) {
            return val;
        }

        StringBuilder buff = new StringBuilder(val);
        Vector<Substitution> subst = findSubstitutions(defSect, buff);
        for (int i = subst.size() - 1; i >= 0; --i) {
            Substitution s = subst.elementAt(i);
            String str = getDef(s.sect, s.ide, s.defVal);
            if (str != null) {
                buff.replace(s.start, s.end, str);
            }
        }

        return buff.toString();
    }

    private final Vector<Substitution> findSubstitutions(String defSect, StringBuilder val)
    {
        Vector<Substitution> ret = new Vector<Substitution>();

        int length = val.length();
        int st = -1;
        int dp = -1;
        int i = 0;
        while (i < length) {
            int inc = getInc(i, val);
            if (inc == 2) {
                switch (val.charAt(i)) {

                    case '[' :
                        st = i;
                        dp = -1;
                        break;

                    case ':' :
                        if (st != -1) {
                            dp = i;
                        }
                        break;

                    case ']' :
                        if (st != -1) {
                            Substitution subst = new Substitution(defSect, st, dp, i, val);
                            ret.addElement(subst);
                            st = -1;
                            dp = -1;
                        }
                        break;
                }
            }
            i += inc;
        }
        return ret;
    }

    private final int getInc(int idx, StringBuilder s)
    {
        char c = s.charAt(idx);
        switch (c) {

            case '[' :
            case ':' :
            case ']' :
                if (idx == s.length() - 1) {
                    return 1;
                }
                if (s.charAt(idx + 1) == c) {
                    return 2;
                }
                return 1;

            default :
                return 1;
        }
    }
}
