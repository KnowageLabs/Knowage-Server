package it.eng.spagobi.kpi.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Threshold;

import java.util.List;

public interface IKpiDAO extends ISpagoBIDao {

	public List<RuleOutput> listRuleOutput();

	public List<RuleOutput> listRuleOutputByType(String type);

	public RuleOutput loadMeasureByName(String name);

	public void insertRule(Rule rule);

	public void updateRule(Rule rule);

	public void removeRule(Integer id);

	public Rule loadRule(Integer id);

	public List<Kpi> listKpi();

	public void insertKpi(Kpi kpi);

	public void updateKpi(Kpi kpi);

	public void removeKpi(Integer id);

	public Kpi loadKpi(Integer id);

	public List<Alias> listAlias();

	public List<Placeholder> listPlaceholder();

	public List<Threshold> listThreshold();

	public Threshold loadThreshold(Integer id);

	public void insertThreshold(Threshold t);

	public void updateThreshold(Threshold id);

	public void removeThreshold(Integer id);
}
