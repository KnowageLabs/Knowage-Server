/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.knowageapi.dao.listener;

import java.time.Instant;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.dao.dto.AbstractEntity;

/**
 * Manages the Knowage multitenancy.
 *
 * @author Marco Libanori
 */
@Component
public class TenantListener {

	// HACK to inject Spring bean into non-Spring bean classes
	private static BusinessRequestContext business;

	@Autowired
	public void init(BusinessRequestContext businessRequestContext) {
		TenantListener.business = businessRequestContext;
	}

	private static final Logger LOGGER = Logger.getLogger(TenantListener.class);

	@PrePersist
	public void prePersist(AbstractEntity entity) {
		LOGGER.warn("Setting persistence tenant data for: " + entity);

		entity.setOrganization(TenantListener.business.getOrganization());
		entity.setSbiVersionIn(TenantListener.business.getVersion());
		entity.setTimeIn(Instant.now());
		entity.setUserIn(TenantListener.business.getUsername());
	}

	@PreUpdate
	public void preUpdate(AbstractEntity entity) {
		LOGGER.warn("Setting updating tenant data for: " + entity);

		entity.setSbiVersionUp(TenantListener.business.getVersion());
		entity.setTimeUp(Instant.now());
		entity.setUserUp(TenantListener.business.getUsername());
	}

	@PreRemove
	public void preRemove(AbstractEntity entity) {
		LOGGER.warn("Setting removing tenant data for: " + entity);

		entity.setSbiVersionDe(TenantListener.business.getVersion());
		entity.setTimeDe(Instant.now());
		entity.setUserDe(TenantListener.business.getUsername());
	}
}
