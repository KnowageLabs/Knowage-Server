/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.sdk.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * Replaces ManagedMemoryDataSource from Axis 1.4.
 */
public class KnowageSoapDataSource implements DataSource {

	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final String contentType;
	private final String name;

	public KnowageSoapDataSource(InputStream inputStream, OutputStream outputStream, String contentType, String name) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.contentType = contentType;
		this.name = name;
	}

	public KnowageSoapDataSource(InputStream inputStream, String name) {
		this(inputStream, null, null, name);
	}

	public KnowageSoapDataSource(InputStream inputStream) {
		this(inputStream, null, null, null);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return outputStream;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getName() {
		return name;
	}

}
