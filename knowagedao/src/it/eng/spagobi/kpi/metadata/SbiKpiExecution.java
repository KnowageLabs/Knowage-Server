package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SbiKpiExecution extends SbiHibernateModel {

	private Integer id;
	private String name;
	private Date startTime;
	private Date endTime;
	private Character delta;

	private final Set<SbiKpiKpi> sbiKpiKpis = new HashSet<>();

	private final Set<SbiKpiExecutionFilter> sbiKpiExecutionFilters = new HashSet<>();

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
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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

}
