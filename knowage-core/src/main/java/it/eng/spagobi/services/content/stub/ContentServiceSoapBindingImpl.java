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

package it.eng.spagobi.services.content.stub;

import it.eng.spagobi.services.content.service.ContentServiceImpl;
import it.eng.spagobi.services.content.service.MapCatalogueImpl;
import it.eng.spagobi.services.content.service.PublishImpl;

public class ContentServiceSoapBindingImpl implements
		it.eng.spagobi.services.content.stub.ContentService {
	public it.eng.spagobi.services.content.bo.Content readTemplate(
			java.lang.String in0, java.lang.String in1, java.lang.String in2,
			java.util.HashMap in3) throws java.rmi.RemoteException {
		ContentServiceImpl service = new ContentServiceImpl();
		return service.readTemplate(in0, in1, in2, in3);
	}

	public it.eng.spagobi.services.content.bo.Content readTemplateByLabel(
			java.lang.String in0, java.lang.String in1, java.lang.String in2,
			java.util.HashMap in3) throws java.rmi.RemoteException {
		ContentServiceImpl service = new ContentServiceImpl();
		return service.readTemplateByLabel(in0, in1, in2, in3);
	}

	public it.eng.spagobi.services.content.bo.Content readSubObjectContent(
			java.lang.String in0, java.lang.String in1, java.lang.String in2,
			java.lang.Integer in3) throws java.rmi.RemoteException {
		ContentServiceImpl service = new ContentServiceImpl();
		return service.readSubObjectContent(in0, in1, in2, in3);
	}

	public it.eng.spagobi.services.content.bo.Content readSubObjectContent(
			java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException {
		ContentServiceImpl service = new ContentServiceImpl();
		return service.readSubObjectContent(in0, in1, in2);
	}

	public java.lang.String saveSubObject(java.lang.String in0,
			java.lang.String in1, java.lang.String in2, java.lang.String in3,
			java.lang.String in4, java.lang.String in5, java.lang.String in6)
			throws java.rmi.RemoteException {
		ContentServiceImpl service = new ContentServiceImpl();
		return service.saveSubObject(in0, in1, in2, in3, in4, in5, in6);
	}

	public java.lang.String saveObjectTemplate(java.lang.String in0,
			java.lang.String in1, java.lang.String in2, java.lang.String in3,
			java.lang.String in4) throws java.rmi.RemoteException {
		ContentServiceImpl service = new ContentServiceImpl();
		return service.saveObjectTemplate(in0, in1, in2, in3, in4);
	}

	public it.eng.spagobi.services.content.bo.Content downloadAll(
			java.lang.String in0, java.lang.String in1, java.lang.String in2,
			java.lang.String in3) throws java.rmi.RemoteException {
		ContentServiceImpl service = new ContentServiceImpl();
		return service.downloadAll(in0, in1, in2, in3);
	}

	public java.lang.String publishTemplate(java.lang.String in0,
			java.lang.String in1, java.util.HashMap in2)
			throws java.rmi.RemoteException {
		PublishImpl service = new PublishImpl();
		return service.publishTemplate(in0, in1, in2);
	}

	public java.lang.String mapCatalogue(java.lang.String in0,
			java.lang.String in1, java.lang.String in2, java.lang.String in3,
			java.lang.String in4, java.lang.String in5)
			throws java.rmi.RemoteException {
		MapCatalogueImpl service = new MapCatalogueImpl();
		return service.mapCatalogue(in0, in1, in2, in3, in4, in5);
	}

	public it.eng.spagobi.services.content.bo.Content readMap(
			java.lang.String in0, java.lang.String in1, java.lang.String in2)
			throws java.rmi.RemoteException {
		MapCatalogueImpl service = new MapCatalogueImpl();
		return service.readMap(in0, in1, in2);
	}
}
