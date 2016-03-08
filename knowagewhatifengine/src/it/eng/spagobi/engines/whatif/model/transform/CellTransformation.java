/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * An instance of this class represents a transformation applied to a particular Cell in a CellSet
 * 
 * 
 * @author Zerbetto Davide (davide.zerbetto@eng.it) 
 */

package it.eng.spagobi.engines.whatif.model.transform;

import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;

public class CellTransformation {

	private SpagoBICellWrapper cell;
	private IAllocationAlgorithm algorithm;
	private Object newValue;
	private Object oldValue;

	public CellTransformation(Object newValue, Object oldValue, SpagoBICellWrapper cell, IAllocationAlgorithm algorithm) {
		this.newValue = newValue;
		this.cell = cell;
		this.algorithm = algorithm;
		this.oldValue = oldValue;
	}

	public SpagoBICellWrapper getCell() {
		return this.cell;
	}

	public void setMembers(SpagoBICellWrapper cell) {
		this.cell = cell;
	}

	public IAllocationAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(IAllocationAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	@Override
	public String toString() {
		return "CellTransformation [cell=" + cell + ", algorithm=" + algorithm.getName()
				+ ", newValue=" + newValue + ", oldValue=" + oldValue + "]";
	}

}
