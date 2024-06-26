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
package it.eng.spagobi.engines.whatif.schema;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.InputStream;
import java.util.Base64;

import org.apache.log4j.Logger;

import mondrian.i18n.LocalizingDynamicSchemaProcessor;
import mondrian.olap.Util;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 *         DATE CONTRIBUTOR/DEVELOPER NOTE 26/03/2013 Davide Zerbetto (davide.zerbetto@eng.it) SpagoBIFilterDynamicSchemaProcessor now extends Andrea Fantappie
 *         (andrea.fantappie@eng.it) LocalizingDynamicSchemaProcessor (instead of FilterDynamicSchemaProcessor) to support i18n
 */
public class SpagoBIFilterDynamicSchemaProcessor extends LocalizingDynamicSchemaProcessor {
	private static Logger logger = Logger.getLogger(SpagoBIFilterDynamicSchemaProcessor.class);

	private static final Base64.Decoder DECODER = Base64.getDecoder();

	Integer indexProgression = null;

	@Override
	public String filter(String schemaUrl, Util.PropertyList connectInfo, InputStream stream) throws Exception {
		logger.debug("IN");
		String originalSchema = super.filter(schemaUrl, connectInfo, stream);
		String modifiedSchema = originalSchema;
		// // search for profile attributes to substitute in schema definition,
		// // identified by $P{attribute_name}
		// indexProgression = Integer.valueOf(0);
		// String att = findProfileAttributeInSchema(originalSchema);
		// while (att != null) {
		// // if value is null I put null, if instead there is no the attribute
		// // name in connectInfo I don't substitute
		// if (connectInfo.get(att) != null) {
		// String attrValueBase64 = connectInfo.get(att);
		// logger.debug("Attribute value in Base64 encoding is " + attrValueBase64);
		// String value = null;
		// try {
		// value = new String(DECODER.decodeBuffer(attrValueBase64), StandardCharsets.UTF_8);
		// } catch (UnsupportedEncodingException e) {
		// logger.error("UTF-8 encoding not supported!!!!!", e);
		// value = new String(DECODER.decodeBuffer(attrValueBase64));
		// }
		// logger.debug("change attribute " + att + " with  [" + value + "]");
		//
		// modifiedSchema = modifiedSchema.replaceAll("\\$\\{" + att + "\\}", value);
		// }
		// att = findProfileAttributeInSchema(modifiedSchema);
		// }
		// Substitutes profile attributes values
		modifiedSchema = substituteProfileValues(originalSchema, connectInfo);
		// Substitutes parameters values (pass modifiedSchema for don't loose profile attribute values)
		modifiedSchema = substituteParameterValues(modifiedSchema, connectInfo);
		logger.debug("OUT: modified schema is:\n" + modifiedSchema);
		return modifiedSchema;
	}

	@Override
	public String processSchema(String schemaUrl, Util.PropertyList connectInfo) throws Exception {
		logger.debug("IN: schemaUrl: " + schemaUrl);
		try {
			if (schemaUrl.startsWith("file:")) {
				schemaUrl = schemaUrl.substring("file:".length());
			}
			File schemaFile = new File(schemaUrl);
			schemaUrl = schemaFile.getAbsolutePath();
			logger.debug("Absolute file path: " + schemaUrl);
			return super.processSchema(schemaUrl, connectInfo);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * return the first profile attribute in schema
	 *
	 * @param schema
	 * @param indexProgression
	 *            . keeps track of the last found index to go always ahead in reading
	 * @return
	 */
	public String findProfileAttributeInSchema(String schema) {
		logger.debug("IN");
		String toReturn = null;
		int index = schema.indexOf("${", indexProgression);
		if (index != -1) {
			int indexEnd = schema.indexOf("}", index);
			toReturn = schema.substring(index + 2, indexEnd);
			indexProgression = new Integer(indexEnd);
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * return the first parameter in schema
	 *
	 * @param schema
	 * @param indexProgression
	 *            . keeps track of the last found index to go always ahead in reading
	 * @return
	 */
	public String findParameterInSchema(String schema) {
		logger.debug("IN");
		String toReturn = null;
		int index = schema.indexOf("$P{", indexProgression);
		if (index != -1) {
			int indexEnd = schema.indexOf("}", index);
			toReturn = schema.substring(index + 3, indexEnd);
			indexProgression = new Integer(indexEnd);
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Search for profile attributes to substitute in schema definition, identified by $P{attribute_name}
	 *
	 * @param originalSchema
	 * @param connectInfo
	 * @return modifiedSchema
	 * @throws Exception
	 */
	private String substituteProfileValues(String originalSchema, Util.PropertyList connectInfo) throws Exception {
		String modifiedSchema = originalSchema;
		indexProgression = Integer.valueOf(0);
		String att = findProfileAttributeInSchema(originalSchema);
		while (att != null) {
			// if value is null I put null, if instead there is no the attribute
			// name in connectInfo I don't substitute
			if (connectInfo.get(att) != null) {
				String attrValueBase64 = connectInfo.get(att);
				logger.debug("Attribute value in Base64 encoding is " + attrValueBase64);
				String value = null;
				value = new String(DECODER.decode(attrValueBase64), UTF_8);
				logger.debug("change attribute " + att + " with  [" + value + "]");

				modifiedSchema = modifiedSchema.replaceAll("\\$\\{" + att + "\\}", value);
			}
			att = findProfileAttributeInSchema(modifiedSchema);
		}
		return modifiedSchema;
	}

	/**
	 * Search for parameters to substitute in schema definition, identified by $P{attribute_name}
	 *
	 * @param originalSchema
	 * @param connectInfo
	 * @return modifiedSchema
	 * @throws Exception
	 */
	private String substituteParameterValues(String originalSchema, Util.PropertyList connectInfo) throws Exception {
		String modifiedSchema = originalSchema;
		indexProgression = Integer.valueOf(0);
		String att = findParameterInSchema(originalSchema);
		while (att != null) {
			// if value is null I put null, if instead there is no the attribute
			// name in connectInfo I don't substitute
			if (connectInfo.get(att) != null) {
				String attrValueBase64 = connectInfo.get(att);
				logger.debug("Parameter value in Base64 encoding is " + attrValueBase64);
				String value = null;
				value = new String(DECODER.decode(attrValueBase64), UTF_8);
				logger.debug("change attribute " + att + " with  [" + value + "]");

				modifiedSchema = modifiedSchema.replaceAll("\\$P\\{" + att + "\\}", value);
			}
			att = findParameterInSchema(modifiedSchema);
		}
		return modifiedSchema;
	}
}