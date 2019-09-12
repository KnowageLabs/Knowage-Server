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
		String dataStoreString = (String) new CSVDataWriter().write(dataStore);
		byte[] bytes = dataStoreString.getBytes();
		Path f = null;
		try {
			f = Files.createTempFile(directory, fileName + "_", extension, new FileAttribute[0]);

			Files.write(f, bytes);
		} catch (IOException e) {

		}
		return f;
	}

}
