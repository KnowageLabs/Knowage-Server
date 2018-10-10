package it.eng.spagobi.commons.utilities;

import java.io.File;

public class ClassFileLoader implements IFileLoader {

	private final ClassLoader classLoader;

	public ClassFileLoader() {
		classLoader = this.getClass().getClassLoader();
	}

	@Override
	public File load(String relativefilePath) {
		String filePath = classLoader.getResource(relativefilePath).getPath();
		return new File(filePath);
	}

}
