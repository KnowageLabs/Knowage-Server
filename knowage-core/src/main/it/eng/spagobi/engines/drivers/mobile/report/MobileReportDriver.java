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
package it.eng.spagobi.engines.drivers.mobile.report;

import java.util.Map;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;

public class MobileReportDriver extends GenericDriver {

	@Override
	public Map getParameterMap(Object biobject, IEngUserProfile profile,
			String roleName) {
		throw new RuntimeException(
				"This kind of document cannot be executed within SpagoBI server; " +
				"log into SpagoBI Mobile application using a mobile device and retry");
	}

	@Override
	public Map getParameterMap(Object object, Object subObject,
			IEngUserProfile profile, String roleName) {
		throw new RuntimeException(
				"This kind of document cannot be executed within SpagoBI server; " +
				"log into SpagoBI Mobile application using a mobile device and retry");
	}

}
