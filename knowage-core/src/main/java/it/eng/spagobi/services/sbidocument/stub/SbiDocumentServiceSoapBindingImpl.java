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

package it.eng.spagobi.services.sbidocument.stub;

import it.eng.spagobi.services.sbidocument.service.SbiDocumentServiceImpl;

public class SbiDocumentServiceSoapBindingImpl implements it.eng.spagobi.services.sbidocument.stub.SbiDocumentService{
    public it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver[] getDocumentAnalyticalDrivers(java.lang.String in0, java.lang.String in1, java.lang.Integer in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException {
        SbiDocumentServiceImpl impl = new SbiDocumentServiceImpl();
        return impl.getDocumentAnalyticalDrivers(in0, in1, in2, in3, in4);
    }

    public java.lang.String getDocumentAnalyticalDriversJSON(java.lang.String in0, java.lang.String in1, java.lang.Integer in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException {
        SbiDocumentServiceImpl impl = new SbiDocumentServiceImpl();
        return impl.getDocumentAnalyticalDriversJSON(in0, in1, in2, in3, in4);
    }

}
