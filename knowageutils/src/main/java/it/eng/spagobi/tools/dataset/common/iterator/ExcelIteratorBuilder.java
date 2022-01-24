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

import java.nio.file.Path;

import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public final class ExcelIteratorBuilder {

	private IMetaData metadata;
	private String fileType;
	private int sheetNumber;
	private int initialRow;
	private Path filePath;

	private ExcelIteratorBuilder() {
		super();
	}

	public static ExcelIteratorBuilder newBuilder() {
		return new ExcelIteratorBuilder();
	}

	public ExcelIteratorBuilder metadata(IMetaData metadata) {
		this.metadata = metadata;
		return this;
	}

	public ExcelIteratorBuilder fileType(String fileType) {
		this.fileType = fileType;
		return this;
	}

	public ExcelIteratorBuilder initialRow(int initialRow) {
		this.initialRow = initialRow;
		return this;
	}

	public ExcelIteratorBuilder sheetNumber(int sheetNumber) {
		this.sheetNumber = sheetNumber;
		return this;
	}

	public ExcelIteratorBuilder filePath(Path filePath) {
		this.filePath = filePath;
		return this;
	}

	public ExcelIterator build() {
		try {
			return new ExcelIterator(metadata, filePath, fileType, sheetNumber, initialRow);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot build ExcelIterator", e);
		}
	}
}
