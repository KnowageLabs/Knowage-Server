/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it) 
 */

package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.transform.CellRelation;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.olap4j.Position;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public abstract class AbstractAllocationAlgorithm implements IAllocationAlgorithm {

	private static Logger logger = Logger.getLogger(AbstractAllocationAlgorithm.class);
	public static final String ENGINEINSTANCE_PROPERTY = "EngineInstance";
	public static final String USEINCLAUSE_PROPERTY = "UseInClause";

	public void apply(SpagoBICellWrapper cell, Object oldValue,
			Object newValue, SpagoBICellSetWrapper cellSetWrapper) {

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");
		try {
			totalTimeMonitor = MonitorFactory.start(getMonitorName() + ".apply.totalTime");
			this.applyInternal(cell, oldValue, newValue, cellSetWrapper);
		} catch (Exception e) {
			errorHitsMonitor = MonitorFactory.start(getMonitorName() + ".apply.errorHits");
			errorHitsMonitor.stop();
			logger.error("Error while applying transformation", e);
			throw new SpagoBIRuntimeException("Error while applying transformation", e);
		} finally {
			if (totalTimeMonitor != null) {
				totalTimeMonitor.stop();
			}
			logger.debug("OUT");
		}

	}

	protected void applyInternal(SpagoBICellWrapper cell, Object oldValue,
			Object newValue, SpagoBICellSetWrapper cellSetWrapper) throws Exception {

		// Iteration over a two-axis query
		for (Position axis_0_Position : cellSetWrapper.getAxes()
				.get(0).getPositions()) {

			for (Position axis_1_Position : cellSetWrapper.getAxes()
					.get(1).getPositions()) {

				SpagoBICellWrapper wrappedCell = (SpagoBICellWrapper) cellSetWrapper.getCell(axis_0_Position, axis_1_Position);

				CellRelation relation = wrappedCell.getRelationTo(cell);
				Double newDoubleValue = null;
				switch (relation) {
				case EQUAL:
					newDoubleValue = applyEqual(cellSetWrapper, cell, wrappedCell, ((Number) newValue).doubleValue(), ((Number) oldValue).doubleValue());
					wrappedCell.setValue(newDoubleValue);
					break;
				case ABOVE:
					// in case we modified a cell that had no value, we consider
					// 0 as previous value
					if (oldValue == null) {
						oldValue = 0;
					}
					newDoubleValue = applyAbove(cellSetWrapper, cell, wrappedCell, ((Number) newValue).doubleValue(), ((Number) oldValue).doubleValue());
					wrappedCell.setValue(newDoubleValue);
					break;
				case BELOW:
					// in case the cell is below and doesn't contain a value, we
					// don't modify it
					if (wrappedCell.isNull() || wrappedCell.isError() || wrappedCell.isEmpty()) {
						continue;
					}
					newDoubleValue = applyBelow(cellSetWrapper, cell, wrappedCell, ((Number) newValue).doubleValue(), ((Number) oldValue).doubleValue());
					wrappedCell.setValue(newDoubleValue);
					break;
				default:
					break;
				}

			}
		}
	}

	/**
	 * Apply the algorithm to the edited cell
	 * 
	 * @param cellSetWrapper
	 *            the cell set wrapper
	 * @param cell
	 *            the edited cell
	 * @param wrappedCell
	 *            the analyzed cell
	 * @param newValue
	 *            the new value
	 * @param oldValue
	 *            the old value
	 * @return
	 * @throws Exception
	 */
	protected double applyEqual(SpagoBICellSetWrapper cellSetWrapper, SpagoBICellWrapper cell, SpagoBICellWrapper wrappedCell, double newValue, double oldValue) throws Exception {
		return newValue;
	}

	/**
	 * Apply the algorithm to the cell ancestors of wrappedCell
	 * 
	 * @param cellSetWrapper
	 *            the cell set wrapper
	 * @param cell
	 *            the edited cell
	 * @param wrappedCell
	 *            the analyzed cell
	 * @param newValue
	 *            the new value
	 * @param oldValue
	 *            the old value
	 * @return
	 * @throws Exception
	 */
	protected double applyAbove(SpagoBICellSetWrapper cellSetWrapper, SpagoBICellWrapper cell, SpagoBICellWrapper wrappedCell, double newValue, double oldValue) throws Exception {
		return wrappedCell.getDoubleValue() + newValue - oldValue;
	}

	/**
	 * Apply the algorithm to the cell children of wrappedCell
	 * 
	 * @param cellSetWrapper
	 *            the cell set wrapper
	 * @param cell
	 *            the edited cell
	 * @param wrappedCell
	 *            the analyzed cell
	 * @param newValue
	 *            the new value
	 * @param oldValue
	 *            the old value
	 * @return
	 * @throws Exception
	 */
	protected abstract double applyBelow(SpagoBICellSetWrapper cellSetWrapper, SpagoBICellWrapper cell, SpagoBICellWrapper wrappedCell, double newValue, double oldValue)
			throws Exception;

	public void persist(SpagoBICellWrapper cell, Object oldValue, Object newValue, Connection connection, Integer version) throws Exception {

		Monitor totalTimeMonitor = null;
		Monitor errorHitsMonitor = null;

		logger.debug("IN");
		try {
			totalTimeMonitor = MonitorFactory.start(getMonitorName() + ".persist.totalTime");
			this.persistInternal(cell, oldValue, newValue, connection, version);
		} catch (Exception e) {
			errorHitsMonitor = MonitorFactory.start(getMonitorName() + ".persist.errorHits");
			errorHitsMonitor.stop();
			throw e;
		} finally {
			if (totalTimeMonitor != null) {
				totalTimeMonitor.stop();
			}
			logger.debug("OUT");
		}

	}

	/**
	 * Internal method that implement the logic to persist the data
	 * 
	 * @param cell
	 * @param oldValue
	 * @param newValue
	 * @param connection
	 * @param version
	 * @throws Exception
	 */
	protected abstract void persistInternal(SpagoBICellWrapper cell, Object oldValue, Object newValue, Connection connection, Integer version) throws Exception;

	/**
	 * Get the id of the monitor for performance analysis
	 * 
	 * @return
	 */
	protected abstract String getMonitorName();

}
