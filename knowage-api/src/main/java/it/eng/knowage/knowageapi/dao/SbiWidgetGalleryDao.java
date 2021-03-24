package it.eng.knowage.knowageapi.dao;

import java.util.Collection;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;

public interface SbiWidgetGalleryDao {

	String create(SbiWidgetGallery sbiWidgetGallery);

	String update(SbiWidgetGallery sbiWidgetGallery);

	SbiWidgetGallery findById(String id);

	Collection<SbiWidgetGallery> findAll();

	int deleteById(String id);

}