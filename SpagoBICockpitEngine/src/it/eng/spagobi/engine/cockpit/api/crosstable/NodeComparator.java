/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.cockpit.api.crosstable;

import java.util.Comparator;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */

public class NodeComparator implements Comparator<Node> {

	private final int direction;

	public NodeComparator(int direction) {
		this.direction = direction;
	}

	@Override
	public int compare(Node arg0, Node arg1) {
		try {
			Float arg0Value = new Float(arg0.getValue());
			Float arg1Value = new Float(arg1.getValue());
			return direction * arg0Value.compareTo(arg1Value);
		} catch (Exception e) {
			// if its not possible to convert the values in float, consider them
			// as strings
			return direction * arg0.getValue().compareTo(arg1.getValue());
		}
	}

	public int getDirection() {
		return direction;
	}

}
