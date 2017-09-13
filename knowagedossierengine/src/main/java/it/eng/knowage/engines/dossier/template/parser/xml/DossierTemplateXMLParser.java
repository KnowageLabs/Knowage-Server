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
package it.eng.knowage.engines.dossier.template.parser.xml;

import java.io.IOException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.engines.dossier.template.DossierTemplate;
import it.eng.knowage.engines.dossier.template.parser.IDossierTemplateParser;
import it.eng.spagobi.json.Xml;

public class DossierTemplateXMLParser implements IDossierTemplateParser {

	public static transient Logger logger = Logger.getLogger(DossierTemplateXMLParser.class);


	@Override
	public DossierTemplate parse(String template) throws  DossierTemplateXMLParserException{
		
		String jsonTemplateString; 
		ObjectMapper mapper;
		DossierTemplate dossierTemplate;
		JSONObject jsonTemplate;
		try {
			
			template = DossierXmlTemplateAdapter.removeListTags(template);
			 jsonTemplateString = Xml.xml2json(template);
			 jsonTemplate = new JSONObject(jsonTemplateString);
			 jsonTemplate = (JSONObject) jsonTemplate.get("DOSSIER");
			 mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			dossierTemplate = mapper.readValue(jsonTemplate.toString(), DossierTemplate.class);
			return dossierTemplate;
			
			
		} catch (TransformerFactoryConfigurationError | IOException e) {
			logger.error("error parsing XML dossier template",e);
			throw new DossierTemplateXMLParserException("error parsing XML dossier template",e);
		} catch (TransformerException e) {
			logger.error("error parsing XML dossier template",e);
			throw new DossierTemplateXMLParserException(e.getMessage(),e);
		} catch (JSONException e) {
			logger.error("error parsing XML dossier template",e);
			throw new DossierTemplateXMLParserException("error parsing XML dossier template",e);
		}
		
	}

}
