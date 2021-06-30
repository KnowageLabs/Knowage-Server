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
package it.eng.knowage.knowageapi.dao;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.dao.dto.AbstractEntity;

/**
 * @author Marco Libanori
 */
abstract class AbstractDaoImpl {

	@Autowired
	private BusinessRequestContext businessRequestContext;

	protected final void preInsert(AbstractEntity entity) {
		entity.setSbiVersionIn(businessRequestContext.getVersion());
		entity.setTimeIn(Instant.now());
		entity.setUserIn(businessRequestContext.getUsername());
	}

	protected final void preUpdate(AbstractEntity entity) {
		entity.setSbiVersionUp(businessRequestContext.getVersion());
		entity.setTimeUp(Instant.now());
		entity.setUserUp(businessRequestContext.getUsername());
	}

	protected final void preDelete(AbstractEntity entity) {
		entity.setSbiVersionDe(businessRequestContext.getVersion());
		entity.setTimeDe(Instant.now());
		entity.setUserDe(businessRequestContext.getUsername());
	}
}
