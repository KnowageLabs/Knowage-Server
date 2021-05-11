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
package it.eng.spagobi.services.execute;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import it.eng.spagobi.services.common.ParametersWrapper;

/**
 * @author Angelo Bernabei angelo.bernabei@eng.it
 * @author Marco Libanori
 */
@WebService(
		name = "DocumentExecuteServiceService",
		portName = "DocumentExecuteServicePort",
		serviceName = "DocumentExecuteService",
		targetNamespace = "http://documentexecute.services.spagobi.eng.it/"
	)
@SOAPBinding(style = Style.RPC)
public interface DocumentExecuteService {
	/**
	 * Return the IMAGE of a chart The primary goal is to integrate a chart in a JasperReport
	 *
	 * @param token
	 * @param user
	 * @param document
	 * @param parameters
	 * @return
	 */
	byte[] executeChart(String token, String user, String document, ParametersWrapper parameters);

	/**
	 * Returns the XML DATA of the Kpi value with id kpiValueID
	 *
	 * @param token
	 * @param user
	 * @param kpiValueID
	 * @return
	 */
	String getKpiValueXML(String token, String user, Integer kpiValueID);

}
