/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it) 
 */

package it.eng.spagobi.pivot4j.mdx;

import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.OlapDataSource;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

public class MdxQueryExecutor {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(MdxQueryExecutor.class);

	private final OlapDataSource dataSource;

	public MdxQueryExecutor(OlapDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public OlapDataSource getOlapDataSource() {
		return this.dataSource;
	}

	public CellSet executeMdx(String mdx, SpagoBIPivotModel pivotModel) {
		logger.debug("IN: MDX = [" + mdx + "]");
		CellSet toReturn = null;
		try {
			// save the currentMdx query (for the current displayed model)
			String previous = pivotModel.getCurrentMdx();
			// set the new Mdx query to execute
			pivotModel.setMdx(mdx);
			// get the result as CellSet (this will apply pending
			// transformations)
			toReturn = pivotModel.getCellSet();
			// restore the previous situation
			pivotModel.setMdx(previous);
		} catch (Exception e) {
			logger.error("Error while executing MDX [" + mdx + "]", e);
			throw new SpagoBIEngineRuntimeException("Error while executing MDX [" + mdx + "]", e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	public Object getValueForTuple(Member[] members, Cube cube, SpagoBIPivotModel pivotModel) {
		logger.debug("IN: tuple = [" + members + "]");
		Object toReturn = null;
		try {
			Cell cell = this.getCellForTuple(members, cube, pivotModel);
			toReturn = cell.getValue();
		} catch (Exception e) {
			logger.error("Error while getting value for tuple [" + members + "]", e);
			throw new SpagoBIEngineRuntimeException("Error while getting value for tuple [" + members + "]", e);
		} finally {
			logger.debug("OUT: returning [" + toReturn + "]");
		}
		return toReturn;
	}

	public CellSet getCellSetForTuple(Member[] members, Cube cube, SpagoBIPivotModel pivotModel) {
		logger.debug("IN: tuple = [" + members + "]");
		CellSet toReturn = null;
		try {
			MDXQueryBuilder builder = new MDXQueryBuilder();
			String mdx = builder.getMDXForTuple(members, cube);
			logger.debug("Executing MDX : " + mdx + " ...");
			toReturn = this.executeMdx(mdx, pivotModel);
			logger.debug("MDX query executed");
		} catch (Exception e) {
			logger.error("Error while getting cellset for tuple [" + members + "]", e);
			throw new SpagoBIEngineRuntimeException("Error while getting cellset for tuple [" + members + "]", e);
		} finally {
			logger.debug("OUT: returning [" + toReturn + "]");
		}
		return toReturn;
	}

	public Cell getCellForTuple(Member[] members, Cube cube, SpagoBIPivotModel pivotModel) {
		logger.debug("IN: tuple = [" + members + "]");
		Cell toReturn = null;
		try {
			CellSet cellSet = this.getCellSetForTuple(members, cube, pivotModel);
			toReturn = cellSet.getCell(0);
		} catch (Exception e) {
			logger.error("Error while getting cell for tuple [" + members + "]", e);
			throw new SpagoBIEngineRuntimeException("Error while getting cell for tuple [" + members + "]", e);
		} finally {
			logger.debug("OUT: returning [" + toReturn + "]");
		}
		return toReturn;
	}

}
