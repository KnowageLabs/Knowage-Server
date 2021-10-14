/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.cockpit.api.export.pdf;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.json.JSONObject;

public class PdfExporter {

	static private Logger logger = Logger.getLogger(PdfExporter.class);

	private final String userUniqueIdentifier;
	private final JSONObject body;

	public PdfExporter(String userUniqueIdentifier, JSONObject body) {
		super();
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.body = body;
	}

	public byte[] getBinaryData() {
		try (PDDocument document = new PDDocument()) {
			PDPage blankPage = new PDPage();
			document.addPage(blankPage);
			document.save("BlankPage.pdf");
			document.close();
			return null;
		} catch (Exception e) {
			return null;
		}
	}

}
