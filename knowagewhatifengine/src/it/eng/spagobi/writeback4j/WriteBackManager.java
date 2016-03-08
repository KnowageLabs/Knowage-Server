/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.writeback4j.mondrian.MondrianDriver;
import it.eng.spagobi.writeback4j.mondrian.MondrianSchemaRetriver;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 *         Manager of the writeback..
 */
public class WriteBackManager {
	ISchemaRetriver retriver;

	public WriteBackManager(String editCubeName, IXmlaDriver xmlaDriver) throws SpagoBIEngineException {
		retriver = new MondrianSchemaRetriver((MondrianDriver) xmlaDriver, editCubeName);

	}

	public ISchemaRetriver getRetriver() {
		return retriver;
	}

}
