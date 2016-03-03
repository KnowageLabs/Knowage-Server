package it.eng.spagobi.kpi.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;

import java.util.List;

public interface IKpiDAO extends ISpagoBIDao {

	public List<String> aliasValidation(Rule rule);

	public List<RuleOutput> listRuleOutputByType(String type);

	public RuleOutput loadMeasureByName(String name);

	public void insertRule(Rule rule) throws SpagoBIException;

	public void updateRule(Rule rule) throws SpagoBIException;

	public void removeRule(Integer id);

	public Rule loadRule(Integer id);

	public List<Kpi> listKpi();

	public void insertKpi(Kpi kpi);

	public void updateKpi(Kpi kpi);

	public void removeKpi(Integer id);

	public Kpi loadKpi(Integer id);

	public List<Alias> listAlias();

	/**
	 * Retrieve all aliases not currently used as measure in all rules excluding the one with id=ruleId (optional)
	 * 
	 * @param ruleId
	 * @return a list of Alias
	 */
	public List<Alias> listAliasNotInMeasure(Integer ruleId);

	public Alias loadAlias(String name);

	public List<Placeholder> listPlaceholder();

	public List<Threshold> listThreshold();

}
