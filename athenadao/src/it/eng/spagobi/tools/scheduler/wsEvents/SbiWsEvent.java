/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.scheduler.wsEvents;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;

public class SbiWsEvent extends SbiHibernateModel {

	/*
	 * <property name="takeChargeDate" type="timestamp"> <column name="TAKE_CHARGE_DATE" length="80" not-null="false" /> </property> <property name="audits"
	 * type="string"> <column name="AUDITS" length="200" not-null="false" /> </property>
	 */

	// Fields
	private Integer id;
	private String eventName;
	private String ipComeFrom;
	private Date incomingDate;
	private Date takeChargeDate;

	/**
	 * @param id
	 * @param eventName
	 * @param ipComeFrom
	 * @param incomingDate
	 * @param takeChargeDate
	 */
	public SbiWsEvent(Integer id, String eventName, String ipComeFrom, Date incomingDate, Date takeChargeDate, String audits) {
		this.id = id;
		this.eventName = eventName;
		this.ipComeFrom = ipComeFrom;
		this.incomingDate = incomingDate;
		this.takeChargeDate = takeChargeDate;
	}

	public SbiWsEvent(String eventName, String ipComeFrom, Date incomingDate) {
		this.eventName = eventName;
		this.ipComeFrom = ipComeFrom;
		this.incomingDate = incomingDate;
		this.takeChargeDate = null;
	}

	/** default constructor */
	public SbiWsEvent() {
		this.id = null;
		this.eventName = null;
		this.ipComeFrom = null;
		this.incomingDate = null;
		this.takeChargeDate = null;
	};

	// Property accessors

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getIpComeFrom() {
		return ipComeFrom;
	}

	public void setIpComeFrom(String ipComeFrom) {
		this.ipComeFrom = ipComeFrom;
	}

	public Date getIncomingDate() {
		return incomingDate;
	}

	public void setIncomingDate(Date incomingDate) {
		this.incomingDate = incomingDate;
	}

	public Date getTakeChargeDate() {
		return takeChargeDate;
	}

	public void setTakeChargeDate(Date takeChargeDate) {
		this.takeChargeDate = takeChargeDate;
	}
}
