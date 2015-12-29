/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it) 
 */

package it.eng.spagobi.engines.whatif.model.transform;

import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CellTransformationsAnalyzer {

	public static transient Logger logger = Logger.getLogger(CellTransformationsAnalyzer.class);

	public CellTransformationsStack getShortestTransformationsStack(CellTransformationsStack stack) {
		logger.debug("IN");
		CellTransformationsStack toReturn = null;
		try {
			toReturn = cleanOverriddenTransformations(stack);
			toReturn = this.mergeContiguousTransformations(toReturn);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Look for contiguous transformations with the same algorithm on the same
	 * cell and merge them.
	 * 
	 * @param stack
	 *            The original transformation stack
	 * @return Another stack containing a reduced number (if possible) of merged
	 *         transformations
	 */
	public CellTransformationsStack mergeContiguousTransformations(CellTransformationsStack stack) {
		logger.debug("IN");
		CellTransformationsStack toReturn = new CellTransformationsStack();
		try {

			for (int i = 0; i < stack.size(); i++) {
				CellTransformation former = stack.get(i);
				SpagoBICellWrapper formerCell = former.getCell();
				CellTransformation equal = null;
				int counter = 0; // counts the contiguous transformations on the
				// same cell with the same algorithm
				for (int j = i + 1; j < stack.size(); j++) {
					CellTransformation latter = stack.get(j);
					SpagoBICellWrapper latterCell = latter.getCell();
					if (!(former.getAlgorithm().getName().equals(latter.getAlgorithm().getName()))) {
						logger.debug("Contiguous transformations have different algorithm. Cannot merge them");
						break; // in case the 2 transformations are not instance
						// of the same algorithm, we cannot merge them
					}
					if (!(formerCell.getRelationTo(latterCell) == CellRelation.EQUAL)) {
						logger.debug("Contiguous transformations refer to different cells. Cannot merge them");
						break; // in case the 2 transformations refer to
						// different cells, we cannot merge them
					}
					equal = latter;
					logger.debug("Found another tranformation on same cell with same algorithm");
					counter++;
				}
				if (equal != null) {
					CellTransformation newTransformation = new CellTransformation(equal.getNewValue(), former.getOldValue(), former.getCell(), former.getAlgorithm());
					toReturn.add(newTransformation);
				} else {
					toReturn.add(former);
				}
				i += counter; // we have to skip the next transformations
				// specified by counter, since they were already
				// considered
			}

		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Clean the overridable transformations. If in a cell has been applied an
	 * algorithm that can override previous modification this method clean all
	 * the previous ones
	 * 
	 * @param stack
	 * @return
	 */
	public CellTransformationsStack cleanOverriddenTransformations(CellTransformationsStack stack) {
		logger.debug("IN");
		CellTransformationsStack toReturn = new CellTransformationsStack();
		List<Integer> modificationToRemove = new ArrayList<Integer>();

		for (int i = stack.size() - 1; i > 0; i--) {
			CellTransformation former = stack.get(i);
			SpagoBICellWrapper formerCell = former.getCell();
			IAllocationAlgorithm algorithm = former.getAlgorithm();

			if (algorithm.canOverridePrevious() && !modificationToRemove.contains(i)) {

				for (int j = i - 1; j >= 0; j--) {
					CellTransformation latter = stack.get(j);
					SpagoBICellWrapper latterCell = latter.getCell();

					if (formerCell.getRelationTo(latterCell) == CellRelation.EQUAL) {
						logger.debug("Contiguous transformations refer to different cells. Cannot merge them");
						modificationToRemove.add(j);
					}
				}
			}
		}

		for (int i = 0; i < stack.size(); i++) {

			if (!modificationToRemove.contains(i)) {
				toReturn.add(stack.get(i));
			}
		}

		return toReturn;
	}

}
