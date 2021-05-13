package it.eng.knowage.knowageapi.service;

import java.util.List;

import org.json.JSONException;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTag;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

public interface WidgetGalleryAPI {

	List<WidgetGalleryDTO> getWidgets() throws JSONException;

	void updateGalleryCounter(SbiWidgetGallery newSbiWidgetGallery);

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