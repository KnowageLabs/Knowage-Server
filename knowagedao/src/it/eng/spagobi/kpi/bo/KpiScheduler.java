package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.tools.scheduler.bo.Frequency;

import java.util.ArrayList;
import java.util.List;

public class KpiScheduler {

	private Integer id;
	private String name;

	private List<SchedulerFilter> filters = new ArrayList<>();
	private Boolean delta;
	private List<Kpi> kpis = new ArrayList<>();
	private String kpiNames;
	private String author;

	private Frequency frequency = new Frequency();

	/**
	 * @return the frequency
	 */
	public Frequency getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
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
