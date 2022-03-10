/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.knowageapi.dao;
// default package

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTag;
import it.eng.knowage.knowageapi.resource.dto.Code;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;

/**
 * Home object for domain model class SbiWidgetGallery.
 *
 * @see .SbiWidgetGallery
 * @author Hibernate Tools
 */
@Component
@Transactional
public class SbiWidgetGalleryDaoImpl implements SbiWidgetGalleryDao {

	private static final Logger logger = Logger.getLogger(SbiWidgetGalleryDaoImpl.class.getName());

	@PersistenceContext(unitName = "knowage-gallery")
	private EntityManager em;

	@Override
	@Transactional(value = TxType.REQUIRED)
	public String create(SbiWidgetGallery sbiWidgetGallery) {

		// persist the entity
		em.persist(sbiWidgetGallery);
		return sbiWidgetGallery.getId().getUuid();
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public String update(SbiWidgetGallery sbiWidgetGallery) {
		logger.debug("IN");

		SbiWidgetGallery sbiWidgetGalleryFound = null;
		List<SbiWidgetGallery> resultList = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.id.uuid = :value1 and t.id.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value1", sbiWidgetGallery.getId().getUuid()).setParameter("value2", sbiWidgetGallery.getId().getOrganization()).getResultList();
		if (resultList.size() == 1) {
			sbiWidgetGalleryFound = resultList.get(0);
		}

		sbiWidgetGalleryFound.setAuthor(sbiWidgetGallery.getAuthor());
		sbiWidgetGalleryFound.setDescription(sbiWidgetGallery.getDescription());
		sbiWidgetGalleryFound.setName(sbiWidgetGallery.getName());
		sbiWidgetGalleryFound.setLabel(sbiWidgetGallery.getLabel());
		sbiWidgetGalleryFound.getId().setOrganization(sbiWidgetGallery.getId().getOrganization());
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

		logger.debug("OUT");
		return sbiWidgetGallery.getId().getUuid();
	}

	@Override
	public WidgetGalleryDTO findById(String id) {
		SbiWidgetGallery gallery = em.find(SbiWidgetGallery.class, id);
		WidgetGalleryDTO galleryDto = null;
		try {
			galleryDto = mapTo(gallery);
		} catch (JSONException e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return galleryDto;
	}

	@Override
	public Collection<WidgetGalleryDTO> findAll() {
		Query query = em.createQuery("SELECT e FROM SbiWidgetGallery e");
		Collection<SbiWidgetGallery> galleryss = query.getResultList();
		List<WidgetGalleryDTO> galeryDtoss = new ArrayList<WidgetGalleryDTO>();
		for (SbiWidgetGallery sbiWidgetGallery : galleryss) {
			try {
				galeryDtoss.add(mapToLight(sbiWidgetGallery));
			} catch (JSONException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}
		return galeryDtoss;
	}

	@Override
	public SbiWidgetGallery findByIdTenantSbiWidgetGallery(String id, String tenant) {
		logger.debug("IN");

		SbiWidgetGallery result = null;
		List<SbiWidgetGallery> resultList = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.id.uuid = :value1 and t.id.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value1", id).setParameter("value2", tenant).getResultList();
		if (resultList.size() == 1)
			result = resultList.get(0);

		logger.debug("OUT");
		return result;
	}

	@Override
	public WidgetGalleryDTO findByIdTenant(String id, String tenant) {
		logger.debug("IN");

		SbiWidgetGallery result = null;
		WidgetGalleryDTO galeryDtoss = null;
		List<SbiWidgetGallery> resultList = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.id.uuid = :value1 and t.id.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value1", id).setParameter("value2", tenant).getResultList();
		if (resultList.size() == 1) {
			result = resultList.get(0);
			try {
				galeryDtoss = mapTo(result);
			} catch (JSONException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}

		return galeryDtoss;
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public Collection<WidgetGalleryDTO> findAllByTenant(String tenant) {
		logger.debug("IN");

		List<WidgetGalleryDTO> galeryDtoss = new ArrayList<WidgetGalleryDTO>();
		Collection<SbiWidgetGallery> results = em.createQuery("SELECT t FROM SbiWidgetGallery t where t.id.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value2", tenant).getResultList();

		for (SbiWidgetGallery sbiWidgetGallery : results) {
			try {
				galeryDtoss.add(mapToLight(sbiWidgetGallery));
			} catch (JSONException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}

		logger.debug("OUT");
		return galeryDtoss;
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public int deleteByIdTenant(String id, String tenant) {
		logger.debug("IN");

		SbiWidgetGallery result = null;
		List<SbiWidgetGallery> resultList = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.id.uuid = :value1 and t.id.organization = :value2", SbiWidgetGallery.class)
				.setParameter("value1", id).setParameter("value2", tenant).getResultList();
		if (resultList.size() == 1) {
			result = resultList.get(0);
		}

		em.remove(result);

		logger.debug("OUT");
		return 1;
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public Collection<WidgetGalleryDTO> findAllByTenantAndType(String tenant, String type) {
		logger.debug("IN");

		Collection<SbiWidgetGallery> results = em
				.createQuery("SELECT t FROM SbiWidgetGallery t where t.id.organization = :tenant and type=:valueType", SbiWidgetGallery.class)
				.setParameter("tenant", tenant).setParameter("valueType", type).getResultList();
		List<WidgetGalleryDTO> galeryDtoss = new ArrayList<WidgetGalleryDTO>();
		for (SbiWidgetGallery sbiWidgetGallery : results) {
			try {
				WidgetGalleryDTO obj = mapTo(sbiWidgetGallery);
				galeryDtoss.add(obj);
			} catch (JSONException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}

		logger.debug("OUT");
		return galeryDtoss;
	}

	private WidgetGalleryDTO mapTo(SbiWidgetGallery sbiWidgetGallery) throws JSONException {

		WidgetGalleryDTO toRet = new WidgetGalleryDTO();

		toRet.setAuthor(sbiWidgetGallery.getAuthor());
		toRet.setId(sbiWidgetGallery.getId().getUuid());
		toRet.setName(sbiWidgetGallery.getName());
		toRet.setLabel(sbiWidgetGallery.getLabel());
		toRet.setDescription(sbiWidgetGallery.getDescription());
		toRet.setType(sbiWidgetGallery.getType());
		if (sbiWidgetGallery.getPreviewImage() != null) {
			toRet.setImage(new String(sbiWidgetGallery.getPreviewImage()));
		}
		toRet.setOrganization(sbiWidgetGallery.getId().getOrganization());
		toRet.setUsageCounter(sbiWidgetGallery.getUsageCounter());
		List<SbiWidgetGalleryTag> tagList = sbiWidgetGallery.getSbiWidgetGalleryTags();
		if (tagList != null && tagList.size() > 0) {
			List<String> tags = new ArrayList<String>();
			for (int i = 0; i < tagList.size(); i++) {
				tags.add(tagList.get(i).getId().getTag());
			}
			toRet.setTags(tags);
		}

		JSONObject jsonBody = new JSONObject();
		byte[] template = sbiWidgetGallery.getTemplate();
		if (template != null)
			jsonBody = new JSONObject(new String(template));

		Code code = new Code();
		JSONObject jsonCode = jsonBody.optJSONObject("code");
		String html = jsonCode.getString("html");
		String javascript = jsonCode.getString("javascript");
		String python = jsonCode.getString("python");
		String css = jsonCode.getString("css");
		code.setCss(css);
		code.setJavascript(javascript);
		code.setPython(python);
		code.setHtml(html);
		toRet.setCode(code);
		toRet.setOutputType(sbiWidgetGallery.getOutputType());

		return toRet;
	}

	private WidgetGalleryDTO mapToLight(SbiWidgetGallery sbiWidgetGallery) throws JSONException {

		WidgetGalleryDTO toRet = new WidgetGalleryDTO();

		toRet.setAuthor(sbiWidgetGallery.getAuthor());
		toRet.setDescription(sbiWidgetGallery.getDescription());
		toRet.setId(sbiWidgetGallery.getId().getUuid());
		toRet.setName(sbiWidgetGallery.getName());
		toRet.setLabel(sbiWidgetGallery.getLabel());
		toRet.setType(sbiWidgetGallery.getType());

		List<SbiWidgetGalleryTag> tagList = sbiWidgetGallery.getSbiWidgetGalleryTags();
		if (tagList != null && tagList.size() > 0) {
			List<String> tags = new ArrayList<String>();
			for (int i = 0; i < tagList.size(); i++) {
				tags.add(tagList.get(i).getId().getTag());
			}
			toRet.setTags(tags);
		}

		return toRet;
	}
}
