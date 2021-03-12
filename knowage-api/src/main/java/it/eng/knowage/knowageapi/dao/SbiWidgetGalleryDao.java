package it.eng.knowage.knowageapi.dao;

import java.util.Collection;
import java.util.UUID;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;

public interface SbiWidgetGalleryDao {

	UUID create(SbiWidgetGallery sbiWidgetGallery);

	SbiWidgetGallery findById(int id);

	Collection<SbiWidgetGallery> findAll();

}