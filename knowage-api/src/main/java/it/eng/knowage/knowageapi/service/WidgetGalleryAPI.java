/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.knowageapi.service;

import java.util.List;

import org.json.JSONException;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTag;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

public interface WidgetGalleryAPI {

	List<WidgetGalleryDTO> getWidgets() throws JSONException;

//	WidgetGalleryDTO updateGalleryCounter(SbiWidgetGallery newSbiWidgetGallery);

	List<SbiWidgetGalleryTag> createNewWidgetTagsByList(SbiWidgetGallery sbiWidgetGallery, String userid, String tags);

	boolean canSeeGallery(SpagoBIUserProfile userProfile);

	WidgetGalleryDTO getWidgetsById(String id, SpagoBIUserProfile profile) throws JSONException;

	List<WidgetGalleryDTO> getWidgetsByTenant(SpagoBIUserProfile profile) throws JSONException;

	List<WidgetGalleryDTO> getWidgetsByTenantType(SpagoBIUserProfile profile, String type) throws JSONException;

	int deleteGallery(String id, SpagoBIUserProfile profile);

	WidgetGalleryDTO updateWidget(WidgetGalleryDTO widgetGalleryDTO, SpagoBIUserProfile profile);

	WidgetGalleryDTO makeNewWidget(WidgetGalleryDTO widgetGalleryDTO, SpagoBIUserProfile profile, boolean create);

	WidgetGalleryDTO importOrUpdateWidget(WidgetGalleryDTO widgetGalleryDTO, SpagoBIUserProfile profile) throws JSONException;

//	public default WidgetGalleryDTO updateSbiCommonInfo4Update(WidgetGalleryDTO obj, boolean useDefaultTenant) {
//		obj.setTimeUp(new Date());
//		obj.setSbiVersionUp(SbiCommonInfo.getVersion());
//		obj.getCommonInfo().setUserUp(userID);
//		String tenantId = this.getTenant();
//		// sets the tenant if it is set and input object hasn't
//		if (tenantId != null && obj.getCommonInfo().getOrganization() == null) {
//			obj.getCommonInfo().setOrganization(tenantId);
//		}
//		if (obj.getCommonInfo().getOrganization() == null) {
//			if (useDefaultTenant)
//				obj.getCommonInfo().setOrganization(TENANT_DEFAULT);
//			else
//				throw new KnowageRuntimeException("Organization not set!!!");
//		}
//		return obj;
//	}
//
//	public default WidgetGalleryDTO updateSbiCommonInfo4Insert(WidgetGalleryDTO obj) {
//		obj.getCommonInfo().setTimeIn(new Date());
//		obj.getCommonInfo().setSbiVersionIn(SbiCommonInfo.getVersion());
//		obj.getCommonInfo().setUserIn(userID);
//
//		// sets the tenant if it is set and input object hasn't
//		String tenantId = this.getTenant();
//		if (tenantId != null && obj.getCommonInfo().getOrganization() == null) {
//			obj.getCommonInfo().setOrganization(tenantId);
//		}
//
//		if (obj.getCommonInfo().getOrganization() == null) {
//			if (useDefaultTenant)
//				obj.getCommonInfo().setOrganization(TENANT_DEFAULT);
//			else
//		if (obj.getCommonInfo().getOrganization() == null) {
//			throw new KnowageRuntimeException("Organization not set!!!");
//		}
//		return obj;
//	}
}