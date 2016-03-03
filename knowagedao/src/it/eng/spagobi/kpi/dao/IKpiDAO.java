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
import it.eng.spagobi.kpi.bo.Placeholder;
import it.eng.spagobi.kpi.bo.Rule;
import it.eng.spagobi.kpi.bo.RuleOutput;
import it.eng.spagobi.kpi.bo.Threshold;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;

import java.util.List;

public interface IKpiDAO extends ISpagoBIDao {

	/**
	 * Given a list of measures it builds a cardinality matrix (measures/attributes)
	 * 
	 * @param measures
	 * @return a list of Cardinality
	 */
	public List<Cardinality> buildCardinality(final List<String> measures);

	public List<String> aliasValidation(Rule rule);

	public List<RuleOutput> listRuleOutputByType(String type);

	public RuleOutput loadMeasureByName(String name);

	/**
	 * Saves a new Rule and returns its id
	 * 
	 * @param rule
	 * @return rule id
	 * @throws SpagoBIException
	 */
	public Integer insertRule(Rule rule) throws SpagoBIException;

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

	/**
	 * Given a list of measures it retrieves a list of placeholder related to that measures
	 * 
	 * @param measureList
	 * @return a list of placeholder name
	 */
	public List<String> listPlaceholderByMeasures(List<String> measureList);

}
