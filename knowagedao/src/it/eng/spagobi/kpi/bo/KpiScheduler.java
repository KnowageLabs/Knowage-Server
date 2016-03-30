package it.eng.spagobi.kpi.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KpiScheduler {

	private Integer id;
	private String name;

	private final List<SchedulerFilter> filters = new ArrayList<>();
	private Boolean delta;
	private final List<Kpi> kpis = new ArrayList<>();
	private Date startDate;
	private Date endDate;
	private String kpiNames;
	private String author;

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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getKpiNames() {
		return kpiNames;
	}

	public void setKpiNames(String kpiNames) {
		this.kpiNames = kpiNames;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

}
