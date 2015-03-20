/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.transformers;

import it.eng.spagobi.tools.importexport.ITransformer;

import org.apache.log4j.Logger;

public class TransformerFrom3_2_0To3_3_0 implements ITransformer {

	static private Logger logger = Logger.getLogger(TransformerFrom3_2_0To3_3_0.class);

	public byte[] transform(byte[] content, String pathImpTmpFolder, String archiveName) {
		logger.debug("IN");
		logger.debug("No import database changes from 3.2 to 3.3 version");
		logger.debug("OUT");
		return content;
	}




}
