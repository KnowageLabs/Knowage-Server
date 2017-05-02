/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
