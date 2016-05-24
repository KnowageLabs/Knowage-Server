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

import it.eng.qbe.InExpressionIgnoringCase;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDAOObjectNotExistingException;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Cardinality;
import it.eng.spagobi.kpi.bo.CardinalityBuilder;
import it.eng.spagobi.kpi.bo.IScorecardCriterion;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.KpiExecution;
import it.eng.spagobi.kpi.bo.KpiScheduler;
import it.eng.spagobi.kpi.bo.KpiValue;
import it.eng.spagobi.kpi.bo.KpiValueExecLog;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.SchedulerFilter;
import it.eng.spagobi.kpi.bo.Scorecard;
import it.eng.spagobi.kpi.bo.ScorecardPerspective;
import it.eng.spagobi.kpi.bo.ScorecardStatus;
import it.eng.spagobi.kpi.bo.ScorecardSubview;
import it.eng.spagobi.kpi.bo.ScorecardTarget;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.kpi.bo.TargetValue;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.bo.ThresholdValue;
import it.eng.spagobi.kpi.job.ProcessKpiJob;
import it.eng.spagobi.kpi.metadata.SbiKpiAlias;
import it.eng.spagobi.kpi.metadata.SbiKpiExecution;
import it.eng.spagobi.kpi.metadata.SbiKpiExecutionFilter;
import it.eng.spagobi.kpi.metadata.SbiKpiExecutionFilterId;
import it.eng.spagobi.kpi.metadata.SbiKpiKpi;
import it.eng.spagobi.kpi.metadata.SbiKpiKpiId;
import it.eng.spagobi.kpi.metadata.SbiKpiPlaceholder;
import it.eng.spagobi.kpi.metadata.SbiKpiRule;
import it.eng.spagobi.kpi.metadata.SbiKpiRuleId;
import it.eng.spagobi.kpi.metadata.SbiKpiRuleOutput;
import it.eng.spagobi.kpi.metadata.SbiKpiScorecard;
import it.eng.spagobi.kpi.metadata.SbiKpiTarget;
import it.eng.spagobi.kpi.metadata.SbiKpiTargetValue;
import it.eng.spagobi.kpi.metadata.SbiKpiTargetValueId;
import it.eng.spagobi.kpi.metadata.SbiKpiThreshold;
import it.eng.spagobi.kpi.metadata.SbiKpiThresholdValue;
import it.eng.spagobi.kpi.metadata.SbiKpiValue;
import it.eng.spagobi.kpi.metadata.SbiKpiValueExecLog;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.alert.listener.AbstractSuspendableJob.JOB_STATUS;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinFragment;
import org.hibernate.transform.Transformers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KpiDAOImpl extends AbstractHibernateDAO implements IKpiDAO {

	private static Logger logger = Logger.getLogger(KpiDAOImpl.class);

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

	private static final String KPI_SCHEDULER_GROUP = "KPI_SCHEDULER_GROUP";

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

	@SuppressWarnings("unchecked")
	@Override
	public String valueTargetbyKpi(final Kpi kpi) {
		Session tmpSession = getSession();
		List<SbiKpiTargetValue> targets = new ArrayList<>();

		String hql = " from SbiKpiTargetValue WHERE sbiKpiKpi.sbiKpiKpiId.id =? and sbiKpiKpi.sbiKpiKpiId.version=? and " + "sbiKpiTarget.endValidity > ? ";
		Query q = tmpSession.createQuery(hql);
		q.setInteger(0, kpi.getId());
		q.setInteger(1, kpi.getVersion());
		q.setDate(2, new Date());
		targets = q.list();
		if (targets.size() == 0) {
			return null;
		}
		return targets.get(0).getValue().toString();
	};

	@Override
	public List<Cardinality> buildCardinality(final List<String> measures) {
		return executeOnTransaction(new IExecuteOnTransaction<List<Cardinality>>() {
			@Override
			public List<Cardinality> execute(Session session) throws Exception {

				List<SbiKpiRuleOutput> allRuleOutputs = session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiRule", "sbiKpiRule")
						.createAlias("sbiKpiRule.sbiKpiRuleOutputs", "sbiKpiRule_sbiKpiRuleOutputs")
						.createAlias("sbiKpiRule_sbiKpiRuleOutputs.sbiKpiAlias", "parent_sbiKpiAlias").add(Restrictions.eq("sbiKpiRule.active", 'T'))
						.add(new InExpressionIgnoringCase("parent_sbiKpiAlias.name", measures)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
				Map<SbiKpiRuleId, List<Cardinality>> cardinalityMap = new HashMap<>();

				CardinalityBuilder cardinalityBuilder = new CardinalityBuilder();

				for (SbiKpiRuleOutput sbiRuleOutput : allRuleOutputs) {
					if (MEASURE.equals(sbiRuleOutput.getType().getValueCd())) {
						cardinalityBuilder.addMeasure(sbiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getId(), sbiRuleOutput.getSbiKpiRule().getSbiKpiRuleId()
								.getVersion(), sbiRuleOutput.getSbiKpiRule().getName(), sbiRuleOutput.getSbiKpiAlias().getName());
					} else {
						cardinalityBuilder.addAttribute(sbiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getId(), sbiRuleOutput.getSbiKpiRule()
								.getSbiKpiRuleId().getVersion(), sbiRuleOutput.getSbiKpiAlias().getName());
					}
				}

				List<Cardinality> cardinalityOrdered = new ArrayList<>();
				Collection<Cardinality> cardinality = cardinalityBuilder.getCardinality();
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
				Set<SbiKpiKpi> relatedKpis = new HashSet<>();
				if (newVersion) {
					SbiKpiRule oldRule = (SbiKpiRule) session.load(SbiKpiRule.class, new SbiKpiRuleId(rule.getId(), rule.getVersion()));
					sbiRule.getSbiKpiRuleId().setId(rule.getId());
					if (oldRule.getActive() == 'T') {
						oldRule.setActive(null);
					}
					// gathering related Kpis
					for (SbiKpiRuleOutput sbiKpiRuleOutput : oldRule.getSbiKpiRuleOutputs()) {
						for (SbiKpiKpi sbiKpiKpi : sbiKpiRuleOutput.getSbiKpiKpis()) {
							relatedKpis.add(sbiKpiKpi);
						}
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

				// handling related Kpis
				if (newVersion) {
					for (SbiKpiKpi sbiKpiKpi : relatedKpis) {
						if (isKpiVersioned(sbiKpiKpi.getSbiKpiKpiId().getId())) {
							insertNewVersionKpi(from(sbiKpiKpi, (SbiKpiThreshold) session.load(SbiKpiThreshold.class, sbiKpiKpi.getThresholdId()), true));
						} else {
							try {
								refreshKpiRuleOutputRel(session, sbiKpiKpi);
							} catch (JSONException e) {
								throw new SpagoBIDOAException(e);
							}
						}
					}
				}
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
	public void removeRule(final Integer id, final Integer version, final boolean toBeVersioned) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws Exception {
				SbiKpiRule rule = (SbiKpiRule) session.load(SbiKpiRule.class, new SbiKpiRuleId(id, version));

				if (toBeVersioned && Character.valueOf('T').equals(rule.getActive())) {
					rule.setActive(null);
				} else {
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
				}
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public Map<Kpi, List<String>> listKpisLinkedToRule(final Integer ruleId, final Integer ruleVersion, final boolean onlyActiveKpis) {
		return executeOnTransaction(new IExecuteOnTransaction<Map<Kpi, List<String>>>() {
			@Override
			public Map<Kpi, List<String>> execute(Session session) throws Exception {
				Map<Kpi, List<String>> kpis = new HashMap<>();
				SbiKpiRule rule = (SbiKpiRule) session.load(SbiKpiRule.class, new SbiKpiRuleId(ruleId, ruleVersion));
				for (SbiKpiRuleOutput sbiKpiRuleOutput : rule.getSbiKpiRuleOutputs()) {
					for (SbiKpiKpi sbiKpiKpi : sbiKpiRuleOutput.getSbiKpiKpis()) {
						Kpi kpi = new Kpi(sbiKpiKpi.getSbiKpiKpiId().getId(), sbiKpiKpi.getSbiKpiKpiId().getVersion());
						kpi.setName(sbiKpiKpi.getName());
						kpi.setActive(Character.valueOf('T').equals(sbiKpiKpi.getActive()));
						if (kpi.isActive() || !onlyActiveKpis) {
							if (kpis.get(kpi) == null) {
								kpis.put(kpi, new ArrayList<String>());
							}
							kpis.get(kpi).add(sbiKpiRuleOutput.getSbiKpiAlias().getName());
						}
					}
				}
				return kpis;
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
	public List<Kpi> listKpi(final STATUS status, IEngUserProfile profile) {
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
			// if category is present check if role has authorization
			if (sbi.getCategory() != null && !UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[0])
					&& !UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ROLE_TYPE_DEV, new String[0])) {
				Collection<String> rolesProfile;
				try {
					rolesProfile = profile.getRoles();
					Iterator it = rolesProfile.iterator();
					while (it.hasNext()) {
						String roleName = (String) it.next();
						Role role = DAOFactory.getRoleDAO().loadByName(roleName);
						List<RoleMetaModelCategory> lstCategory = DAOFactory.getRoleDAO().getMetaModelCategoriesForRole(role.getId());
						if (!userIsAbilited(lstCategory, sbi.getCategory())) {
							continue;
						} else {
							Kpi kpi = from(sbi, null, false);
							kpis.add(kpi);
						}
					}

				} catch (EMFInternalError | EMFUserError e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				Kpi kpi = from(sbi, null, false);
				kpis.add(kpi);
			}

		}
		return kpis;
	}

	private boolean userIsAbilited(List<RoleMetaModelCategory> lstCategory, SbiDomains domain) {

		for (RoleMetaModelCategory cat : lstCategory) {
			if (cat.getCategoryId().equals(domain.getValueId())) {
				return true;
			}
		}
		return false;
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
					sbiKpiKpi = new SbiKpiKpi();
					if (Character.valueOf('T').equals(oldKpi.getActive())) {
						oldKpi.setActive(null);
						sbiKpiKpi.setActive('T');
					}
					sbiKpiKpi.getSbiKpiKpiId().setId(kpi.getId());
				}
				try {
					SbiKpiKpi sbiKpi = from(session, sbiKpiKpi, kpi);
					updateSbiCommonInfo4Insert(sbiKpi);
					SbiKpiKpiId sbiKpiKpiId = (SbiKpiKpiId) session.save(sbiKpi);
					kpi.setId(sbiKpiKpiId.getId());
					kpi.setVersion(sbiKpiKpiId.getVersion());
					return kpi;
				} catch (JSONException e) {
					throw new SpagoBIDOAException(e);
				}
			}

		});
	}

	@Override
	public void updateKpi(final Kpi kpi) {
		if (kpi.isEnableVersioning()) {
			insertNewVersionKpi(kpi);
		} else {
			executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
				@Override
				public Boolean execute(Session session) {
					SbiKpiKpi sbiKpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(kpi.getId(), kpi.getVersion()));
					if (sbiKpi == null) {
						throw new SpagoBIDAOObjectNotExistingException(MessageFormat.format(message.getMessage(NEW_KPI_KPI_NOT_FOUND), kpi.getId()));
					}
					try {
						sbiKpi = from(session, sbiKpi, kpi);
						updateSbiCommonInfo4Update(sbiKpi);
						return Boolean.TRUE;
					} catch (JSONException e) {
						throw new SpagoBIDOAException(e);
					}
				}
			});
		}
	}

	private SbiKpiKpi from(Session session, SbiKpiKpi sbiKpi, Kpi kpi) throws JSONException {
		if (sbiKpi == null) {
			sbiKpi = new SbiKpiKpi();
			sbiKpi.setActive('T');
		}
		sbiKpi.setName(kpi.getName());
		sbiKpi.setDefinition(kpi.getDefinition());
		sbiKpi.setCardinality(kpi.getCardinality());
		sbiKpi.setPlaceholder(kpi.getPlaceholder());

		if (kpi.getThreshold() == null || kpi.getThreshold().getId() == null) {
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
		// Updating relations with RuleOutput and KpiScheduler
		refreshKpiRuleOutputRel(session, sbiKpi);
		return sbiKpi;
	}

	/**
	 * It gets measure names from "definition", loads last version rules by measure names, updates relations between kpi and rule output, updates "cardinality"
	 * 
	 * @param session
	 * @param persistentSbiKpiKpi
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	private void refreshKpiRuleOutputRel(Session session, SbiKpiKpi persistentSbiKpiKpi) throws JSONException {
		// Updating relations starting from "definition"
		List<String> measureNames = JSONUtils.asList(new JSONObject(persistentSbiKpiKpi.getDefinition()).getJSONArray("measures"));
		persistentSbiKpiKpi.getSbiKpiRuleOutputs().clear();
		List<SbiKpiRuleOutput> measures = session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias")
				.createAlias("sbiKpiRule", "sbiKpiRule").add(Restrictions.in("sbiKpiAlias.name", measureNames)).add(Restrictions.eq("sbiKpiRule.active", 'T'))
				.list();
		persistentSbiKpiKpi.getSbiKpiRuleOutputs().addAll(measures);
		// Updating "cardinality" with correct rule version
		JSONArray measureList = new JSONObject(persistentSbiKpiKpi.getCardinality()).getJSONArray("measureList");
		Map<Integer, Integer> ruleVersion = new HashMap<>();
		for (SbiKpiRuleOutput sbiKpiRuleOutput : measures) {
			ruleVersion.put(sbiKpiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getId(), sbiKpiRuleOutput.getSbiKpiRule().getSbiKpiRuleId().getVersion());
		}
		for (int i = 0; i < measureList.length(); i++) {
			JSONObject rule = measureList.getJSONObject(i);
			Integer version = ruleVersion.get(rule.getInt("ruleId"));
			if (version != null) {
				measureList.getJSONObject(i).put("ruleVersion", version);
			}
		}
		persistentSbiKpiKpi.setCardinality(new JSONObject(persistentSbiKpiKpi.getCardinality()).put("measureList", measureList).toString());
		// Updating relation with SbiKpiExecution
		// - Gathering all SbiKpiExecution using this Kpi (another version of this Kpi)
		Criteria c = session.createCriteria(SbiKpiExecution.class).createAlias("sbiKpiKpis", "_kpi")
				.add(Restrictions.eq("_kpi.sbiKpiKpiId.id", persistentSbiKpiKpi.getSbiKpiKpiId().getId()));
		if (persistentSbiKpiKpi.getSbiKpiKpiId().getVersion() != null) {
			c.add(Restrictions.ne("_kpi.sbiKpiKpiId.version", persistentSbiKpiKpi.getSbiKpiKpiId().getVersion()));
		}
		List<SbiKpiExecution> executions = c.list();
		for (SbiKpiExecution sbiKpiExecution : executions) {
			Iterator<SbiKpiKpi> oldKpis = sbiKpiExecution.getSbiKpiKpis().iterator();
			while (oldKpis.hasNext()) {
				SbiKpiKpi oldKpi = oldKpis.next();
				if (oldKpi.getSbiKpiKpiId().getId().equals(persistentSbiKpiKpi.getSbiKpiKpiId().getId())) {
					// - Removing old version Kpi
					oldKpis.remove();
					break;
				}
			}
			// - Adding new version Kpi to SbiKpiExecution
			sbiKpiExecution.getSbiKpiKpis().add(persistentSbiKpiKpi);
		}
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
	public void removeKpi(final Integer id, final Integer version) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws Exception {
				SbiKpiKpi kpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(id, version));
				if (isKpiVersioned(id)) {
					kpi.setActive(null);
				} else {
					Integer kpiId = kpi.getSbiKpiKpiId().getId();
					session.delete(kpi);
					// Category will never be removed anymore 'cause it can be linked to an user role (ref.KNOWAGE-940)
					// if (kpi.getCategory() != null) {
					// Integer categoryId = kpi.getCategory().getValueId();
					// removeKpiCategory(session, categoryId, kpiId);
					// }
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
		if (isKpiVersioned(id)) {
			kpi.setEnableVersioning(true);
		}
		return kpi;
	}

	@Override
	public Kpi loadLastActiveKpi(final Integer id) {
		
		
		return executeOnTransaction(new IExecuteOnTransaction<Kpi>() {
			@Override
			public Kpi execute(Session session) throws Exception {
				String hql = " from SbiKpiKpi WHERE sbiKpiKpiId.id =? AND active='T'";
				Query q = session.createQuery(hql);
				q.setInteger(0, id);
				SbiKpiKpi kpi = (SbiKpiKpi) q.uniqueResult();

				Kpi kpife = from(kpi, (SbiKpiThreshold) session.load(SbiKpiThreshold.class, kpi.getThresholdId()), true);

				return kpife;
			}
		}
		);

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
		return listAliasNotInMeasure(null, null);
	}

	@Override
	public List<Alias> listAliasNotInMeasure(final Integer ruleId, final Integer ruleVersion) {
		return executeOnTransaction(new IExecuteOnTransaction<List<Alias>>() {
			@Override
			public List<Alias> execute(Session session) throws Exception {
				DetachedCriteria dc = DetachedCriteria.forClass(SbiKpiRuleOutput.class).createAlias("type", "_type").createAlias("sbiKpiRule", "_sbiKpiRule")
						.createAlias("sbiKpiAlias", "_sbiKpiAlias").add(Restrictions.eq("_type.valueCd", MEASURE))
						.add(Restrictions.eq("_sbiKpiRule.active", 'T')).setProjection(Property.forName("_sbiKpiAlias.id"));

				// Retriving all aliases not used as measure
				List<SbiKpiAlias> alias = session.createCriteria(SbiKpiAlias.class).add(Subqueries.propertyNotIn("id", dc)).list();
				Set<SbiKpiAlias> sbiAlias = new HashSet<>(alias);
				if (ruleId != null && ruleVersion != null) {
					List<SbiKpiAlias> aliasesUsedByCurrentRule = session.createCriteria(SbiKpiAlias.class)
							.createAlias("sbiKpiRuleOutputs", "_sbiKpiRuleOutputs").createAlias("_sbiKpiRuleOutputs.sbiKpiRule", "_sbiKpiRule")
							.add(Restrictions.eq("_sbiKpiRule.sbiKpiRuleId.id", ruleId)).add(Restrictions.eq("_sbiKpiRule.sbiKpiRuleId.version", ruleVersion))
							.list();
					sbiAlias.addAll(aliasesUsedByCurrentRule);
				}

				List<Alias> ret = new ArrayList<>();
				for (SbiKpiAlias sbiKpiAlias : sbiAlias) {
					ret.add(new Alias(sbiKpiAlias.getId(), sbiKpiAlias.getName()));
				}
				return ret;
			}
		});
	}

	@Override
	public List<Alias> listAliasInMeasure(final Integer ruleId, final Integer ruleVersion) {
		return executeOnTransaction(new IExecuteOnTransaction<List<Alias>>() {
			@Override
			public List<Alias> execute(Session session) throws Exception {
				DetachedCriteria dc = DetachedCriteria.forClass(SbiKpiRuleOutput.class).createAlias("type", "_type").createAlias("sbiKpiRule", "_sbiKpiRule")
						.createAlias("sbiKpiAlias", "_sbiKpiAlias").add(Restrictions.eq("_type.valueCd", MEASURE))
						.add(Restrictions.eq("_sbiKpiRule.active", 'T')).setProjection(Property.forName("_sbiKpiAlias.id"));

				// Retriving all aliases not used as measure
				List<SbiKpiAlias> alias = session.createCriteria(SbiKpiAlias.class).add(Subqueries.propertyIn("id", dc)).list();
				Set<SbiKpiAlias> sbiAlias = new HashSet<>(alias);
				if (ruleId != null && ruleVersion != null) {
					List<SbiKpiAlias> aliasesUsedByCurrentRule = session.createCriteria(SbiKpiAlias.class)
							.createAlias("sbiKpiRuleOutputs", "_sbiKpiRuleOutputs").createAlias("_sbiKpiRuleOutputs.sbiKpiRule", "_sbiKpiRule")
							.add(Restrictions.eq("_sbiKpiRule.sbiKpiRuleId.id", ruleId)).add(Restrictions.eq("_sbiKpiRule.sbiKpiRuleId.version", ruleVersion))
							.list();
					sbiAlias.removeAll(aliasesUsedByCurrentRule);
				}

				List<Alias> ret = new ArrayList<>();
				for (SbiKpiAlias sbiKpiAlias : sbiAlias) {
					ret.add(new Alias(sbiKpiAlias.getId(), sbiKpiAlias.getName()));
				}
				return ret;
			}
		});
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
						.createCriteria(SbiKpiThreshold.class)
						.setProjection(
								Projections.projectionList().add(Projections.property("id"), "id").add(Projections.property("name"), "name")
										.add(Projections.property("description"), "description"))
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
		return from(new Kpi(), sbi, sbiKpiThreshold, full);
	}

	private <T extends Kpi> T from(T kpi, SbiKpiKpi sbi, SbiKpiThreshold sbiKpiThreshold, boolean full) {
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
					tv.setSeverityCd(sbiValue.getSeverity().getValueCd());
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
					validateRuleOutput(session, ruleOutput.getAliasId(), ruleOutput.getAlias(), MEASURE.equals(ruleOutput.getType().getValueCd()),
							rule.getId(), rule.getVersion(), invalidAlias);
				}
				return invalidAlias;
			}
		});
	}

	private void validateRuleOutput(Session session, Integer aliasId, String aliasName, boolean isMeasure, Integer ruleId, Integer ruleVersion,
			Map<String, List<String>> invalidAlias) {
		// Looking for a RuleOutput with same alias name or alias id
		Criteria c = session.createCriteria(SbiKpiRuleOutput.class).createAlias("sbiKpiAlias", "sbiKpiAlias").createAlias("sbiKpiRule", "sbiKpiRule")
				.setMaxResults(1);
		c.add(Restrictions.eq("sbiKpiRule.active", 'T'));
		if (ruleId != null && ruleVersion != null) {
			c.add(Restrictions.ne("sbiKpiRule.sbiKpiRuleId.id", ruleId)).add(Restrictions.ne("sbiKpiRule.sbiKpiRuleId.version", ruleVersion));
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
			boolean isMeasure = MEASURE.equals(ruleOutput.getType().getValueCd());
			if (isMeasure) {
				hasMeasure = true;
			}
			validateRuleOutput(session, ruleOutput.getAliasId(), ruleOutput.getAlias(), isMeasure, rule.getId(), rule.getVersion(), invalidAlias);
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
			boolean isMeasure = MEASURE.equals(ruleOutput.getType().getValueCd());
			if (isMeasure) {
				hasMeasure = true;
			}
			validateRuleOutput(session, ruleOutput.getAliasId(), ruleOutput.getAlias(), isMeasure, rule.getId(), rule.getVersion(), invalidAlias);
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

	private boolean isKpiVersioned(final Integer kpiId) {
		return executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws Exception {
				Number count = (Number) session.createCriteria(SbiKpiKpi.class).add(Restrictions.eq("sbiKpiKpiId.id", kpiId))
						.setProjection(Projections.rowCount()).uniqueResult();
				return count.longValue() > 1;
			}
		});
	}

	@Override
	public List<Target> listTarget() {
		return executeOnTransaction(new IExecuteOnTransaction<List<Target>>() {
			@Override
			public List<Target> execute(Session session) throws Exception {
				List<SbiKpiTarget> lst = session.createCriteria(SbiKpiTarget.class).addOrder(Order.desc("startValidity")).list();
				List<Target> targetList = new ArrayList<>();
				for (SbiKpiTarget sbiKpiTarget : lst) {
					targetList.add(from(sbiKpiTarget, false));
				}
				return targetList;
			}
		});
	}

	@Override
	public List<Target> listOverlappingTargets(final Integer targetId, final Date startDate, final Date endDate, final Set<Kpi> kpis) {
		return executeOnTransaction(new IExecuteOnTransaction<List<Target>>() {
			@Override
			public List<Target> execute(Session session) throws Exception {
				Junction kpiJunction = Restrictions.disjunction();
				for (Kpi kpi : kpis) {
					kpiJunction.add(Restrictions.conjunction().add(Restrictions.eq("_kpi.sbiKpiKpiId.id", kpi.getId()))
							.add(Restrictions.eq("_kpi.sbiKpiKpiId.version", kpi.getVersion())));
				}
				// DetachedCriteria kpis = DetachedCriteria.forClass(SbiKpiKpi.class)
				Criteria c = session.createCriteria(SbiKpiTarget.class);
				Junction d = Restrictions.disjunction();
				d.add(Restrictions.conjunction()
						.add(Restrictions.disjunction().add(Restrictions.le("startValidity", startDate)).add(Restrictions.isNull("startValidity")))
						.add(Restrictions.disjunction().add(Restrictions.ge("endValidity", startDate)).add(Restrictions.isNull("endValidity"))));
				d.add(Restrictions.conjunction()
						.add(Restrictions.disjunction().add(Restrictions.le("startValidity", endDate)).add(Restrictions.isNull("startValidity")))
						.add(Restrictions.disjunction().add(Restrictions.ge("endValidity", endDate)).add(Restrictions.isNull("endValidity"))));
				c.createAlias("sbiKpiTargetValues", "_targetValues").add(d).createAlias("_targetValues.sbiKpiKpi", "_kpi").add(kpiJunction)
						.add(Restrictions.ne("targetId", targetId));
				List<SbiKpiTarget> lst = c.list();
				List<Target> targetList = new ArrayList<>();
				for (SbiKpiTarget sbiKpiTarget : lst) {
					targetList.add(from(sbiKpiTarget, false));
				}
				return targetList;
			}
		});
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
		try {
			final List<String> suspendedTriggers = DAOFactory.getSchedulerDAO().listTriggerPausedByGroup(KPI_SCHEDULER_GROUP, KPI_SCHEDULER_GROUP);
			return executeOnTransaction(new IExecuteOnTransaction<List<KpiScheduler>>() {
				@Override
				public List<KpiScheduler> execute(Session session) throws Exception {
					List<KpiScheduler> ret = new ArrayList<>();
					List<SbiKpiExecution> lst = session.createCriteria(SbiKpiExecution.class).list();
					if (lst != null) {
						for (SbiKpiExecution sbiKpiExecution : lst) {
							KpiScheduler kpiExecution = from(sbiKpiExecution, false);

							if (DAOFactory.getSchedulerDAO().loadTrigger(KPI_SCHEDULER_GROUP, kpiExecution.getId().toString()) == null) {
								// trigger expired
								kpiExecution.setJobStatus(JOB_STATUS.EXPIRED);

							} else {
								kpiExecution.setJobStatus(suspendedTriggers.contains("" + sbiKpiExecution.getId()) ? JOB_STATUS.SUSPENDED : JOB_STATUS.ACTIVE);

							}
							ret.add(kpiExecution);
						}
					}
					return ret;
				}
			});
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
	}

	@Override
	public void removeKpiScheduler(Integer id) {
		try {
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.deleteJob("" + id, KPI_SCHEDULER_GROUP);
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
		delete(SbiKpiExecution.class, id);
	}

	@Override
	public String evaluateScorecardStatus(Integer criterionId, List<ScorecardStatus> scorecardStatusLst) {
		SbiDomains d = load(SbiDomains.class, criterionId);
		String clazz = d.getValueDs();
		try {
			IScorecardCriterion criterion = (IScorecardCriterion) Class.forName(clazz).newInstance();
			return criterion.evaluate(scorecardStatusLst).name();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new SpagoBIDOAException("Criterion class error: " + clazz, e);
		}

	}

	@Override
	public KpiScheduler loadKpiScheduler(final Integer id) {
		KpiScheduler scheduler = executeOnTransaction(new IExecuteOnTransaction<KpiScheduler>() {
			@Override
			public KpiScheduler execute(Session session) throws Exception {
				SbiKpiExecution sbi = (SbiKpiExecution) session.load(SbiKpiExecution.class, id);
				return from(sbi, true);
			}
		});
		/**
		 * Setting status
		 */
		try {
			// loading trigger
			String name = id + "";
			ISchedulerDAO daoScheduler = DAOFactory.getSchedulerDAO();
			Trigger tr = daoScheduler.loadTrigger(KPI_SCHEDULER_GROUP, name);
			if (tr == null) {
				// Calendar now = GregorianCalendar.getInstance(); // creates a new calendar instance
				scheduler.getFrequency().setStartTime("00:00");
				scheduler.getFrequency().setCron(null);
				scheduler.setJobStatus(JOB_STATUS.EXPIRED);
			} else {
				scheduler.setJobStatus(daoScheduler.isTriggerPaused(KPI_SCHEDULER_GROUP, name, KPI_SCHEDULER_GROUP, name) ? JOB_STATUS.SUSPENDED
						: JOB_STATUS.ACTIVE);
				Date startTime = tr.getStartTime();
				Calendar dateStartFreq = GregorianCalendar.getInstance(); // creates a new calendar instance
				dateStartFreq.setTime(startTime); // assigns calendar to given date
				scheduler.getFrequency().setStartTime(dateStartFreq.get(Calendar.HOUR_OF_DAY) + ":" + dateStartFreq.get(Calendar.MINUTE));
				scheduler.getFrequency().setStartDate(dateStartFreq.getTime().getTime());
				if (tr.getEndTime() != null) {
					Date endTime = tr.getEndTime();
					Calendar dateEndFreq = GregorianCalendar.getInstance(); // creates a new calendar instance
					dateEndFreq.setTime(endTime); // assigns calendar to given date
					scheduler.getFrequency().setEndTime(dateEndFreq.get(Calendar.HOUR_OF_DAY) + ":" + dateEndFreq.get(Calendar.MINUTE));
					scheduler.getFrequency().setEndDate(dateEndFreq.getTime().getTime());
				}
				scheduler.getFrequency().setCron(tr.getChronExpression() != null ? tr.getChronExpression().getExpression().replace("'", "\"") : null);
			}
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
		return scheduler;
	}

	private Scorecard from(SbiKpiScorecard sbiScorecard, boolean full) {
		Scorecard scorecard = new Scorecard();
		scorecard.setId(sbiScorecard.getId());
		scorecard.setName(sbiScorecard.getName());
		scorecard.setCreationDate(sbiScorecard.getCommonInfo().getTimeIn());
		scorecard.setAuthor(sbiScorecard.getCommonInfo().getUserIn());
		if (full) {
			for (SbiKpiScorecard sbiPerspective : sbiScorecard.getSubviews()) {
				ScorecardPerspective perspective = new ScorecardPerspective();
				perspective = (ScorecardPerspective) from(perspective, sbiPerspective);
				scorecard.getPerspectives().add(perspective);
				List<ScorecardStatus> ssForPerspective = new ArrayList<>();
				for (SbiKpiScorecard sbiTarget : sbiPerspective.getSubviews()) {
					ScorecardTarget goal = new ScorecardTarget();
					goal = (ScorecardTarget) from(goal, sbiTarget);
					List<ScorecardStatus> ssForTarget = new ArrayList<>();
					for (SbiKpiKpi sbiKpi : sbiTarget.getSbiKpiKpis()) {
						SbiKpiThreshold threshold = load(SbiKpiThreshold.class, sbiKpi.getThresholdId());
						KpiExecution kpi = from(new KpiExecution(), sbiKpi, threshold, true);
						calculateKpiStatus(kpi);
						ScorecardStatus scorecardStatus = new ScorecardStatus();
						scorecardStatus.setStatusEnum(kpi.getStatus());
						ssForTarget.add(scorecardStatus);
						if (goal.getScorecardOption() != null) {
							for (String kpiName : goal.getScorecardOption().getCriterionPriority()) {
								if (kpi.getName().equals(kpiName)) {
									scorecardStatus.setPriority(true);
									break;
								}
							}
						}
						goal.getKpis().add(kpi);
						goal.countKpi(kpi);
					}

					String criterionClassName = goal.getCriterion().getValueDescription();
					try {
						IScorecardCriterion criterion = (IScorecardCriterion) Class.forName(criterionClassName).newInstance();
						goal.setStatus(criterion.evaluate(ssForTarget));
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						logger.error("Criterion class not found: " + criterionClassName, e);
					}
					ScorecardStatus scorecardStatus = new ScorecardStatus();
					scorecardStatus.setStatusEnum(goal.getStatus());
					ssForPerspective.add(scorecardStatus);
					for (String targetName : perspective.getScorecardOption().getCriterionPriority()) {
						if (goal.getName().equals(targetName)) {
							scorecardStatus.setPriority(true);
							break;
						}
					}
					perspective.getTargets().add(goal);
					perspective.countKpiByGoal(goal);
				}
				String criterionClassName = perspective.getCriterion().getValueDescription();
				try {
					IScorecardCriterion criterion = (IScorecardCriterion) Class.forName(criterionClassName).newInstance();
					perspective.setStatus(criterion.evaluate(ssForPerspective));
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					logger.error("Criterion class not found: " + criterionClassName, e);
				}
			}
		}
		return scorecard;
	}

	private void calculateKpiStatus(KpiExecution kpi) {
		List<KpiValue> values = findKpiValues(kpi.getId(), kpi.getVersion(), null, null, new HashMap<String, String>());
		if (values != null && !values.isEmpty()) {
			double value = values.get(values.size() - 1).getComputedValue();
			String color = "";
			for (ThresholdValue threshold : kpi.getThreshold().getThresholdValues()) {
				boolean minValueOk = threshold.getMinValue() == null || value > threshold.getMinValue().doubleValue() || threshold.isIncludeMin()
						&& value == threshold.getMinValue().doubleValue();
				boolean maxValueOk = threshold.getMaxValue() == null || value < threshold.getMaxValue().doubleValue() || threshold.isIncludeMax()
						&& value == threshold.getMaxValue().doubleValue();
				if (minValueOk && maxValueOk) {
					color = threshold.getColor();
					break;
				}
			}
			kpi.setColor(color);
		}
	}

	private KpiScheduler from(SbiKpiExecution sbi, boolean full) throws EMFUserError {
		KpiScheduler scd = new KpiScheduler();
		scd.setId(sbi.getId());
		scd.setName(sbi.getName());
		scd.setAuthor(sbi.getCommonInfo().getUserIn());
		scd.getFrequency().setStartDate(sbi.getStartDate().getTime());
		scd.setDelta(sbi.getDelta() != null && sbi.getDelta().charValue() == 'T' ? Boolean.TRUE : Boolean.FALSE);
		if (sbi.getEndDate() != null)
			scd.getFrequency().setEndDate(sbi.getEndDate().getTime());
		scd.setDelta(Character.valueOf('T').equals(sbi.getDelta()));
		if (full) {
			for (SbiKpiExecutionFilter sbiFilter : sbi.getSbiKpiExecutionFilters()) {
				SchedulerFilter filter = new SchedulerFilter();
				filter.setExecutionId(sbiFilter.getSbiKpiExecutionFilterId().getExecutionId());
				// filter.setPlaceholderId(sbiFilter.getSbiKpiExecutionFilterId().getPlaceholderId());
				filter.setPlaceholderName(sbiFilter.getSbiKpiPlaceholder().getName());
				filter.setKpiName(sbiFilter.getSbiKpiKpi().getName());
				filter.setKpiId(sbiFilter.getSbiKpiKpi().getSbiKpiKpiId().getId());
				filter.setKpiVersion(sbiFilter.getSbiKpiKpi().getSbiKpiKpiId().getVersion());
				if (sbiFilter.getType() != null)
					filter.setType(from(sbiFilter.getType()));
				filter.setValue(sbiFilter.getValue());
				scd.getFilters().add(filter);
			}
			for (SbiKpiKpi sbiKpi : sbi.getSbiKpiKpis()) {
				scd.getKpis().add(from(sbiKpi, null, false));
			}
		} else {
			StringBuilder kpiNames = new StringBuilder();
			for (SbiKpiKpi kpi : sbi.getSbiKpiKpis()) {
				if (kpiNames.length() != 0) {
					kpiNames.append(", ");
				}
				kpiNames.append(kpi.getName());
			}
			scd.setKpiNames(kpiNames.toString());
		}
		return scd;
	}

	private ScorecardSubview from(ScorecardSubview subview, SbiKpiScorecard sbi) {
		subview.setId(sbi.getId());
		subview.setName(sbi.getName());
		subview.setOptions(sbi.getOptions());
		if (sbi.getCriterion() != null) {
			subview.setCriterion(from(sbi.getCriterion()));
		}
		return subview;
	}

	@Override
	public List<Scorecard> listScorecard() {
		List<SbiKpiScorecard> lst = list(new ICriterion<SbiKpiScorecard>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiScorecard.class).add(Restrictions.isNull("parentId"));
			}
		});
		List<Scorecard> scorecardList = new ArrayList<>();
		for (SbiKpiScorecard sbiKpiScorecard : lst) {
			scorecardList.add(from(sbiKpiScorecard, false));
		}
		return scorecardList;
	}

	@Override
	public List<Scorecard> listScorecardByKpi(final Integer id, final Integer version) {
		List<SbiKpiScorecard> lst = list(new ICriterion<SbiKpiScorecard>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiScorecard.class).createAlias("sbiKpiKpis", "_kpis").add(Restrictions.eq("_kpis.sbiKpiKpiId.id", id))
						.add(Restrictions.eq("_kpis.sbiKpiKpiId.version", version));
			}
		});
		List<Scorecard> scorecardList = new ArrayList<>();
		for (SbiKpiScorecard sbiKpiScorecard : lst) {
			scorecardList.add(from(sbiKpiScorecard, false));
		}
		return scorecardList;
	}

	@Override
	public Scorecard loadScorecard(final Integer id) {
		return executeOnTransaction(new IExecuteOnTransaction<Scorecard>() {
			@Override
			public Scorecard execute(Session session) throws Exception {
				return from((SbiKpiScorecard) session.load(SbiKpiScorecard.class, id), true);
			}
		});
	}

	@Override
	public Integer insertScorecard(final Scorecard scorecard) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) throws Exception {
				SbiKpiScorecard sbiScorecard = from(scorecard, null);
				updateSbiCommonInfo4Insert(sbiScorecard);
				Integer id = (Integer) session.save(sbiScorecard);
				saveScorecard(scorecard, session, sbiScorecard);
				return id;
			}
		});
	}

	private void saveScorecard(Scorecard scorecard, Session session, SbiKpiScorecard sbiScorecard) throws JSONException {
		// Perspective
		for (ScorecardPerspective scorecardPerspective : scorecard.getPerspectives()) {
			SbiKpiScorecard sbiPerspective = from(scorecardPerspective, sbiScorecard, session);
			sbiScorecard.getSubviews().add(sbiPerspective);
			// ScorecardTarget
			sbiPerspective.getSubviews().clear();
			session.save(sbiPerspective);
			for (ScorecardTarget scorecardTarget : scorecardPerspective.getTargets()) {
				SbiKpiScorecard sbiScorecardTarget = from(scorecardTarget, sbiPerspective, session);
				sbiPerspective.getSubviews().add(sbiScorecardTarget);
				// KPI
				sbiScorecardTarget.getSbiKpiKpis().clear();
				session.save(sbiScorecardTarget);
				for (Kpi kpi : scorecardTarget.getKpis()) {
					SbiKpiKpi sbiKpiKpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(kpi.getId(), kpi.getVersion()));
					sbiScorecardTarget.getSbiKpiKpis().add(sbiKpiKpi);
					// session.save(sbiKpiKpi);
				}

			}

		}
	}

	private SbiKpiScorecard from(ScorecardSubview scorecardSubview, SbiKpiScorecard sbiScorecard, Session session) throws JSONException {
		SbiKpiScorecard sbiKpiScorecard = null;
		if (scorecardSubview.getId() == null) {
			if (sbiScorecard.getId() != null) {
				sbiKpiScorecard = new SbiKpiScorecard();
				sbiKpiScorecard.setParentId(sbiScorecard.getId());
			}
			updateSbiCommonInfo4Insert(sbiKpiScorecard);
		} else {
			sbiKpiScorecard = (SbiKpiScorecard) session.load(SbiKpiScorecard.class, scorecardSubview.getId());
			updateSbiCommonInfo4Update(sbiKpiScorecard);
		}
		sbiKpiScorecard.setName(scorecardSubview.getName());
		sbiKpiScorecard.setOptions(scorecardSubview.getOptions().toString());
		sbiKpiScorecard.setCriterionId(scorecardSubview.getCriterion().getValueId());

		return sbiKpiScorecard;
	}

	private SbiKpiScorecard from(Scorecard scorecard, SbiKpiScorecard sbiScorecard) {
		if (sbiScorecard == null) {
			sbiScorecard = new SbiKpiScorecard();
		}
		sbiScorecard.setName(scorecard.getName());
		return sbiScorecard;
	}

	@Override
	public void updateScorecard(final Scorecard scorecard) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws Exception {
				SbiKpiScorecard sbiScorecard = (SbiKpiScorecard) session.load(SbiKpiScorecard.class, scorecard.getId());
				sbiScorecard = from(scorecard, sbiScorecard);
				updateSbiCommonInfo4Update(sbiScorecard);
				session.save(sbiScorecard);
				// Removing old values
				Iterator<SbiKpiScorecard> scorecardPerspectiveIterator = sbiScorecard.getSubviews().iterator();
				while (scorecardPerspectiveIterator.hasNext()) {
					SbiKpiScorecard sbiKpiScorecardPerspective = scorecardPerspectiveIterator.next();
					boolean found = false;
					ScorecardPerspective perspective = null;
					for (ScorecardPerspective scorecardPerspective : scorecard.getPerspectives()) {
						if (sbiKpiScorecardPerspective.getId().equals(scorecardPerspective.getId())) {
							found = true;
							perspective = scorecardPerspective;
							break;
						}
					}
					if (!found) {
						scorecardPerspectiveIterator.remove();
					} else {
						// Target
						Iterator<SbiKpiScorecard> scorecardPerspectiveTargetIterator = sbiKpiScorecardPerspective.getSubviews().iterator();
						while (scorecardPerspectiveTargetIterator.hasNext()) {
							SbiKpiScorecard sbiKpiScorecardTarget = scorecardPerspectiveTargetIterator.next();
							boolean foundScorecardTarget = false;
							ScorecardTarget target = null;
							for (ScorecardTarget scorecardTarget : perspective.getTargets()) {
								if (sbiKpiScorecardTarget.getId().equals(scorecardTarget.getId())) {
									foundScorecardTarget = true;
									target = scorecardTarget;
									break;
								}
							}
							if (!foundScorecardTarget) {
								scorecardPerspectiveTargetIterator.remove();
							} else {
								// KPI
								Iterator<SbiKpiKpi> scorecardPerspectiveTargetKpiIterator = sbiKpiScorecardTarget.getSbiKpiKpis().iterator();
								while (scorecardPerspectiveTargetKpiIterator.hasNext()) {
									SbiKpiKpi sbiKpiKpi = scorecardPerspectiveTargetKpiIterator.next();
									boolean foundKpi = false;
									for (Kpi kpi : target.getKpis()) {
										if (sbiKpiKpi.getSbiKpiKpiId().equals(kpi.getId())) {
											foundKpi = true;
											break;
										}
									}
									if (!foundKpi) {
										scorecardPerspectiveTargetKpiIterator.remove();
									}
								}
							}
						}
					}
				}
				// Adding new values
				saveScorecard(scorecard, session, sbiScorecard);
				return Boolean.TRUE;
			}

		});
	}

	@Override
	public void removeScorecard(Integer id) {
		delete(SbiKpiScorecard.class, id);
	}

	@Override
	public Map<Kpi, List<String>> listPlaceholderByKpiList(final List<Kpi> kpis) {
		List<Map<String, Object>> measures = executeOnTransaction(new IExecuteOnTransaction<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> execute(Session session) throws Exception {
				Disjunction disjunction = Restrictions.disjunction();
				for (Kpi kpi : kpis) {
					disjunction.add(Restrictions.conjunction().add(Restrictions.eq("sbiKpiKpis.sbiKpiKpiId.id", kpi.getId()))
							.add(Restrictions.eq("sbiKpiKpis.sbiKpiKpiId.version", kpi.getVersion())));
				}

				List<Map<String, Object>> measures = session
						.createCriteria(SbiKpiRuleOutput.class)
						.createAlias("sbiKpiRule", "sbiKpiRule")
						.createAlias("sbiKpiKpis", "sbiKpiKpis")
						.createAlias("sbiKpiAlias", "sbiKpiAlias")
						.add(disjunction)
						.setProjection(
								Projections.projectionList().add(Property.forName("sbiKpiKpis.sbiKpiKpiId.id").as("id"))
										.add(Property.forName("sbiKpiKpis.sbiKpiKpiId.version").as("version"))
										.add(Property.forName("sbiKpiKpis.placeholder").as("placeholder")).add(Property.forName("sbiKpiKpis.name").as("name"))
										.add(Property.forName("sbiKpiAlias.name").as("measure"))).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
				return measures;
			}
		});
		/**
		 * Initially this is a map of {kpi name: list of measure names}, then it will contain a map of {kpi name: list of placeholder names}
		 */
		Map<Kpi, List<String>> ret = new HashMap<>();
		for (Map<String, Object> o : measures) {
			String measure = (String) o.get("measure");
			String name = (String) o.get("name");
			String placeholder = (String) o.get("placeholder");
			Integer kpiId = (Integer) o.get("id");
			Integer kpiVersion = (Integer) o.get("version");
			Kpi kpi = new Kpi(kpiId, kpiVersion);
			if (!ret.containsKey(kpi)) {
				kpi.setName(name);
				kpi.setPlaceholder(placeholder);
				ret.put(kpi, new ArrayList<String>());
			}
			ret.get(kpi).add(measure);
		}
		for (Kpi kpi : ret.keySet()) {
			ret.put(kpi, listPlaceholderByMeasures(ret.get(kpi)));
		}
		return ret;
	}

	@Override
	public List<KpiExecution> listKpiWithResult() {
		List<SbiKpiKpi> lst = list(new ICriterion<SbiKpiKpi>() {
			@Override
			public Criteria evaluate(Session session) {
				return session.createCriteria(SbiKpiKpi.class).add(Restrictions.eq("active", 'T'));
			}
		});
		List<KpiExecution> kpis = new ArrayList<>();
		for (SbiKpiKpi sbi : lst) {
			KpiExecution kpi = new KpiExecution();
			SbiKpiThreshold threshold = load(SbiKpiThreshold.class, sbi.getThresholdId());
			from(kpi, sbi, threshold, true);
			calculateKpiStatus(kpi);
			kpis.add(kpi);
		}
		return kpis;
	}

	@Override
	public void editKpiValue(Integer id, double value, String comment) {

		Session tmpSession = getSession();
		Transaction tx = tmpSession.beginTransaction();

		String hql = " from SbiKpiValue WHERE id =?";
		Query q = tmpSession.createQuery(hql);
		q.setInteger(0, id);
		SbiKpiValue kpiValue = (SbiKpiValue) tmpSession.load(SbiKpiValue.class, id);
		if (value == -999) {
			kpiValue.setManualValue(null);
		} else {
			kpiValue.setManualValue(value);
		}

		kpiValue.setManualNote(comment);
		tmpSession.save(kpiValue);

		tx.commit();

	}

	// TODO: test and debug
	@Override
	public List<KpiValue> findKpiValues(final Integer kpiId, Integer kpiVersion, final Date computedAfter, final Date computedBefore,
			Map<String, String> attributesValues) {
		// Ensure attributesValues keys to be case-insensitive
		TreeMap<String, String> cioAttributesValues = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		cioAttributesValues.putAll(attributesValues);
		attributesValues = cioAttributesValues;

		// Retrieve the KPI
		Kpi kpi;
		if (kpiVersion == null) {
			Integer tempId = findlastKpiFromKpiValue(kpiId);
			if (tempId != null) {
				kpiVersion = tempId;
			} else {
				return new ArrayList<>();
			}

		}
		kpi = loadKpi(kpiId, kpiVersion);

		// Find the main measure rule and attributes
		Integer mainMeasureRuleId = null;
		Integer mainMeasureRuleVersion = null;
		TreeSet<String> mainMeasureAttributes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		try {
			JSONObject cardinality = new JSONObject(kpi.getCardinality());
			JSONArray measureList = cardinality.getJSONArray("measureList");
			for (int m = 0; m < measureList.length(); m++) {
				JSONObject unparsedMeasure = measureList.getJSONObject(m);
				TreeSet<String> attributes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
				Iterator<String> ait = unparsedMeasure.getJSONObject("attributes").keys();
				while (ait.hasNext()) {
					String attributeName = ait.next();
					if (unparsedMeasure.getJSONObject("attributes").getBoolean(attributeName)) {
						attributes.add(attributeName);
					}
				}
				if (attributes.size() > mainMeasureAttributes.size() || mainMeasureRuleId == null) {
					mainMeasureRuleId = unparsedMeasure.getInt("ruleId");
					mainMeasureRuleVersion = unparsedMeasure.getInt("ruleVersion");
					mainMeasureAttributes = attributes;
				}
			}
		} catch (JSONException e) {
			throw new SpagoBIDOAException(e);
		}

		// Find temporal attributes
		Rule rule = loadRule(mainMeasureRuleId, mainMeasureRuleVersion);
		Map<String, String> attributesTemporalTypes = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		for (RuleOutput ruleOutput : rule.getRuleOutputs()) {
			if ("TEMPORAL_ATTRIBUTE".equals(ruleOutput.getType().getValueCd())) {
				String attributeName = ruleOutput.getAlias();
				String attributeTemporalType = ruleOutput.getHierarchy().getValueCd(); // YEAR, QUARTER, MONTH, WEEK, DAY
				attributesTemporalTypes.put(attributeName, attributeTemporalType);
			}
		}

		// Build logical key and find temporal attributes values
		StringBuffer logicalKeyTmp = new StringBuffer();
		final Map<String, String> temporalValues = new HashMap<String, String>();
		for (String attributeName : mainMeasureAttributes) {
			String attributeValue = attributesValues.get(attributeName);
			if (attributeValue == null) {
				if (ProcessKpiJob.INCLUDE_IGNORED_NON_TEMPORAL_ATTRIBUTES_INTO_KPI_VALUE_LOGICAL_KEY) {
					attributeValue = "ALL";
				} else {
					continue;
				}
			}
			String temporalType = attributesTemporalTypes.get(attributeName);
			if (temporalType != null) {
				temporalValues.put(temporalType, attributeValue.replaceAll("'", "''"));
				if (ProcessKpiJob.EXCLUDE_TEMPORAL_ATTRIBUTES_FROM_KPI_VALUE_LOGICAL_KEY)
					continue;
			}
			if (logicalKeyTmp.length() > 0)
				logicalKeyTmp.append(",");
			logicalKeyTmp.append(attributeName.toUpperCase()).append("=").append(attributeValue.trim());
		}
		final String logicalKey = logicalKeyTmp.toString();

		final Integer kpiVersionFinal = kpiVersion;
		// Execute query
		List<SbiKpiValue> sbiKpiValues = list(new ICriterion<SbiKpiValue>() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria criteria = session.createCriteria(SbiKpiValue.class);
				if (kpiId != null)
					criteria.add(Restrictions.eq("kpiId", kpiId));
				if (kpiVersionFinal != null)
					criteria.add(Restrictions.eq("kpiVersion", kpiVersionFinal));
				if (computedAfter != null)
					criteria.add(Restrictions.ge("timeRun", computedAfter));
				if (computedBefore != null)
					criteria.add(Restrictions.le("timeRun", computedBefore));
				if (!logicalKey.isEmpty())
					criteria.add(Restrictions.eq("logicalKey", logicalKey));
				criteria.add(Restrictions.eq("theDay", ifNull(temporalValues.get("DAY"), "ALL")));
				criteria.add(Restrictions.eq("theWeek", ifNull(temporalValues.get("WEEK"), "ALL")));
				criteria.add(Restrictions.eq("theMonth", ifNull(temporalValues.get("MONTH"), "ALL")));
				criteria.add(Restrictions.eq("theQuarter", ifNull(temporalValues.get("QUARTER"), "ALL")));
				criteria.add(Restrictions.eq("theYear", ifNull(temporalValues.get("YEAR"), "ALL")));
				if (computedAfter == null && computedBefore == null) {
					criteria.addOrder(Order.desc("timeRun")).setMaxResults(1).uniqueResult();
				} else {
					criteria.addOrder(Order.asc("timeRun")).addOrder(Order.asc("kpiId")).addOrder(Order.asc("kpiVersion"));
				}
				return criteria;
			}
		});

		// Convert data
		List<KpiValue> kpiValues = new ArrayList<>();
		for (SbiKpiValue sbiKpiValue : sbiKpiValues) {
			KpiValue kpiValue = new KpiValue();
			kpiValue.setId(sbiKpiValue.getId());
			kpiValue.setKpiId(sbiKpiValue.getKpiId());
			kpiValue.setKpiVersion(sbiKpiValue.getKpiVersion());
			kpiValue.setLogicalKey(sbiKpiValue.getLogicalKey());
			kpiValue.setTimeRun(sbiKpiValue.getTimeRun());
			kpiValue.setManualValue(sbiKpiValue.getManualValue());
			kpiValue.setComputedValue(sbiKpiValue.getComputedValue());
			kpiValue.setManualNote(sbiKpiValue.getManualNote());
			kpiValue.setTheDay(sbiKpiValue.getTheDay());
			kpiValue.setTheMonth(sbiKpiValue.getTheMonth());
			kpiValue.setTheQuarter(sbiKpiValue.getTheQuarter());
			kpiValue.setTheWeek(sbiKpiValue.getTheWeek());
			kpiValue.setTheYear(sbiKpiValue.getTheYear());
			kpiValue.setState(sbiKpiValue.getState());
			kpiValue.setManualNote(sbiKpiValue.getManualNote());
			kpiValues.add(kpiValue);
		}

		return kpiValues;
	}

	private Integer findlastKpiFromKpiValue(Integer id) {
		Session tmpSession = getSession();
		Transaction tx = tmpSession.beginTransaction();

		String hql = " from SbiKpiValue WHERE kpiId =?";
		Query q = tmpSession.createQuery(hql);
		q.setInteger(0, id);
		List<SbiKpiValue> kpiValue = q.list();

		if (kpiValue.size() != 0) {
			return kpiValue.get(kpiValue.size() - 1).getKpiVersion();
		} else {
			return null;
		}

	}

	@Override
	public void insertKpiValueExecLog(KpiValueExecLog kpiValueExecLog) {
		SbiKpiValueExecLog sbiKpiValueExecLog = new SbiKpiValueExecLog();
		sbiKpiValueExecLog.setSchedulerId(kpiValueExecLog.getSchedulerId());
		sbiKpiValueExecLog.setTimeRun(kpiValueExecLog.getTimeRun());
		sbiKpiValueExecLog.setErrorCount(kpiValueExecLog.getErrorCount());
		sbiKpiValueExecLog.setSuccessCount(kpiValueExecLog.getSuccessCount());
		sbiKpiValueExecLog.setTotalCount(kpiValueExecLog.getTotalCount());
		sbiKpiValueExecLog.setOutput(kpiValueExecLog.getOutput() == null ? null : kpiValueExecLog.getOutput().getBytes(Charset.forName("utf8")));
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		session.save(sbiKpiValueExecLog);
		tx.commit();
	}

	private static Object ifNull(Object a, Object b) {
		return a == null ? b : a;
	}

	private static final List<it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS> statusValues = Collections.unmodifiableList(Arrays
			.asList(it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS.values()));
	private static final int SIZE = statusValues.size();
	private static final Random RANDOM = new Random();

	@Override
	public Integer insertScheduler(final KpiScheduler scheduler) throws SpagoBIException {
		Integer id = executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) throws Exception {
				SbiKpiExecution sbiKpiExecution = from(scheduler, null, session);
				return sbiKpiExecution.getId();
			}
		});
		ISchedulerDAO schedulerDAO = null;
		try {
			schedulerDAO = DAOFactory.getSchedulerDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException(e);
		}
		String name = "" + id;
		Map<String, String> parameters = new HashMap<>();
		parameters.put("kpiSchedulerId", name);
		// This try/catch is needed to trace errors coming from quartz, because they must be sent to user
		try {
			schedulerDAO.createOrUpdateJobAndTrigger(name, ProcessKpiJob.class, KPI_SCHEDULER_GROUP, KPI_SCHEDULER_GROUP, scheduler.getFrequency(), parameters);
		} catch (SpagoBIDOAException e) {
			logger.error(e);
			throw new SpagoBIException("Error inserting scheduler", e);
		}
		return id;
	}

	@Override
	public Integer updateScheduler(final KpiScheduler scheduler) throws SpagoBIException {
		Integer id = executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) throws Exception {
				SbiKpiExecution sbiKpiExecution = (SbiKpiExecution) session.load(SbiKpiExecution.class, scheduler.getId());
				sbiKpiExecution = from(scheduler, sbiKpiExecution, session);
				return sbiKpiExecution.getId();
			}
		});
		try {
			String name = "" + id;
			Map<String, String> parameters = new HashMap<>();
			parameters.put("kpiSchedulerId", name);
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			// This try/catch is needed to trace errors coming from quartz, because they must be sent to user
			try {
				schedulerDAO.createOrUpdateJobAndTrigger(name, ProcessKpiJob.class, KPI_SCHEDULER_GROUP, KPI_SCHEDULER_GROUP, scheduler.getFrequency(),
						parameters);
			} catch (SpagoBIDOAException e) {
				logger.error(e);
				throw new SpagoBIException("Error creating or updating job or trigger");
			}
		} catch (EMFUserError e) {
			throw new SpagoBIDOAException("Error updating scheduler", e);
		}
		return id;
	}

	private SbiKpiExecution from(KpiScheduler scheduler, SbiKpiExecution sbiKpiExecution, Session session) {
		if (sbiKpiExecution == null) {
			sbiKpiExecution = new SbiKpiExecution();
			updateSbiCommonInfo4Insert(sbiKpiExecution);
		} else {
			updateSbiCommonInfo4Update(sbiKpiExecution);
		}
		sbiKpiExecution.setName(scheduler.getName());
		sbiKpiExecution.setDelta(Boolean.TRUE.equals(scheduler.getDelta()) ? 'T' : 'F');

		Calendar startDate = Calendar.getInstance();
		startDate.setTimeInMillis(scheduler.getFrequency().getStartDate());
		sbiKpiExecution.setStartDate(startDate.getTime());

		if (scheduler.getFrequency().getEndDate() != null) {
			Calendar endDate = Calendar.getInstance();
			endDate.setTimeInMillis(scheduler.getFrequency().getEndDate());
			sbiKpiExecution.setEndDate(endDate.getTime());
		}

		Integer id = (Integer) session.save(sbiKpiExecution);
		// Removing old filters / updating existing one
		Iterator<SbiKpiExecutionFilter> persistentFilters = sbiKpiExecution.getSbiKpiExecutionFilters().iterator();
		while (persistentFilters.hasNext()) {
			SchedulerFilter existingFilter = null;
			SbiKpiExecutionFilter sbiFilter = persistentFilters.next();
			for (SchedulerFilter filter : scheduler.getFilters()) {
				if (sbiFilter.getSbiKpiExecutionFilterId().getKpiId().equals(filter.getKpiId())
						&& sbiFilter.getSbiKpiExecutionFilterId().getKpiVersion().equals(filter.getKpiVersion())
						&& sbiFilter.getSbiKpiPlaceholder().getName().equals(filter.getPlaceholderName())) {
					existingFilter = filter;
					break;
				}
			}
			if (existingFilter == null) {
				persistentFilters.remove();
			} else {
				sbiKpiExecution.getSbiKpiExecutionFilters().add(from(existingFilter, sbiFilter, session));
			}

		}
		// Adding new filters
		for (SchedulerFilter sf : scheduler.getFilters()) {
			if (sf.getExecutionId() == null) {
				sf.setExecutionId(sbiKpiExecution.getId());
			}
			sbiKpiExecution.getSbiKpiExecutionFilters().add(from(sf, null, session));
		}
		sbiKpiExecution.getSbiKpiKpis().clear();
		for (Kpi kpi : scheduler.getKpis()) {
			SbiKpiKpi sbiKpi = (SbiKpiKpi) session.load(SbiKpiKpi.class, new SbiKpiKpiId(kpi.getId(), kpi.getVersion()));
			sbiKpiExecution.getSbiKpiKpis().add(sbiKpi);
		}
		return sbiKpiExecution;
	}

	private SbiKpiExecutionFilter from(SchedulerFilter schedulerFilter, SbiKpiExecutionFilter sbiFilter, Session session) {
		Integer placeholderId = null;
		if (schedulerFilter.getPlaceholderId() != null) {
			placeholderId = schedulerFilter.getPlaceholderId();
		} else if (schedulerFilter.getPlaceholderName() != null) {
			placeholderId = (Integer) session.createCriteria(SbiKpiPlaceholder.class).add(Restrictions.eq("name", schedulerFilter.getPlaceholderName()))
					.setProjection(Property.forName("id")).uniqueResult();
		}
		if (sbiFilter == null) {
			sbiFilter = new SbiKpiExecutionFilter();
			SbiKpiExecutionFilterId id = new SbiKpiExecutionFilterId(placeholderId, schedulerFilter.getExecutionId(), schedulerFilter.getKpiId(),
					schedulerFilter.getKpiVersion());
			sbiFilter.setSbiKpiExecutionFilterId(id);
			updateSbiCommonInfo4Insert(sbiFilter);
		} else {
			updateSbiCommonInfo4Update(sbiFilter);
		}
		sbiFilter.setValue(schedulerFilter.getValue());
		sbiFilter.setTypeId(schedulerFilter.getType().getValueId());
		return sbiFilter;
	}

	/**
	 * Add kpi/placeholders to job parameter map in this form ["kpiId|kpiVersion":[{"placeholderName":"placeholderValue"}]]
	 * 
	 * @param job
	 * @param scheduler
	 */
	private void addKpiToJobParameter(Job job, KpiScheduler scheduler) {
		Map<String, Map<String, String>> kpis = new HashMap<>();
		for (SchedulerFilter sf : scheduler.getFilters()) {
			String kpiKey = sf.getKpiId() + "|" + sf.getKpiVersion();
			if (!kpis.containsKey(kpiKey)) {
				kpis.put(kpiKey, new HashMap<String, String>());
			}
			kpis.get(kpiKey).put(sf.getPlaceholderName(), sf.getValue());
		}
		for (Kpi kpi : scheduler.getKpis()) {
			String kpiKey = kpi.getId() + "|" + kpi.getVersion();
			if (!kpis.containsKey(kpiKey)) {
				kpis.put(kpiKey, new HashMap<String, String>());
			}
		}
		if (!kpis.isEmpty()) {
			job.addParameter("kpis", JsonConverter.objectToJson(kpis, kpis.getClass()));
		}
	}

	@Override
	public List<Target> listTargetByKpi(final Integer kpiId, final Integer kpiVersion) {
		List<SbiKpiTarget> sbiKpiTargets = list(new ICriterion<SbiKpiTarget>() {
			@Override
			public Criteria evaluate(Session session) {
				// Ordering by rule name and measure name
				Criteria c = session.createCriteria(SbiKpiTarget.class).createAlias("sbiKpiTargetValues", "_tValues").createAlias("_tValues.sbiKpiKpi", "_kpi")
						.add(Restrictions.eq("_kpi.sbiKpiKpiId.id", kpiId)).add(Restrictions.eq("_kpi.sbiKpiKpiId.version", kpiVersion));
				return c;
			}
		});

		List<Target> targets = new ArrayList<>();
		for (SbiKpiTarget sbiKpiTarget : sbiKpiTargets) {
			targets.add(from(sbiKpiTarget, false));

		}
		return targets;
	}

	@Override
	public List<KpiScheduler> listSchedulerByKpi(final Integer kpiId, final Integer kpiVersion) {
		return executeOnTransaction(new IExecuteOnTransaction<List<KpiScheduler>>() {
			@Override
			public List<KpiScheduler> execute(Session session) throws Exception {
				List<KpiScheduler> ret = new ArrayList<>();
				List<SbiKpiExecution> lst = session.createCriteria(SbiKpiExecution.class).createAlias("sbiKpiKpis", "_kpis")
						.add(Restrictions.eq("_kpis.sbiKpiKpiId.id", kpiId)).add(Restrictions.eq("_kpis.sbiKpiKpiId.version", kpiVersion)).list();
				if (lst != null) {
					for (SbiKpiExecution sbiKpiExecution : lst) {
						KpiScheduler kpiExecution = from(sbiKpiExecution, false);
						ret.add(kpiExecution);
					}
				}
				return ret;
			}
		});
	}
}
