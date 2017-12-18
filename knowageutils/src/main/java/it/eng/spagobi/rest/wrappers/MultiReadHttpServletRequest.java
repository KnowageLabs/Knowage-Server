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
package it.eng.spagobi.rest.wrappers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
	private ByteArrayOutputStream cachedBytes;

	public MultiReadHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (cachedBytes == null)
			cacheInputStream();

		return new CachedServletInputStream();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	private void cacheInputStream() throws IOException {
		/*
		 * Cache the inputstream in order to read it multiple times. For convenience, I use apache.commons IOUtils
		 */
		cachedBytes = new ByteArrayOutputStream();
		IOUtils.copy(super.getInputStream(), cachedBytes);
	}

	/* An inputstream which reads the cached request body */
	public class CachedServletInputStream extends ServletInputStream {
		private final ByteArrayInputStream input;

		public CachedServletInputStream() {
			/* create a new input stream from the cached request body */
			input = new ByteArrayInputStream(cachedBytes.toByteArray());
		}

		@Override
		public int read() throws IOException {
			return input.read();
		}
	}
}