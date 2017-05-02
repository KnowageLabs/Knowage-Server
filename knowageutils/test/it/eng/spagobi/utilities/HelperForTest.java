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
package it.eng.spagobi.utilities;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class HelperForTest {

	private HelperForTest() {
	}

	public static String readFile(String fileName, Class<?> clazz) throws IOException {
		InputStream in = clazz.getResourceAsStream(fileName);
		String res= IOUtils.toString(in, "UTF-8");
		in.close();
		return res;
	}

	public static boolean all(boolean[] done) {
		for (int i = 1; i < done.length; i++) {
			if (done[i] != done[i - 1]) {
				return false;
			}
		}
		return true;
	}

	public static byte[] getFileContent(String fileName, Class<?> clazz) throws IOException {
		InputStream in = clazz.getResourceAsStream(fileName);
		byte[] res= IOUtils.toByteArray(in);
		in.close();
		return res;
	}

}
