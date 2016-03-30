/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.kpi.dao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinFragment;
import org.hibernate.transform.Transformers;

import it.eng.qbe.InExpressionIgnoringCase;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDAOObjectNotExistingException;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Cardinality;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.KpiScheduler;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.kpi.bo.TargetValue;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.bo.ThresholdValue;
import it.eng.spagobi.kpi.metadata.SbiKpiAlias;
import it.eng.spagobi.kpi.metadata.SbiKpiExecution;
import it.eng.spagobi.kpi.metadata.SbiKpiKpi;
import it.eng.spagobi.kpi.metadata.SbiKpiKpiId;
import it.eng.spagobi.kpi.metadata.SbiKpiPlaceholder;
import it.eng.spagobi.kpi.metadata.SbiKpiRule;
import it.eng.spagobi.kpi.metadata.SbiKpiRuleId;
import it.eng.spagobi.kpi.metadata.SbiKpiRuleOutput;
import it.eng.spagobi.kpi.metadata.SbiKpiTarget;
import it.eng.spagobi.kpi.metadata.SbiKpiTargetValue;
import it.eng.spagobi.kpi.metadata.SbiKpiTargetValueId;
import it.eng.spagobi.kpi.metadata.SbiKpiThreshold;
import it.eng.spagobi.kpi.metadata.SbiKpiThresholdValue;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;

public class KpiDAOImpl extends AbstractHibernateDAO implements IKpiDAO {

	private static final String NEW_KPI_RULEOUTPUT_ALIAS_MANDATORY = "newKpi.ruleoutput.alias.mandatory";
	private static final String NEW_KPI_RULEOUTPUT_TYPE_MANDATORY = "newKpi.ruleoutput.type.mandatory";
	private static final String NEW_KPI_RULE_DATASOURCE_MANDATORY = "newKpi.rule.datasource.mandatory";
	private static final String NEW_KPI_RULE_DEFINITION_MANDATORY = "newKpi.rule.definition.mandatory";
	private static final String NEW_KPI_RULE_NAME_MANDATORY = "newKpi.rule.name.mandatory";
	private static final String NEW_KPI_RULE_MEASURES_MANDATORY = "newKpi.rule.measures.mandatory";
	private static final String NEW_KPI_ALIAS_NOT_AVAILABLE_AS_MEASURE = "newKpi.aliasNotAvailableAsMeasure";
	private static final String NEW_KPI_ALIAS_NOT_AVAILABLE = "newKpi.aliasNotAvailable";
	private static final String NEW_KPI_KPI_NOT_FOUND = "newKpi.kpiNotFound";
	private static final String KPI_MEASURE_CATEGORY = "KPI_MEASURE_CATEGORY";
	private static final String KPI_KPI_CATEGORY = "KPI_KPI_CATEGORY";
	private static final String KPI_TARGET_CATEGORY = "KPI_TARGET_CATEGORY";
	private static final String MEASURE = "MEASURE";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

	// Status of Kpi or Rule
	public enum STATUS {
		ACTIVE, NOT_ACTIVE, ALL;
	}

	@Override
	public List<String> listPlaceholderByMeasures(final List<String> measures) {
		List<SbiKpiPlaceholder> lst = list(new ICriterion<SbiKpiPlaceholder>() {
			@Override
			public Criteria evaluate(Session session) {
				DetachedCriteria detachedCriteria = DetachedCriteria.forClass(SbiKpiRuleOutput.class).createAlias("sbiKpiRule", "sbiKpiRule")
						.createAlias("sbiKpiAlias", "sbiKpiAlias").setProjection(Property.forName("sbiKpiRule.sbiKpiRuleId.id"))
						.add(Restrictions.eq("sbiKpiRule.active", 'T')).add(Restrictions.in("sbiKpiAlias.name", measures));

				Criteria c = session.createCriteria(SbiKpiRule.class).createAlias("sbiKpiPlaceholders", "sbiKpiPlaceholders")
						.add(Property.forName("sbiKpiRuleId.id").in(detachedCriteria))
						.setProjection(Projections.distinct(Projections.property("sbiKpiPlaceholders.name").as("name")))
						.setResultTransformer(Transformers.aliasToBean(SbiKpiPlaceholder.class));
				return c;
			}
		});
		List<String> placeholdername = new ArrayList<>();
		for (SbiKpiPlaceholder sbiKpiPlaceholder : lst) {
			placeholdername.add(sbiKpiPlaceholder.getName());
		}
		return placeholdername;
	}

	@Override
	public List<Cardinality> buildCardinality(final List<String> measures) {
		return executeOnTransaction(new IExecuteOnTransaction<List<Cardinality>>() {
			@Override
			public List<Cardinality> execute(Session session) throws Exception {

				List<SbiKpiRuleOutput> allRuleOutputs = session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiRule", "sbiKpiRule")
						.createAlias("sbiKpiRule.sbiKpiRuleOutputs", "sbiKpiRule_sbiKpiRuleOutputs")
						.createAlias("sbiKpiRule_sbiKpiRuleOutputs.sbiKpiAlias", "parent_sbiKpiAlias").add(Restrictions.eq("sbiKpiRule.active", 'T'))
						.add(new InExpressionIgnoringCase("parent_sbiKpiAlias.name", measures)).list();
				Map<SbiKpiRuleId, Cardinality> cardinalityMap = new HashMap<>();

				for (SbiKpiRuleOutput sbiRuleOutput : allRuleOutputs) {
					Cardinality cardinality = cardinalityMap.get(sbiRuleOutput.getSbiKpiRule().getSbiKpiRuleId());
					if (cardinality == null) {
						cardinality = new Cardinality();
						cardinality.setRuleId(sbiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getId());
						cardinality.setRuleVersion(sbiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getVersion());
						cardinality.setRuleName(sbiRuleOutput.getSbiKpiRule().getName());
						cardinalityMap.put(sbiRuleOutput.getSbiKpiRule().getSbiKpiRuleId(), cardinality);
					}
					if (MEASURE.equals(sbiRuleOutput.getType().getValueCd())) {
						cardinality.setMeasureName(sbiRuleOutput.getSbiKpiAlias().getName());
					} else {
						cardinality.getAttributes().put(sbiRuleOutput.getSbiKpiAlias().getName(), Boolean.FALSE);
					}
				}

				List<Cardinality> cardinalityOrdered = new ArrayList<>();
				Collection<Cardinality> cardinality = cardinalityMap.values();
				// output order has to be the same one as input measures list
				for (String measure : measures) {
					boolean found = false;
					for (Cardinality c : cardinality) {
						if (measure.equals(c.getMeasureName())) {
							cardinalityOrdered.add(c);
							found = true;
						}
					}
					if (!found) {
						cardinalityOrdered.add(new Cardinality(measure));
					}
				}
				return cardinalityOrdered;
			}
		});
	}

	@Override
	public Rule insertRule(final Rule rule) {
		return insertRule(rule, false);
	}

	private Rule insertRule(final Rule rule, final boolean newVersion) {
		return executeOnTransaction(new IExecuteOnTransaction<Rule>() {
			@Override
			public Rule execute(Session session) throws SpagoBIException {
				ruleValidationForInsert(session, rule);

				SbiKpiRule sbiRule = new SbiKpiRule();
				if (newVersion) {
					SbiKpiRule oldRule = (SbiKpiRule) session.load(SbiKpiRule.class, new SbiKpiRuleId(rule.getId(), rule.getVersion()));
					if (oldRule.getActive() == 'T') {
						oldRule.setActive(null);
						sbiRule.getSbiKpiRuleId().setId(rule.getId());
					}
				}
				sbiRule.setActive('T');
				sbiRule.setName(rule.getName());
				sbiRule.setDefinition(rule.getDefinition());
				sbiRule.setDataSourceId(rule.getDataSourceId());
				// handling RuleOutputs
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					SbiKpiRuleOutput sbiRuleOutput = new SbiKpiRuleOutput();
					sbiRuleOutput.setSbiKpiRule(sbiRule);
					sbiRuleOutput.setTypeId(ruleOutput.getType().getValueId());
					if (ruleOutput.getHierarchy() != null) {
						sbiRuleOutput.setHierarchyId(ruleOutput.getHierarchy().getValueId());
					}
					// handling Alias
					SbiKpiAlias sbiAlias = manageAlias(session, ruleOutput.getAliasId(), ruleOutput.getAlias());
					sbiRuleOutput.setSbiKpiAlias(sbiAlias);
					// handling Category
					SbiDomains category = insertOrUpdateCategory(session, ruleOutput.getCategory(), KPI_MEASURE_CATEGORY);
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

				SbiKpiRuleId sbiKpiRuleId = (SbiKpiRuleId) session.save(sbiRule);
				rule.setId(sbiKpiRuleId.getId());
				rule.setVersion(sbiKpiRuleId.getVersion());
				return rule;
			}
		});
	}

	@Override
	public Kpi insertNewVersionKpi(final Kpi kpi) {
		return insertKpi(kpi, true);
	}

	@Override
	public Rule insertNewVersionRule(final Rule rule) {
		return insertRule(rule, true);
	}

	@Override
	public void updateRule(final Rule rule) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws EMFInternalError, SpagoBIException {
				ruleValidationForUpdate(session, rule);
				SbiKpiRule sbiRule = (SbiKpiRule) session.load(SbiKpiRule.class, new SbiKpiRuleId(rule.getId(), rule.getVersion()));
				sbiRule.setName(rule.getName());
				sbiRule.setDefinition(rule.getDefinition());
				sbiRule.setDataSourceId(rule.getDataSourceId());

				// clearing removed references
				Iterator<SbiKpiRuleOutput> iterator = sbiRule.getSbiKpiRuleOutputs().iterator();
				while (iterator.hasNext()) {
					SbiKpiRuleOutput sbiKpiRuleOutput = iterator.next();
					// List of RuleOutput cannot be empty
					if (rule.getRuleOutputs().indexOf(new RuleOutput(sbiKpiRuleOutput.getId())) == -1) {
						iterator.remove();
					}
				}
				// handling RuleOutputs
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					SbiKpiRuleOutput sbiRuleOutput = manageRuleOutput(session, sbiRule, ruleOutput);
					if (ruleOutput.getHierarchy() != null) {
						sbiRuleOutput.setHierarchyId(ruleOutput.getHierarchy().getValueId());
					}
					sbiRuleOutput.setTypeId(ruleOutput.getType().getValueId());
					// handling Alias
					SbiKpiAlias sbiAlias = manageAlias(session, ruleOutput.getAliasId(), ruleOutput.getAlias());

					sbiRuleOutput.setSbiKpiAlias(sbiAlias);
					// handling Category
					SbiDomains category = insertOrUpdateCategory(session, ruleOutput.getCategory(), KPI_MEASURE_CATEGORY);
					sbiRuleOutput.setCategory(category);

					sbiRule.getSbiKpiRuleOutputs().add(sbiRuleOutput);
				}

				// handling Placeholders
				for (Placeholder placeholder : rule.getPlaceholders()) {
					SbiKpiPlaceholder sbiKpiPlaceholder = managePlaceholder(session, placeholder, rule);
					sbiRule.getSbiKpiPlaceholders().add(sbiKpiPlaceholder);
				}
				updateSbiCommonInfo4Update(sbiRule);

				session.save(sbiRule);
				return Boolean.TRUE;
			}

		});
	}

	private SbiKpiRuleOutput manageRuleOutput(Session session, SbiKpiRule sbiRule, RuleOutput ruleOutput) {
		SbiKpiRuleOutput sbiRuleOutput = null;
		if (ruleOutput.getId() == null) {
			sbiRuleOutput = new SbiKpiRuleOutput();
			sbiRuleOutput.setSbiKpiRule(sbiRule);
			updateSbiCommonInfo4Insert(sbiRuleOutput);
		} else {
			sbiRuleOutput = (SbiKpiRuleOutput) session.load(SbiKpiRuleOutput.class, ruleOutput.getId());
			updateSbiCommonInfo4Update(sbiRuleOutput);
		}
		return sbiRuleOutput;
	}

	@Override
	public void removeRule(final Integer id, final Integer version, boolean toBeVersioned) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws Exception {
				SbiKpiRule rule = (SbiKpiRule) session.load(SbiKpiRule.class, new SbiKpiRuleId(id, version));

				// Deleting placeholders
				for (SbiKpiPlaceholder sbiKpiPlaceholder : rule.getSbiKpiPlaceholders()) {
					session.delete(sbiKpiPlaceholder);
				}

				// Deleting Rule
				session.delete(rule);
				session.flush();

				// Deleting unused categories
				DetachedCriteria usedCategories = DetachedCriteria.forClass(SbiKpiRuleOutput.class).createAlias("category", "category")
						.add(Restrictions.isNotNull("category")).setProjection(Property.forName("category.valueId"));
				List<SbiDomains> categoriesToDelete = session.createCriteria(SbiDomains.class).add(Restrictions.eq("domainCd", KPI_MEASURE_CATEGORY))
						.add(Property.forName("valueId").notIn(usedCategories)).list();
				for (SbiDomains cat : categoriesToDelete) {
					session.delete(cat);
				}

				// Deleting unused aliases
				DetachedCriteria usedAliases = DetachedCriteria.forClass(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias")
						.setProjection(Property.forName("sbiKpiAlias.id"));
				List<SbiKpiAlias> aliasesToDelete = session.createCriteria(SbiKpiAlias.class).add(Property.forName("id").notIn(usedAliases)).list();
				for (SbiKpiAlias alias : aliasesToDelete) {
					session.delete(alias);
				}

				return Boolean.TRUE;
			}
		});
	}

	@Override
	public Rule loadRule(final Integer id, final Integer version) {
		SbiKpiRule rule = executeOnTransaction(new IExecuteOnTransaction<SbiKpiRule>() {
			@Override
			public SbiKpiRule execute(Session session) throws Exception {
				SbiKpiRule rule = (SbiKpiRule) session.load(SbiKpiRule.class, new SbiKpiRuleId(id, version));
				Hibernate.initialize(rule.getSbiKpiPlaceholders());
				Hibernate.initialize(rule.getSbiKpiRuleOutputs());
				return rule;
			}
		});
		return from(rule);
	}

	@Override
	public List<Kpi> listKpi(final STATUS status) {
		List<SbiKpiKpi> lst = list(new ICriterion<SbiKpiKpi>() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria c = session.createCriteria(SbiKpiKpi.class);
				switch (status) {
				case ACTIVE:
					c.add(Restrictions.eq("active", 'T'));
					break;
				case NOT_ACTIVE:
					c.add(Restrictions.ne("active", 'T'));
					break;
				case ALL:
					// No restrictions
					break;
				}
				return c;
			}
		});
		List<Kpi> kpis = new ArrayList<>();
		for (SbiKpiKpi sbi : lst) {
			Kpi kpi = from(sbi, null, false);
			kpis.add(kpi);
		}
		return kpis;
	}

	@Override
	public Kpi insertKpi(final Kpi kpi) {
		return insertKpi(kpi, false);
	}

	public Kpi insertKpi(final Kpi kpi, final boolean newVersion) {
		return executeOnTransaction(new IExecuteOnTransaction<Kpi>() {
			@Override
			public Kpi execute(Session session) {
				SbiKpiKpi sbiKpiKpi = null;
				if (newVersion) {
					SbiKpiKpi oldKpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(kpi.getId(), kpi.getVersion()));
					if (oldKpi.getActive() == 'T') {
						oldKpi.setActive(null);
						sbiKpiKpi = new SbiKpiKpi();
						sbiKpiKpi.setActive('T');
						sbiKpiKpi.getSbiKpiKpiId().setId(kpi.getId());
					}
				}
				SbiKpiKpi sbiKpi = from(session, sbiKpiKpi, kpi);
				updateSbiCommonInfo4Insert(sbiKpi);
				SbiKpiKpiId sbiKpiKpiId = (SbiKpiKpiId) session.save(sbiKpi);
				kpi.setId(sbiKpiKpiId.getId());
				kpi.setVersion(sbiKpiKpiId.getVersion());
				return kpi;
			}

		});
	}

	@Override
	public void updateKpi(final Kpi kpi) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) {
				SbiKpiKpi sbiKpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(kpi.getId(), kpi.getVersion()));
				if (sbiKpi == null) {
					throw new SpagoBIDAOObjectNotExistingException(MessageFormat.format(message.getMessage(NEW_KPI_KPI_NOT_FOUND), kpi.getId()));
				}
				sbiKpi = from(session, sbiKpi, kpi);
				updateSbiCommonInfo4Update(sbiKpi);
				return Boolean.TRUE;
			}
		});
	}

	private SbiKpiKpi from(Session session, SbiKpiKpi sbiKpi, Kpi kpi) {
		if (sbiKpi == null) {
			sbiKpi = new SbiKpiKpi();
			sbiKpi.setActive('T');
		}
		sbiKpi.setName(kpi.getName());
		sbiKpi.setDefinition(kpi.getDefinition());
		sbiKpi.setCardinality(kpi.getCardinality());
		sbiKpi.setPlaceholder(kpi.getPlaceholder());

		if (kpi.getThreshold().getId() == null) {
			SbiKpiThreshold sbiKpiThreshold = from(session, null, kpi.getThreshold());
			updateSbiCommonInfo4Insert(sbiKpiThreshold);
			sbiKpi.setThresholdId((Integer) session.save(sbiKpiThreshold));
		} else {
			SbiKpiThreshold sbiKpiThreshold = (SbiKpiThreshold) session.load(SbiKpiThreshold.class, kpi.getThreshold().getId());
			from(session, sbiKpiThreshold, kpi.getThreshold());
			updateSbiCommonInfo4Update(sbiKpiThreshold);
			sbiKpi.setThresholdId(kpi.getThreshold().getId());
		}
		// handling Category
		SbiDomains category = insertOrUpdateCategory(session, kpi.getCategory(), KPI_KPI_CATEGORY);
		sbiKpi.setCategory(category);
		return sbiKpi;
	}

	private SbiKpiAlias manageAlias(Session session, Integer aliasId, String aliasName) {
		SbiKpiAlias alias = null;
		if (aliasId != null) {
			alias = (SbiKpiAlias) session.load(SbiKpiAlias.class, aliasId);
		} else if (aliasName != null && !aliasName.isEmpty()) {
			alias = (SbiKpiAlias) session.createCriteria(SbiKpiAlias.class).add(Restrictions.eq("name", aliasName).ignoreCase()).uniqueResult();
			if (alias == null) {
				alias = new SbiKpiAlias();
				alias.setName(aliasName);
				updateSbiCommonInfo4Insert(alias);
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
			}
		}
		return sbiKpiPlaceholder;
	}

	private SbiDomains insertOrUpdateCategory(Session session, Domain category, String categoryName) {
		SbiDomains cat = null;
		if (category != null) {
			if (category.getValueId() != null) {
				cat = (SbiDomains) session.load(SbiDomains.class, category.getValueId());
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

	/**
	 * Delete category after checking if no other Kpi object is using it
	 *
	 * @param session
	 * @param category
	 * @param kpi
	 */
	private void removeKpiCategory(Session session, Integer categoryId, Integer kpiId) {
		// check if no other objects are using this category
		if (session.createCriteria(SbiKpiKpi.class).createAlias("category", "category").add(Restrictions.eq("category.valueId", categoryId))
				.add(Restrictions.ne("sbiKpiKpiId.id", kpiId)).setMaxResults(1).uniqueResult() == null) {
			// category is not used so can be deleted
			SbiDomains category = (SbiDomains) session.get(SbiDomains.class, categoryId);
			session.delete(category);
		}
	}

	@Override
	public void removeKpi(final Integer id, final Integer version, boolean toBeVersioned) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws Exception {
				SbiKpiKpi kpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(id, version));
				Integer kpiId = kpi.getSbiKpiKpiId().getId();
				session.delete(kpi);
				if (kpi.getCategory() != null) {
					Integer categoryId = kpi.getCategory().getValueId();
					removeKpiCategory(session, categoryId, kpiId);
				}
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public Kpi loadKpi(final Integer id, final Integer version) {
		Kpi kpi = executeOnTransaction(new IExecuteOnTransaction<Kpi>() {
			@Override
			public Kpi execute(Session session) {
				SbiKpiKpi sbiKpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(id, version));
				SbiKpiThreshold sbiKpiThreshold = (SbiKpiThreshold) session.load(SbiKpiThreshold.class, sbiKpi.getThresholdId());
				return from(sbiKpi, sbiKpiThreshold, true);
			}
		});
		if (isThresholdUsedByOtherKpi(id, kpi.getThreshold().getId())) {
			kpi.getThreshold().setUsedByKpi(true);
		}
		return kpi;
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
	public List<Alias> listAliasNotInMeasure(final Integer ruleId) {
		List<SbiKpiAlias> sbiAlias = list(new ICriterion<SbiKpiAlias>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiAlias.class).createAlias("sbiKpiRuleOutputs", "sbiKpiRuleOutputs")
						.createAlias("sbiKpiRuleOutputs.type", "sbiKpiRuleOutputs_type").createAlias("sbiKpiRuleOutputs.sbiKpiRule", "_sbiKpiRule")
						.add(Restrictions.eq("sbiKpiRuleOutputs_type.valueCd", MEASURE)).add(Restrictions.eq("_sbiKpiRule.active", 'T'))
						.add(Restrictions.ne("_sbiKpiRule.sbiKpiRuleId.id", ruleId));
			}
		});
		List<Alias> ret = new ArrayList<>();
		for (SbiKpiAlias sbiKpiAlias : sbiAlias) {
			ret.add(new Alias(sbiKpiAlias.getId(), sbiKpiAlias.getName()));
		}
		return ret;
	}

	// @Override
	// public List<Alias> listAliasNotInMeasureCaseSensitive(final Integer ruleId) {
	// List<SbiKpiAlias> sbiAlias = list(new ICriterion<SbiKpiAlias>() {
	// @Override
	// public Criteria evaluate(Session session) {
	// DetachedCriteria detachedcriteria = DetachedCriteria.forClass(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias")
	// .createAlias("sbiKpiRule", "sbiKpiRule").setProjection(Property.forName("sbiKpiAlias.name")).createAlias("type", "type")
	// .add(Restrictions.eq("type.valueCd", MEASURE)).add(Restrictions.eq("sbiKpiRule.active", 'T'));
	// if (ruleId != null) {
	// detachedcriteria.add(Restrictions.ne("sbiKpiRule.sbiKpiRuleId.id", ruleId));
	// }
	// Criteria c = session.createCriteria(SbiKpiAlias.class).add(Property.forName("name").notIn(detachedcriteria));
	// return c;
	// }
	// });
	// List<Alias> ret = new ArrayList<>();
	// for (SbiKpiAlias sbiKpiAlias : sbiAlias) {
	// ret.add(new Alias(sbiKpiAlias.getId(), sbiKpiAlias.getName()));
	// }
	// return ret;
	// }

	@Override
	public Alias loadAlias(final String name) {
		List<SbiKpiAlias> aliases = list(new ICriterion<SbiKpiAlias>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiAlias.class).add(Restrictions.eq("name", name).ignoreCase());
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
	public List<RuleOutput> listRuleOutputByType(final String type, final STATUS status) {
		List<SbiKpiRuleOutput> sbiRuleOutputs = list(new ICriterion<SbiKpiRuleOutput>() {
			@Override
			public Criteria evaluate(Session session) {
				// Ordering by rule name and measure name
				Criteria c = session.createCriteria(SbiKpiRuleOutput.class).createAlias("type", "type").createAlias("sbiKpiAlias", "sbiKpiAlias")
						.createAlias("sbiKpiRule", "sbiKpiRule").add(Restrictions.eq("type.valueCd", type)).addOrder(Order.asc("sbiKpiRule.name"))
						.addOrder(Order.asc("sbiKpiAlias.name"));
				switch (status) {
				case ACTIVE:
					c.add(Restrictions.eq("sbiKpiRule.active", 'T'));
					break;
				case NOT_ACTIVE:
					c.add(Restrictions.ne("sbiKpiRule.active", 'T'));
					break;
				case ALL:
					// No restrictions
					break;
				}
				return c;
			}
		});

		List<RuleOutput> ruleOutputs = new ArrayList<>();
		for (SbiKpiRuleOutput sbiKpiRuleOutput : sbiRuleOutputs) {
			ruleOutputs.add(from(sbiKpiRuleOutput));

		}
		return ruleOutputs;
	}

	private SbiKpiThreshold from(Session session, SbiKpiThreshold sbiKpiThreshold, Threshold t) {
		boolean saveAsNew = false;
		if (sbiKpiThreshold == null) {
			sbiKpiThreshold = new SbiKpiThreshold();
			saveAsNew = true;
		}
		sbiKpiThreshold.setName(t.getName());
		sbiKpiThreshold.setDescription(t.getDescription());

		sbiKpiThreshold.setType(new SbiDomains(t.getTypeId()));

		// clearing removed references
		Iterator<SbiKpiThresholdValue> iterator = sbiKpiThreshold.getSbiKpiThresholdValues().iterator();
		while (iterator.hasNext()) {
			SbiKpiThresholdValue sbiKpiThresholdValue = iterator.next();
			// List of threshold values cannot be null
			if (t.getThresholdValues().indexOf(new ThresholdValue(sbiKpiThresholdValue.getId())) == -1) {
				iterator.remove();
			}
		}

		session.save(sbiKpiThreshold);

		for (ThresholdValue tv : t.getThresholdValues()) {
			SbiKpiThresholdValue sbiThresholdValue = null;
			if (tv.getId() == null || saveAsNew) {
				sbiThresholdValue = from(null, tv);
				updateSbiCommonInfo4Insert(sbiThresholdValue);
				sbiThresholdValue.setSbiKpiThreshold(sbiKpiThreshold);
				sbiKpiThreshold.getSbiKpiThresholdValues().add(sbiThresholdValue);
			} else {
				sbiThresholdValue = (SbiKpiThresholdValue) session.load(SbiKpiThresholdValue.class, tv.getId());
				from(sbiThresholdValue, tv);
				updateSbiCommonInfo4Update(sbiThresholdValue);
			}
			sbiThresholdValue.setSbiKpiThreshold(sbiKpiThreshold);
			session.save(sbiThresholdValue);
			sbiKpiThreshold.getSbiKpiThresholdValues().add(sbiThresholdValue);
		}
		return sbiKpiThreshold;
	}

	private SbiKpiThresholdValue from(SbiKpiThresholdValue sbiValue, ThresholdValue tv) {
		if (sbiValue == null) {
			sbiValue = new SbiKpiThresholdValue();
		}
		sbiValue.setColor(tv.getColor());
		char isIncludeMax = tv.isIncludeMax() ? 'T' : 'F';
		sbiValue.setIncludeMax(isIncludeMax);
		char isIncludeMin = tv.isIncludeMin() ? 'T' : 'F';
		sbiValue.setIncludeMin(isIncludeMin);
		sbiValue.setMaxValue(tv.getMaxValue());
		sbiValue.setMinValue(tv.getMinValue());
		sbiValue.setLabel(tv.getLabel());
		sbiValue.setPosition(tv.getPosition());
		if (tv.getSeverityId() != null) {
			sbiValue.setSeverity(new SbiDomains(tv.getSeverityId()));
		}
		return sbiValue;
	}

	@Override
	public List<Threshold> listThreshold() {
		List<SbiKpiThreshold> sbiLst = list(new ICriterion<SbiKpiThreshold>() {
			@Override
			public Criteria evaluate(Session session) {
				return session
						.createCriteria(SbiKpiThreshold.class).setProjection(Projections.projectionList().add(Projections.property("id"), "id")
								.add(Projections.property("name"), "name").add(Projections.property("description"), "description"))
						.setResultTransformer(Transformers.aliasToBean(SbiKpiThreshold.class));
			}
		});
		List<Threshold> thresholds = new ArrayList<>();
		for (SbiKpiThreshold sbiThreshold : sbiLst) {
			thresholds.add(from(sbiThreshold, false));
		}
		return thresholds;
	}

	private RuleOutput from(SbiKpiRuleOutput sbiKpiRuleOutput) {
		RuleOutput ruleOutput = new RuleOutput();
		ruleOutput.setId(sbiKpiRuleOutput.getId());
		ruleOutput.setAlias(sbiKpiRuleOutput.getSbiKpiAlias().getName());
		ruleOutput.setAliasId(sbiKpiRuleOutput.getSbiKpiAlias().getId());
		if (sbiKpiRuleOutput.getCategory() != null) {
			ruleOutput.setCategory(from(sbiKpiRuleOutput.getCategory()));
		}
		if (sbiKpiRuleOutput.getHierarchy() != null) {
			ruleOutput.setHierarchy(from(sbiKpiRuleOutput.getHierarchy()));
		}
		ruleOutput.setType(from(sbiKpiRuleOutput.getType()));
		// Fields from Rule: Rule Id, Rule Name, Author, Date Creation
		ruleOutput.setRuleId(sbiKpiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getId());
		ruleOutput.setRuleVersion(sbiKpiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getVersion());
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
		kpi.setId(sbi.getSbiKpiKpiId().getId());
		kpi.setVersion(sbi.getSbiKpiKpiId().getVersion());
		kpi.setName(sbi.getName());
		if (sbi.getCategory() != null) {
			kpi.setCategory(from(sbi.getCategory()));
		}
		kpi.setAuthor(sbi.getCommonInfo().getUserIn());
		kpi.setDateCreation(sbi.getCommonInfo().getTimeIn());
		if (full) {
			kpi.setCardinality(sbi.getCardinality());
			kpi.setDefinition(sbi.getDefinition());
			kpi.setPlaceholder(sbi.getPlaceholder());
			if (sbiKpiThreshold != null) {
				kpi.setThreshold(from(sbiKpiThreshold, full));
			}
		}
		return kpi;
	}

	/**
	 * Converts a SbiKpiThreshold in a Threshold. If full=false it gets only id, name and description
	 *
	 * @param sbiKpiThreshold
	 * @param full
	 * @return
	 */
	private Threshold from(SbiKpiThreshold sbiKpiThreshold, boolean full) {
		Threshold threshold = new Threshold();
		threshold.setId(sbiKpiThreshold.getId());
		threshold.setName(sbiKpiThreshold.getName());
		threshold.setDescription(sbiKpiThreshold.getDescription());
		if (full) {
			threshold.setType(MessageBuilderFactory.getMessageBuilder().getMessage(sbiKpiThreshold.getType().getValueNm()));
			threshold.setTypeId(sbiKpiThreshold.getType().getValueId());
			for (Object obj : sbiKpiThreshold.getSbiKpiThresholdValues()) {
				SbiKpiThresholdValue sbiValue = (SbiKpiThresholdValue) obj;
				ThresholdValue tv = new ThresholdValue();
				tv.setColor(sbiValue.getColor());
				tv.setId(sbiValue.getId());
				if (sbiKpiThreshold.getType().getValueCd().equals("RANGE") || sbiKpiThreshold.getType().getValueCd().equals("MINIMUM")) {
					tv.setIncludeMin(sbiValue.getIncludeMin() != null && sbiValue.getIncludeMin().charValue() == 'T');
					tv.setMinValue(sbiValue.getMinValue());
				}
				if (sbiKpiThreshold.getType().getValueCd().equals("RANGE") || sbiKpiThreshold.getType().getValueCd().equals("MAXIMUM")) {
					tv.setIncludeMax(sbiValue.getIncludeMax() != null && sbiValue.getIncludeMax().charValue() == 'T');
					tv.setMaxValue(sbiValue.getMaxValue());
				}
				tv.setLabel(sbiValue.getLabel());
				tv.setPosition(sbiValue.getPosition());
				if (sbiValue.getSeverity() != null) {
					tv.setSeverityId(sbiValue.getSeverity().getValueId());
				}
				threshold.getThresholdValues().add(tv);
			}
		}
		return threshold;
	}

	private Rule from(SbiKpiRule sbiRule) {
		Rule rule = new Rule();
		rule.setId(sbiRule.getSbiKpiRuleId().getId());
		rule.setVersion(sbiRule.getSbiKpiRuleId().getVersion());
		rule.setName(sbiRule.getName());
		rule.setDefinition(sbiRule.getDefinition());
		rule.setDataSourceId(sbiRule.getDataSourceId());
		if (sbiRule.getSbiKpiPlaceholders() != null) {
			for (SbiKpiPlaceholder sbiKpiPlaceholder : sbiRule.getSbiKpiPlaceholders()) {
				rule.getPlaceholders().add(new Placeholder(sbiKpiPlaceholder.getId(), sbiKpiPlaceholder.getName()));
			}
		}
		if (sbiRule.getSbiKpiRuleOutputs() != null) {
			for (SbiKpiRuleOutput sbiKpiRuleOutput : sbiRule.getSbiKpiRuleOutputs()) {
				rule.getRuleOutputs().add(from(sbiKpiRuleOutput));
			}
		}
		return rule;
	}

	@Override
	public Map<String, List<String>> aliasValidation(final Rule rule) {
		return executeOnTransaction(new IExecuteOnTransaction<Map<String, List<String>>>() {
			@Override
			public Map<String, List<String>> execute(Session session) throws Exception {
				Map<String, List<String>> invalidAlias = new HashMap<>();
				for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
					validateRuleOutput(session, ruleOutput.getAliasId(), ruleOutput.getAlias(), MEASURE.equals(ruleOutput.getType().getValueCd()), rule.getId(),
							invalidAlias);
				}
				return invalidAlias;
			}
		});
	}

	private void validateRuleOutput(Session session, Integer aliasId, String aliasName, boolean isMeasure, Integer ruleId,
			Map<String, List<String>> invalidAlias) {
		// Looking for a RuleOutput with same alias name or alias id
		Criteria c = session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias").createAlias("sbiKpiRule", "sbiKpiRule")
				.setMaxResults(1);
		if (ruleId != null) {
			c.add(Restrictions.ne("sbiKpiRule.sbiKpiRuleId.id", ruleId));
		}
		if (aliasName != null) {
			c.add(Restrictions.eq("sbiKpiAlias.name", aliasName).ignoreCase());
		} else {
			c.add(Restrictions.eq("sbiKpiAlias.id", aliasId));
		}
		if (isMeasure && c.uniqueResult() != null) {
			// This alias is already used
			addAliasToErrorMap(invalidAlias, NEW_KPI_ALIAS_NOT_AVAILABLE_AS_MEASURE, aliasName);
		} else if (c.createAlias("type", "type").add(Restrictions.eq("type.valueCd", MEASURE)).uniqueResult() != null) {
			// This alias is already used as measure
			addAliasToErrorMap(invalidAlias, NEW_KPI_ALIAS_NOT_AVAILABLE, aliasName);
		}
	}

	private void addAliasToErrorMap(Map<String, List<String>> errorMap, String errorKey, String alias) {
		if (errorMap.get(errorKey) == null) {
			errorMap.put(errorKey, new ArrayList<String>());
		}
		errorMap.get(errorKey).add(alias);
	}

	private void ruleValidationForInsert(Session session, Rule rule) throws SpagoBIException {
		checkRule(rule);
		boolean hasMeasure = false;
		Map<String, List<String>> invalidAlias = new HashMap<>();
		for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
			if (ruleOutput.getAliasId() == null && ruleOutput.getAlias() == null) {
				throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULEOUTPUT_ALIAS_MANDATORY));
			}
			if (ruleOutput.getType().getValueId() == null) {
				throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULEOUTPUT_TYPE_MANDATORY));
			}
			if (MEASURE.equals(ruleOutput.getType().getValueCd())) {
				hasMeasure = true;
			}
			validateRuleOutput(session, ruleOutput.getAliasId(), ruleOutput.getAlias(), hasMeasure, rule.getId(), invalidAlias);
		}
		if (!hasMeasure) {
			throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULE_MEASURES_MANDATORY));
		}
		if (!invalidAlias.isEmpty()) {
			Entry<String, List<String>> firstError = invalidAlias.entrySet().iterator().next();
			throw new SpagoBIDOAException(MessageFormat.format(message.getMessage(firstError.getKey()), firstError.getValue()));
		}
	}

	private void ruleValidationForUpdate(Session session, Rule rule) throws SpagoBIException {
		checkRule(rule);
		Map<String, List<String>> invalidAlias = new HashMap<>();
		boolean hasMeasure = false;
		for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
			if (ruleOutput.getAlias() == null && ruleOutput.getAliasId() == null) {
				throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULEOUTPUT_ALIAS_MANDATORY));
			}
			if (ruleOutput.getType().getValueId() == null) {
				throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULEOUTPUT_TYPE_MANDATORY));
			}
			if (MEASURE.equals(ruleOutput.getType().getValueCd())) {
				hasMeasure = true;
			}
			validateRuleOutput(session, ruleOutput.getAliasId(), ruleOutput.getAlias(), hasMeasure, rule.getId(), invalidAlias);
		}
		if (!hasMeasure) {
			throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULE_MEASURES_MANDATORY));
		}
		if (!invalidAlias.isEmpty()) {
			Entry<String, List<String>> firstError = invalidAlias.entrySet().iterator().next();
			throw new SpagoBIDOAException(MessageFormat.format(message.getMessage(firstError.getKey()), firstError.getValue()));
		}
	}

	private void checkRule(Rule rule) {
		if (rule.getName() == null) {
			throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULE_NAME_MANDATORY));
		}
		if (rule.getDefinition() == null) {
			throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULE_DEFINITION_MANDATORY));
		}
		if (rule.getDataSourceId() == null) {
			throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULE_DATASOURCE_MANDATORY));
		}
		if (rule.getRuleOutputs() == null || rule.getRuleOutputs().isEmpty()) {
			throw new SpagoBIDOAException(message.getMessage(NEW_KPI_RULE_MEASURES_MANDATORY));
		}
	}

	@Override
	public Integer getKpiIdByName(final String name) {
		List<SbiKpiKpi> lst = list(new ICriterion<SbiKpiKpi>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiKpi.class).add(Restrictions.eq("name", name)).add(Restrictions.eq("active", 'T'));
			}
		});
		if (lst != null && !lst.isEmpty()) {
			return lst.get(0).getSbiKpiKpiId().getId();
		}
		return null;
	}

	@Override
	public Threshold loadThreshold(Integer id) {
		return from(load(SbiKpiThreshold.class, id), true);
	}

	@Override
	public Integer getRuleIdByName(final String name) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) throws Exception {
				return (Integer) session.createCriteria(SbiKpiRule.class).add(Restrictions.eq("name", name)).add(Restrictions.eq("active", 'T'))
						.setProjection(Property.forName("sbiKpiRuleId.id")).setMaxResults(1).uniqueResult();
			}
		});
	}

	@Override
	public Boolean existsMeasureNames(final String... names) {
		List<SbiKpiRuleOutput> ruleOutputs = list(new ICriterion<SbiKpiRuleOutput>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiRuleOutput.class).createAlias("type", "type").createAlias("sbiKpiAlias", "sbiKpiAlias")
						.add(Restrictions.eq("type.valueCd", MEASURE)).add(new InExpressionIgnoringCase("sbiKpiAlias.name", names));
			}
		});
		return ruleOutputs != null && ruleOutputs.size() == names.length;
	}

	@Override
	public List<Integer> listKpiByThreshold(final Integer thresholdId) {
		return executeOnTransaction(new IExecuteOnTransaction<List<Integer>>() {
			@Override
			public List<Integer> execute(Session session) throws Exception {
				return session.createCriteria(SbiKpiKpi.class).add(Restrictions.eq("thresholdId", thresholdId))
						.setProjection(Projections.property("sbiKpiKpiId.id")).list();
			}
		});
	}

	@Override
	public boolean isThresholdUsedByOtherKpi(Integer kpiId, Integer thresholdId) {
		// Looking for all kpi using this threshold
		List<Integer> kpiList = listKpiByThreshold(thresholdId);
		if (kpiList == null || kpiList.isEmpty() || kpiList.size() == 1 && kpiList.get(0).equals(kpiId)) {
			// This threshold isn't used by any kpi or at most only by the kpi currently edited by user
			return false;
		} else {
			// This threshold is used by other kpi
			return true;
		}
	}

	@Override
	public List<Target> listTarget() {
		List<SbiKpiTarget> lst = list(new ICriterion<SbiKpiTarget>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiTarget.class).addOrder(Order.desc("startValidity"));
			}
		});
		List<Target> targetList = new ArrayList<>();
		for (SbiKpiTarget sbiKpiTarget : lst) {
			targetList.add(from(sbiKpiTarget, false));
		}
		return targetList;
	}

	private Target from(SbiKpiTarget sbiKpiTarget, boolean full) {
		Target target = new Target();
		target.setId(sbiKpiTarget.getTargetId());
		target.setName(sbiKpiTarget.getName());
		target.setAuthor(sbiKpiTarget.getCommonInfo().getUserIn());
		target.setStartValidity(sbiKpiTarget.getStartValidity());
		target.setEndValidity(sbiKpiTarget.getEndValidity());
		if (sbiKpiTarget.getCategory() != null) {
			target.setCategory(from(sbiKpiTarget.getCategory()));
		}
		if (full) {
			for (SbiKpiTargetValue sbiValue : sbiKpiTarget.getSbiKpiTargetValues()) {
				target.getValues().add(from(sbiValue));
			}
		}
		return target;
	}

	private TargetValue from(SbiKpiTargetValue sbiValue) {
		TargetValue tv = new TargetValue();
		SbiKpiKpi sbiKpi = sbiValue.getSbiKpiKpi();
		tv.setKpi(from(sbiKpi, null, false));
		tv.setTargetId(sbiValue.getSbiKpiTarget().getTargetId());
		tv.setKpiId(sbiKpi.getSbiKpiKpiId().getId());
		tv.setKpiVersion(sbiKpi.getSbiKpiKpiId().getVersion());
		tv.setValue(sbiValue.getValue());
		return tv;
	}

	@Override
	public Target loadTarget(final Integer id) {
		return executeOnTransaction(new IExecuteOnTransaction<Target>() {
			@Override
			public Target execute(Session session) throws Exception {
				return from((SbiKpiTarget) session.load(SbiKpiTarget.class, id), true);
			}
		});
	}

	@Override
	public Integer insertTarget(final Target target) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) throws Exception {
				SbiKpiTarget sbiTarget = from(target, null, session);
				updateSbiCommonInfo4Insert(sbiTarget);
				Integer id = (Integer) session.save(sbiTarget);
				for (TargetValue targetValue : target.getValues()) {
					SbiKpiTargetValue sbiValue = from(targetValue, sbiTarget, session);
					sbiTarget.getSbiKpiTargetValues().add(sbiValue);
					session.save(sbiValue);
				}
				return id;
			}
		});
	}

	@Override
	public void updateTarget(final Target target) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws Exception {
				SbiKpiTarget sbiTarget = (SbiKpiTarget) session.load(SbiKpiTarget.class, target.getId());
				sbiTarget = from(target, sbiTarget, session);
				updateSbiCommonInfo4Update(sbiTarget);
				session.save(sbiTarget);
				// Removing old values
				Iterator<SbiKpiTargetValue> targetIterator = sbiTarget.getSbiKpiTargetValues().iterator();
				while (targetIterator.hasNext()) {
					SbiKpiTargetValue sbiValue = targetIterator.next();
					SbiKpiKpiId sbiKpiKpiId = sbiValue.getSbiKpiKpi().getSbiKpiKpiId();
					boolean found = false;
					for (TargetValue targetValue : target.getValues()) {
						if (sbiKpiKpiId.getId().equals(targetValue.getKpiId()) && sbiKpiKpiId.getVersion().equals(targetValue.getKpiVersion())) {
							found = true;
						}
					}
					if (!found) {
						targetIterator.remove();
					}
				}
				// Adding new values
				for (TargetValue targetValue : target.getValues()) {
					SbiKpiTargetValue sbiValue = from(targetValue, sbiTarget, session);
					sbiTarget.getSbiKpiTargetValues().add(sbiValue);
					session.save(sbiValue);
				}
				return Boolean.TRUE;
			}

		});
	}

	private SbiKpiTarget from(Target target, SbiKpiTarget sbiTarget, Session session) {
		if (sbiTarget == null) {
			sbiTarget = new SbiKpiTarget();
		}
		sbiTarget.setName(target.getName());
		sbiTarget.setStartValidity(target.getStartValidity());
		sbiTarget.setEndValidity(target.getEndValidity());
		// handling Category
		SbiDomains category = insertOrUpdateCategory(session, target.getCategory(), KPI_TARGET_CATEGORY);
		sbiTarget.setCategory(category);
		return sbiTarget;
	}

	private SbiKpiTargetValue from(TargetValue targetValue, SbiKpiTarget sbiTarget, Session session) {
		Integer targetId = sbiTarget.getTargetId();
		Integer kpiId = targetValue.getKpiId();
		Integer kpiVersion = targetValue.getKpiVersion();
		SbiKpiTargetValue sbiValue = null;
		if (kpiId != null && kpiVersion != null && targetId != null) {
			sbiValue = (SbiKpiTargetValue) session.get(SbiKpiTargetValue.class, new SbiKpiTargetValueId(targetId, kpiId, kpiVersion));
		}
		if (sbiValue == null) {
			sbiValue = new SbiKpiTargetValue();
			sbiValue.getSbiKpiTargetValueId().setKpiId(kpiId);
			sbiValue.getSbiKpiTargetValueId().setKpiVersion(kpiVersion);
			sbiValue.getSbiKpiTargetValueId().setTargetId(targetId);
			updateSbiCommonInfo4Insert(sbiValue);
		} else {
			updateSbiCommonInfo4Update(sbiValue);
		}
		sbiValue.setValue(targetValue.getValue());
		return sbiValue;
	}

	@Override
	public void removeTarget(Integer id) {
		delete(SbiKpiTarget.class, id);
	}

	@Override
	public List<TargetValue> listKpiWithTarget(final Integer targetId) {
		List<SbiKpiTargetValue> lst = list(new ICriterion<SbiKpiTargetValue>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiTargetValue.class).createAlias("sbiKpiKpi", "sbiKpiKpi", JoinFragment.RIGHT_OUTER_JOIN)
						.createAlias("sbiKpiTarget", "sbiKpiTarget").add(Restrictions.eq("sbiKpiKpi.active", 'T'))
						.add(Restrictions.eq("sbiKpiTarget.targetId", targetId));
				/*
				 * return session .createCriteria(SbiKpiKpi.class) .createAlias("sbiKpiTargetValues", "sbiKpiTargetValues", JoinFragment.LEFT_OUTER_JOIN)
				 * .createAlias("sbiKpiTargetValues.sbiKpiKpi", "sbiKpiKpi") .add(Restrictions.eq("active", 'T')) .setProjection(
				 * Projections.projectionList().add(Property.forName("sbiKpiKpiId.id").as("kpiId"))
				 * .add(Property.forName("sbiKpiKpiId.version").as("kpiVersion")) .add(Property.forName("sbiKpiTargetValues.value").as("value")))
				 * .setResultTransformer(Transformers.aliasToBean(SbiKpiTargetValue.class));
				 */
			}
		});
		List<TargetValue> ret = new ArrayList<>();
		for (SbiKpiTargetValue sbiTarget : lst) {
			ret.add(from(sbiTarget));
		}
		return ret;
	}

	@Override
	public List<KpiScheduler> listKpiScheduler() {
		List<SbiKpiExecution> lst = list(SbiKpiExecution.class);
		List ret = new ArrayList<>();
		for (SbiKpiExecution sbi : lst) {
			KpiScheduler ks = new KpiScheduler();

		}
		return ret;
	}

	@Override
	public KpiScheduler loadKpiScheduler(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}
}
