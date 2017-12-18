package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SbiKpiExecution extends SbiHibernateModel {

	private Integer id;
	private String name;
	private Date startDate;
	private Date endDate;
	private Character delta;

	private Set<SbiKpiKpi> sbiKpiKpis = new HashSet<>();
	private Set<SbiKpiExecutionFilter> sbiKpiExecutionFilters = new HashSet<>();

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the sbiKpiKpis
	 */
	public Set<SbiKpiKpi> getSbiKpiKpis() {
		return sbiKpiKpis;
	}

	/**
	 * @return the sbiKpiExecutionFilters
	 */
	public Set<SbiKpiExecutionFilter> getSbiKpiExecutionFilters() {
		return sbiKpiExecutionFilters;
	}

	/**
	 * @param sbiKpiKpis
	 *            the sbiKpiKpis to set
	 */
	public void setSbiKpiKpis(Set<SbiKpiKpi> sbiKpiKpis) {
		this.sbiKpiKpis = sbiKpiKpis;
	}

	/**
	 * @param sbiKpiExecutionFilters
	 *            the sbiKpiExecutionFilters to set
	 */
	public void setSbiKpiExecutionFilters(Set<SbiKpiExecutionFilter> sbiKpiExecutionFilters) {
		this.sbiKpiExecutionFilters = sbiKpiExecutionFilters;
	}

	/**
	 * @return the delta
	 */
	public Character getDelta() {
		return delta;
	}

	/**
	 * @param delta
	 *            the delta to set
	 */
	public void setDelta(Character delta) {
		this.delta = delta;
	}

}
