package it.eng.knowage.knowageapi.dao;
// default package

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;

/**
 * Home object for domain model class SbiWidgetGallery.
 *
 * @see .SbiWidgetGallery
 * @author Hibernate Tools
 */
@Component
public class SbiWidgetGalleryDaoImpl implements SbiWidgetGalleryDao {

	private static final Logger logger = Logger.getLogger(SbiWidgetGalleryDaoImpl.class.getName());

	@Autowired
	@Qualifier("knowage-gallery")
	private EntityManager em;

	@Override
	public UUID create(SbiWidgetGallery sbiWidgetGallery) {

		em.persist(sbiWidgetGallery);
		return sbiWidgetGallery.getUuid();
	}

	@Override
	public SbiWidgetGallery findById(int id) {
		return em.find(SbiWidgetGallery.class, id);
	}

	@Override
	public Collection<SbiWidgetGallery> findAll() {
		Query query = em.createQuery("SELECT e FROM SbiWidgetGallery e");
		return query.getResultList();
	}
}
