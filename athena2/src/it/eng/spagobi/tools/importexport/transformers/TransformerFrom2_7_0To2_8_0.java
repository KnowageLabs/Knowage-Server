/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.transformers;

import it.eng.spagobi.tools.importexport.ITransformer;

import org.apache.log4j.Logger;

public class TransformerFrom2_7_0To2_8_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom2_7_0To2_8_0.class);

	public byte[] transform(byte[] content, String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		// no changes
		logger.debug("OUT");
		return content;
	}

}
