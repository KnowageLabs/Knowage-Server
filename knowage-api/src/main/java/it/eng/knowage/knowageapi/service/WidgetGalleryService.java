package it.eng.knowage.knowageapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.dao.SbiWidgetGalleryDao;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;

@Component
public class WidgetGalleryService {

	@Autowired
	private SbiWidgetGalleryDao sbiWidgetGalleryDao;

	public List<WidgetGalleryDTO> getWidgets() {

		List<SbiWidgetGallery> widgets = (List<SbiWidgetGallery>) sbiWidgetGalleryDao.findAll();

		List<WidgetGalleryDTO> ret = widgets.stream().map(el -> mapTo(el)).collect(Collectors.toList());

		return ret;
	}

	private WidgetGalleryDTO mapTo(SbiWidgetGallery sbiWidgetGallery) {

		WidgetGalleryDTO toRet = new WidgetGalleryDTO();

		toRet.setAuthor(sbiWidgetGallery.getAuthor());
		toRet.setId(sbiWidgetGallery.getUuid());
		toRet.setLabel(sbiWidgetGallery.getDescription());
		toRet.setType(sbiWidgetGallery.getType());
		return toRet;
	}

}
