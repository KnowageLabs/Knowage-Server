/**
 *
 */
package it.eng.spagobi.tools.dataset.common.datawriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import it.eng.spagobi.commons.utilities.ZipUtility;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

/**
 * @author Dragan Pirkovic
 *
 */
public class FileManager {

	private String directoryName;
	private String path;
	private Path directory;

	/**
	 * @param directoryName
	 * @param path
	 */
	public FileManager(String directoryName, String path) {
		this.directoryName = directoryName;
		this.path = path;
		try {
			this.directory = Files.createTempDirectory(this.directoryName + "_", new FileAttribute[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param fileName
	 * @param datastore
	 */
	public void createFile(String fileName, IDataStore datastore) {
		new CSVFileDataWriter(this.directory, fileName).write(datastore);
		createZip();

	}

	private void createZip() {
		ZipUtility zipUtility = new ZipUtility(directory.toString());
		zipUtility.generateFileList(directory.toFile());
		zipUtility.zipIt(directory.toString(), path + "/" + "data");
	}

	/**
	 * @return the directoryName
	 */
	public String getDirectoryName() {
		return directoryName;
	}

	/**
	 * @param directoryName
	 *            the directoryName to set
	 */
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the directory
	 */
	public Path getDirectory() {
		return directory;
	}

	/**
	 * @param directory
	 *            the directory to set
	 */
	public void setDirectory(Path directory) {
		this.directory = directory;
	}

}
