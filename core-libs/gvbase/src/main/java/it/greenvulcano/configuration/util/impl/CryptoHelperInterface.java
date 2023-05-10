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
package it.greenvulcano.configuration.util.impl;

import it.greenvulcano.configuration.util.ConfigCryptoInterface;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.crypto.CryptoHelperException;
import it.greenvulcano.util.crypto.CryptoUtilsException;

/**
*
* @version 3.4.0 May 10, 2023
* @author GreenVulcano Developer Team
*/
public class CryptoHelperInterface implements ConfigCryptoInterface {

    @Override
    public String decrypt(String value, String keyId, boolean canBeClear) throws CryptoHelperException, CryptoUtilsException {
        return CryptoHelper.decrypt(keyId, value, canBeClear);
    }

    @Override
    public String encrypt(String value, String keyId, boolean encode) throws CryptoHelperException, CryptoUtilsException {
        return CryptoHelper.encrypt(keyId, value, encode);
    }
}
