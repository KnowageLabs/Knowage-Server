package it.eng.spagobi.tools.calendar.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class CalendarAttributeDomain extends SbiHibernateModel {
	private Integer domainId;
	private String attributeDomain;
	private String attributeDomainDescr;
	private String recStatus;

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	public String getAttributeDomain() {
		return attributeDomain;
	}

	public void setAttributeDomain(String attributeDomain) {
		this.attributeDomain = attributeDomain;
	}

	public String getAttributeDomainDescr() {
		return attributeDomainDescr;
	}

	public void setAttributeDomainDescr(String attributeDomainDescr) {
		this.attributeDomainDescr = attributeDomainDescr;
	}

	public String getRecStatus() {
		return recStatus;
	}

	public void setRecStatus(String recStatus) {
		this.recStatus = recStatus;
	}

}
