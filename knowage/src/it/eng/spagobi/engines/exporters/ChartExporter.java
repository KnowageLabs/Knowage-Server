/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

/**
 * 
 * @author giachino (antonella.giachino@eng.it)
 *
 * This class is intended to take the result of a Chart Execution and giveBack an export in other formats
 *
 *
 */

public class ChartExporter
{

	private static transient Logger logger = Logger.getLogger("ChartExporter");
	private static int MAX_NUM_IMG = 5;
	private static String HORIZONTAL_ORIENTATION = "horizontal";

	public static final int MAX_WIDTH = 550;
	public static final int MAX_HEIGHT = 800;

	public File getChartPDF(String uuid, boolean multichart, String orientation)
	throws Exception
	{
		logger.debug("IN");

		File tmpFile;

		try{
			tmpFile = null;
			String dir = System.getProperty("java.io.tmpdir");
			String path = (new StringBuilder(String.valueOf(dir))).append("/").append(uuid).append(".png").toString();
			File dirF = new File(dir);
			tmpFile = File.createTempFile("tempPDFExport", ".pdf", dirF);
			Document pdfDocument = new Document();
			PdfWriter docWriter = PdfWriter.getInstance(pdfDocument, new FileOutputStream(tmpFile));
			//pdfDocument.open();
			if(multichart)
			{
				pdfDocument.open();

				List images = new ArrayList();
				for(int i = 0; i < MAX_NUM_IMG; i++)
				{
					String imgName = (new StringBuilder(String.valueOf(path.substring(0, path.indexOf(".png"))))).append(i).append(".png").toString();
					Image png = Image.getInstance(imgName);
					if(png == null)
					{
						break;
					}
					images.add(png);
				}

				Table table = new Table(images.size());
				for(int i = 0; i < images.size(); i++)
				{
					Image png = (Image)images.get(i);
					if(HORIZONTAL_ORIENTATION.equalsIgnoreCase(orientation))
					{
						Cell pngCell = new Cell(png);
						pngCell.setBorder(0);
						table.setBorder(0);
						table.addCell(pngCell);
					} else
					{
						png.setAlignment(5);
						pdfDocument.add(png);
					}
				}

				pdfDocument.add(table);
			} else
			{
				Image jpg = Image.getInstance(path);
				float height = jpg.getHeight();
				float width = jpg.getWidth();

				// if in need to change layout
				if(width > MAX_WIDTH || height > MAX_HEIGHT){
					changeLayout(pdfDocument, jpg, width, height);
				}

				pdfDocument.open();
				pdfDocument.add(jpg);
			}
			pdfDocument.close();
			docWriter.close();

			logger.debug("OUT");

			return tmpFile;

		} catch(Throwable e) {
			logger.error("An exception has occured", e);
			throw new Exception(e);
		} finally {

			//tmpFile.delete();

		}
	}

	/** check if the image has to be turned or resize
	 * 
	 * @param document
	 * @param jpg
	 * @return if the image has been turned
	 */

	public void changeLayout(Document pdfDocument, Image jpg, float width, float height){
		logger.debug("IN");
		// if width is more than 600 and height is not more it suffices to turn the image of the chart
		if(width > MAX_WIDTH && ! (height > MAX_WIDTH) && !(width > MAX_HEIGHT)){
			pdfDocument.setPageSize(PageSize.LETTER.rotate());
			logger.debug("pdf rotation");
		} 
		else{
			// otherwise the chart needs to be scaled (if width > height also turn!)
			// SCALED WITH turning
			if(width > height){
				pdfDocument.setPageSize(PageSize.LETTER.rotate());
				// calculate wich size is to reduce most:
				float percentageToReduceHeight = 0;
				float percentageToReduceWidth = 0;
				if(height > MAX_WIDTH) {
					percentageToReduceHeight = (height * 100 ) / MAX_WIDTH;
				}
				if(width > MAX_HEIGHT) {
					percentageToReduceWidth = (width * 100 ) / MAX_HEIGHT;
				}
				float percToReduce = percentageToReduceHeight > percentageToReduceWidth ? percentageToReduceHeight : percentageToReduceWidth;
				float percToScale = percToReduce - 100;
				//						float newHeight = height - ((height/100)*percToReduce);
//				float newWidth = width - ((width/100)*percToReduce);
				logger.debug("pdf scale of percentage "+percToScale);
				jpg.scalePercent(percToScale);

				logger.debug("pdf rotation and scaling");
			}
			else{
				// SCALED WITHOUT turning
				// calculate wich size is to reduce most:
				float percentageToReduceHeight = 0;
				float percentageToReduceWidth = 0;
				if(height > MAX_HEIGHT) {
					percentageToReduceHeight = (height * 100 ) / MAX_HEIGHT;
				}
				if(width > MAX_WIDTH) {
					percentageToReduceWidth = (width * 100 ) / MAX_WIDTH;
				}
				float percToReduce = percentageToReduceHeight > percentageToReduceWidth ? percentageToReduceHeight : percentageToReduceWidth;
				float percToScale = percToReduce - 100;
				//						float newHeight = height - ((height/100)*percToReduce);
//				float newWidth = width - ((width/100)*percToReduce);
				logger.debug("pdf scale of percentage "+percToScale);
				jpg.scalePercent(percToScale);
				logger.debug("pdf scaling");
			}
		}
		logger.debug("OUT");
	}



}
