/*
 * Copyright (c) 2023- GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.configuration.util;

import it.greenvulcano.util.crypto.CryptoHelperException;
import it.greenvulcano.util.crypto.CryptoUtilsException;

/**
 *
 * @version 3.4.0 May 10, 2023
 * @author GreenVulcano Developer Team
 */
public interface ConfigCryptoInterface {
    /**
     * Decrypt the given data with the algorithm of the keyId key. The input
     * must be encoded in Base64 and with the type prefix.
     *
     * @param keyID
     *        the key identification name
     * @param data
     *        the data to decrypt, with encoding 'ISO-8859-1'
     * @param canBeClear
     *        if true the data can be clear
     * @return the decrypted data
     * @throws CryptoHelperException
     *         if error occurs
     * @throws CryptoUtilsException
     *         if error occurs
     */
    String decrypt(String value, String keyId, boolean canBeClear) throws CryptoHelperException, CryptoUtilsException;

    /**
     * Encrypt the given data with the algorithm of the keyId key. The result
     * is encoded in Base64 and with the type prefix.
     *
     * @param keyId
     *        the key identification name
     * @param data
     *        the data to encrypt, with encoding 'ISO-8859-1'
     * @param encode
     *        if true the the output is encoded with the type prefix
     *
     * @return the encrypted data
     * @throws CryptoHelperException
     *         if error occurs
     * @throws CryptoUtilsException
     *         if error occurs
     */
    String encrypt(String value, String keyId, boolean encode) throws CryptoHelperException, CryptoUtilsException;
}
