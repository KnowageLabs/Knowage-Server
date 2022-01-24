/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.dataset.common.iterator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public abstract class FileIterator implements DataIterator {

	protected final IMetaData metadata;
	protected final FileInputStream inputStream;

	public FileIterator(IMetaData metadata, Path filePath) throws IOException {
		super();
		this.metadata = metadata;
		this.inputStream = new FileInputStream(filePath.toFile());
	}

	@Override
	public void close() {
		try {
			inputStream.close();
		} catch (IOException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	@Override
	public IMetaData getMetaData() {
		return metadata;
	}

}
