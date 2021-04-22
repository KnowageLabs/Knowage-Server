package it.eng.knowage.knowageapi.dao;
// default package

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
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
	public String create(SbiWidgetGallery sbiWidgetGallery) {

		em.getTransaction().begin();
		// persist the entity
		em.persist(sbiWidgetGallery);
		em.getTransaction().commit();
		return sbiWidgetGallery.getUuid();
	}

	@Override
	public String update(SbiWidgetGallery sbiWidgetGallery) {
		em.getTransaction().begin();
		SbiWidgetGallery sbiWidgetGalleryFound = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.id = :value1 and t.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value1", sbiWidgetGallery.getUuid()).setParameter("value2", sbiWidgetGallery.getOrganization()).getSingleResult();
		sbiWidgetGalleryFound.setAuthor(sbiWidgetGallery.getAuthor());
		sbiWidgetGalleryFound.setDescription(sbiWidgetGallery.getDescription());
		sbiWidgetGalleryFound.setName(sbiWidgetGallery.getName());
		sbiWidgetGalleryFound.setOrganization(sbiWidgetGallery.getOrganization());
		sbiWidgetGalleryFound.setPreviewImage(sbiWidgetGallery.getPreviewImage());
		sbiWidgetGalleryFound.setSbiVersionIn(sbiWidgetGallery.getSbiVersionIn());
		sbiWidgetGalleryFound.setTemplate(sbiWidgetGallery.getTemplate());
		sbiWidgetGalleryFound.setTimeUp(Timestamp.from(Instant.now()));
		sbiWidgetGalleryFound.setType(sbiWidgetGallery.getType());
		sbiWidgetGalleryFound.setUserUp(sbiWidgetGallery.getUserUp());
		sbiWidgetGalleryFound.setOutputType(sbiWidgetGallery.getOutputType());
		int counter = sbiWidgetGallery.getUsageCounter() + 1;
		sbiWidgetGalleryFound.setUsageCounter(counter);
		sbiWidgetGalleryFound.getSbiWidgetGalleryTags().clear();
		sbiWidgetGalleryFound.getSbiWidgetGalleryTags().addAll(sbiWidgetGallery.getSbiWidgetGalleryTags());
		em.merge(sbiWidgetGalleryFound);
		em.getTransaction().commit();
		return sbiWidgetGallery.getUuid();
	}

	@Override
	public SbiWidgetGallery findById(String id) {
		return em.find(SbiWidgetGallery.class, id);
	}

	@Override
	public Collection<SbiWidgetGallery> findAll() {
		Query query = em.createQuery("SELECT e FROM SbiWidgetGallery e");
		return query.getResultList();
	}

	@Override
	public int deleteById(String id) {
		em.getTransaction().begin();
		int isSuccessful = em.createQuery("delete from SbiWidgetGallery p where p.uuid=:uuid").setParameter("uuid", id).executeUpdate();
		em.getTransaction().commit();
		return isSuccessful;

	}

	@Override
	public SbiWidgetGallery findByIdTenant(String id, String tenant) {
		SbiWidgetGallery result = em.createQuery("SELECT t FROM SbiWidgetGallery t where t.id = :value1 and t.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value1", id).setParameter("value2", tenant).getSingleResult();
		return result;
	}

	@Override
	public Collection<SbiWidgetGallery> findAllByTenant(String tenant) {
		Collection<SbiWidgetGallery> results = em.createQuery("SELECT t FROM SbiWidgetGallery t where t.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value2", tenant).getResultList();
		return results;
	}

	@Override
	public int deleteByIdTenant(String id, String tenant) {
		em.getTransaction().begin();
		int isSuccessful = em.createQuery("delete from SbiWidgetGallery p where p.uuid=:uuid and p.organization = :tenant").setParameter("uuid", id)
				.setParameter("tenant", tenant).executeUpdate();
		em.getTransaction().commit();
		return isSuccessful;
	}

	@Override
	public String updateCounter(SbiWidgetGallery sbiWidgetGallery) {
		em.getTransaction().begin();
		SbiWidgetGallery sbiWidgetGalleryFound = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.id = :value1 and t.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value1", sbiWidgetGallery.getUuid()).setParameter("value2", sbiWidgetGallery.getOrganization()).getSingleResult();

		int counter = sbiWidgetGallery.getUsageCounter() + 1;
		sbiWidgetGalleryFound.setUsageCounter(counter);
		em.merge(sbiWidgetGalleryFound);
		em.getTransaction().commit();
		return sbiWidgetGallery.getUuid();
	}

	@Override
	public Collection<SbiWidgetGallery> findAllByTenantAndType(String tenant, String type) {
		Collection<SbiWidgetGallery> results = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.organization = :tenant and type=:valueType", SbiWidgetGallery.class)
				.setParameter("tenant", tenant).setParameter("valueType", type).getResultList();
		return results;
	}

}
