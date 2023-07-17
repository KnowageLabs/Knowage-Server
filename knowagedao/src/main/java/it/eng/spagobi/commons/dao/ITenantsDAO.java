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

import org.json.JSONObject;

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

	List<SbiTenant> loadAllTenants();

	SbiTenant loadTenantByName(String name) throws EMFUserError;

	SbiTenant loadTenantById(Integer id) throws EMFUserError;

	List<SbiOrganizationDatasource> loadSelectedDS(String tenant) throws EMFUserError;

	List<SbiOrganizationProductType> loadSelectedProductTypes(String tenant) throws EMFUserError;

	void insertTenant(SbiTenant aTenant) throws EMFUserError;

	void modifyTenant(SbiTenant aTenant) throws EMFUserError, Exception;

	void deleteTenant(SbiTenant aTenant) throws EMFUserError;

	SbiUser initializeAdminUser(SbiTenant aTenant);

	List<Integer> loadSelectedProductTypesIds(String tenant) throws EMFUserError;

	/**
	 * @param name
	 * @return
	 * @throws EMFUserError
	 */
	Set loadThemesByTenantName(String name) throws EMFUserError;

	/**
	 *
	 */

	String updateThemes(IEngUserProfile profile, String uuid, String themeName, JSONObject newThemeConfig, boolean isActive) throws EMFUserError;

	void deleteTheme(IEngUserProfile profile, String uuid) throws EMFUserError;

}
