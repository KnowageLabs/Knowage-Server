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
public class CsvExportFileManager {

	private String directoryName;
	private String path;
	private Path directory;
	private String zipFileName;

	/**
	 * @param directoryName
	 * @param path
	 */
	public CsvExportFileManager(String directoryName, String path) {
		this.directoryName = directoryName;
		this.path = path;
		try {
			this.directory = Files.createTempDirectory(this.directoryName + "_", new FileAttribute[0]);
		} catch (IOException e) {

		}
	}

	/**
	 * @param label
	 * @param resourcePath
	 * @param zipFileName
	 */
	public CsvExportFileManager(String directoryName, String path, String zipFileName) {
		this.directoryName = directoryName;
		this.path = path;
		try {
			this.directory = Files.createTempDirectory(this.directoryName + "_", new FileAttribute[0]);
		} catch (IOException e) {

		}

		this.zipFileName = zipFileName;
	}

	/**
	 * @param fileName
	 * @param datastore
	 */
	public void createCSVFileAndZipIt(String fileName, IDataStore datastore) {
		new CSVFileDataWriter(this.directory, fileName).write(datastore);
		createZip();

	}

	private void createZip() {
		ZipUtility zipUtility = new ZipUtility(directory.toString());
		zipUtility.generateFileList(directory.toFile());
		zipUtility.zipIt(directory.toString(), path + "/" + zipFileName);
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
