package it.eng.spagobi.kpi.config.metadata;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiDomains;

public class SbiKpiTarget {
	private int targetId;
	private SbiDomains category;
	private Date startValidity;
	private Date endValidity;
	private SbiCommonInfo commonInfo;
	private Set sbiKpis = new HashSet(0);

	public SbiKpiTarget(int targetId, SbiDomains category, Date startValidity, Date endValidity, SbiCommonInfo commonInfo, Set sbiKpis) {
		super();
		this.targetId = targetId;
		this.category = category;
		this.startValidity = startValidity;
		this.endValidity = endValidity;
		this.commonInfo = commonInfo;
		this.sbiKpis = sbiKpis;
	}

	public SbiKpiTarget() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SbiKpiTarget(int targetId) {
		super();
		this.targetId = targetId;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public SbiDomains getCategory() {
		return category;
	}

	public void setCategory(SbiDomains category) {
		this.category = category;
	}

	public Date getStartValidity() {
		return startValidity;
	}

	public void setStartValidity(Date startValidity) {
		this.startValidity = startValidity;
	}

	public Date getEndValidity() {
		return endValidity;
	}

	public void setEndValidity(Date endValidity) {
		this.endValidity = endValidity;
	}

	public SbiCommonInfo getCommonInfo() {
		return commonInfo;
	}

	public void setCommonInfo(SbiCommonInfo commonInfo) {
		this.commonInfo = commonInfo;
	}

	public Set getSbiKpis() {
		return sbiKpis;
	}

	public void setSbiKpis(Set sbiKpis) {
		this.sbiKpis = sbiKpis;
	}

}
