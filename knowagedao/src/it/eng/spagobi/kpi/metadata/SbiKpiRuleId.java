package it.eng.spagobi.kpi.metadata;

import java.io.Serializable;

public class SbiKpiRuleId implements Serializable {

	private Integer id;
	private Integer version;

	public SbiKpiRuleId() {
	}

	public SbiKpiRuleId(Integer id, Integer version) {
		this.id = id;
		this.version = version;
	}

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

	/**
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		return id != null && version != null ? (id + "_" + version).hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SbiKpiRuleId && id != null && version != null ? id.equals(((SbiKpiRuleId) obj).getId())
				&& version.equals(((SbiKpiRuleId) obj).getVersion()) : super.equals(obj);
	}
}
