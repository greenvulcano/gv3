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
package it.greenvulcano.util.crypto;

import it.greenvulcano.util.ArgsManager;
import it.greenvulcano.util.ArgsManagerException;
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.spec.KeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V1CertificateGenerator;

/**
 * CryptoUtils class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 **/
public final class CryptoUtils
{
    /**
     * the keystore type.
     */
    public static final String             KEY_STORE_TYPE          = "JCEKS";
    /**
     * the public DES type.
     */
    public static final String             DES_TYPE                = "DES";
    /**
     * the DES key size.
     */
    private static final int               DES_KEY_SIZE            = 56;
    /**
     * the public 3DES.
     */
    public static final String             TRIPLE_DES_TYPE         = "3DES";
    /**
     * the private 3DES.
     */
    private static final String            TRIPLE_DES_TYPE_P       = "DESede";
    /**
     * the 3DES key size.
     */
    private static final int               TRIPLE_DES_KEY_SIZE     = 168;
    /**
     * the public AES type.
     */
    public static final String             AES_TYPE                = "AES";
    /**
     * the AES key size.
     */
    private static final int               AES_KEY_SIZE            = 128;
    /**
     * the public DSA type.
     */
    public static final String             DSA_TYPE                = "DSA";
    /**
     * the DSA key size.
     */
    private static final int               DSA_KEY_SIZE            = 512;
    /**
     * the DSA certificate signature algorithm.
     */
    private static final String            DSA_SIGN_ALG            = "SHA1WithDSA";
    /**
     * the public RSA.
     */
    public static final String             RSA_TYPE                = "RSA";
    /**
     * the RSA key size.
     */
    private static final int               RSA_KEY_SIZE            = 512;
    /**
     * the RSA certificate signature algorithm.
     */
    private static final String            RSA_SIGN_ALG            = "SHA1WithRSA";
    /**
     * default key definition file name.
     */
    public static final String             DEFAULT_KEY_FILE        = "cukeystore.dat";
    /**
     * default string encoding.
     */
    public static final String             DEFAULT_STRING_ENCODING = "ISO-8859-1";
    /**
     * default certificate issuer CN.
     */
    public static final String             DEFAULT_CERT_ISSUER     = "CN=GreenVulcano, OU=GreenVulcano, O=GreenVulcano, C=IT";
    /**
     * default certificate subject CN.
     */
    public static final String             DEFAULT_CERT_SUBJECT    = "CN=GreenVulcano, OU=GreenVulcano, O=GreenVulcano, C=IT";
    /**
     * Default validness.
     */
    public static final long               DEFAUL_CERT_EXPIRATION  = 365 * 24 * 60 * 60 * 1000L;
    /**
     * the type -> algorithm conversion map.
     */
    private static HashMap<String, String> typeMap                 = null;

    static {
        typeMap = new HashMap<String, String>();
        typeMap.put(TRIPLE_DES_TYPE, TRIPLE_DES_TYPE_P);
    }

    /**
     * Constructor.
     */
    private CryptoUtils()
    {
        // do nothing
    }

    /**
     * Generate a Secret key.
     * 
     * @param type
     *        the algorithm type
     * @param keyBytes
     *        if not null must contain the serialized key
     * @return the key
     * @throws CryptoUtilsException
     *         if error occurs
     */
    public static SecretKey generateSecretKey(String type, byte[] keyBytes) throws CryptoUtilsException
    {
        String kType = getTypeI(type);
        int kSize = 0;
        if (kType.equals(DES_TYPE)) {
            kSize = DES_KEY_SIZE;
        }
        else if (kType.equals(TRIPLE_DES_TYPE_P)) {
            kSize = TRIPLE_DES_KEY_SIZE;
        }
        else if (kType.equals(AES_TYPE)) {
            kSize = AES_KEY_SIZE;
        }
        else {
            throw new CryptoUtilsException("Invalid algorithm : " + kType);
        }
        if (keyBytes != null) {
            try {
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(kType, "SunJCE");
                KeySpec spec = null;
                if (kType.equals(DES_TYPE)) {
                    spec = new DESKeySpec(keyBytes, 0);
                }
                else if (kType.equals(TRIPLE_DES_TYPE_P)) {
                    spec = new DESedeKeySpec(keyBytes, 0);
                }
                else if (kType.equals(AES_TYPE)) {
                    spec = new SecretKeySpec(keyBytes, "AES");
                }
                return keyFactory.generateSecret(spec);
            }
            catch (Exception exc) {
                throw new CryptoUtilsException("Error creating key from bytes - type '" + type + "'", exc);
            }
        }
        /*
         * Generate the Secret key
         */
        KeyGenerator keyGen = null;
        SecureRandom random = null;
        try {
            keyGen = KeyGenerator.getInstance(kType, "BC");
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        }
        catch (Exception exc) {
            throw new CryptoUtilsException("Error initializing KeyGenerator for key type '" + type + "'", exc);
        }

        random.setSeed(System.currentTimeMillis());
        keyGen.init(kSize, random);

        SecretKey sKey = keyGen.generateKey();

        return sKey;
    }

    /**
     * Generate a key pair.
     * 
     * @param type
     *        the algorithm type
     * @return the key pair
     * @throws CryptoUtilsException
     *         if error occurs
     */
    public static KeyPair generateKeyPair(String type) throws CryptoUtilsException
    {
        String kType = getTypeI(type);
        int kSize = 0;
        if (kType.equals(DSA_TYPE)) {
            kSize = DSA_KEY_SIZE;
        }
        else if (kType.equals(RSA_TYPE)) {
            kSize = RSA_KEY_SIZE;
        }
        else {
            throw new CryptoUtilsException("Invalid algorithm : " + kType);
        }
        /*
         * Generate the key pair
         */
        KeyPairGenerator keyGen = null;
        SecureRandom random = null;
        try {
            keyGen = KeyPairGenerator.getInstance(kType);
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        }
        catch (Exception exc) {
            throw new CryptoUtilsException("Error initializing KeyPairGenerator for key type '" + type + "'", exc);
        }
        random.setSeed(System.currentTimeMillis());
        keyGen.initialize(kSize, random);

        KeyPair sKey = keyGen.generateKeyPair();

        return sKey;
    }

    /**
     * Encrypt the input string.
     * 
     * @param type
     *        the algorithm type
     * @param key
     *        the key to be used
     * @param input
     *        the string to encript, with encoding 'ISO-8859-1'
     * @param encode
     *        if true the the output is encoded qith the type prefix
     * @return the encrypted string with encoding Base64
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String encryptToBase64(String type, Key key, String input, boolean encode)
            throws CryptoUtilsException
    {
        return encryptToBase64(type, key, null, input, DEFAULT_STRING_ENCODING, encode, null);
    }

    /**
     * Encrypt the input string.
     * 
     * @param type
     *        the algorithm type
     * @param key
     *        the key to be used
     * @param input
     *        the string to encript
     * @param inputEnc
     *        the input encoding
     * @param encode
     *        if true the the output is encoded qith the type prefix
     * @return the encrypted string with encoding Base64
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String encryptToBase64(String type, Key key, String input, String inputEnc, boolean encode)
            throws CryptoUtilsException
    {
        return encryptToBase64(type, key, null, input, inputEnc, encode, null);
    }

    /**
     * Encrypt the input string.
     * 
     * @param type
     *        the algorithm type
     * @param key
     *        the key to be used
     * @param params
     *        the algorithm initialization parameters
     * @param input
     *        the string to encript
     * @param inputEnc
     *        the input encoding
     * @param encode
     *        if true the the output is encoded qith the type prefix
     * @return the encrypted string with encoding Base64
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String encryptToBase64(String type, Key key, AlgorithmParameters params, String input,
            String inputEnc, boolean encode, AlgorithmParametersHolder paramsHolder) throws CryptoUtilsException
    {
        String output = null;
        try {
            String kType = getTypeI(type);
            byte[] result = cypher(kType, Cipher.ENCRYPT_MODE, key, params, input.getBytes(inputEnc), paramsHolder);
            result = Base64.encodeBase64(result);
            output = new String(result, inputEnc);
        }
        catch (UnsupportedEncodingException exc) {
            throw new CryptoUtilsException("Error encoding data", exc);
        }
        if (encode) {
            output = encodeType(type, output);
        }
        return output;
    }

    /**
     * Encrypt the input byte array.
     * 
     * @param type
     *        the algorithm type
     * @param key
     *        the key to be used
     * @param input
     *        the byte array to encript
     * @param encode
     *        if true the the output is encoded qith the type prefix
     * @return the encrypted string with encoding Base64
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String encryptToBase64(String type, Key key, byte[] input, boolean encode)
            throws CryptoUtilsException
    {
        return encryptToBase64(type, key, null, input, encode, null);
    }

    /**
     * Encrypt the input byte array.
     * 
     * @param type
     *        the algorithm type
     * @param key
     *        the key to be used
     * @param params
     *        the algorithm initialization parameters
     * @param input
     *        the byte array to encript
     * @param encode
     *        if true the the output is encoded qith the type prefix
     * @return the encrypted string with encoding Base64
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String encryptToBase64(String type, Key key, AlgorithmParameters params, byte[] input,
            boolean encode, AlgorithmParametersHolder paramsHolder) throws CryptoUtilsException
    {
        String kType = getTypeI(type);
        byte[] result = cypher(kType, Cipher.ENCRYPT_MODE, key, params, input, paramsHolder);
        result = Base64.encodeBase64(result);
        String output = null;
        try {
            output = new String(result, DEFAULT_STRING_ENCODING);
        }
        catch (UnsupportedEncodingException exc) {
            throw new CryptoUtilsException("Error encoding data", exc);
        }
        if (encode) {
            output = encodeType(type, output);
        }
        return output;
    }

    /**
     * Decrypt the input string.
     * 
     * @param type
     *        the algorithm type, if null or empty must be present as prefix in
     *        input
     * @param key
     *        the key to be used
     * @param input
     *        the string to decript, with encoding 'ISO-8859-1'
     * @param canBeClear
     *        if true the data can be unencrypted
     * @return the decrypted string
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String decryptFromBase64(String type, Key key, String input, boolean canBeClear)
            throws CryptoUtilsException
    {
        return decryptFromBase64(type, key, input, DEFAULT_STRING_ENCODING, canBeClear);
    }

    /**
     * Decrypt the input string.
     * 
     * @param type
     *        the algorithm type, if null or empty must be present as prefix in
     *        input
     * @param key
     *        the key to be used
     * @param input
     *        the string to decript
     * @param inputEnc
     *        the input encoding
     * @param canBeClear
     *        if true the data can be unencrypted
     * @return the decrypted string
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String decryptFromBase64(String type, Key key, String input, String inputEnc, boolean canBeClear)
            throws CryptoUtilsException
    {
        return decryptFromBase64(type, key, null, input, inputEnc, canBeClear);
    }

    /**
     * Decrypt the input string.
     * 
     * @param type
     *        the algorithm type, if null or empty must be present as prefix in
     *        input
     * @param key
     *        the key to be used
     * @param params
     *        the algorithm initialization parameters
     * @param input
     *        the string to decript
     * @param inputEnc
     *        the input encoding
     * @param canBeClear
     *        if true the data can be unencrypted
     * @return the decrypted string
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String decryptFromBase64(String type, Key key, AlgorithmParameters params, String input,
            String inputEnc, boolean canBeClear) throws CryptoUtilsException
    {
        try {
            if ((type == null) || (type.length() == 0)) {
                type = getType(input);
            }
            else {
                type = getTypeI(type);
            }

            input = removeType(input);
            byte[] result = cypher(type, Cipher.DECRYPT_MODE, key, params,
                    Base64.decodeBase64(input.getBytes(inputEnc)), null);

            return new String(result, inputEnc);
        }
        catch (CryptoUtilsException exc) {
            if (canBeClear) {
                return input;
            }
            throw exc;
        }
        catch (UnsupportedEncodingException exc) {
            if (canBeClear) {
                return input;
            }
            throw new CryptoUtilsException("Error encoding data", exc);
        }
    }

    /**
     * Decrypt the input byte array.
     * 
     * @param type
     *        the algorithm type, if null or empty must be present as prefix in
     *        input
     * @param key
     *        the key to be used
     * @param input
     *        the byte array to decript
     * @param canBeClear
     *        if true the data can be unencrypted
     * @return the decrypted string
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String decryptFromBase64(String type, Key key, byte[] input, boolean canBeClear)
            throws CryptoUtilsException
    {
        return decryptFromBase64(type, key, null, input, canBeClear);
    }

    /**
     * Decrypt the input byte array.
     * 
     * @param type
     *        the algorithm type, if null or empty must be present as prefix in
     *        input
     * @param key
     *        the key to be used
     * @param params
     *        the algorithm initialization parameters
     * @param input
     *        the byte array to decript
     * @param canBeClear
     *        if true the data can be unencrypted
     * @return the decrypted string
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String decryptFromBase64(String type, Key key, AlgorithmParameters pSpec, byte[] input,
            boolean canBeClear) throws CryptoUtilsException
    {
        try {
            try {
                if ((type == null) || (type.length() == 0)) {
                    type = getType(input);
                }
                else {
                    type = getTypeI(type);
                }

                input = decodeType(input);
                byte[] result = cypher(type, Cipher.DECRYPT_MODE, key, pSpec, Base64.decodeBase64(input), null);

                return new String(result, DEFAULT_STRING_ENCODING);
            }
            catch (CryptoUtilsException exc) {
                if (canBeClear) {
                    return new String(input, DEFAULT_STRING_ENCODING);
                }
                throw exc;
            }
        }
        catch (UnsupportedEncodingException exc) {
            throw new CryptoUtilsException("Error encoding data", exc);
        }
    }

    /**
     * Encrypt the input data buffer.
     * 
     * @param type
     *        the algorithm to be used
     * @param key
     *        the key to be used
     * @param input
     *        the data to encrypt
     * @param encode
     *        if true the the output is encoded with the type prefix
     * @return the encrypted data
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static byte[] encrypt(String type, Key key, byte[] input, boolean encode) throws CryptoUtilsException
    {
        return encrypt(type, key, null, input, encode, null);
    }

    /**
     * Encrypt the input data buffer.
     * 
     * @param type
     *        the algorithm to be used
     * @param key
     *        the key to be used
     * @param params
     *        the algorithm initialization parameters
     * @param input
     *        the data to encrypt
     * @param encode
     *        if true the the output is encoded with the type prefix
     * @return the encrypted data
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static byte[] encrypt(String type, Key key, AlgorithmParameters params, byte[] input, boolean encode,
            AlgorithmParametersHolder paramsHolder) throws CryptoUtilsException
    {
        String kType = getTypeI(type);
        byte[] output = cypher(kType, Cipher.ENCRYPT_MODE, key, params, input, paramsHolder);
        if (encode) {
            output = encodeType(type, output);
        }
        return output;
    }

    /**
     * Decrypt the input data buffer.
     * 
     * @param type
     *        the algorithm to be used
     * @param key
     *        the key to be used
     * @param input
     *        the data to decript
     * @param canBeClear
     *        if true the data can be unencrypted
     * @return the decrypted data
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static byte[] decrypt(String type, Key key, byte[] input, boolean canBeClear) throws CryptoUtilsException
    {
        return decrypt(type, key, null, input, canBeClear);
    }

    /**
     * Decrypt the input data buffer.
     * 
     * @param type
     *        the algorithm to be used
     * @param key
     *        the key to be used
     * @param params
     *        the algorithm initialization parameters
     * @param input
     *        the data to decript
     * @param canBeClear
     *        if true the data can be unencrypted
     * @return the decrypted data
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static byte[] decrypt(String type, Key key, AlgorithmParameters params, byte[] input, boolean canBeClear)
            throws CryptoUtilsException
    {
        if ((type == null) || (type.length() == 0)) {
            try {
                type = getType(input);
            }
            catch (CryptoUtilsException exc) {
                if (canBeClear) {
                    return input;
                }
                throw exc;
            }
        }
        else {
            type = getTypeI(type);
        }
        input = decodeType(input);
        return cypher(type, Cipher.DECRYPT_MODE, key, params, input, null);
    }

    /**
     * Encrypt the input data buffer.
     * 
     * @param type
     *        the algorithm to be used
     * @param mode
     *        the encrypt or decrypt mode
     * @param key
     *        the key to be used
     * @param input
     *        the data to encript
     * @return the encripted data
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    private static synchronized byte[] cypher(String type, int mode, Key key, AlgorithmParameters params, byte[] input,
            AlgorithmParametersHolder paramsHolder) throws CryptoUtilsException
    {
        try {
            /*
             * Create the cipher algorithms cipher algorithm/mode/padding or
             * algorithm
             */
            Cipher cipher = Cipher.getInstance(type);

            /*
             * Encrypt/Decrypt the message with the key.
             */
            if (params == null) {
                cipher.init(mode, key);
            }
            else {
                cipher.init(mode, key, params);
            }

            if (paramsHolder != null) {
                System.out.println(cipher.getParameters());
                paramsHolder.setAlgorithmParameters(cipher.getParameters());
            }

            return cipher.doFinal(input, 0, input.length);
        }
        catch (Exception exc) {
            throw new CryptoUtilsException("Error performing cypher operation", exc);
        }
    }

    /**
     * Get the algorithm used for encryption: DES, 3DES.
     * 
     * @param type
     *        the type prefix
     * @return the encryption algorithm
     * @throws CryptoUtilsException
     *         if error occurs
     */
    static String getTypeI(String type) throws CryptoUtilsException
    {
        String locType = type;
        if (locType.length() < 3) {
            throw new CryptoUtilsException("Invalid value for type prefix: " + type);
        }
        int mIdx = locType.indexOf("/");
        if (mIdx != -1) {
            locType = locType.substring(0, mIdx);
        }
        String sType = typeMap.get(locType);
        if (sType == null) {
            //throw new CryptoUtilsException("Invalid value for type prefix: " + type);
            return type;
        }

        if (mIdx != -1) {
            sType += type.substring(mIdx);
        }
        return sType;
    }

    /**
     * Get the algorithm used for encryption: DES, 3DES.
     * 
     * @param input
     *        the encrypted string, with the type prefix
     * @return the encryption algorithm
     * @throws CryptoUtilsException
     *         if error occurs
     */
    public static String getType(String input) throws CryptoUtilsException
    {
        int start = -1;
        int end = -1;

        if (input.startsWith("{")) {
            start = input.indexOf("{");
            end = input.indexOf("}");
        }
        if ((start == -1) || (end == -1)) {
            throw new CryptoUtilsException("Unable to extract the type prefix.");
        }
        String type = input.substring(start + 1, end - start);

        return getTypeI(type);
    }

    /**
     * Get the algorithm used for encryption.
     * 
     * @param input
     *        the encrypted string, with the type prefix
     * @return the encryption algorithm
     * @throws CryptoUtilsException
     *         if error occurs
     */
    public static String getType(byte[] input) throws CryptoUtilsException
    {
        String sInput = null;
        try {
            sInput = new String(input, DEFAULT_STRING_ENCODING);
        }
        catch (UnsupportedEncodingException exc) {
            throw new CryptoUtilsException("Error encoding data", exc);
        }
        return getType(sInput);
    }

    /**
     * Add the type prefix to input.
     * 
     * @param type
     *        the encryption type
     * @param input
     *        the input
     * @return the encoded input
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static String encodeType(String type, String input) throws CryptoUtilsException
    {
        getTypeI(type);
        return "{" + type + "}" + input;
    }

    /**
     * Add the type prefix to input.
     * 
     * @param type
     *        the encryption type
     * @param input
     *        the input
     * @return the encoded input
     * @throws CryptoUtilsException
     *         if errors occurs
     */
    public static byte[] encodeType(String type, byte[] input) throws CryptoUtilsException
    {
        getTypeI(type);
        byte[] bType = null;
        try {
            bType = ("{" + type + "}").getBytes(DEFAULT_STRING_ENCODING);
        }
        catch (UnsupportedEncodingException exc) {
            throw new CryptoUtilsException("Error encoding data", exc);
        }
        byte[] output = new byte[bType.length + input.length];
        System.arraycopy(bType, 0, output, 0, bType.length);
        System.arraycopy(input, 0, output, bType.length, input.length);
        return output;
    }

    /**
     * Remove the type prefix from input.
     * 
     * @param input
     *        the input
     * @return the encoded input
     */
    public static String removeType(String input)
    {
        if (!PropertiesHandler.isExpanded(input)) {
            return input;
        }
        try {
            getType(input);
            int end = input.indexOf("}");
            String output = input.substring(end + 1);
            return output;
        }
        catch (Exception exc) {
            return input;
        }
    }

    /**
     * Remove the type prefix from input.
     * 
     * @param input
     *        the input
     * @return the encoded input
     */
    public static byte[] decodeType(byte[] input)
    {
        try {
            String type = getType(input);
            byte[] bType = ("{" + type + "}").getBytes(DEFAULT_STRING_ENCODING);
            byte[] output = new byte[input.length - bType.length];
            System.arraycopy(input, bType.length, output, 0, input.length - bType.length);
            return output;
        }
        catch (Exception exc) {
            return input;
        }
    }

    /**
     * Generate a X.509 certificate for the given key pair.
     * 
     * @param keyPair
     *        the key pair to certify
     * @param serial
     *        the certificate serial number
     * @param issuer
     *        the certificate issuer CN
     * @param subject
     *        the certificate issuer CN
     * @param expires
     *        the certificate expiration date
     * @return the created certificate
     * @throws CryptoUtilsException
     *         if error occurs
     */
    public static Certificate getAutoCertificate(KeyPair keyPair, long serial, String issuer, String subject,
            Date expires) throws CryptoUtilsException
    {
        String signAlg = "";
        String signProv = "";
        if (keyPair.getPrivate().getAlgorithm().equals(DSA_TYPE)) {
            signAlg = DSA_SIGN_ALG;
            signProv = "SUN";
        }
        else {
            signAlg = RSA_SIGN_ALG;
            signProv = "SunRsaSign";
        }

        X509Name xName = new X509Name(issuer);
        X509V1CertificateGenerator certGenerator = new X509V1CertificateGenerator();
        certGenerator.setSerialNumber(BigInteger.valueOf(serial));
        certGenerator.setIssuerDN(xName);
        xName = new X509Name(subject);
        certGenerator.setSubjectDN(xName);
        certGenerator.setPublicKey(keyPair.getPublic());
        certGenerator.setSignatureAlgorithm(signAlg);
        certGenerator.setNotBefore(new Date());
        certGenerator.setNotAfter(expires);
        Certificate cert = null;
        try {
            cert = certGenerator.generate(keyPair.getPrivate(), signProv);
        }
        catch (Exception exc) {
            throw new CryptoUtilsException("Error occurred creating certificate for '" + issuer + "'", exc);
        }
        return cert;
    }

    /** **************************************************************** */

    /**
     * @param args
     *        the input arguments
     */
    public static void main(String[] args)
    {
        String infile = null;
        String outfile = null;
        String algType = TRIPLE_DES_TYPE;
        Key key = null;
        byte[] keyBytes = null;
        byte[] bmessage = null;
        String smessage = null;
        boolean verbose = false;
        boolean encode = false;
        boolean asString = false;
        String prAlias = "";
        String puAlias = "";
        String prPwd = "";
        String puPwd = "";
        KeyStoreID keySid = new KeyStoreID("TEMP", KeyStoreUtils.DEFAULT_KEYSTORE_TYPE, "", "",
                KeyStoreUtils.DEFAULT_KEYSTORE_PROVIDER);
        KeyID keyid = new KeyID("TEMP", keySid, "");
        String certIssuer = "";
        String certSubject = "";

        try {
            ArgsManager argsM = new ArgsManager("m:t:k:K:a:p:A:P:c:C:i:I:O:b:vsed", args);

            if (argsM.exist("d")) {
                dumpProviders();
                return;
            }

            verbose = argsM.exist("v");
            asString = argsM.exist("s");
            encode = argsM.exist("e");

            int mode = argsM.getInteger("m");
            if ((mode < 0) || (mode > 2)) {
                throw new Exception("Invalid value for -m argument: must be 0..2");
            }

            int type = argsM.getInteger("t");
            if ((type < 0) || (type > 3)) {
                throw new Exception("Invalid value for -t argument: must be 0..3");
            }

            keySid.setKeyStoreName(argsM.get("k", DEFAULT_KEY_FILE));
            keySid.setKeyStorePwd(argsM.get("K"));

            prAlias = argsM.get("A");
            prPwd = argsM.get("P");
            if (argsM.exist("-a")) {
                puAlias = argsM.get("a", "");
                puPwd = argsM.get("p", "");
            }
            else {
                keyid.setKeyAlias(prAlias);
                keyid.setKeyPwd(prPwd);
            }

            certIssuer = argsM.get("c", DEFAULT_CERT_ISSUER);
            certSubject = argsM.get("C", DEFAULT_CERT_SUBJECT);

            switch (type) {
                case 0 :
                    algType = DES_TYPE;
                    break;
                case 1 :
                    algType = TRIPLE_DES_TYPE;
                    break;
                case 2 :
                    algType = AES_TYPE;
                    break;
                case 3 :
                    algType = DSA_TYPE;
                    break;
                case 4 :
                    algType = RSA_TYPE;
                    break;
            }
            System.out.println("Mode: " + mode);
            System.out.println("Algorithm: " + algType);
            System.out.println("KeyStore: " + keyid);

            if (mode > 0) {
                if (argsM.exist("i")) {
                    smessage = argsM.get("i");
                }
                else {
                    infile = argsM.get("I");
                }
                if (verbose) {
                    outfile = argsM.get("O", "");
                }
                else {
                    outfile = argsM.get("O");
                }
                key = KeyStoreUtils.readKey(keyid);
            }

            System.out.println("In file : " + infile);
            System.out.println("Out file: " + outfile);

            switch (mode) {
                case 0 : // generate key in key store file
                    if (argsM.exist("b")) {
                        keyBytes = Base64.decodeBase64(argsM.get("b").getBytes());
                    }
                    makeKey(algType, prAlias, puAlias, prPwd, puPwd, certIssuer, certSubject, keyid, keyBytes, type);
                    break;
                case 1 : // encrypt(keyfile) infile -> outfile
                    if (infile != null) {
                        bmessage = BinaryUtils.readFileAsBytes(infile);
                    }
                    else {
                        bmessage = smessage.getBytes(DEFAULT_STRING_ENCODING);
                    }
                    bmessage = encryptData(outfile, algType, key, bmessage, verbose, encode, asString);
                    if (outfile.length() > 0) {
                        BinaryUtils.writeBytesToFile(bmessage, outfile);
                    }
                    break;
                case 2 : // encrypt(keyfile) infile -> outfile
                    if (infile != null) {
                        bmessage = BinaryUtils.readFileAsBytes(infile);
                    }
                    else {
                        bmessage = smessage.getBytes(DEFAULT_STRING_ENCODING);
                    }
                    bmessage = decryptData(outfile, algType, key, bmessage, verbose, asString);
                    if (outfile.length() > 0) {
                        BinaryUtils.writeBytesToFile(bmessage, outfile);
                    }
                    break;
            }

        }
        catch (ArgsManagerException exc) {
            System.out.println(exc.getMessage());
            usage();
        }
        catch (Exception exc) {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
    }

    /**
     * Usage
     */
    private static void usage()
    {
        System.out.println();
        System.out.println("Usage:");
        System.out.println("\tCryptoUtils <-m mode> <-t type> [-k file] <-K pwd> <-A alias> [-P pwd] [-a alias [-p pwd]] [-i data | -I file] [-O file] [-e] [-s] [-v]");
        System.out.println("\t-m : 0 generate key -> -k or cukeydef.dat");
        System.out.println("\t     1/2 encrypt/decrypt -i or -I -> -O or stdout");
        System.out.println("\t-k : keystore (default cukeystore.dat)");
        System.out.println("\t-K : keystore password");
        System.out.println("\t-A : private/secret key alias");
        System.out.println("\t-P : private/secret key password");
        System.out.println("\t-a : public key alias");
        System.out.println("\t-p : public key password");
        System.out.println("\t-t : 0 DES (Secret)");
        System.out.println("\t     1 3DES (Secret)");
        System.out.println("\t     2 AES (Secret)");
        System.out.println("\t     3 DSA (Private/Public)");
        System.out.println("\t     4 RSA (Private/Public)");
        System.out.println("\t-e : encode the encryption type in the output");
        System.out.println("\t-s : input and output as string (Base64)");
        System.out.println("\t-d : dump Crypto providers data");
        System.out.println("\t-v : verbose");
    }

    /**
     * @param outfile
     *        .
     * @param algType
     *        .
     * @param key
     *        .
     * @param bmessage
     *        .
     * @param verbose
     *        .
     * @param asString
     *        .
     * @return the decrypted data
     * @throws Exception
     *         if an error occurs
     */
    private static byte[] decryptData(String outfile, String algType, Key key, byte[] bmessage, boolean verbose,
            boolean asString) throws Exception
    {
        String smessage;
        if (asString) {
            smessage = new String(bmessage, DEFAULT_STRING_ENCODING);
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Input data: \n'" + smessage + "'");
                System.out.println("***************************************");
            }
            smessage = decryptFromBase64(algType, key, smessage, true);
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Output data: \n'" + smessage + "'");
                System.out.println("***************************************");
            }
            if (outfile.length() > 0) {
                bmessage = smessage.getBytes(DEFAULT_STRING_ENCODING);
            }
        }
        else {
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Input data: \n" + new Dump(bmessage, Dump.UNBOUNDED));
                System.out.println("***************************************");
            }
            bmessage = decrypt(algType, key, bmessage, false);
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Output data: \n" + new Dump(bmessage, Dump.UNBOUNDED));
                System.out.println("***************************************");
            }
        }
        return bmessage;
    }

    /**
     * @param outfile
     *        .
     * @param algType
     *        .
     * @param key
     *        .
     * @param bmessage
     *        .
     * @param verbose
     *        .
     * @param encode
     *        .
     * @param asString
     *        .
     * @return the encrypted data
     * @throws Exception
     *         if an error occurs
     */
    private static byte[] encryptData(String outfile, String algType, Key key, byte[] bmessage, boolean verbose,
            boolean encode, boolean asString) throws Exception
    {
        String smessage;
        if (asString) {
            smessage = new String(bmessage, DEFAULT_STRING_ENCODING);
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Input data: \n'" + smessage + "'");
                System.out.println("***************************************");
            }
            smessage = encryptToBase64(algType, key, smessage, encode);
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Output data: \n'" + smessage + "'");
                System.out.println("***************************************");
            }
            if (outfile.length() > 0) {
                bmessage = smessage.getBytes(DEFAULT_STRING_ENCODING);
            }
        }
        else {
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Input data: \n" + new Dump(bmessage, Dump.UNBOUNDED));
                System.out.println("***************************************");
            }
            bmessage = encrypt(algType, key, bmessage, encode);
            if (verbose) {
                System.out.println("***************************************");
                System.out.println("Output data: \n" + new Dump(bmessage, Dump.UNBOUNDED));
                System.out.println("***************************************");
            }
        }
        return bmessage;
    }

    /**
     * @param algType
     *        .
     * @param prAlias
     *        .
     * @param puAlias
     *        .
     * @param prPwd
     *        .
     * @param puPwd
     *        .
     * @param certIssuer
     *        .
     * @param certSubject
     *        .
     * @param keyid
     *        .
     * @param type
     *        .
     * @throws Exception
     *         if an error occurs
     */
    private static void makeKey(String algType, String prAlias, String puAlias, String prPwd, String puPwd,
            String certIssuer, String certSubject, KeyID keyid, byte[] keyBytes, int type) throws Exception
    {
        Key key;
        switch (type) {
            case 0 :
            case 1 :
            case 2 :
                key = generateSecretKey(algType, keyBytes);
                keyid.setKeyAlias(prAlias);
                keyid.setKeyPwd(prPwd);
                System.out.println("***************************************");
                System.out.println("Registering SecretKey: " + key.getAlgorithm() + " " + key.getFormat() + " "
                        + key.toString());
                System.out.println("In: " + keyid);
                KeyStoreUtils.writeKey(keyid, key, null);
                System.out.println("***************************************");
                break;
            case 3 :
            case 4 :
                KeyPair kPair = generateKeyPair(algType);
                Certificate[] cert = new Certificate[1];
                Date date = new Date();
                date.setTime(date.getTime() + DEFAUL_CERT_EXPIRATION);
                cert[0] = getAutoCertificate(kPair, Math.abs(new Random().nextLong()), certIssuer, certSubject, date);
                keyid.setKeyAlias(prAlias);
                keyid.setKeyPwd(prPwd);
                System.out.println("***************************************");
                System.out.println("Registering PrivateKey: " + kPair.getPrivate());
                System.out.println("With Certificate: " + cert[0]);
                System.out.println("In: " + keyid);
                KeyStoreUtils.writeKey(keyid, kPair.getPrivate(), cert);
                System.out.println("***************************************");
                keyid.setKeyAlias(puAlias);
                keyid.setKeyPwd(puPwd);
                System.out.println("Registering PublicKey: " + kPair.getPublic());
                System.out.println("In: " + keyid);
                KeyStoreUtils.writeKey(keyid, kPair.getPublic(), null);
                System.out.println("***************************************");
                break;
        }
    }

    private static void dumpProviders() throws Exception
    {

    }
}
