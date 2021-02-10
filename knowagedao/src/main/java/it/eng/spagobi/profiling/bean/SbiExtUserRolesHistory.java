/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.profiling.bean;

import java.util.Date;

import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * Wrapper of {@link SbiExtUserRoles}.
 *
 * Doesn't extend {@link SbiExtUserRoles} because it's used by Hibernate.
 */
public class SbiExtUserRolesHistory extends SbiHibernateModel {

	private final SbiExtUserRoles wrappedObj;
	private SbiCommonInfo commonInfo;
	private int eventId;
	private Date timeStart;
	private Date timeEnd;

	public SbiExtUserRolesHistory() {
		wrappedObj = new SbiExtUserRoles();
		wrappedObj.setId(new SbiExtUserRolesId());
		commonInfo = new SbiCommonInfo();
	}

	public SbiExtUserRolesHistory(SbiExtUserRoles bean) {
		this.wrappedObj = bean;
		this.commonInfo = new SbiCommonInfo(bean.getCommonInfo());
	}


	@Override
	public SbiCommonInfo getCommonInfo() {
		return commonInfo;
	}


	@Override
	public void setCommonInfo(SbiCommonInfo commonInfo) {
		this.commonInfo = commonInfo;
	}


	public int getId() {
		return wrappedObj.getId().getId();
	}


	public SbiUser getSbiUser() {
		return wrappedObj.getSbiUser();
	}


	public void setSbiUser(SbiUser sbiUser) {
		wrappedObj.setSbiUser(sbiUser);
	}


	@Override
	public int hashCode() {
		return wrappedObj.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		return wrappedObj.equals(obj);
	}


	@Override
	public String toString() {
		return wrappedObj.toString();
	}


	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public void setId(int id) {
		this.wrappedObj.getId().setId(id);
	}

	public int getExtRoleId() {
		return this.wrappedObj.getId().getExtRoleId();
	}

	public void setExtRoleId(int extRoleId) {
		this.wrappedObj.getId().setExtRoleId(extRoleId);
	}

	public Date getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(Date timeStart) {
		this.timeStart = timeStart;
	}

	public Date getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(Date timeEnd) {
		this.timeEnd = timeEnd;
	}


}
