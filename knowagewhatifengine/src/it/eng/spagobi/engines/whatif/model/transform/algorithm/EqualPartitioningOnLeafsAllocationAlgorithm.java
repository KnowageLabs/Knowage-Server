/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 */

package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.writeback4j.sql.EqualPartitioningOnLeafsAllocationAlgorithmDataManager;

import java.sql.Connection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olap4j.metadata.Member;

public class EqualPartitioningOnLeafsAllocationAlgorithm extends AbstractAllocationAlgorithm {

	private static Logger logger = Logger.getLogger(EqualPartitioningOnLeafsAllocationAlgorithm.class);
	private WhatIfEngineInstance ei;
	public static final String NAME = "EQUAL_PARTITIONING_ON_LEAFS_ALLOCATION_ALGORITHM";
	private EqualPartitioningOnLeafsAllocationAlgorithmDataManager dataManager;
	private String lastQuery;// variable used in the tes cases

	public EqualPartitioningOnLeafsAllocationAlgorithm() {
	}

	public EqualPartitioningOnLeafsAllocationAlgorithm(WhatIfEngineInstance ei) {
		this();
		this.ei = ei;
		initAlgorithm();
	}

	public void initAlgorithm() {
		dataManager = new EqualPartitioningOnLeafsAllocationAlgorithmDataManager(ei.getWriteBackManager().getRetriver(), ei.getDataSource());
	}

	@Override
	protected String getMonitorName() {
		return "SpagoBIWhatIfEngine/it.eng.spagobi.engines.whatif.model.transform.EqualPartitioningOnLeafsAllocationAlgorithm";
	}

	public String getName() {
		return NAME;
	}

	public void setProperties(Map<String, Object> properties) {
		if (properties != null) {
			this.ei = (WhatIfEngineInstance) properties.get(ENGINEINSTANCE_PROPERTY);
		}
		initAlgorithm();
	}

	@Override
	protected void persistInternal(SpagoBICellWrapper cell, Object oldValue, Object newValue, Connection connection, Integer version) throws Exception {
		Double value = ((Number) newValue).doubleValue() / getCellLeafs(cell);
		logger.debug("The value of the leafs is " + value);
		lastQuery = dataManager.executeUpdate(cell.getMembers(), value, connection, version);
	}

	@Override
	protected double applyBelow(SpagoBICellSetWrapper cellSetWrapper, SpagoBICellWrapper editedCell, SpagoBICellWrapper wrappedCell, double newValue, double oldValue)
			throws Exception {
		return (newValue / getCellLeafs(editedCell)) * getCellLeafs(wrappedCell);
	}

	private long getCellLeafs(SpagoBICellWrapper cell) throws Exception {
		Member[] membersCell = cell.getMembers();

		Long cellLeafsCount = cell.getLeafsCount();

		if (cellLeafsCount == null) {
			cellLeafsCount = dataManager.getLeafs(membersCell);
			cell.setLeafsCount(cellLeafsCount);
		}

		return cellLeafsCount;
	}

	public String getLastQuery() {
		return lastQuery;
	}

	public void setLastQuery(String lastQuery) {
		this.lastQuery = lastQuery;
	}

	public EqualPartitioningOnLeafsAllocationAlgorithmDataManager getDataManager() {
		return dataManager;
	}

	public boolean canOverridePrevious() {
		return true;
	}

	public boolean isInMemory() {
		return true;
	}

	public boolean isPersistent() {
		return true;
	}

}
