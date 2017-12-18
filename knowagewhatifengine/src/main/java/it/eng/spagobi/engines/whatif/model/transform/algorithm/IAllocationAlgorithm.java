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
