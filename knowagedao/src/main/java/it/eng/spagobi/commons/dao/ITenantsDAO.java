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
package it.eng.spagobi.commons.dao;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductType;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.profiling.bean.SbiUser;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */

public interface ITenantsDAO extends ISpagoBIDao {

	public List<SbiTenant> loadAllTenants();

	public SbiTenant loadTenantByName(String name) throws EMFUserError;

	public SbiTenant loadTenantById(Integer id) throws EMFUserError;

	// public List<SbiOrganizationEngine> loadSelectedEngines(String tenant) throws EMFUserError;

	public List<SbiOrganizationDatasource> loadSelectedDS(String tenant) throws EMFUserError;

	public List<SbiOrganizationProductType> loadSelectedProductTypes(String tenant) throws EMFUserError;

	public void insertTenant(SbiTenant aTenant) throws EMFUserError;

	public void modifyTenant(SbiTenant aTenant) throws EMFUserError, Exception;

	public void deleteTenant(SbiTenant aTenant) throws EMFUserError;

	public SbiUser initializeAdminUser(SbiTenant aTenant);

	public List<Integer> loadSelectedProductTypesIds(String tenant) throws EMFUserError;

	/**
	 * @param name
	 * @return
	 * @throws EMFUserError
	 */
	public Set loadThemesByTenantName(String name) throws EMFUserError;

	/**
	 *
	 */

	public String updateThemes(IEngUserProfile profile, String uuid, String themeName, ObjectNode newThemeConfig, boolean isActive) throws EMFUserError;

	public void deleteTheme(IEngUserProfile profile, String uuid) throws EMFUserError;

}
