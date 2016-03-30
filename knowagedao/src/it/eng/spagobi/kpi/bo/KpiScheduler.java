package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class KpiScheduler {

	private Integer id;
	private String name;

	private final List<SchedulerFilter> filters = new ArrayList<>();
	private Boolean delta;
	private final List<Kpi> kpis = new ArrayList<>();

	private String getKpiNames() {
		return StringUtils.join(kpis.toArray(new String[0]), ", ");
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

}
