package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiKpiExecutionFilter extends SbiHibernateModel {

	private SbiKpiExecutionFilterId sbiKpiExecutionFilterId = new SbiKpiExecutionFilterId();

	private SbiKpiPlaceholder sbiKpiPlaceholder;
	private SbiKpiExecution sbiKpiExecution;

	private SbiDomains type;
	private Integer typeId;
	private String value;

	/**
	 * @return the sbiKpiExecutionFilterId
	 */
	public SbiKpiExecutionFilterId getSbiKpiExecutionFilterId() {
		return sbiKpiExecutionFilterId;
	}

	/**
	 * @param sbiKpiExecutionFilterId
	 *            the sbiKpiExecutionFilterId to set
	 */
	public void setSbiKpiExecutionFilterId(SbiKpiExecutionFilterId sbiKpiExecutionFilterId) {
		this.sbiKpiExecutionFilterId = sbiKpiExecutionFilterId;
	}

	/**
	 * @return the sbiKpiPlaceholder
	 */
	public SbiKpiPlaceholder getSbiKpiPlaceholder() {
		return sbiKpiPlaceholder;
	}

	/**
	 * @param sbiKpiPlaceholder
	 *            the sbiKpiPlaceholder to set
	 */
	public void setSbiKpiPlaceholder(SbiKpiPlaceholder sbiKpiPlaceholder) {
		this.sbiKpiPlaceholder = sbiKpiPlaceholder;
	}

	/**
	 * @return the sbiKpiExecution
	 */
	public SbiKpiExecution getSbiKpiExecution() {
		return sbiKpiExecution;
	}

	/**
	 * @param sbiKpiExecution
	 *            the sbiKpiExecution to set
	 */
	public void setSbiKpiExecution(SbiKpiExecution sbiKpiExecution) {
		this.sbiKpiExecution = sbiKpiExecution;
	}

	/**
	 * @return the type
	 */
	public SbiDomains getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(SbiDomains type) {
		this.type = type;
	}

	/**
	 * @return the typeId
	 */
	public Integer getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId
	 *            the typeId to set
	 */
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
