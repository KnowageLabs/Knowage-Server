package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;
import it.eng.spagobi.services.serialization.JsonConverter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ScorecardSubview {

	private Integer id;
	private String name;
	private Domain criterion;
	private String options;

	@JsonIgnore
	private ScorecardOption scorecardOption;

	// TODO status will be rendered as a color (green/yellow/red)
	private STATUS status;
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
		this.scorecardOption = (ScorecardOption) JsonConverter.jsonToObject(options, ScorecardOption.class);
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
	 * @return the scorecardOption
	 */
	public ScorecardOption getScorecardOption() {
		return scorecardOption;
	}

	/**
	 * @param scorecardOption
	 *            the scorecardOption to set
	 */
	public void setScorecardOption(ScorecardOption scorecardOption) {
		this.scorecardOption = scorecardOption;
	}

}

class CountByStatus {
	ScorecardStatus.STATUS status;
	int count;
}
