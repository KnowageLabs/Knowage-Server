package it.eng.knowage.knowageapi.dao;

import java.util.Collection;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTag;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTagId;

public interface SbiWidgetGalleryTagDao {

	SbiWidgetGalleryTagId create(SbiWidgetGalleryTag sbiWidgetGalleryTag);

	SbiWidgetGalleryTag findById(int id);

	Collection<SbiWidgetGalleryTag> findAll();

}
