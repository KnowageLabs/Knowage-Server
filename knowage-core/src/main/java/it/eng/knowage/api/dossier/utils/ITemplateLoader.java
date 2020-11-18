package it.eng.knowage.api.dossier.utils;

import java.io.File;

import org.apache.log4j.Logger;

public interface ITemplateLoader {

	static Logger logger = Logger.getLogger(ITemplateLoader.class);

	final String PATH_TO_TEMP = File.separator + File.separator + "dossier";

	public default File loadTemplate(String templateName) {
		logger.debug("IN");

		String resourcePath = DossierEngineConfig.getInstance().getEngineResourcePath();
		logger.debug("resourcePath: " + resourcePath);

		String pathToFile = resourcePath + PATH_TO_TEMP + File.separator + templateName;
		logger.debug("pathToFile: " + pathToFile);

		File pptTemplate = new File(pathToFile);

		logger.debug("OUT");
		return pptTemplate;

	}
}
