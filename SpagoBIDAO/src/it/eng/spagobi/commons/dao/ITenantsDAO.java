/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationEngine;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.profiling.bean.SbiUser;

import java.util.List;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */

public interface ITenantsDAO extends ISpagoBIDao {

	public List<SbiTenant> loadAllTenants();
	
	public SbiTenant loadTenantByName(String name) throws EMFUserError;
	
	public SbiTenant loadTenantById(Integer id) throws EMFUserError;
	
	public List<SbiOrganizationEngine> loadSelectedEngines(String tenant) throws EMFUserError;
	
	public List<SbiOrganizationDatasource> loadSelectedDS(String tenant) throws EMFUserError;
	
	public void insertTenant(SbiTenant aTenant) throws EMFUserError;
	
	public void modifyTenant(SbiTenant aTenant) throws EMFUserError, Exception;
	
	public void deleteTenant(SbiTenant aTenant)throws EMFUserError;
	
	public SbiUser initializeAdminUser(SbiTenant aTenant);
	
}
