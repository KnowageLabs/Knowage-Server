package it.eng.spagobi.kpi.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KpiDAOImpl extends AbstractHibernateDAO implements IKpiDAO {

	// TODO These mock lists must be removed after implementing related services
	private static List<Kpi> kpis = new ArrayList<>();
	private static List<Alias> aliases = new ArrayList<>();
	private static List<Rule> rules = new ArrayList<>();
	private static List<Placeholder> placeholders = new ArrayList<>();

	@Override
	public List<RuleOutput> listRuleOutput() {
		// TODO
		List<RuleOutput> measures = new ArrayList<>();
		for (Rule rule : rules) {
			measures.addAll(rule.getRuleOutputs());
		}
		return measures;
	}

	@Override
	public void insertRule(Rule rule) {
		// TODO Auto-generated method stub
		rule.setId(rules.size());
		rules.add(rule);
	}

	@Override
	public void updateRule(Rule rule) {
		// TODO Auto-generated method stub
		int i = rules.indexOf(rule);
		rules.remove(i);
		rules.add(rule);
	}

	@Override
	public void removeRule(Integer id) {
		// TODO Auto-generated method stub
		int i = rules.indexOf(new Rule(id));
		rules.remove(i);
	}

	@Override
	public Rule loadRule(Integer id) {
		// TODO
		int i = rules.indexOf(new Rule(id));
		return rules.get(i);
	}

	@Override
	public List<Kpi> listKpi() {
		// TODO
		return kpis;
	}

	@Override
	public void insertKpi(Kpi kpi) {
		// TODO
		kpi.setId(kpis.size());
		kpis.add(kpi);

	}

	@Override
	public void updateKpi(Kpi kpi) {
		// TODO Auto-generated method stub
		int i = kpis.indexOf(kpi);
		kpis.remove(i);
		kpis.add(kpi);
	}

	@Override
	public void removeKpi(Integer id) {
		// TODO Auto-generated method stub
		int i = kpis.indexOf(new Kpi(id));
		kpis.remove(i);
	}

	@Override
	public Kpi loadKpi(Integer id) {
		// TODO
		int i = kpis.indexOf(new Kpi(id));
		return kpis.get(i);
	}

	@Override
	public List<Alias> listAlias() {
		// TODO
		return aliases;
	}

	@Override
	public List<Placeholder> listPlaceholder() {
		// TODO
		return placeholders;
	}

	@Override
	public List<RuleOutput> listMeasure() {
		List types;
		try {
			types = DAOFactory.getDomainDAO().loadListDomainsByTypeAndTenant("KPIRULE_OUTPUT_TYPE");
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
		Map<Integer, String> typeMap = new HashMap<>();
		for (Object object : types) {
			Domain type = (Domain) object;
			typeMap.put(type.getValueId(), type.getValueCd());
		}
		List<RuleOutput> measures = new ArrayList<>();
		for (RuleOutput ruleOutput : listRuleOutput()) {
			if (typeMap.get(ruleOutput.getTypeId()).equals("MEASURE")) {
				measures.add(ruleOutput);
			}
		}
		return measures;
	}

}
