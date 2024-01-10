/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.encryption;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.ZeroSaltGenerator;

/**
 *
 */
public class EncryptorFactory {

	private static final EncryptorFactory INSTANCE = new EncryptorFactory();

	public static EncryptorFactory getInstance() {
		return INSTANCE;
	}

	private EncryptorFactory() {

	}

	public PBEStringEncryptor create(EncryptionConfiguration cfg) {
		String algorithm = cfg.getAlgorithm();
		String password = cfg.getEncryptionPwd();

		return create(algorithm, password);
	}

	public PBEStringEncryptor create(String algorithm, String password) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

		encryptor.setAlgorithm(algorithm);
		encryptor.setPassword(password);
		encryptor.setSaltGenerator(new ZeroSaltGenerator());

		return encryptor;
	}
}
