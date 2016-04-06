package it.eng.spagobi.kpi.bo;

import java.util.ArrayList;
import java.util.List;

public class KpiScheduler {

	private Integer id;
	private String name;

	private List<SchedulerFilter> filters = new ArrayList<>();
	private Boolean delta;
	private List<Kpi> kpis = new ArrayList<>();
	private Long startDate;
	private Long endDate;
	private String kpiNames;
	private String author;

	private String chron;
	private boolean runImmediately;

	private String startTime;
	private String endTime;

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
	 * @return the delta
	 */
	public Boolean getDelta() {
		return delta;
	}

	/**
	 * @param delta
	 *            the delta to set
	 */
	public void setDelta(Boolean delta) {
		this.delta = delta;
	}

	/**
	 * @return the filters
	 */
	public List<SchedulerFilter> getFilters() {
		return filters;
	}

	/**
	 * @return the kpis
	 */
	public List<Kpi> getKpis() {
		return kpis;
	}

	/**
	 * @return the startDate
	 */
	public Long getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Long getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the kpiNames
	 */
	public String getKpiNames() {
		return kpiNames;
	}

	/**
	 * @param kpiNames
	 *            the kpiNames to set
	 */
	public void setKpiNames(String kpiNames) {
		this.kpiNames = kpiNames;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the chron
	 */
	public String getChron() {
		return chron;
	}

	/**
	 * @param chron
	 *            the chron to set
	 */
	public void setChron(String chron) {
		this.chron = chron;
	}

	/**
	 * @return the runImmediately
	 */
	public boolean isRunImmediately() {
		return runImmediately;
	}

	/**
	 * @param runImmediately
	 *            the runImmediately to set
	 */
	public void setRunImmediately(boolean runImmediately) {
		this.runImmediately = runImmediately;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param kpis
	 *            the kpis to set
	 */
	public void setKpis(List<Kpi> kpis) {
		this.kpis = kpis;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(List<SchedulerFilter> filters) {
		this.filters = filters;
	}

}
