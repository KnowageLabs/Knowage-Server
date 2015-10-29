/**
 * SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.eng.spagobi.security.hmacfilter;

import it.eng.spagobi.utilities.Helper;

/**
 * Token validator based on System Clock: the client token is valid if it's in the same time window (defined by
 * {@link SystemTimeHMACTokenValidator#MAX_TIME_DELTA_DEFAULT} of this server.
 *
 * @author fabrizio
 *
 */
public class SystemTimeHMACTokenValidator implements HMACTokenValidator {

	private final long maxDeltaMillis;

	public SystemTimeHMACTokenValidator(long maxDeltaMillis) {
		Helper.checkNotNegative(maxDeltaMillis, "maxDeltaMillis");

		this.maxDeltaMillis = maxDeltaMillis;
	}

	@Override
	public void validate(String token) throws HMACSecurityException {
		Helper.checkNotNullNotTrimNotEmpty(token, "token");

		long time;
		try {
			time = Long.parseLong(token);
		} catch (NumberFormatException e) {
			throw new HMACSecurityException("Token doesn't represent a time");
		}

		long current = System.currentTimeMillis();
		if (Math.abs(current - time) > maxDeltaMillis) {
			throw new HMACSecurityException("Token is not valid");
		}
	}

	@Override
	public String generateToken() throws HMACSecurityException {
		return Long.toString(System.currentTimeMillis());
	}

}
