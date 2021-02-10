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
 * Wrapper of {@link SbiUserAttributes}.
 *
 * Doesn't extend {@link SbiUserAttributes} because it's used by Hibernate.
 */
public class SbiUserAttributesHistory extends SbiHibernateModel {

	private final SbiUserAttributes wrappedObj;
	private SbiCommonInfo commonInfo;
	private int eventId;
	private Date timeStart;
	private Date timeEnd;

	public SbiUserAttributesHistory() {
		this.wrappedObj = new SbiUserAttributes();
		this.wrappedObj.setId(new SbiUserAttributesId());
		this.commonInfo = new SbiCommonInfo();
	}

	public SbiUserAttributesHistory(SbiUserAttributes wrappedObj) {
		this.wrappedObj = wrappedObj;
		this.commonInfo = new SbiCommonInfo(wrappedObj.getCommonInfo());
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
		return this.wrappedObj.getId().getId();
	}

	public void setId(int id) {
		this.wrappedObj.getId().setId(id);
	}

	public int getAttributeId() {
		return this.wrappedObj.getId().getAttributeId();
	}

	public void setAttributeId(int attributeId) {
		this.wrappedObj.getId().setAttributeId(attributeId);
	}

	public String getAttributeValue() {
		return wrappedObj.getAttributeValue();
	}

	public void setAttributeValue(String attributeValue) {
		wrappedObj.setAttributeValue(attributeValue);
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
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
