package it.eng.spagobi.engines.qbe.query.formula.DAO.Impl;

import java.io.File;

import it.eng.spagobi.commons.utilities.ClassFileLoader;
import it.eng.spagobi.commons.utilities.EngineResourceFileLoader;
import it.eng.spagobi.commons.utilities.IFileLoader;
import it.eng.spagobi.engines.qbe.query.formula.FormulaConfig;
import it.eng.spagobi.engines.qbe.query.formula.FormulaConfigConstants;

public class FormulaFileRetriver {

	IFileLoader defaultFileLoader;
	IFileLoader activeFileLoader;
	FormulaConfig formulaConfig;

	public FormulaFileRetriver() {
		activeFileLoader = new EngineResourceFileLoader();
		defaultFileLoader = new ClassFileLoader();
		formulaConfig = new FormulaConfig();
	}

	public File getFormulaFile() {
		File file = null;
		file = getActiveFile();
		if (file == null || !file.exists()) {
			file = getDefaultFile();
		}
		return file;
	}

	private File getActiveFile() {
		File file = null;
		String activeFileName = formulaConfig.getProperty(FormulaConfigConstants.ACTIVE_FORMULA_FILE);
		if (activeFileName != null) {
			return activeFileLoader.load(activeFileName);
		}
		return file;
	}

	private File getDefaultFile() {
		File file = null;
		String defaultFileName = formulaConfig.getProperty(FormulaConfigConstants.DEFAULT_FORMULA_FILE);
		if (defaultFileName != null) {
			return defaultFileLoader.load(defaultFileName);
		}
		return file;
	}

}
