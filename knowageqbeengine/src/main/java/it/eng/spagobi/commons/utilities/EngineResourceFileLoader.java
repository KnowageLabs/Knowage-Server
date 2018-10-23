package it.eng.spagobi.commons.utilities;

import java.io.File;

import it.eng.spagobi.engines.qbe.QbeEngineConfig;

/**
 *
 * @author dpirkovic
 *
 */

public class EngineResourceFileLoader implements IFileLoader {

	private final String engineResourcePath;

	public EngineResourceFileLoader() {
		engineResourcePath = QbeEngineConfig.getInstance().getEngineResourcePath();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public File load(String relativefilePath) {

		String filePath = engineResourcePath + File.separator + relativefilePath;
		return new File(filePath);
	}

}
