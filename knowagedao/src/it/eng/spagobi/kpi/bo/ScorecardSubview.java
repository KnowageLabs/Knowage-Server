package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.kpi.bo.ScorecardSubview.STATUS;

import java.util.ArrayList;
import java.util.List;

public class ScorecardSubview {
	public enum STATUS {
		RED, YELLOW, GREEN, GRAY
	};

	private Integer id;
	private String name;
	private Domain criterion;

	// TODO status will be rendered as a color (green/yellow/red)
	private STATUS status = STATUS.GRAY;
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
	 * @return the groupedKpis
	 */
	public List<CountByStatus> getGroupedKpis() {
		return groupedKpis;
	}

}

class CountByStatus {
	STATUS status;
	int count;
}
