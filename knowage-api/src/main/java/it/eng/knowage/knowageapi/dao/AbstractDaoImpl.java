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
import it.eng.knowage.knowageapi.dao.dto.SbiCommonInfo;

/**
 * @author Marco Libanori
 */
abstract class AbstractDaoImpl {

	@Autowired
	private BusinessRequestContext businessRequestContext;

	protected final void preInsert(AbstractEntity entity) {
		SbiCommonInfo commonInfo = entity.getCommonInfo();
		commonInfo.setOrganization(businessRequestContext.getOrganization());
		commonInfo.setSbiVersionIn(businessRequestContext.getVersion());
		commonInfo.setTimeIn(Instant.now());
		commonInfo.setUserIn(businessRequestContext.getUsername());
	}

	protected final void preUpdate(AbstractEntity entity) {
		SbiCommonInfo commonInfo = entity.getCommonInfo();
		commonInfo.setSbiVersionUp(businessRequestContext.getVersion());
		commonInfo.setTimeUp(Instant.now());
		commonInfo.setUserUp(businessRequestContext.getUsername());
	}

	protected final void preDelete(AbstractEntity entity) {
		SbiCommonInfo commonInfo = entity.getCommonInfo();
		commonInfo.setSbiVersionDe(businessRequestContext.getVersion());
		commonInfo.setTimeDe(Instant.now());
		commonInfo.setUserDe(businessRequestContext.getUsername());
	}
}
