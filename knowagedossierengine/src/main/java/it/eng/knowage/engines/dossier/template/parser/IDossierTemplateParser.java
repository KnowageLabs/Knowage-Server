package it.eng.knowage.engines.dossier.template.parser;

import it.eng.knowage.engines.dossier.template.DossierTemplate;
import it.eng.knowage.engines.dossier.template.parser.xml.DossierTemplateXMLParserException;

public interface IDossierTemplateParser {
	DossierTemplate parse(String template) throws  DossierTemplateXMLParserException;
}
