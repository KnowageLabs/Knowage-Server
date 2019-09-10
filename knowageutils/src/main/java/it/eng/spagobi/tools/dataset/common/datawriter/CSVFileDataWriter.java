/**
 *
 */
package it.eng.spagobi.tools.dataset.common.datawriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

/**
 * @author Dragan Pirkovic
 *
 */
public class CSVFileDataWriter implements IDataWriter {

	private final Path directory;
	private final String fileName;
	String extension = ".csv";

	/**
	 * @param directory
	 * @param fileName
	 */
	public CSVFileDataWriter(Path directory, String fileName) {
		this.directory = directory;
		this.fileName = fileName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter#write(it.eng.spagobi.tools.dataset.common.datastore.IDataStore)
	 */
	@Override
	public Object write(IDataStore dataStore) {
		byte[] bytes = (byte[]) new CSVByteDataWriter().write(dataStore);
		Path f = null;
		try {
			f = Files.createTempFile(directory, fileName + "_", extension, new FileAttribute[0]);

			Files.write(f, bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}

}
