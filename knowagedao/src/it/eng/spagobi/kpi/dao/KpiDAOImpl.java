package it.eng.spagobi.kpi.dao;

import it.eng.spago.error.EMFInternalError;
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
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
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
import it.eng.spagobi.utilities.exceptions.SpagoBIException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

public class KpiDAOImpl extends AbstractHibernateDAO implements IKpiDAO {

	private static final String NEW_KPI_MEASURE_ALIAS_ERROR = "newKpi.measure.alias.error";
	private static final String NEW_KPI_RULEOUTPUT_ALIAS_ERROR = "newKpi.ruleoutput.alias.error";
	private static final String NEW_KPI_COPY_OF = "newKpi.copyOf";
	private static final String KPI_MEASURE_CATEGORY = "KPI_MEASURE_CATEGORY";
	private static final String KPI_KPI_CATEGORY = "KPI_KPI_CATEGORY";
	private static final String KPI_RULEOUTPUT_TYPE = "KPI_RULEOUTPUT_TYPE";
	private static final String MEASURE = "MEASURE";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

	@Override
	public void insertRule(final Rule rule) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws SpagoBIException {
				SbiKpiRule sbiRule = new SbiKpiRule();
				sbiRule.setName(rule.getName());
				sbiRule.setDefinition(rule.getDefinition());
				sbiRule.setDataSourceId(rule.getDataSourceId());
				// handling RuleOutputs
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					SbiKpiRuleOutput sbiRuleOutput = new SbiKpiRuleOutput();
					sbiRuleOutput.setSbiKpiRule(sbiRule);
					if (ruleOutput.getType() != null) {
						sbiRuleOutput.setTypeId(ruleOutput.getType().getValueId());
					}
					// handling Alias
					SbiKpiAlias sbiAlias = manageAlias(session, ruleOutput.getAliasId(), ruleOutput.getAlias());
					sbiRuleOutput.setSbiKpiAlias(sbiAlias);
					// handling Category
					SbiDomains category = manageCategory(session, ruleOutput.getCategory(), KPI_MEASURE_CATEGORY);
					sbiRuleOutput.setCategory(category);

					updateSbiCommonInfo4Insert(sbiRuleOutput);
					sbiRule.getSbiKpiRuleOutputs().add(sbiRuleOutput);
				}
				// handling Placeholders
				for (Placeholder placeholder : rule.getPlaceholders()) {
					SbiKpiPlaceholder sbiKpiPlaceholder = managePlaceholder(session, placeholder, rule);
					sbiRule.getSbiKpiPlaceholders().add(sbiKpiPlaceholder);
				}
				updateSbiCommonInfo4Insert(sbiRule);

				sbiKpiRuleValidationForInsert(session, sbiRule);

				session.save(sbiRule);
				return Boolean.TRUE;
			}

		});
	}

	@Override
	public void updateRule(final Rule rule) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws EMFInternalError, SpagoBIException {
				SbiKpiRule sbiRule = (SbiKpiRule) session.load(SbiKpiRule.class, rule.getId());
				sbiRule.setName(rule.getName());
				sbiRule.setDefinition(rule.getDefinition());
				sbiRule.setDataSourceId(rule.getDataSourceId());
				// handling RuleOutputs
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					SbiKpiRuleOutput sbiRuleOutput = new SbiKpiRuleOutput();
					sbiRuleOutput.setSbiKpiRule(sbiRule);
					if (ruleOutput.getType() != null) {
						sbiRuleOutput.setTypeId(ruleOutput.getType().getValueId());
					}
					// handling Alias
					SbiKpiAlias sbiAlias = manageAlias(session, ruleOutput.getAliasId(), ruleOutput.getAlias());

					sbiRuleOutput.setSbiKpiAlias(sbiAlias);
					// handling Category
					SbiDomains category = manageCategory(session, ruleOutput.getCategory(), KPI_MEASURE_CATEGORY);
					sbiRuleOutput.setCategory(category);
					if (ruleOutput.getId() == null) {
						updateSbiCommonInfo4Insert(sbiRuleOutput);
					} else {
						updateSbiCommonInfo4Update(sbiRuleOutput);
					}
					sbiRule.getSbiKpiRuleOutputs().add(sbiRuleOutput);
				}
				// handling Placeholders
				for (Placeholder placeholder : rule.getPlaceholders()) {
					SbiKpiPlaceholder sbiKpiPlaceholder = managePlaceholder(session, placeholder, rule);
					sbiRule.getSbiKpiPlaceholders().add(sbiKpiPlaceholder);
				}
				updateSbiCommonInfo4Update(sbiRule);

				sbiKpiRuleValidationForUpdate(session, sbiRule);

				session.save(sbiRule);
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public void cloneRule(Integer id) {
		Rule rule = loadRule(id);
		rule.setId(null);
		rule.setName(getCopyMsg(rule.getName()));
		for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
			ruleOutput.setId(null);
			ruleOutput.setRuleId(null);
			if (ruleOutput.getType().getValueCd().equals("MEASURE")) {
				String newAlias = getCopyMsg(ruleOutput.getAlias());
				Alias alias = loadAlias(newAlias);
				int i = 0;
				while (alias != null) {
					i++;
					newAlias = getCopyMsg(ruleOutput.getAlias(), i);
					alias = loadAlias(newAlias);
				}
				ruleOutput.setAliasId(null);
				ruleOutput.setAlias(newAlias);
			}
		}
		insertRule(rule);
	}

	private String getCopyMsg(String entityName, Object... o) {
		return MessageFormat.format(message.getMessage(NEW_KPI_COPY_OF), o.length == 0 ? "" : o, entityName) + " ";
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
			public Boolean execute(Session session) {
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
			public Boolean execute(Session session) {
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
		SbiDomains category = manageCategory(session, kpi.getCategory(), KPI_KPI_CATEGORY);
		sbiKpi.setCategory(category);
		return sbiKpi;
	}

	private SbiKpiAlias manageAlias(Session session, Integer aliasId, String aliasName) {
		SbiKpiAlias alias = null;
		if (aliasId != null) {
			alias = (SbiKpiAlias) session.load(SbiKpiAlias.class, aliasId);
		} else if (aliasName != null && !aliasName.isEmpty()) {
			alias = (SbiKpiAlias) session.createCriteria(SbiKpiAlias.class).add(Restrictions.eq("name", aliasName)).uniqueResult();
			if (alias == null) {
				alias = new SbiKpiAlias();
				alias.setName(aliasName);
				updateSbiCommonInfo4Insert(alias);
				// Integer id = (Integer) session.save(sbiAlias);
				// sbiAlias.setId(id);
			}
		}
		return alias;
	}

	private SbiKpiPlaceholder managePlaceholder(Session session, Placeholder placeholder, Rule rule) {
		SbiKpiPlaceholder sbiKpiPlaceholder = null;
		if (placeholder.getId() != null) {
			sbiKpiPlaceholder = (SbiKpiPlaceholder) session.load(SbiKpiPlaceholder.class, placeholder.getId());
		} else if (placeholder.getName() != null && !placeholder.getName().isEmpty()) {
			sbiKpiPlaceholder = (SbiKpiPlaceholder) session.createCriteria(SbiKpiPlaceholder.class).add(Restrictions.eq("name", placeholder.getName()))
					.uniqueResult();
			if (sbiKpiPlaceholder == null) {
				sbiKpiPlaceholder = new SbiKpiPlaceholder();
				sbiKpiPlaceholder.setName(placeholder.getName());
				updateSbiCommonInfo4Insert(sbiKpiPlaceholder);
				// Integer id = (Integer) session.save(sbiKpiPlaceholder);
				// sbiKpiPlaceholder.setId(id);
			}
		}
		return sbiKpiPlaceholder;
	}

	private SbiDomains manageCategory(Session session, Domain category, String categoryName) {
		SbiDomains cat = null;
		if (category != null) {
			if (category.getValueId() != null) {
				cat = new SbiDomains(category.getValueId());
			} else if (category.getValueCd() != null) {
				cat = (SbiDomains) session.createCriteria(SbiDomains.class).add(Restrictions.eq("domainCd", categoryName))
						.add(Restrictions.eq("valueCd", category.getValueCd())).uniqueResult();
				if (cat == null) {
					cat = new SbiDomains();
					cat.setDomainCd(categoryName);
					cat.setDomainNm(categoryName);
					cat.setValueCd(category.getValueCd());
					cat.setValueNm(category.getValueCd());
					cat.setValueDs(category.getValueCd());
					updateSbiCommonInfo4Insert(cat);
					session.save(cat);
				}
			}
		}
		return cat;
	}

	@Override
	public void removeKpi(Integer id) {
		delete(SbiKpiKpi.class, id);
	}

	@Override
	public Kpi loadKpi(final Integer id) {
		return executeOnTransaction(new IExecuteOnTransaction<Kpi>() {
			@Override
			public Kpi execute(Session session) {
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
	public List<Alias> listAliasNotInMeasure() {
		List<SbiKpiAlias> sbiAlias = list(new ICriterion<SbiKpiAlias>() {
			@Override
			public Criteria evaluate(Session session) {
				/*
				 * return session.createCriteria(SbiKpiRuleOutput.class).createAlias("type", "type").createAlias("sbiKpiAlias", "sbiKpiAlias")
				 * .createAlias("sbiKpiRule", "sbiKpiRule").add(Restrictions.eq("type.valueCd", type)).addOrder(Order.asc("sbiKpiRule.name"))
				 * .addOrder(Order.asc("sbiKpiAlias.name"));
				 */
				Criteria c = session.createCriteria(SbiKpiAlias.class);
				DetachedCriteria detachedcriteria = DetachedCriteria.forClass(SbiKpiRuleOutput.class);
				detachedcriteria.createAlias("sbiKpiAlias", "sbiKpiAlias").setProjection(Property.forName("sbiKpiAlias.name")).createAlias("type", "type")
						.add(Restrictions.eq("type.valueCd", MEASURE));
				c.add(Property.forName("name").notIn(detachedcriteria));
				return c;
			}
		});
		List<Alias> ret = new ArrayList<>();
		for (SbiKpiAlias sbiKpiAlias : sbiAlias) {
			ret.add(new Alias(sbiKpiAlias.getId(), sbiKpiAlias.getName()));
		}
		return ret;
	}

	@Override
	public Alias loadAlias(final String name) {
		List<SbiKpiAlias> aliases = list(new ICriterion<SbiKpiAlias>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiAlias.class).add(Restrictions.eq("name", name));
			}
		});
		if (aliases != null && !aliases.isEmpty()) {
			SbiKpiAlias alias = aliases.get(0);
			return new Alias(alias.getId(), alias.getName());
		}
		return null;
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
				// Ordering by rule name and measure name
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
		// TODO
		SbiKpiThreshold sbiTh = from(null, null, t);
		insert(sbiTh);
	}

	private SbiKpiThreshold from(Session session, SbiKpiThreshold sbiKpiThreshold, Threshold t) {
		if (sbiKpiThreshold == null) {
			sbiKpiThreshold = new SbiKpiThreshold();
			sbiKpiThreshold.setId(t.getId());
		}
		sbiKpiThreshold.setName(t.getName());
		sbiKpiThreshold.setDescription(t.getDescription());
		sbiKpiThreshold.setType(new SbiDomains(t.getTypeId()));
		Map<Integer, SbiKpiThresholdValue> oldValueMap = new HashMap<>();
		for (Object obj : sbiKpiThreshold.getSbiKpiThresholdValues()) {
			SbiKpiThresholdValue oldValue = (SbiKpiThresholdValue) obj;
			oldValueMap.put(oldValue.getId(), oldValue);
		}
		for (ThresholdValue tv : t.getThresholdValues()) {
			SbiKpiThresholdValue sbiThresholdValue = null;
			if (tv.getId() == null) {
				sbiThresholdValue = from(sbiThresholdValue, tv);
				updateSbiCommonInfo4Insert(sbiThresholdValue);
				sbiThresholdValue.setSbiKpiThreshold(sbiKpiThreshold);
				sbiKpiThreshold.getSbiKpiThresholdValues().add(sbiThresholdValue);
			} else {
				sbiThresholdValue = oldValueMap.get(tv.getId());
				oldValueMap.remove(tv.getId());
				sbiThresholdValue = from(sbiThresholdValue, tv);
				updateSbiCommonInfo4Update(sbiThresholdValue);
			}
		}
		Iterator<SbiKpiThresholdValue> iterator = sbiKpiThreshold.getSbiKpiThresholdValues().iterator();
		while (iterator.hasNext()) {
			SbiKpiThresholdValue oldValue = iterator.next();
			if (oldValueMap.get(oldValue.getId()) != null) {
				iterator.remove();
			}
		}
		return sbiKpiThreshold;
	}

	private SbiKpiThresholdValue from(SbiKpiThresholdValue sbiValue, ThresholdValue tv) {
		if (sbiValue == null) {
			sbiValue = new SbiKpiThresholdValue();
		}
		sbiValue.setId(tv.getId());
		sbiValue.setColor(tv.getColor());
		char isIncludeMax = tv.isIncludeMax() ? 'T' : 'F';
		sbiValue.setIncludeMax(isIncludeMax);
		char isIncludeMin = tv.isIncludeMin() ? 'T' : 'F';
		sbiValue.setIncludeMin(isIncludeMin);
		sbiValue.setMaxValue(tv.getMaxValue());
		sbiValue.setMinValue(tv.getMinValue());
		sbiValue.setLabel(tv.getLabel());
		sbiValue.setPosition(tv.getPosition());
		sbiValue.setSeverity(new SbiDomains(tv.getSeverityId()));
		return sbiValue;
	}

	@Override
	public void updateThreshold(final Threshold t) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) {
				SbiKpiThreshold sbiTh = (SbiKpiThreshold) session.load(SbiKpiThreshold.class, t.getId());
				sbiTh = from(session, sbiTh, t);
				session.merge(sbiTh);
				session.flush();
				return Boolean.TRUE;
			}
		});
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
			final Domain domain = domainDAO.loadDomainByCodeAndValue(KPI_RULEOUTPUT_TYPE, MEASURE);
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
			Domain category = new Domain();
			category.setValueId(sbiKpiRuleOutput.getCategory().getValueId());
			category.setValueCd(sbiKpiRuleOutput.getCategory().getValueCd());
			ruleOutput.setCategory(category);
		}
		if (sbiKpiRuleOutput.getType() != null) {
		}
		// TODO remove this line ruleOutput.setType(MessageBuilderFactory.getMessageBuilder().getMessage(sbiKpiRuleOutput.getType().getValueNm()));
		ruleOutput.setType(from(sbiKpiRuleOutput.getType()));
		// Fields from Rule: Rule Id, Rule Name, Author, Date Creation
		ruleOutput.setRuleId(sbiKpiRuleOutput.getSbiKpiRule().getId());
		ruleOutput.setRule(sbiKpiRuleOutput.getSbiKpiRule().getName());
		ruleOutput.setAuthor(sbiKpiRuleOutput.getSbiKpiRule().getCommonInfo().getUserIn());
		ruleOutput.setDateCreation(sbiKpiRuleOutput.getSbiKpiRule().getCommonInfo().getTimeIn());
		return ruleOutput;
	}

	private Domain from(SbiDomains sbiType) {
		Domain type = new Domain();
		type.setDomainCode(sbiType.getDomainCd());
		type.setDomainName(sbiType.getDomainNm());
		type.setValueCd(sbiType.getValueCd());
		type.setValueDescription(sbiType.getValueDs());
		type.setValueName(sbiType.getValueNm());
		type.setValueId(sbiType.getValueId());
		return type;
	}

	private Kpi from(SbiKpiKpi sbi, SbiKpiThreshold sbiKpiThreshold, boolean full) {
		Kpi kpi = new Kpi();
		kpi.setId(sbi.getId());
		kpi.setCardinality(sbi.getCardinality());
		if (sbi.getCategory() != null) {
			kpi.setCategory(from(sbi.getCategory()));
		}
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
			for (Object obj : sbiKpiThreshold.getSbiKpiThresholdValues()) {
				SbiKpiThresholdValue sbiValue = (SbiKpiThresholdValue) obj;
				ThresholdValue tv = new ThresholdValue();
				tv.setColor(sbiValue.getColor());
				tv.setId(sbiValue.getId());
				if (sbiKpiThreshold.getType().getValueCd().equals("RANGE") || sbiKpiThreshold.getType().getValueCd().equals("MINIMUM")) {
					tv.setIncludeMin(sbiValue.getIncludeMin() != null && sbiValue.getIncludeMin().charValue() == 'T');
					tv.setMinValue(sbiValue.getMinValue());
				} else if (sbiKpiThreshold.getType().getValueCd().equals("RANGE") || sbiKpiThreshold.getType().getValueCd().equals("MAXIMUM")) {
					tv.setIncludeMax(sbiValue.getIncludeMax() != null && sbiValue.getIncludeMax().charValue() == 'T');
					tv.setMaxValue(sbiValue.getMaxValue());
				}
				tv.setLabel(sbiValue.getLabel());
				tv.setPosition(sbiValue.getPosition());
				tv.setSeverity(MessageBuilderFactory.getMessageBuilder().getMessage(sbiValue.getSeverity().getValueNm()));// sbiValue.getSeverity().getValueNm());
				tv.setSeverityId(sbiValue.getSeverity().getValueId());
				threshold.getThresholdValues().add(tv);
			}
		}
		return threshold;
	}

	private Rule from(SbiKpiRule sbiRule, List<SbiKpiRuleOutput> sbiRuleOutputs) {
		Rule rule = new Rule();
		rule.setId(sbiRule.getId());
		rule.setName(sbiRule.getName());
		rule.setDefinition(sbiRule.getDefinition());
		rule.setDataSourceId(sbiRule.getDataSourceId());
		rule.setRuleOutputs(new ArrayList<RuleOutput>());
		if (sbiRuleOutputs != null) {
			for (SbiKpiRuleOutput ruleOutput : sbiRuleOutputs) {
				rule.getRuleOutputs().add(from(ruleOutput));
			}
		}
		return rule;
	}

	private void sbiKpiRuleValidationForInsert(Session session, SbiKpiRule rule) throws SpagoBIException {
		checkRule(rule);
		boolean hasMeasure = false;
		for (SbiKpiRuleOutput ruleOutput : rule.getSbiKpiRuleOutputs()) {
			if (ruleOutput.getSbiKpiAlias() == null) {
				throw new SpagoBIDOAException("RuleOutput Alias is mandatory");
			}
			if (ruleOutput.getType().getValueId() == null) {
				throw new SpagoBIDOAException("RuleOutput Type is mandatory");
			}
			if (MEASURE.equals(ruleOutput.getType().getValueCd())) {
				hasMeasure = true;
				// looking for a ruleoutput with same alias
				if (session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias")
						.add(Restrictions.eq("sbiKpiAlias.name", ruleOutput.getSbiKpiAlias().getName())).uniqueResult() != null) {
					throw new SpagoBIException(MessageFormat.format(message.getMessage(NEW_KPI_MEASURE_ALIAS_ERROR), ruleOutput.getSbiKpiAlias().getName()));
				}
			} else {
				// looking for a measures with same alias
				if (session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias").createAlias("type", "type")
						.add(Restrictions.eq("type.valueCd", MEASURE)).add(Restrictions.eq("sbiKpiAlias.name", ruleOutput.getSbiKpiAlias().getName()))
						.uniqueResult() != null) {
					throw new SpagoBIException(MessageFormat.format(message.getMessage(NEW_KPI_RULEOUTPUT_ALIAS_ERROR), ruleOutput.getSbiKpiAlias().getName()));
				}
			}
			if (!hasMeasure) {
				throw new SpagoBIDOAException("Rule must contain at least one measure");
			}
		}
	}

	private void sbiKpiRuleValidationForUpdate(Session session, SbiKpiRule rule) throws SpagoBIException {
		checkRule(rule);
		boolean hasMeasure = false;
		for (SbiKpiRuleOutput ruleOutput : rule.getSbiKpiRuleOutputs()) {
			if (ruleOutput.getSbiKpiAlias() == null) {
				throw new SpagoBIDOAException("RuleOutput Alias is mandatory");
			}
			if (ruleOutput.getType().getValueId() == null) {
				throw new SpagoBIDOAException("RuleOutput Type is mandatory");
			}
			if (MEASURE.equals(ruleOutput.getType().getValueCd())) {
				// looking for a ruleoutput with same alias
				if (session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias").add(Restrictions.ne("id", ruleOutput.getId()))
						.add(Restrictions.eq("sbiKpiAlias.name", ruleOutput.getSbiKpiAlias().getName())).uniqueResult() != null) {

					throw new SpagoBIException(MessageFormat.format(message.getMessage(NEW_KPI_MEASURE_ALIAS_ERROR), ruleOutput.getSbiKpiAlias().getName()));
				}
			} else {
				// looking for a measures with same alias
				if (session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias").createAlias("type", "type")
						.add(Restrictions.ne("id", ruleOutput.getId())).add(Restrictions.eq("type.valueCd", MEASURE))
						.add(Restrictions.eq("sbiKpiAlias.name", ruleOutput.getSbiKpiAlias().getName())).uniqueResult() != null) {
					throw new SpagoBIException(MessageFormat.format(message.getMessage(NEW_KPI_RULEOUTPUT_ALIAS_ERROR), ruleOutput.getSbiKpiAlias().getName()));
				}
			}
			if (!hasMeasure) {
				throw new SpagoBIDOAException("Rule must contain at least one measure");
			}
		}
	}

	private void checkRule(SbiKpiRule rule) {
		if (rule.getName() == null) {
			throw new SpagoBIDOAException("Rule Name is mandatory");
		}
		if (rule.getDefinition() == null) {
			throw new SpagoBIDOAException("Rule Definition is mandatory");
		}
		if (rule.getDataSourceId() == null) {
			throw new SpagoBIDOAException("Rule Datasource is mandatory");
		}
		if (rule.getSbiKpiRuleOutputs() == null || rule.getSbiKpiRuleOutputs().isEmpty()) {
			throw new SpagoBIDOAException("Rule must contain at least one measure");
		}
	}

}
