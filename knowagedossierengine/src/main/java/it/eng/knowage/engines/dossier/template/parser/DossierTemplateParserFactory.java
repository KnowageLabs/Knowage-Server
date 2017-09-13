package it.eng.knowage.engines.dossier.template.parser;

import java.util.EnumMap;

import org.apache.log4j.Logger;

import it.eng.knowage.engines.dossier.template.parser.xml.DossierTemplateXMLParser;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class DossierTemplateParserFactory {
	
	static private Logger logger = Logger.getLogger(DossierTemplateParserFactory.class);
	static private EnumMap<DossierTemplateType,Class<? extends IDossierTemplateParser>> dossierTemplateTypes = new EnumMap<>(DossierTemplateType.class);
	
	public DossierTemplateParserFactory() {
		dossierTemplateTypes.put(DossierTemplateType.XML_STRING, DossierTemplateXMLParser.class);
	}
	
	public IDossierTemplateParser getDossierTemplateParser(DossierTemplateType dossierTemplateType){
		
		
		try {
			return dossierTemplateTypes.get(dossierTemplateType).newInstance();
		} catch (InstantiationException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		} catch (IllegalAccessException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		}
	}

}
