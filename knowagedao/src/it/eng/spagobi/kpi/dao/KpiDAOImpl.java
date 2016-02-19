package it.eng.spagobi.kpi.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.metadata.SbiKpiKpi;
import it.eng.spagobi.kpi.metadata.SbiKpiRuleOutput;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class KpiDAOImpl extends AbstractHibernateDAO implements IKpiDAO {

	// TODO These mock lists must be removed after implementing related services
	private static List<Kpi> kpis = new ArrayList<>();
	private static List<Alias> aliases = new ArrayList<>();
	private static List<Rule> rules = new ArrayList<>();
	private static List<Placeholder> placeholders = new ArrayList<>();
	private static List<Threshold> thresholds = new ArrayList<>();

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
	public void insertRule(final Rule rule) {
		// TODO Auto-generated method stub
		rule.setId(rules.size());
		rules.add(rule);
		/*
		 * executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
		 * 
		 * @Override public Boolean execute(Session session) throws JSONException { SbiKpiRule r = new SbiKpiRule(); r.setName(rule.getName());
		 * r.setDefinition(rule.getDefinition()); Serializable id = session.save(r); for (RuleOutput ruleOutput : rule.getRuleOutputs()) { SbiKpiRuleOutput krl
		 * = new SbiKpiRuleOutput(); krl.setId(ruleOutput.getId()); krl.setCategory(ruleOutput.getCategory()); r.getSbiKpiRuleOutputs().add(arg0); } return
		 * Boolean.TRUE; } });
		 */

	}

	@Override
	public void updateRule(Rule rule) {
		// TODO Auto-generated method stub
		rules.remove(rule);
		rules.add(rule);
	}

	@Override
	public void removeRule(Integer id) {
		// TODO Auto-generated method stub
		rules.remove(new Rule(id));
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
		// return kpis;
		List<SbiKpiKpi> lst = list(SbiKpiKpi.class);
		List<Kpi> kpis = new ArrayList<>();
		for (SbiKpiKpi sbi : lst) {
			Kpi kpi = from(sbi);
			kpis.add(kpi);
		}
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
		kpis.remove(kpi);
		kpis.add(kpi);
	}

	@Override
	public void removeKpi(Integer id) {
		// TODO Auto-generated method stub
		kpis.remove(new Kpi(id));
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
	public List<RuleOutput> listRuleOutputByType(final String type) {
		List<SbiKpiRuleOutput> sbiRuleOutputs = new ArrayList<>();

		sbiRuleOutputs = list(new ICriterion<SbiKpiRuleOutput>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiRuleOutput.class).createAlias("type", "type").add(Restrictions.eq("type.valueCd", type));
			}
		});

		List<RuleOutput> ruleOutputs = new ArrayList<>();
		for (SbiKpiRuleOutput sbiKpiRuleOutput : sbiRuleOutputs) {
			ruleOutputs.add(from(sbiKpiRuleOutput));
		}
		return ruleOutputs;
		/*
		 * List types; try { types = DAOFactory.getDomainDAO().loadListDomainsByTypeAndTenant("KPI_RULEOUTPUT_TYPE"); } catch (EMFUserError e) { throw new
		 * SpagoBIDOAException(e); } Map<Integer, String> typeMap = new HashMap<>(); for (Object object : types) { Domain type = (Domain) object;
		 * typeMap.put(type.getValueId(), type.getValueCd()); } List<RuleOutput> measures = new ArrayList<>(); for (RuleOutput ruleOutput : listRuleOutput()) {
		 * if (typeMap.get(ruleOutput.getTypeId()).equals("MEASURE")) { measures.add(ruleOutput); } }
		 */
	}

	@Override
	public Threshold loadThreshold(Integer id) {
		// TODO Auto-generated method stub
		int i = thresholds.indexOf(new Threshold(id));
		return thresholds.get(i);
	}

	@Override
	public void insertThreshold(Threshold t) {
		// TODO
		t.setId(thresholds.size());
		thresholds.add(t);
	}

	@Override
	public void updateThreshold(Threshold t) {
		// TODO
		thresholds.remove(t);
		thresholds.add(t);
	}

	@Override
	public void removeThreshold(Integer id) {
		// TODO
		thresholds.remove(new Threshold(id));
	}

	@Override
	public List<Threshold> listThreshold() {
		// TODO Auto-generated method stub
		return thresholds;
	}

	@Override
	public RuleOutput loadMeasureByName(final String name) {
		List<SbiKpiRuleOutput> ruleOutputs = null;
		try {
			IDomainDAO domainDAO = DAOFactory.getDomainDAO();
			final Domain domain = domainDAO.loadDomainByCodeAndValue("KPI_RULEOUTPUT_TYPE", "MEASURE");
			ruleOutputs = list(new ICriterion<SbiKpiRuleOutput>() {
				@Override
				public Criteria evaluate(Session session) {
					return session.createCriteria(SbiKpiRuleOutput.class).add(Restrictions.eq("typeId", domain.getValueId()))
							.createAlias("sbiKpiAlias", "sbiKpiAlias").add(Restrictions.eq("sbiKpiAlias.name", name));
				}
			});
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
		if (ruleOutputs != null && !ruleOutputs.isEmpty()) {
			return from(ruleOutputs.get(0));
		}
		return null;
	}

	private RuleOutput from(SbiKpiRuleOutput sbiKpiRuleOutput) {
		RuleOutput ruleOutput = new RuleOutput();
		ruleOutput.setId(sbiKpiRuleOutput.getId());
		ruleOutput.setAlias(sbiKpiRuleOutput.getSbiKpiAlias().getName());
		ruleOutput.setAliasId(sbiKpiRuleOutput.getSbiKpiAlias().getId());
		ruleOutput.setAuthor(sbiKpiRuleOutput.getCommonInfo().getUserIn());
		ruleOutput.setCategory(sbiKpiRuleOutput.getCategory().getValueNm());
		ruleOutput.setCategoryId(sbiKpiRuleOutput.getCategory().getValueId());
		ruleOutput.setType(sbiKpiRuleOutput.getType().getValueNm());
		ruleOutput.setTypeId(sbiKpiRuleOutput.getType().getValueId());
		ruleOutput.setDateCreation(sbiKpiRuleOutput.getCommonInfo().getTimeIn());
		ruleOutput.setRuleId(sbiKpiRuleOutput.getRuleId());
		// TODO
		// ruleOutput.setRule(rule);
		return ruleOutput;
	}

	private Kpi from(SbiKpiKpi sbi) {
		Kpi kpi = new Kpi();
		kpi.setId(sbi.getId());
		kpi.setCardinality(sbi.getCardinality());
		kpi.setCategory(sbi.getCategory().getValueNm());
		kpi.setCategoryId(sbi.getCategoryId());
		kpi.setDefinition(sbi.getDefinition());
		kpi.setName(sbi.getName());
		kpi.setPlaceholder(sbi.getPlaceholder());
		// kpi.setListThreshold(listThreshold); lazy list
		return kpi;
	}

}
