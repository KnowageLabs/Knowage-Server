package it.eng.knowage.knowageapi.dao;

import java.util.Collection;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTag;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTagId;

@Component
public class SbiWidgetGalleryTagDaoImpl implements SbiWidgetGalleryTagDao {

	private static final Logger logger = Logger.getLogger(SbiWidgetGalleryDaoImpl.class.getName());

	@Autowired
	@Qualifier("knowage-gallery")
	private EntityManager em;

	@Override
	public SbiWidgetGalleryTagId create(SbiWidgetGalleryTag sbiWidgetGalleryTag) {

		em.persist(sbiWidgetGalleryTag);
		return sbiWidgetGalleryTag.getId();
	}

	@Override
	public SbiWidgetGallery findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SbiWidgetGallery> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
