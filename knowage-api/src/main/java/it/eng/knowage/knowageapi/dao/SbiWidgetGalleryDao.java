package it.eng.knowage.knowageapi.dao;

import java.util.Collection;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;

public interface SbiWidgetGalleryDao {

	String create(SbiWidgetGallery sbiWidgetGallery);

	String update(SbiWidgetGallery sbiWidgetGallery);

	String updateCounter(SbiWidgetGallery sbiWidgetGallery);

	SbiWidgetGallery findById(String id);

	Collection<SbiWidgetGallery> findAll();

	int deleteById(String id);

	SbiWidgetGallery findByIdTenant(String id, String tenant);

	Collection<SbiWidgetGallery> findAllByTenant(String tenant);

	Collection<SbiWidgetGallery> findAllByTenantAndType(String tenant, String type);

	int deleteByIdTenant(String id, String tenant);

}