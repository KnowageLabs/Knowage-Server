package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.HashSet;
import java.util.Set;

public class SbiKpiScorecard extends SbiHibernateModel {

	private Integer id;
	private Integer parentId;
	private String name;

	private Integer typeId;
	private SbiDomains type;

	private Integer criterionId;
	private SbiDomains criterion;

	private Set<SbiKpiKpi> sbiKpiKpis = new HashSet<>();

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
	 * @return the typeId
	 */
	public Integer getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId
	 *            the typeId to set
	 */
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the type
	 */
	public SbiDomains getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(SbiDomains type) {
		this.type = type;
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

}
