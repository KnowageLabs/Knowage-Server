/**
 * SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package it.eng.spagobi.security.hmacfilter;

/**
 * It's used by {@link HMACFilter} to validate the uniqueness token.
 *
 * @author fabrizio
 *
 */
public interface HMACTokenValidator {

	public void validate(String token) throws HMACSecurityException;

	public String generateToken() throws HMACSecurityException;
}
