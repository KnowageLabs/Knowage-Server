/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
