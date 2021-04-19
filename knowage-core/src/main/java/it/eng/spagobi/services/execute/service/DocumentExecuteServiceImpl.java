/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

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

package it.eng.spagobi.services.execute.service;

import javax.jws.WebService;

import it.eng.spagobi.services.execute.DocumentExecuteService;
import it.eng.spagobi.services.execute.bo.ParametersWrapper;

/**
 * @author Marco Libanori
 */
@WebService(
		name = "DocumentExecuteServiceService",
		portName = "DocumentExecuteServicePort",
		serviceName = "DocumentExecuteService",
		targetNamespace = "http://documentexecute.services.spagobi.eng.it/"
	)
public class DocumentExecuteServiceImpl implements DocumentExecuteService {

	@Override
	public byte[] executeChart(String token, String user, String document, ParametersWrapper parameters) {
		ServiceChartImpl service = new ServiceChartImpl();
		return service.executeChart(token, user, document, parameters.getMap());
	}

	@Override
	public String getKpiValueXML(String token, String user, Integer kpiValueID) {
		ServiceKpiValueXml service = new ServiceKpiValueXml();
		return service.getKpiValueXML(token, user, kpiValueID);
	}

}
