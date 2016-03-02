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

package it.eng.spagobi.services.execute.stub;

import it.eng.spagobi.services.event.service.EventServiceImpl;
import it.eng.spagobi.services.execute.service.ServiceChartImpl;
import it.eng.spagobi.services.execute.service.ServiceKpiValueXml;

public class DocumentExecuteServiceSoapBindingImpl implements it.eng.spagobi.services.execute.stub.DocumentExecuteService{
    public byte[] executeChart(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.util.HashMap in3) throws java.rmi.RemoteException {
    	ServiceChartImpl service=new ServiceChartImpl();
    	return service.executeChart(in0, in1, in2, in3);
    }

    public java.lang.String getKpiValueXML(java.lang.String in0, java.lang.String in1, java.lang.Integer in2) throws java.rmi.RemoteException {
        ServiceKpiValueXml service = new ServiceKpiValueXml();
        return service.getKpiValueXML(in0, in1, in2);
    }

}
