package it.eng.spagobi.kpi.metadata;

import java.io.Serializable;

public class SbiKpiKpiId implements Serializable {

	private Integer id;
	private Integer version;

	public SbiKpiKpiId() {
	}

	public SbiKpiKpiId(Integer id, Integer version) {
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
		return obj instanceof SbiKpiKpiId && id != null && version != null ? id.equals(((SbiKpiKpiId) obj).getId())
				&& version.equals(((SbiKpiKpiId) obj).getVersion()) : super.equals(obj);
	}

}
