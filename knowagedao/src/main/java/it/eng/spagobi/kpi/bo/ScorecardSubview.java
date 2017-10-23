package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScorecardSubview {

	private Integer id;
	private String name;
	private Domain criterion;
	private ScorecardOption options;

	private STATUS status;
	@JsonIgnore
	private final Map<STATUS, Integer> groupedKpiMap = new HashMap<>();

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
	 * @return the criterion
	 */
	public Domain getCriterion() {
		return criterion;
	}

	/**
	 * @param criterion
	 *            the criterion to set
	 */
	public void setCriterion(Domain criterion) {
		this.criterion = criterion;
	}

	/**
	 * @return the groupedKpis
	 */
	public List<CountByStatus> getGroupedKpis() {
		List<CountByStatus> ret = new ArrayList<>();
		for (Entry<STATUS, Integer> countByStatus : groupedKpiMap.entrySet()) {
			ret.add(new CountByStatus(countByStatus.getKey(), countByStatus.getValue()));
		}
		return ret;
	}

	/**
	 * @return the options
	 */
	public ScorecardOption getOptions() {
		return options;
	}

	/**
	 * @param options
	 *            the options to set
	 */
	public void setOptions(ScorecardOption options) {
		this.options = options;
	}

	/**
	 * @return the status
	 */
	public STATUS getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(STATUS status) {
		this.status = status;
	}

	/**
	 * @return the groupedKpiMap
	 */
	protected Map<STATUS, Integer> getGroupedKpiMap() {
		return groupedKpiMap;
	}

}

class CountByStatus {
	private final ScorecardStatus.STATUS status;
	private final Integer count;

	public CountByStatus(ScorecardStatus.STATUS status, Integer count) {
		this.status = status;
		this.count = count;
	}

	/**
	 * @return the status
	 */
	public ScorecardStatus.STATUS getStatus() {
		return status;
	}

	/**
	 * @return the count
	 */
	public Integer getCount() {
		return count;
	}

}