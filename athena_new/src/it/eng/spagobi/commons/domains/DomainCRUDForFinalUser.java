/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.domains;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Provide a subset of functionalities of the DomainCRUD. This replication of the code is useful to give more than one SpagoBI functionality to the same physical functionality 
 */
@Path("/domainsforfinaluser")
public class DomainCRUDForFinalUser extends DomainCRUD{
	
	@GET
	@Path("/listValueDescriptionByType")
	@Produces(MediaType.APPLICATION_JSON)
	public String getListDomainsByType(@Context HttpServletRequest req){
		return super.getListDomainsByType(req);
	}
}
