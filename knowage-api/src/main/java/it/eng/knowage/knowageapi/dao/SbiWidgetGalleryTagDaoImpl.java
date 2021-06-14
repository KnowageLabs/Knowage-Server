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

import java.util.Collection;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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

		em.getTransaction().begin();
		// persist the entity
		em.persist(sbiWidgetGalleryTag);
		em.getTransaction().commit();
		return sbiWidgetGalleryTag.getId();
	}

	@Override
	public SbiWidgetGalleryTag findById(int id) {
		// TODO Auto-generated method stub
		return em.find(SbiWidgetGalleryTag.class, id);

	}

	@Override
	public Collection<SbiWidgetGalleryTag> findAll() {
		Query query = em.createQuery("SELECT e FROM SbiWidgetGalleryTag e");
		return query.getResultList();
	}

}
