package it.eng.knowage.knowageapi.service;

import java.util.List;

import org.json.JSONException;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTag;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;

public interface WidgetGalleryAPI {

	List<WidgetGalleryDTO> getWidgets() throws JSONException;

	List<WidgetGalleryDTO> getWidgetsByTenant(String tenant) throws JSONException;

	WidgetGalleryDTO getWidgetsById(String id, String tenant) throws JSONException;

	WidgetGalleryDTO createNewGallery(String name, String type, String author, String description, String licenseText, String licenseName, String organization,
			String image, String sbiversion, String template, String userid, String tags);

	void updateGallery(String uuid, String name, String type, String author, String description, String licenseText, String licenseName, String organization,
			String image, String sbiversion, String template, String userid, String tags);

	void updateGalleryCounter(SbiWidgetGallery newSbiWidgetGallery);

	int deleteGallery(String id, String tenant);

	WidgetGalleryDTO createWidgetGalleryDTO(String name, String type, String author, String description, String licenseText, String licenseName,
			String organization, String image, String sbiversion, String template, String userid, String tags);

	List<SbiWidgetGalleryTag> createNewWidgetTagsByList(SbiWidgetGallery sbiWidgetGallery, String userid, String tags);

}