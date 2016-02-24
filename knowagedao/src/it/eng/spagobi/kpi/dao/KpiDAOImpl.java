package it.eng.spagobi.kpi.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDAOObjectNotExistingException;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.bo.ThresholdValue;
import it.eng.spagobi.kpi.metadata.SbiKpiAlias;
import it.eng.spagobi.kpi.metadata.SbiKpiKpi;
import it.eng.spagobi.kpi.metadata.SbiKpiPlaceholder;
import it.eng.spagobi.kpi.metadata.SbiKpiRule;
import it.eng.spagobi.kpi.metadata.SbiKpiRuleOutput;
import it.eng.spagobi.kpi.metadata.SbiKpiThreshold;
import it.eng.spagobi.kpi.metadata.SbiKpiThresholdValue;
import it.eng.spagobi.kpi.threshold.metadata.SbiThreshold;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;

public class KpiDAOImpl extends AbstractHibernateDAO implements IKpiDAO {

	private static final String KPI_MEASURE_CATEGORY = "KPI_MEASURE_CATEGORY";
	private static final String KPI_KPI_CATEGORY = "KPI_KPI_CATEGORY";

	@Override
	public void insertRule(final Rule rule) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				SbiKpiRule sbiRule = new SbiKpiRule();
				sbiRule.setName(rule.getName());
				sbiRule.setDefinition(rule.getDefinition());
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					SbiKpiRuleOutput sbiRuleOutput = new SbiKpiRuleOutput();
					sbiRuleOutput.setSbiKpiRule(sbiRule);
					sbiRuleOutput.setTypeId(ruleOutput.getTypeId());
					// handling Alias
					SbiKpiAlias sbiAlias = manageAlias(session, ruleOutput.getAliasId(), ruleOutput.getAlias());
					if (sbiAlias == null) {
						throw new SpagoBIDOAException("Alias is mandatory. RuleOutput id[" + ruleOutput.getId() + "] alias[" + ruleOutput.getAlias() + "]");
					}
					sbiRuleOutput.setSbiKpiAlias(sbiAlias);
					// handling Category
					Integer newCategoryId = manageCategory(session, ruleOutput.getCategoryId(), ruleOutput.getCategory(), KPI_MEASURE_CATEGORY);
					sbiRuleOutput.setCategoryId(newCategoryId);
					updateSbiCommonInfo4Insert(sbiRuleOutput);
					sbiRule.getSbiKpiRuleOutputs().add(sbiRuleOutput);
				}
				updateSbiCommonInfo4Insert(sbiRule);
				session.save(sbiRule);
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public void updateRule(final Rule rule) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				SbiKpiRule sbiRule = (SbiKpiRule) session.get(SbiKpiRule.class, rule.getId());
				if (sbiRule == null) {
					throw new SpagoBIDAOObjectNotExistingException("KpiRule not found with id [" + rule.getId() + "]");
				}
				sbiRule.setName(rule.getName());
				sbiRule.setDefinition(rule.getDefinition());
				// sbiRule.getSbiKpiRuleOutputs().clear();
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					SbiKpiRuleOutput sbiRuleOutput = new SbiKpiRuleOutput();
					sbiRuleOutput.setSbiKpiRule(sbiRule);
					sbiRuleOutput.setTypeId(ruleOutput.getTypeId());
					// handling Alias
					SbiKpiAlias sbiAlias = manageAlias(session, ruleOutput.getAliasId(), ruleOutput.getAlias());
					if (sbiAlias == null) {
						throw new SpagoBIDOAException("Alias is mandatory. RuleOutput id[" + ruleOutput.getId() + "] alias[" + ruleOutput.getAlias() + "]");
					}
					sbiRuleOutput.setSbiKpiAlias(sbiAlias);
					// handling Category
					Integer newCategoryId = manageCategory(session, ruleOutput.getCategoryId(), ruleOutput.getCategory(), KPI_MEASURE_CATEGORY);
					sbiRuleOutput.setCategoryId(newCategoryId);
					if (ruleOutput.getId() == null) {
						updateSbiCommonInfo4Insert(sbiRuleOutput);
					} else {
						updateSbiCommonInfo4Update(sbiRuleOutput);
					}
					sbiRule.getSbiKpiRuleOutputs().add(sbiRuleOutput);
				}
				updateSbiCommonInfo4Update(sbiRule);
				session.save(sbiRule);
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public void removeRule(Integer id) {
		delete(SbiKpiRule.class, id);
	}

	@Override
	public Rule loadRule(final Integer id) {
		SbiKpiRule rule = load(SbiKpiRule.class, id);
		List<SbiKpiRuleOutput> sbiKpiRuleOutputs = list(new ICriterion<SbiKpiRuleOutput>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiRule", "sbiKpiRule").add(Restrictions.eq("sbiKpiRule.id", id));
			}
		});
		return from(rule, sbiKpiRuleOutputs);
	}

	@Override
	public List<Kpi> listKpi() {
		List<SbiKpiKpi> lst = list(SbiKpiKpi.class);
		List<Kpi> kpis = new ArrayList<>();
		for (SbiKpiKpi sbi : lst) {
			Kpi kpi = from(sbi, null, false);
			kpis.add(kpi);
		}
		return kpis;
	}

	@Override
	public void insertKpi(final Kpi kpi) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				SbiKpiKpi sbiKpi = from(session, null, kpi);
				updateSbiCommonInfo4Insert(sbiKpi);
				session.save(sbiKpi);
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public void updateKpi(final Kpi kpi) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				SbiKpiKpi sbiKpi = (SbiKpiKpi) session.get(SbiKpiKpi.class, kpi.getId());
				if (sbiKpi == null) {
					throw new SpagoBIDAOObjectNotExistingException("Kpi not found with id [" + kpi.getId() + "]");
				}
				sbiKpi = from(session, sbiKpi, kpi);
				updateSbiCommonInfo4Update(sbiKpi);
				session.save(sbiKpi);
				return Boolean.TRUE;
			}
		});
	}

	private SbiKpiKpi from(Session session, SbiKpiKpi sbiKpi, Kpi kpi) {
		if (sbiKpi == null) {
			sbiKpi = new SbiKpiKpi();
			sbiKpi.setId(kpi.getId());
		}
		sbiKpi.setCardinality(kpi.getCardinality());
		sbiKpi.setDefinition(kpi.getDefinition());
		sbiKpi.setName(kpi.getName());
		sbiKpi.setPlaceholder(kpi.getPlaceholder());
		if (kpi.getThreshold() != null) {
			sbiKpi.setThresholdId(kpi.getThreshold().getId());
		}
		// handling Category
		Integer newCategoryId = manageCategory(session, kpi.getCategoryId(), kpi.getCategory(), KPI_KPI_CATEGORY);
		sbiKpi.setCategoryId(newCategoryId);
		return sbiKpi;
	}

	private SbiKpiAlias manageAlias(Session session, Integer aliasId, String aliasName) {
		if (aliasId != null) {
			return new SbiKpiAlias(aliasId);
		} else if (aliasName != null && !aliasName.isEmpty()) {
			SbiKpiAlias sbiAlias = (SbiKpiAlias) session.createCriteria(SbiKpiAlias.class).add(Restrictions.eq("name", aliasName)).uniqueResult();
			if (sbiAlias != null) {
				return sbiAlias;
			} else {
				sbiAlias = new SbiKpiAlias();
				sbiAlias.setName(aliasName);
				updateSbiCommonInfo4Insert(sbiAlias);
				// Integer id = (Integer) session.save(sbiAlias);
				// sbiAlias.setId(id);
				return sbiAlias;
			}
		} else {
			return null;
		}
	}

	private Integer manageCategory(Session session, Integer categoryId, String category, String categoryName) {
		if (categoryId != null && session.get(SbiDomains.class, categoryId) != null) {
			return categoryId;
		} else if (category != null && !category.isEmpty()) {
			SbiDomains cat = (SbiDomains) session.createCriteria(SbiDomains.class).add(Restrictions.eq("domainCd", categoryName))
					.add(Restrictions.eq("valueCd", category)).uniqueResult();
			if (cat != null) {
				return cat.getValueId();
			} else {
				SbiDomains newCategory = new SbiDomains();
				newCategory.setDomainCd(KPI_MEASURE_CATEGORY);
				newCategory.setValueCd(category);
				updateSbiCommonInfo4Insert(newCategory);
				return (Integer) session.save(newCategory);
			}
		} else {
			return null;
		}
	}

	@Override
	public void removeKpi(Integer id) {
		delete(SbiKpiKpi.class, id);
	}

	@Override
	public Kpi loadKpi(final Integer id) {
		return executeOnTransaction(new IExecuteOnTransaction<Kpi>() {
			@Override
			public Kpi execute(Session session) throws JSONException {
				SbiKpiKpi sbiKpi = (SbiKpiKpi) session.get(SbiKpiKpi.class, id);
				if (sbiKpi == null) {
					throw new SpagoBIDAOObjectNotExistingException("Kpi with id [" + id + "] not found");
				}
				SbiKpiThreshold sbiKpiThreshold = (SbiKpiThreshold) session.get(SbiKpiThreshold.class, sbiKpi.getThresholdId());
				return from(sbiKpi, sbiKpiThreshold, true);
			}
		});
	}

	@Override
	public List<Alias> listAlias() {
		List<Alias> ret = new ArrayList<>();
		List<SbiKpiAlias> sbiAlias = list(SbiKpiAlias.class);
		for (SbiKpiAlias sbiKpiAlias : sbiAlias) {
			ret.add(new Alias(sbiKpiAlias.getId(), sbiKpiAlias.getName()));
		}
		return ret;
	}

	@Override
	public List<Placeholder> listPlaceholder() {
		List<Placeholder> ret = new ArrayList<>();
		List<SbiKpiPlaceholder> sbiAlias = list(SbiKpiPlaceholder.class);
		for (SbiKpiPlaceholder sbi : sbiAlias) {
			ret.add(new Placeholder(sbi.getId(), sbi.getName()));
		}
		return ret;
	}

	@Override
	public List<RuleOutput> listRuleOutputByType(final String type) {
		List<SbiKpiRuleOutput> sbiRuleOutputs = list(new ICriterion<SbiKpiRuleOutput>() {
			@Override
			public Criteria evaluate(Session session) {
				// ordering by rule name and measure name
				return session.createCriteria(SbiKpiRuleOutput.class).createAlias("type", "type").createAlias("sbiKpiAlias", "sbiKpiAlias")
						.createAlias("sbiKpiRule", "sbiKpiRule").add(Restrictions.eq("type.valueCd", type)).addOrder(Order.asc("sbiKpiRule.name"))
						.addOrder(Order.asc("sbiKpiAlias.name"));
			}
		});

		List<RuleOutput> ruleOutputs = new ArrayList<>();
		for (SbiKpiRuleOutput sbiKpiRuleOutput : sbiRuleOutputs) {
			ruleOutputs.add(from(sbiKpiRuleOutput));

		}
		return ruleOutputs;
	}

	@Override
	public Threshold loadThreshold(Integer id) {
		SbiKpiThreshold sbiKpiThreshold = load(SbiKpiThreshold.class, id);
		return from(sbiKpiThreshold, true);
	}

	private Threshold from_(SbiKpiThreshold sbiKpiThreshold) {
		Threshold threshold = new Threshold();
		threshold.setId(sbiKpiThreshold.getId());
		threshold.setName(sbiKpiThreshold.getName());
		threshold.setTypeId(sbiKpiThreshold.getType().getValueId());
		threshold.setType(MessageBuilderFactory.getMessageBuilder().getMessage(sbiKpiThreshold.getType().getValueNm()));// sbiKpiThreshold.getType().getValueNm());
		for (Object obj : sbiKpiThreshold.getSbiKpiThresholdValues()) {
			SbiKpiThresholdValue sbiValue = (SbiKpiThresholdValue) obj;
			threshold.getThresholdValues().add(from(sbiValue));
		}
		return threshold;
	}

	private ThresholdValue from(SbiKpiThresholdValue sbiValue) {
		ThresholdValue tv = new ThresholdValue();
		tv.setId(sbiValue.getId());
		tv.setColor(sbiValue.getColor());
		tv.setLabel(sbiValue.getLabel());
		tv.setPosition(sbiValue.getPosition());
		tv.setSeverity(MessageBuilderFactory.getMessageBuilder().getMessage(sbiValue.getSeverity().getValueNm()));// sbiValue.getSeverity().getValueNm());
		tv.setSeverityId(sbiValue.getSeverity().getValueId());
		return tv;
	}

	@Override
	public void insertThreshold(Threshold t) {
		SbiKpiThreshold sbiTh = from(null, t);
		insert(sbiTh);
	}

	private SbiKpiThreshold from(SbiKpiThreshold sbiKpiThreshold, Threshold t) {
		if (sbiKpiThreshold == null) {
			sbiKpiThreshold = new SbiKpiThreshold();
			sbiKpiThreshold.setId(t.getId());
		}
		sbiKpiThreshold.setName(t.getName());
		sbiKpiThreshold.setDescription(t.getDescription());
		sbiKpiThreshold.setType(new SbiDomains(t.getTypeId()));
		for (ThresholdValue tv : t.getThresholdValues()) {
			sbiKpiThreshold.getSbiKpiThresholdValues().add(from(tv));
		}
		return sbiKpiThreshold;
	}

	private SbiKpiThresholdValue from(ThresholdValue tv) {
		SbiKpiThresholdValue sbiValue = new SbiKpiThresholdValue();
		sbiValue.setId(sbiValue.getId());
		sbiValue.setColor(tv.getColor());
		sbiValue.setIncludeMax(tv.isIncludeMax());
		sbiValue.setIncludeMin(tv.isIncludeMin());
		sbiValue.setLabel(tv.getLabel());
		sbiValue.setPosition(tv.getPosition());
		sbiValue.setSeverity(new SbiDomains(tv.getSeverityId()));
		return sbiValue;
	}

	@Override
	public void updateThreshold(Threshold t) {
		SbiKpiThreshold sbiTh = load(SbiKpiThreshold.class, t.getId());
		sbiTh = from(sbiTh, t);
		update(sbiTh);
	}

	@Override
	public void removeThreshold(Integer id) {
		delete(SbiThreshold.class, id);
	}

	@Override
	public List<Threshold> listThreshold() {
		List<SbiKpiThreshold> sbiLst = list(SbiKpiThreshold.class);
		List<Threshold> thresholds = new ArrayList<>();
		for (SbiKpiThreshold sbiThreshold : sbiLst) {
			thresholds.add(from(sbiThreshold, false));
		}
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
		if (sbiKpiRuleOutput.getCategory() != null) {
			ruleOutput.setCategory(sbiKpiRuleOutput.getCategory().getValueNm());
			ruleOutput.setCategoryId(sbiKpiRuleOutput.getCategory().getValueId());
		}
		ruleOutput.setType(MessageBuilderFactory.getMessageBuilder().getMessage(sbiKpiRuleOutput.getType().getValueNm()));
		ruleOutput.setTypeId(sbiKpiRuleOutput.getType().getValueId());
		// Fields from Rule: Rule Id, Rule Name, Author, Date Creation
		ruleOutput.setRuleId(sbiKpiRuleOutput.getSbiKpiRule().getId());
		ruleOutput.setRule(sbiKpiRuleOutput.getSbiKpiRule().getName());
		ruleOutput.setAuthor(sbiKpiRuleOutput.getSbiKpiRule().getCommonInfo().getUserIn());
		ruleOutput.setDateCreation(sbiKpiRuleOutput.getSbiKpiRule().getCommonInfo().getTimeIn());
		return ruleOutput;
	}

	private Kpi from(SbiKpiKpi sbi, SbiKpiThreshold sbiKpiThreshold, boolean full) {
		Kpi kpi = new Kpi();
		kpi.setId(sbi.getId());
		kpi.setCardinality(sbi.getCardinality());
		kpi.setCategory(sbi.getCategory().getValueNm());
		kpi.setCategoryId(sbi.getCategoryId());
		kpi.setDefinition(sbi.getDefinition());
		kpi.setName(sbi.getName());
		kpi.setPlaceholder(sbi.getPlaceholder());
		if (sbiKpiThreshold != null && full) {
			kpi.setThreshold(from(sbiKpiThreshold, full));
		}
		return kpi;
	}

	private Threshold from(SbiKpiThreshold sbiKpiThreshold, boolean full) {
		Threshold threshold = new Threshold();
		threshold.setId(sbiKpiThreshold.getId());
		threshold.setName(sbiKpiThreshold.getName());
		threshold.setDescription(sbiKpiThreshold.getDescription());
		threshold.setType(MessageBuilderFactory.getMessageBuilder().getMessage(sbiKpiThreshold.getType().getValueNm()));// sbiKpiThreshold.getType().getValueNm());
		threshold.setTypeId(sbiKpiThreshold.getType().getValueId());
		if (full) {
			ThresholdValue value = null;
			for (Object obj : sbiKpiThreshold.getSbiKpiThresholdValues()) {
				SbiKpiThresholdValue sbiValue = (SbiKpiThresholdValue) obj;
				ThresholdValue tv = new ThresholdValue();
				tv.setColor(sbiValue.getColor());
				tv.setId(sbiValue.getId());
				if (sbiKpiThreshold.getType().getValueCd().equals("RANGE") || sbiKpiThreshold.getType().getValueCd().equals("MINIMUM")) {
					tv.setIncludeMin(sbiValue.isIncludeMin());
					tv.setMinValue(sbiValue.getMinValue());
				} else if (sbiKpiThreshold.getType().getValueCd().equals("RANGE") || sbiKpiThreshold.getType().getValueCd().equals("MAXIMUM")) {
					tv.setIncludeMax(sbiValue.isIncludeMax());
					tv.setMaxValue(sbiValue.getMaxValue());
				}
				tv.setLabel(sbiValue.getLabel());
				tv.setPosition(sbiValue.getPosition());
				tv.setSeverity(MessageBuilderFactory.getMessageBuilder().getMessage(sbiValue.getSeverity().getValueNm()));// sbiValue.getSeverity().getValueNm());
				tv.setSeverityId(sbiValue.getSeverity().getValueId());
			}
			threshold.getThresholdValues().add(value);
		}
		return threshold;
	}

	private Rule from(SbiKpiRule sbiRule, List<SbiKpiRuleOutput> sbiRuleOutputs) {
		Rule rule = new Rule();
		rule.setId(sbiRule.getId());
		rule.setName(sbiRule.getName());
		rule.setDefinition(sbiRule.getDefinition());
		rule.setRuleOutputs(new ArrayList<RuleOutput>());
		if (sbiRuleOutputs != null) {
			for (SbiKpiRuleOutput ruleOutput : sbiRuleOutputs) {
				rule.getRuleOutputs().add(from(ruleOutput));
			}
		}
		return rule;
	}

}
