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
package it.eng.spagobi.tools.importexportOLD.transformers;

import it.eng.spagobi.tools.importexportOLD.ITransformer;

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
