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

package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.writeback4j.sql.DefaultWeightedAllocationAlgorithmDataManager;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;

public class DefaultWeightedAllocationAlgorithm extends AbstractAllocationAlgorithm {

	public static final String NAME = "DEFAULT_WEIGHTED_ALLOCATION_ALGORITHM";

	private static Logger logger = Logger.getLogger(DefaultWeightedAllocationAlgorithm.class);
	private WhatIfEngineInstance ei;
	private DefaultWeightedAllocationAlgorithmDataManager persister;
	private String lastQuery;
	private boolean useInClause = true;

	public DefaultWeightedAllocationAlgorithm() {
	}

	public DefaultWeightedAllocationAlgorithm(WhatIfEngineInstance ei) {
		this();
		this.ei = ei;
		initAlgorithm();
	}

	public void initAlgorithm() {
		persister = new DefaultWeightedAllocationAlgorithmDataManager(ei.getWriteBackManager().getRetriver(), ei.getDataSource());
		persister.setUseInClause(useInClause);
	}

	public String getName() {
		return NAME;
	}

	@Override
	protected void persistInternal(SpagoBICellWrapper cell, Object oldValue, Object newValue, Connection connection, Integer version) throws Exception {
		Double prop = ((Number) newValue).doubleValue() / ((Number) oldValue).doubleValue();
		lastQuery = persister.executeUpdate(cell.getMembers(), prop, connection, version);
	}

	public String getLastQuery() {
		return lastQuery;
	}

	public DefaultWeightedAllocationAlgorithmDataManager getPersister() {
		return persister;
	}

	@Override
	protected double applyBelow(SpagoBICellSetWrapper cellSetWrapper, SpagoBICellWrapper cell, SpagoBICellWrapper wrappedCell, double newValue, double oldValue) throws Exception {
		return wrappedCell.getDoubleValue() * (newValue / oldValue);
	}

	public void setProperties(Map<String, Object> properties) {
		if (properties != null) {
			this.ei = (WhatIfEngineInstance) properties.get(ENGINEINSTANCE_PROPERTY);
			String useInClauseString = (String) properties.get(USEINCLAUSE_PROPERTY);

			if (useInClauseString != null) {
				try {
					useInClause = (new Boolean(useInClauseString));
				} catch (Exception e) {
					logger.error("Impossible to decode te property useInClause. The value " + useInClauseString + " is not admissible");
				}
			}
		}
		initAlgorithm();
	}

	public void setUseInClause(boolean useInClause) {
		this.useInClause = useInClause;
	}

	@Override
	protected String getMonitorName() {
		return "SpagoBIWhatIfEngine/it.eng.spagobi.engines.whatif.model.transform.DefaultWeightedAllocationAlgorithm";
	}

	public boolean canOverridePrevious() {
		return false;
	}

	public boolean isInMemory() {
		return true;
	}

	public boolean isPersistent() {
		return true;
	}

}
