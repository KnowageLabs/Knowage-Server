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
package it.eng.spagobi.engines.qbe.exporter;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;

public class QbeXLSXExporter extends QbeXLSExporter {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QbeXLSXExporter.class);

	public QbeXLSXExporter(DataIterator iterator, Locale locale) {
		super(iterator, locale);
	}

	@Override
	protected Workbook instantiateWorkbook() {
		Workbook workbook = new SXSSFWorkbook();
		return workbook;
	}

	@Override
	protected short getBuiltinFormat(String formatStr) {
		short format = (short) BuiltinFormats.getBuiltinFormat(formatStr);
		return format;
	}

	@Override
	protected int getMaxRows() {
		return SpreadsheetVersion.EXCEL2007.getLastRowIndex();
	}

	@Override
	protected void fillMessageHeader(Sheet sheet, Integer exportLimit) {
		String message = "Query results are exceeding configured threshold, therefore only " + exportLimit + " were exported.";

		Drawing<?> drawing = sheet.createDrawingPatriarch();

		int dx1 = Units.pixelToEMU(25);
		int dy1 = Units.pixelToEMU(25);
		int dx2 = Units.pixelToEMU(800);
		int dy2 = Units.pixelToEMU(1200);

		// Magic numbers just to show a user friendly comment of suitable size
		ClientAnchor anchor = drawing.createAnchor(dx1, dy1, dx2, dy2, 0, 1, getFieldCount(), 5);
		Comment comment = drawing.createCellComment(anchor);

		comment.setAuthor("Knowage");
		comment.setString(new XSSFRichTextString(message));
		comment.setVisible(true);

	}

}
