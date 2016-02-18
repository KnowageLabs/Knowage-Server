package it.eng.spagobi.kpi.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;

import java.util.List;

public interface IKpiDAO extends ISpagoBIDao {

	public List<RuleOutput> listRuleOutput();

	public List<RuleOutput> listMeasure();

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
}
