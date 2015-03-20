/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.graph.validator;


import it.eng.qbe.model.structure.IModelEntity;

import java.util.Set;

import org.jgrapht.Graph;

/**
 * This is a fake implementation of the validator.. it returns always true.
 * 
 * 
 * @authors
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class AlwaysValidValidator extends AbstractGraphValidator {

	/**
	 * Returns always true
	 */
	public boolean validate(Graph G,  Set<IModelEntity> unjoinedEntities) {

		return true;
	}

}
