/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/** 
 * @author Zerbetto Davide (davide.zerbetto@eng.it) 
 */

package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;

import java.sql.Connection;
import java.util.Map;

public interface IAllocationAlgorithm {

	public String getName();

	/**
	 * Apply the allocation algorithm to the target cellset, given modified
	 * cell, the old value and the new value. Pay attention to the fact that the
	 * modified cell may refer to a cellset that is DIFFERENT from the target
	 * cellset.
	 * 
	 * @param cell
	 *            The modified cell
	 * @param oldValue
	 *            The old value of the modified cell
	 * @param newValue
	 *            The new value of the modified cell
	 * @param targetCellSet
	 */
	public void apply(SpagoBICellWrapper cell, Object oldValue, Object newValue, SpagoBICellSetWrapper targetCellSet);

	/**
	 * Apply the allocation algorithm to the facts in the db
	 * 
	 * @param cell
	 *            The modified cell
	 * @param oldValue
	 *            The old value of the modified cell
	 * @param newValue
	 *            The new value of the modified cell
	 * @param connection
	 *            The connection to the Db
	 * @param version
	 *            The version where the data should be persisted
	 */
	public void persist(SpagoBICellWrapper cell, Object oldValue, Object newValue, Connection connection, Integer version) throws Exception;

	/**
	 * Set the properties of the algorithm
	 * 
	 * @param properties
	 *            The properties
	 */
	public void setProperties(Map<String, Object> properties);

	/**
	 * If a cell has been edited with this algorithm, it override the previous
	 * modification. So it's not mandatory to persist the previous modifications
	 * 
	 * @return
	 */
	public boolean canOverridePrevious();

	/**
	 * @return true if it's possible to spare the modification in memory (no
	 *         need to persist the data)
	 */
	public boolean isInMemory();

	/**
	 * @return true if it's possible to save the modifications into the db
	 */
	public boolean isPersistent();

}
