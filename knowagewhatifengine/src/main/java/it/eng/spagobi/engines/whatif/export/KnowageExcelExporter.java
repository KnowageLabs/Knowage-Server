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
package it.eng.spagobi.engines.whatif.export;

import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.ui.poi.ExcelExporter;
import org.pivot4j.ui.table.TableRenderContext;

import it.eng.spagobi.utilities.messages.EngineMessageBundle;

public class KnowageExcelExporter extends ExcelExporter {

	public static transient Logger logger = Logger.getLogger(KnowageExcelExporter.class);

	private Locale locale;

	public KnowageExcelExporter(OutputStream out, Locale locale) {
		super(out);
		this.locale = locale;
	}

	protected Locale getLocale() {
		return locale;
	}

	@Override
	public void endRender(TableRenderContext context) {
		Workbook workbook = this.getWorkbook();
		manageSlicers(workbook, context);
		super.endRender(context);
	}

	private void manageSlicers(Workbook workbook, TableRenderContext context) {
		PivotModel model = context.getModel();
		ChangeSlicer changeSlicer = model.getTransform(ChangeSlicer.class);
		List<Member> slicers = changeSlicer.getSlicer();
		if (!slicers.isEmpty()) {
			logger.debug("Some slicers found, adding them into the XLS file ...");
			// at this time, members are sorted by selection order. We sort them by unique name
			slicers.sort(Comparator.comparing(Member::getUniqueName));
			addSlicersSheet(workbook, slicers);
		}
	}

	private void addSlicersSheet(Workbook workbook, List<Member> slicers) {
		String slicersSheetName = EngineMessageBundle.getMessage("olap.export.xls.slicers.sheet.name", getLocale());
		Sheet sheet = workbook.createSheet(slicersSheetName);
		int rowIndex = 0;
		for (Member member : slicers) {
			Row row = sheet.createRow(rowIndex++);

			logger.debug("Adding row for slicer member " + member.getUniqueName());

			String hierarchyName = member.getHierarchy().getName();
			Cell hierarchyNameCell = row.createCell(0);
			hierarchyNameCell.setCellValue(hierarchyName);
			hierarchyNameCell.setCellStyle(getHeaderCellStyle());

			String memberStrippedName = stripHierarchyFromMemberName(member);
			Cell memberNameCell = row.createCell(1);
			memberNameCell.setCellValue(memberStrippedName);
			memberNameCell.setCellStyle(getValueCellStyle());
		}

		logger.debug("Auto-sizing the columns ...");
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);

		logger.debug("Merging cells with same hierarchy ...");
		mergeCellsWithSameHierarchyName(sheet);
	}

	private void mergeCellsWithSameHierarchyName(Sheet sheet) {
		int first = 0;
		int last = 0;
		int i = 0;
		while (i < sheet.getPhysicalNumberOfRows()) {
			first = i;
			last = i;
			for (int j = i + 1; j < sheet.getPhysicalNumberOfRows(); j++) {
				if (sheet.getRow(i).getCell(0).toString().equals(sheet.getRow(j).getCell(0).toString())) {
					last = j;
				} else {
					break;
				}
			}
			if (last > first) {
				CellRangeAddress cellRangeAddress = new CellRangeAddress(first, last, 0, 0);
				sheet.addMergedRegion(cellRangeAddress);
			}
			i = last + 1;
		}

	}

	private String stripHierarchyFromMemberName(Member member) {
		logger.debug("IN: member unique name : " + member.getUniqueName());
		String toReturn = member.getUniqueName().replaceAll("^\\[" + Pattern.quote(member.getHierarchy().getName()) + "\\]\\.", "");
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

}
