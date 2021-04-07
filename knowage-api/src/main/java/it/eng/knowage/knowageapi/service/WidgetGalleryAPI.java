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

	WidgetGalleryDTO createWidgetGalleryDTO(String name, String type, String author, String description, String image, String sbiversion, String template,
			SpagoBIUserProfile profile, String tags);

	void updateGallery(String uuid, String name, String type, String author, String description, String image, String sbiversion, String template,
			SpagoBIUserProfile profile, String tags);

	WidgetGalleryDTO getWidgetsById(String id, SpagoBIUserProfile profile) throws JSONException;

	List<WidgetGalleryDTO> getWidgetsByTenant(SpagoBIUserProfile profile) throws JSONException;

	WidgetGalleryDTO createNewGallery(String name, String type, String author, String description, String image, String sbiversion, String template,
			SpagoBIUserProfile profile, String tags);

	int deleteGallery(String id, SpagoBIUserProfile profile);
}