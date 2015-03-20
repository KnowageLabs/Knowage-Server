/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.crosstable.exporter;

import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.qbe.crosstable.CrossTab;
import it.eng.spagobi.engines.qbe.crosstable.Node;
import it.eng.spagobi.engines.worksheet.services.export.MeasureFormatter;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;


/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class CrosstabPDFExporter {

	
	private Vector<List<PdfPCell>> dataMatrix;
	//private static final Color headersFontColor = new Color(55,131,232);
	private static final Color headersBackgroundColor = new Color(200,200,200);
	private static final Color sumBackgroundColor = new Color(238,238,238);
	//private static final Color superSumBackgroundColor = new Color(210,210,210);
	//private static final Color contentBackgroundColor = new Color(255,255,255);
	//private static final Color tableBorderColor = new Color(153,187,232);
	private static final Color cellsBorderColor = new Color(170,170,170);
	private Font cellFont = new Font(Font.HELVETICA, 8);
	private MeasureFormatter measureMetadata;
	
	public static transient Logger logger = Logger.getLogger(CrosstabPDFExporter.class);
	
	
	private DecimalFormat numberFormat;
	
	/**
	 * Builds the table for the crosstab
	 * @param json the JSON representation of the crosstab
	 * @param pdfDocument the pdf document that should contains the crosstab
	 * @param numberFormat the formatter for the numbers
	 * @throws JSONException
	 * @throws BadElementException
	 * @throws DocumentException
	 */
	public void export(JSONObject json, Document pdfDocument, DecimalFormat numberFormat) throws SerializationException, JSONException, BadElementException, DocumentException {
		logger.debug("IN: exporting the crosstab");
		//prepare the crosstab for the export
		CrosstabExporterUtility.calculateDescendants(json);
		JSONObject columnsRoot = (JSONObject) json.get(CrossTab.CROSSTAB_JSON_COLUMNS_HEADERS);
    	JSONArray columnsRootChilds = columnsRoot.getJSONArray(CrossTab.CROSSTAB_NODE_JSON_CHILDS);
		JSONObject rowsRoot = (JSONObject) json.get(CrossTab.CROSSTAB_JSON_ROWS_HEADERS);
		JSONArray rowsRootChilds = rowsRoot.getJSONArray(CrossTab.CROSSTAB_NODE_JSON_CHILDS);
		JSONArray rowHeadersDescription = json.getJSONArray(CrossTab.CROSSTAB_JSON_ROWS_HEADER_TITLE);
		JSONArray data = (JSONArray) json.get(CrossTab.CROSSTAB_JSON_DATA);
		measureMetadata = new MeasureFormatter(json, numberFormat, "##,##0.00");
		this.numberFormat = numberFormat;
		
		//build the matrix for the content
		dataMatrix = new Vector<List<PdfPCell>>();
		buildDataMatrix(data);

		//number of headers lavels
		int rowsDepth = CrosstabExporterUtility.getDepth(rowsRoot);
		int columnsDepth = CrosstabExporterUtility.getDepth(columnsRoot);

		//build the table
		PdfPTable table = new PdfPTable(rowsDepth+dataMatrix.get(0).size());
		
		//build the empty cell on the top left 
		PdfPCell  topLeftCell = new PdfPCell(new Phrase(""));
		topLeftCell.setRowspan(columnsDepth-1);//-1 because of the title of the rows header
		topLeftCell.setColspan(rowsDepth);
		topLeftCell.setBorderColor(Color.WHITE);
		table.addCell(topLeftCell);
		
		List<PdfPCell> cells = new ArrayList<PdfPCell>();

		//builds the headers
		int dataColumnNumber = ((JSONArray) data.get(0)).length();
		cells.addAll(buildColumnsHeader(columnsRootChilds,rowHeadersDescription,dataColumnNumber));
		cells.addAll(buildRowsHeaders(rowsRootChilds));
		
		logger.debug("Addign the content");
		//adds the headers
		for(int i=0; i<cells.size();i++){
			table.addCell(cells.get(i));
		}

		table.setWidthPercentage(100);
		pdfDocument.add(table);
		logger.debug("IN: exported the crosstab");
	}
	

	/**
	 * Build the matrix for the content of the crosstab
	 * @param data
	 * @throws JSONException
	 */
	private void buildDataMatrix(JSONArray data) throws JSONException {
		logger.debug("IN: building the crosstab content");
		PdfPCell cell;
		for (int i = 0; i < data.length(); i++) {
			JSONArray array = (JSONArray) data.get(i);
			List<PdfPCell> dataRow = new ArrayList<PdfPCell>();
			for (int j = 0; j < array.length(); j++) {
				String text = (String) array.get(j);
				//Check if a cell is a sum
				if(text.length()>5 && text.substring(0, 5).equals("[sum]")){
					text= text.substring(5);
					cell = new PdfPCell(new Phrase (getFormattedString(text,i,j), cellFont));
					cell.setBackgroundColor(sumBackgroundColor);
				} else{
					cell = new PdfPCell(new Phrase (getFormattedString(text,i,j), cellFont));
				}
				
				cell.setBorderColor(cellsBorderColor);
				dataRow.add(cell);				
			}
			dataMatrix.add(dataRow);
		}
		logger.debug("OUT: built the crosstab content");
	}

	/**
	 * Builds the row headers. This method performs a depth first visit
	 * of the row headers tree
	 * @param siblings: a level (L) of headers
	 * @return the cells from level L to the leafs
	 * @throws JSONException
	 * @throws BadElementException
	 */
	private List<PdfPCell> buildRowsHeaders(JSONArray siblings) throws JSONException, BadElementException {
		JSONArray childs;
		List<PdfPCell> rowNodes = new ArrayList<PdfPCell>(); 

		//For every node of the level..
		for (int i = 0; i < siblings.length(); i++) {
			JSONObject aNode = (JSONObject) siblings.get(i);
			String text = (String) aNode.opt(Node.CROSSTAB_NODE_JSON_DESCRIPTION);
			if (text == null) {
				// in case of calculated fields
				text = (String) aNode.get(CrossTab.CROSSTAB_NODE_JSON_KEY);
			}
			
			int descendants = aNode.getInt(CrosstabExporterUtility.CROSSTAB_JSON_DESCENDANTS_NUMBER);
		    
			PdfPCell cell = new PdfPCell(new Phrase( text, cellFont));
			cell.setBackgroundColor(headersBackgroundColor);
			cell.setBorderColor(cellsBorderColor);
			
			if(descendants>1){
				cell.setRowspan(descendants);
			}
			
			//1) add the node name
			rowNodes.add(cell);
			
			//2) add the child node names
			childs = aNode.optJSONArray(CrossTab.CROSSTAB_NODE_JSON_CHILDS);
		    if (childs != null && childs.length() > 0) {
		    	rowNodes.addAll(buildRowsHeaders(childs));
		    }else{
		    	rowNodes.addAll(dataMatrix.remove(0));
		    }
		}
		return rowNodes;
	}

	/**
	 * Builds cells for the column headers
	 * @param siblings the top level 
	 * @return
	 * @throws JSONException
	 * @throws BadElementException
	 */
	private List<PdfPCell> buildColumnsHeader(JSONArray siblings, JSONArray rowHeadersDescription, int dataColumnNumber) throws JSONException, BadElementException {

		List<PdfPCell> cells = new ArrayList<PdfPCell>();
		
		List<JSONObject> columnNodes = getAllNodes(siblings); 
			
		for (int i = 0; i < columnNodes.size(); i++) {
			//adds the row headers
			if(rowHeadersDescription!=null && i==columnNodes.size()-dataColumnNumber){
				for(int y=0; y<rowHeadersDescription.length(); y++ ){
					String text =  rowHeadersDescription.getString(y);
					PdfPCell cell = new PdfPCell(new Phrase(text,cellFont));
					cell.setBorderColor(cellsBorderColor);
					cell.setBackgroundColor(headersBackgroundColor);
					cells.add(cell);
				}

			}
			JSONObject aNode = (JSONObject) columnNodes.get(i);
			String text = (String) aNode.opt(Node.CROSSTAB_NODE_JSON_DESCRIPTION);
			if(text==null || text.equals("")){
				text =  (String) aNode.opt(Node.CROSSTAB_NODE_JSON_KEY);
			}

			
			int descendants = aNode.getInt(CrosstabExporterUtility.CROSSTAB_JSON_DESCENDANTS_NUMBER);
		    
			PdfPCell cell = new PdfPCell(new Phrase(text,cellFont));
			cell.setBorderColor(cellsBorderColor);
			cell.setBackgroundColor(headersBackgroundColor);

			
			if(descendants>1){
				cell.setColspan(descendants);
			}
			cells.add(cell);
		}
		
		return cells;
	}

	/**
	 * Performs a breadth first visit of the tree..
	 * @param siblings
	 * @return
	 * @throws JSONException
	 */
	private List<JSONObject> getAllNodes(JSONArray siblings) throws JSONException{
		JSONArray childs;
		List<JSONObject> childLevelNodes = new ArrayList<JSONObject>();
		List<JSONObject> levelNodes = new ArrayList<JSONObject>();
		
		for (int i = 0; i < siblings.length(); i++) {
			JSONObject aNode = (JSONObject) siblings.get(i);
			levelNodes.add(aNode);
			childs = aNode.optJSONArray(CrossTab.CROSSTAB_NODE_JSON_CHILDS);
		    if (childs != null && childs.length() > 0) {
		    	childLevelNodes.addAll(getAllNodes(childs));
		    }
		}

		levelNodes.addAll(childLevelNodes);
		
		return levelNodes;
	}
	
	private String getFormattedString(String string, int i, int j) {
		try{
			Float f = new Float(string);
			if(measureMetadata!=null){
				return measureMetadata.getFormat(f, i, j);
			}			
			return numberFormat.format(f);
		}catch (Exception e) {
			return string;
		}
	}

	


}
