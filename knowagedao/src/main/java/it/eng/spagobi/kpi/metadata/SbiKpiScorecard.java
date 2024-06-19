package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.HashSet;
import java.util.Set;

public class SbiKpiScorecard extends SbiHibernateModel {

	private Integer id;
	private Integer parentId;
	private String name;
	private Integer criterionId;
	private SbiDomains criterion;

	private Set<SbiKpiScorecard> subviews = new HashSet<>();

	private Set<SbiKpiKpi> sbiKpiKpis = new HashSet<>();

	private String options = null;

	public SbiKpiScorecard() {
		super();
	}
	public SbiKpiScorecard(Integer id) {
		super();
		this.setId(id);
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
	private void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the parentId
	 */
	public Integer getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
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
	 * @return the criterionId
	 */
	public Integer getCriterionId() {
		return criterionId;
	}

	/**
	 * @param criterionId
	 *            the criterionId to set
	 */
	public void setCriterionId(Integer criterionId) {
		this.criterionId = criterionId;
	}

	/**
	 * @return the criterion
	 */
	public SbiDomains getCriterion() {
		return criterion;
	}

	/**
	 * @param criterion
	 *            the criterion to set
	 */
	public void setCriterion(SbiDomains criterion) {
		this.criterion = criterion;
	}

	/**
	 * @return the sbiKpiKpis
	 */
	public Set<SbiKpiKpi> getSbiKpiKpis() {
		return sbiKpiKpis;
	}

	/**
	 * @param sbiKpiKpis
	 *            the sbiKpiKpis to set
	 */
	public void setSbiKpiKpis(Set<SbiKpiKpi> sbiKpiKpis) {
		this.sbiKpiKpis = sbiKpiKpis;
	}

	/**
	 * @return the subviews
	 */
	public Set<SbiKpiScorecard> getSubviews() {
		return subviews;
	}

	/**
	 * @param subviews
	 *            the subviews to set
	 */
	public void setSubviews(Set<SbiKpiScorecard> subviews) {
		this.subviews = subviews;
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
