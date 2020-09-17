/**
 *
 */
package it.greenvulcano.util.crypto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

import it.greenvulcano.event.EventHandler;
import it.greenvulcano.util.ArgsManager;
import it.greenvulcano.util.ArgsManagerException;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;

/**
 * @author gianluca
 *
 */
public class XMLConfigCryptoHelper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("gv.app.home", ".");
		try {
			Security.addProvider((Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance());
		} catch (Exception exc) {
			System.out.println("Error initializing JCE provider: " + exc);
			System.exit(1);
		}
		EventHandler.getEventLauncher().stopThread();
		EventHandler.removeShutdownHook();
		String infile = null;
        String outfile = null;

        try {
            ArgsManager argsM = new ArgsManager("m:i:o:", args);

            int mode = argsM.getInteger("m");
            if ((mode < 0) || (mode > 2)) {
                throw new Exception("Invalid value for -m argument: must be 0..2");
            }

            if (mode > 0) {
                infile = argsM.get("i");
                outfile = argsM.get("o");
            }

            switch (mode) {
	            case 0 :
	            	System.out.println("Generate key in key store file");
	                break;
	            case 1 :
	            	System.out.println("Encrypt(keyfile) infile -> outfile");
	                System.out.println("In file : " + infile);
	                System.out.println("Out file: " + outfile);
	                break;
	            case 2 :
	            	System.out.println("Decrypt(keyfile) infile -> outfile");
	                System.out.println("In file : " + infile);
	                System.out.println("Out file: " + outfile);
	                break;
	        }

            switch (mode) {
                case 0 : // generate key in key store file
                	generateKeystore();
                    break;
                case 1 : // encrypt(keyfile) infile -> outfile
                    encryptData(infile, outfile);
                    break;
                case 2 : // decrypt(keyfile) infile -> outfile
                    decryptData(infile, outfile);
                    break;
            }
        }
        catch (ArgsManagerException exc) {
            System.out.println(exc.getMessage());
            usage();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private static void usage()
    {
        System.out.println();
        System.out.println("Usage:");
        System.out.println("\tXMLConfigCryptoHelper <-m mode> [-i file] [-o file]");
        System.out.println("\t-m : 0 generate keystore");
        System.out.println("\t     1/2 encrypt/decrypt properties file -i -> -o");
    }

	public static void generateKeystore() {
		try {
			System.out.println("Generating new " + CryptoHelper.DEFAULT_KEY_STORE_NAME + " keystore...");
			System.out.println("\n");
			generateDefaultKeystore(null);
			System.out.println("\n");
			System.out.println("Keystore " + CryptoHelper.DEFAULT_KEY_STORE_NAME + " generated.");
			System.out.println("\n");
			System.out.println("Testing...");
			CryptoHelper.resetCache();
			testKeystore();
			System.out.println("\n");
			System.out.println("Test OK");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void generateDefaultKeystore(String outPath) throws Exception {
		KeyStoreID keySid = new KeyStoreID(CryptoHelper.DEFAULT_KEYSTORE_ID, KeyStoreUtils.DEFAULT_KEYSTORE_TYPE,
				CryptoHelper.DEFAULT_KEY_STORE_NAME, "__GreenVulcanoPassword__", KeyStoreUtils.DEFAULT_KEYSTORE_PROVIDER);
        KeyID keyid = new KeyID(CryptoHelper.DEFAULT_KEY_ID, keySid, "XMLConfigKey");

		makeKey(CryptoUtils.TRIPLE_DES_TYPE, "XMLConfigKey", "XMLConfigPassword", keyid,1);
	}

	private static void makeKey(String algType, String prAlias, String prPwd, KeyID keyid, int type) throws Exception
    {
        Key key = CryptoUtils.generateSecretKey(algType, null);
        keyid.setKeyType(algType);
        keyid.setKeyAlias(prAlias);
        keyid.setKeyPwd(prPwd);
        System.out.println("***************************************");
        System.out.println("Registering SecretKey: " + key.getAlgorithm() + " " + key.getFormat() + " "
                + key.toString());
       	System.out.println("KeySpec: " + Base64.encodeBase64String(((SecretKey) key).getEncoded()));
        System.out.println("In: " + keyid);
        KeyStoreUtils.writeKey(keyid, key, null);
        System.out.println("***************************************");
    }

	private static void testKeystore() throws Exception {
		String s1c = "Test string!";
		String s1e = CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, s1c, true);

		if (!s1c.equals(CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, s1e, false))) {
			throw new Exception("Failed decrypt...");
		}
	}

	private static void decryptData(String infile, String outfile) {
		BufferedReader infr = null;
		try {
			List<String> converted = new ArrayList<String>();
			infr = new BufferedReader(new FileReader(infile));
			String s = infr.readLine();
			while (s != null) {
				s = s.replaceAll("^\\s*", "");
				if (s.startsWith("#") || s.startsWith("!")) {
					converted.add(s);
				}
				if (s.contains("=") || s.contains(":")) {
					int idx = s.indexOf("=");
					if (idx == -1) {
						idx = s.indexOf(":");
					}
					String pName = s.substring(0, idx).trim();
					String pVal = s.substring(idx + 1);
					pVal = XMLUtils.replaceXMLEntities(pVal);
					if (pVal.contains("{3DES}")) {
						try {
							pVal = "{ENC}" + CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, pVal, false);
						}
						catch (Exception exc) {
							System.out.println("**** Error decrypting value for property [" + pName + "]: " + exc);
						}
					}
					pVal = XMLUtils.replaceXMLInvalidChars(pVal);
					converted.add(pName + "=" + pVal);
				}
				converted.add("");
			};

			/*System.out.println("\n");
			for (String string : converted) {
				System.out.println(string);
			}*/

			TextUtils.writeFile(converted, new File(outfile), "\n");
		}
		catch (Exception exc) {
			System.out.println("**** Error decrypting properties: " + exc);
		}
		finally {
			if (infr != null) {
				try {
					infr.close();
				} catch (Exception exc2) {
					// TODO: handle exception
				}
			}
		}
	}

	private static void encryptData(String infile, String outfile) {
		BufferedReader infr = null;
		try {
			List<String> converted = new ArrayList<String>();
			infr = new BufferedReader(new FileReader(infile));
			String s = infr.readLine();
			while (s != null) {
				s = s.replaceAll("^\\s*", "");
				if (s.startsWith("#") || s.startsWith("!")) {
					converted.add(s);
				}
				if (s.contains("=") || s.contains(":")) {
					int idx = s.indexOf("=");
					if (idx == -1) {
						idx = s.indexOf(":");
					}
					String pName = s.substring(0, idx).trim();
					String pVal = s.substring(idx + 1);
					pVal = XMLUtils.replaceXMLEntities(pVal);
					if (pVal.contains("{ENC}")) {
						try {
							pVal = pVal.substring(5);
							pVal = CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, pVal, true);
						}
						catch (Exception exc) {
							System.out.println("**** Error encrypting value for property [" + pName + "]: " + exc);
						}
					}
					pVal = XMLUtils.replaceXMLInvalidChars(pVal);
					converted.add(pName + "=" + pVal);
				}
				converted.add("");
			};

			/*System.out.println("\n");
			for (String string : converted) {
				System.out.println(string);
			}*/

			TextUtils.writeFile(converted, new File(outfile), "\n");
		}
		catch (Exception exc) {
			System.out.println("**** Error encrypting properties: " + exc);
		}
		finally {
			if (infr != null) {
				try {
					infr.close();
				} catch (Exception exc2) {
					// TODO: handle exception
				}
			}
		}
	}
}
