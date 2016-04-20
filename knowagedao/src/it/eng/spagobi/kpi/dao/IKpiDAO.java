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

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.bo.Alias;
import it.eng.spagobi.kpi.bo.Cardinality;
import it.eng.spagobi.kpi.bo.Kpi;
import it.eng.spagobi.kpi.bo.KpiExecution;
import it.eng.spagobi.kpi.bo.KpiScheduler;
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Scorecard;
import it.eng.spagobi.kpi.bo.ScorecardStatus;
import it.eng.spagobi.kpi.bo.Target;
import it.eng.spagobi.kpi.bo.TargetValue;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.kpi.dao.KpiDAOImpl.STATUS;
import it.eng.spagobi.kpi.metadata.SbiKpiTarget;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IKpiDAO extends ISpagoBIDao {

	/**
	 * Given a list of measures it builds a cardinality matrix (measures/attributes)
	 * 
	 * @param measures
	 * @return a list of Cardinality
	 */
	public List<Cardinality> buildCardinality(final List<String> measures);

	/**
	 * Validate a Rule returning errors if any
	 * 
	 * @param rule
	 * @return a map of {error key: list of alias}
	 */
	public Map<String, List<String>> aliasValidation(Rule rule);

	/**
	 * Retrieves all rule output filtered by type (MEASURE, ATTRIBUTE, TEMPORAL_ATTRIBUTE) and status (only active / only not active / all records)
	 * 
	 * @param type
	 *            (see SbiDomains)
	 * @param status
	 * @return rule output list
	 */
	public List<RuleOutput> listRuleOutputByType(String type, STATUS status);

	/**
	 * Checks if given measure names are really existing on db
	 * 
	 * @param measure
	 *            names
	 * @return true if all measures are existing false otherwise
	 */
	public Boolean existsMeasureNames(String... names);

	/**
	 * Retrieves all kpi that are using a threshold
	 * 
	 * @param threshold
	 *            id
	 * @return a list of kpi id
	 */
	public List<Integer> listKpiByThreshold(Integer thresholdId);

	/**
	 * Return 'true' if a threshold is used by kpi other then the one with id = kpiId
	 * 
	 * @param kpiId
	 * @param thresholdId
	 * @return
	 */
	public boolean isThresholdUsedByOtherKpi(Integer kpiId, Integer thresholdId);

	/**
	 * Saves a new Rule and returns its id
	 * 
	 * @param rule
	 * @return rule id
	 * @throws SpagoBIException
	 */
	public Rule insertRule(Rule rule) throws SpagoBIException;

	public Rule insertNewVersionRule(Rule rule) throws SpagoBIException;

	public Kpi insertNewVersionKpi(Kpi kpi) throws SpagoBIException;

	public void updateRule(Rule rule) throws SpagoBIException;

	public void removeRule(Integer id, Integer version, boolean toBeVersioned);

	public Rule loadRule(Integer id, Integer version);

	public Integer getRuleIdByName(String name);

	/**
	 * Retrieves all kpi filtered by status (only active / only not active / all records)
	 * 
	 * @param status
	 * @return
	 */
	public List<Kpi> listKpi(STATUS status);

	public List<KpiExecution> listKpiWithResult();

	/**
	 * Insert a new kpi
	 * 
	 * @param kpi
	 * @return new generated kpi id
	 */
	public Kpi insertKpi(Kpi kpi);

	/**
	 * Update an existing kpi
	 * 
	 * @param kpi
	 * @return
	 */
	public void updateKpi(Kpi kpi);

	public void removeKpi(Integer id, Integer version, boolean toBeVersioned);

	public Kpi loadKpi(Integer id, Integer version);

	/**
	 * Retrieves a kpi id by searching for its name
	 * 
	 * @param kpi
	 *            name
	 * @return kpi id
	 */
	public Integer getKpiIdByName(String name);

	public List<Alias> listAlias();

	/**
	 * Retrieve all aliases not currently used as measure in all rules excluding the one with specific id and version
	 * 
	 * @param ruleId
	 * @param ruleVersion
	 * @return a list of Alias
	 */
	public List<Alias> listAliasNotInMeasure(Integer ruleId, Integer ruleVersion);

	/**
	 * Retrieve all aliases not currently used as measure in all rules
	 * 
	 * @return a list of Alias
	 */
	public List<Alias> listAliasNotInMeasure();

	public Alias loadAlias(String name);

	public List<Placeholder> listPlaceholder();

	public List<Threshold> listThreshold();

	public Threshold loadThreshold(Integer id);

	/**
	 * Given a list of measures it retrieves a list of placeholder related to that measures
	 * 
	 * @param measureList
	 * @return a list of placeholder name
	 */
	public List<String> listPlaceholderByMeasures(List<String> measureList);

	/**
	 * Given a kpi id and version, it retrieves a list of placeholder related to its measures
	 * 
	 * @param kpi
	 *            id
	 * @param kpi
	 *            version
	 * @return a list of placeholder name
	 */
	public Map<Kpi, List<String>> listPlaceholderByKpiList(List<Kpi> kpis);

	public List<Target> listTarget(Date startDate, Date endDate);

	public List<Target> listTarget();

	public Target loadTarget(Integer id);

	public Integer insertTarget(Target target);

	public void updateTarget(Target target);

	public void removeTarget(Integer id);

	public List<TargetValue> listKpiWithTarget(Integer targetId);

	public List<KpiScheduler> listKpiScheduler();

	public void removeKpiScheduler(Integer id);

	public KpiScheduler loadKpiScheduler(Integer id);

	public List<Scorecard> listScorecard();

	public Scorecard loadScorecard(Integer id);

	public Integer insertScorecard(Scorecard scorecard);

	public void updateScorecard(Scorecard scorecard);

	public void removeScorecard(Integer id);

	public Integer insertScheduler(KpiScheduler scheduler);

	public Integer updateScheduler(KpiScheduler scheduler);

	public List<SbiKpiTarget> listTargetbyKpi(Kpi kpi);

	/**
	 * Gets a criterion id (ie a domain) and a list of ScorecardStatus and returns a status
	 * 
	 * @param scorecardId
	 * @param scorecardStatusLst
	 * @return status
	 */
	public String evaluateScorecardStatus(Integer criterionId, List<ScorecardStatus> scorecardStatusLst);
}
