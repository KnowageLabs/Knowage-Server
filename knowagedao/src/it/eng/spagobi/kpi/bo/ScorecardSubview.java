package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.kpi.bo.ScorecardSubview.STATUS;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScorecardSubview {
	public static enum STATUS {
		RED, YELLOW, GREEN, GRAY
	};

	private Integer id;
	private String name;
	private Domain criterion;
	private String options;

	// TODO status will be rendered as a color (green/yellow/red)
	private List<STATUS> status = new ArrayList<>();
	private final List<CountByStatus> groupedKpis = new ArrayList<>();

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
	 * @return the status
	 */
	public List<STATUS> getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	@JsonIgnore
	public void setStatus(List<STATUS> status) {
		this.status = status;
	}

	/**
	 * @return the groupedKpis
	 */
	@JsonIgnore
	public List<CountByStatus> getGroupedKpis() {
		return groupedKpis;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options
	 *            the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

}

class CountByStatus {
	STATUS status;
	int count;
}
