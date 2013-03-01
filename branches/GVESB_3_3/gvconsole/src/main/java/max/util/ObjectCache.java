/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-11 14:32:08 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/ObjectCache.java,v 1.2 2010-04-11 14:32:08 nlariviera Exp $
 * $Id: ObjectCache.java,v 1.2 2010-04-11 14:32:08 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.2 $
 * $State: Exp $
 */
package max.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;


/**
 * Mantiene in memoria un numero massimo prefissato di oggetti.
 * <p>
 * Gli oggetti che non entrano in memoria sono serializzati.
 */
public class ObjectCache
{
	private class CacheElement
	{
		public String name;
		public Serializable object;
		public CacheElement prev;
		public CacheElement next;

		public CacheElement(String nm, Serializable obj)
		{
			name = nm;
			object = obj;
			prev = null;
			next = null;
		}

		public void makeFirst()
		{
			next = first;
			prev = null;
			if(first != null) first.prev = this;
			if(last == null) last = this;
			first = this;
		}

		public void unlink()
		{
			if(prev != null) prev.next = next;
			if(next != null) next.prev = prev;
			if(last == this) last = prev;
			if(first == this) first = next;
			prev = null;
			next = null;
		}
	}

	private File workDir;
	private String prefix;
	private String suffix;

	private Hashtable cache = new Hashtable();
	protected CacheElement first = null;
	protected CacheElement last = null;
	private HashMap files = new HashMap();
	private int maxDim;

	public ObjectCache(int maxDim, File wrkDir, String prefix, String suffix)
	{
		workDir = wrkDir;
		this.prefix = prefix;
		this.suffix = suffix;
		this.maxDim = maxDim;
	}

	public synchronized void set(String name, Serializable object)
	{
		// Il vecchio file (ammesso che esista) non va piu' bene...
		Object fileObj = files.get(name);
		if(fileObj != null) {
			((File)fileObj).delete();
			files.remove(name);
		}

		// Vediamo se abbiamo l'oggetto in memoria...
		CacheElement ce = (CacheElement)cache.get(name);

		if(ce != null) {
			//... si: impostiamo il valore e lo sganciamo.
			ce.object = object;
			ce.unlink();
		}
		else {
			//... no: ne creiamo uno nuovo e lo mettiamo nella cache
			ce = new CacheElement(name, object);
			cache.put(name, ce);

			//se la cache e' piena mettiamo su file l'ultimo elemento
			if(cache.size() > maxDim) {
				lastToFile();
			}
		}

		// in ogni caso l'elemento va marcato come il piu' recentemente acceduto
		ce.makeFirst();
	}

	public synchronized Object get(String name) throws IOException
	{
		// prendiamo l'elemento dalla cache
		CacheElement ce = (CacheElement)cache.get(name);

		if(ce != null) {
			// ok, e' nella cache. Lo sganciamo dalla posizione attuale.
			ce.unlink();
		}
		else {
			// se non e' nemmeno tra i files allora l'oggetto non esiste.
			Object fileObj = files.get(name);
			if(fileObj == null) return null;

			// questo file contiene l'oggetto
			File file = (File)fileObj;

			//se la cache e' piena mettiamo su file l'ultimo elemento
			if(cache.size() >= maxDim) {
				lastToFile();
			}

			// costruiamo un elemento con il file letto e lo mettiamo nella cache
			ce = new CacheElement(name, readFile(file));
			cache.put(name, ce);
		}

		// in ogni caso l'elemento va marcato come il piu' recentemente acceduto
		ce.makeFirst();

		return ce.object;
	}

	public synchronized void remove(String name)
	{
		// Il file (ammesso che esista) non va piu' bene...
		Object fileObj = files.get(name);
		if(fileObj != null) {
			((File)fileObj).delete();
			files.remove(name);
		}

		CacheElement ce = (CacheElement)cache.get(name);
		if(ce != null) ce.unlink();
		cache.remove(name);
	}


	private void lastToFile()
	{
		if(last == null) return;

		String name = last.name;
		Object fileObj = files.get(name);
		boolean fileExists = false;
		if(fileObj != null) {
			fileExists = ((File)fileObj).exists();
		}

		if(!fileExists) {
			File file;
			try {
				file = File.createTempFile(prefix, suffix, workDir);
				file.deleteOnExit();
			}
			catch(IOException exc) {
				exc.printStackTrace();
				return;
			}

	        try {
		        FileOutputStream fos = new FileOutputStream(file);
		        BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
		        ObjectOutputStream oos = new ObjectOutputStream(bos);
		        oos.writeObject(last.object);
		        oos.flush();
		        oos.close();
			}
			catch(IOException exc) {
				exc.printStackTrace();
				return;
			}
	        files.put(name, file);
		}

        last.unlink();
        cache.remove(name);
	}


	private Serializable readFile(File file) throws IOException
	{
		try {
	        FileInputStream fis = new FileInputStream(file);
	        BufferedInputStream bis = new BufferedInputStream(fis, 1024);
	        ObjectInputStream ois = new ObjectInputStream(bis);
	        Object obj = ois.readObject();
	        ois.close();

	        return (Serializable)obj;
	    }
	    catch(ClassNotFoundException exc) {
			exc.printStackTrace();
			return null;
	    }
	}


	public synchronized String toString()
	{
		StringBuffer sb = new StringBuffer("CACHED (" + cache.size() + "):\n");
		CacheElement ce = first;
		int counter = 0;
		while(ce != null) {
			++counter;
			sb.append("" + counter + ":\t");
			sb.append(ce.name).append("\t").append(ce.object).append("\n");
			ce = ce.next;
		}
		sb.append("FILES:\n");
		for(Iterator it = files.keySet().iterator(); it.hasNext();) {
			Object k = it.next();
			Object v = files.get(k);
			sb.append(k).append("\t").append("" + v).append("\n");
		}
		return sb.toString();
	}


	public static void main(String args[]) throws Exception
	{
		BufferedReader in = new BufferedReader(
			new InputStreamReader(System.in), 1024
		);
		ObjectCache oc = new ObjectCache(
			Integer.parseInt(args[0]),
			new File(args[1]),
			args[2],
			args[3]
		);
		while(true) {
			String line = in.readLine();
			StringTokenizer st = new StringTokenizer(line, " \t\n\r", false);
			String cmd = st.nextToken();
			if(cmd.equals("get")) {
				String name = st.nextToken();
				System.out.println("----------> " + oc.get(name));
			}
			else if(cmd.equals("set")) {
				String name = st.nextToken();
				String value = st.nextToken();
				oc.set(name, value);
			}
			else if(cmd.equals("remove")) {
				String name = st.nextToken();
				oc.remove(name);
			}
			else if(cmd.equals("quit")) {
				break;
			}
		}
	}
}
