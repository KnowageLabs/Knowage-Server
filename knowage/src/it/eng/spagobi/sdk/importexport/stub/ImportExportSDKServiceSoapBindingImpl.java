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

package it.eng.spagobi.sdk.importexport.stub;

import it.eng.spagobi.sdk.importexport.impl.ImportExportSDKServiceImpl;

public class ImportExportSDKServiceSoapBindingImpl implements
		it.eng.spagobi.sdk.importexport.stub.ImportExportSDKService {
	public it.eng.spagobi.sdk.importexport.bo.SDKFile importDocuments(
			it.eng.spagobi.sdk.importexport.bo.SDKFile in0,
			it.eng.spagobi.sdk.importexport.bo.SDKFile in1,
			boolean in2) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
		ImportExportSDKServiceImpl serviceImpl = new ImportExportSDKServiceImpl();
		return serviceImpl.importDocuments(in0, in1,
				in2);
	}

}
