/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.query;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The Class BasicTemplateBuilder.
 * 
 * @author Andrea Gioia
 */
public class TemplateBuilder {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(TemplateBuilder.class);
	
	String query;
	Vector queryFields;
	Map params;
	File baseTemplateFile;
	
	/** The Constant PN_BAND_WIDTH. */
	public static final String PN_BAND_WIDTH = "bandWidth";	
	
	/** The Constant PN_HEADER_HEIGHT. */
	public static final String PN_HEADER_HEIGHT = "columnHeaderHeight";	
	
	/** The Constant PN_PIXEL_PER_CHAR. */
	public static final String PN_PIXEL_PER_CHAR = "pixelPerChar";
	
	/** The Constant PN_PIXEL_PER_ROW. */
	public static final String PN_PIXEL_PER_ROW = "pixelPerRow";
	
	/** The Constant PN_MAXLINE_PER_ROW. */
	public static final String PN_MAXLINE_PER_ROW = "maxLinesPerRow";	
	
	/** The Constant PN_HEADER_FONT. */
	public static final String PN_HEADER_FONT = "columnHeaderFont";	
	
	/** The Constant PN_HEADER_FONT_SIZE. */
	public static final String PN_HEADER_FONT_SIZE = "columnHeaderFontSize";
	
	/** The Constant PN_HEADER_FONT_BOLD. */
	public static final String PN_HEADER_FONT_BOLD = "columnHeaderFontBold";
	
	/** The Constant PN_HEADER_FONT_ITALIC. */
	public static final String PN_HEADER_FONT_ITALIC = "columnHeaderFontItalic";
	
	/** The Constant PN_HEADER_FORECOLOR. */
	public static final String PN_HEADER_FORECOLOR = "columnHeaderForegroundColor";
	
	/** The Constant PN_HEADER_BACKCOLOR. */
	public static final String PN_HEADER_BACKCOLOR = "columnHeaderBackgroundColor";
	
	/** The Constant PN_ROW_FONT. */
	public static final String PN_ROW_FONT = "rowFont";	
	
	/** The Constant PN_ROW_FONT_SIZE. */
	public static final String PN_ROW_FONT_SIZE = "rowFontSize";
	
	/** The Constant PN_DETAIL_EVEN_ROW_FORECOLOR. */
	public static final String PN_DETAIL_EVEN_ROW_FORECOLOR = "evenRowsForegroundColor";
	
	/** The Constant PN_DETAIL_EVEN_ROW_BACKCOLOR. */
	public static final String PN_DETAIL_EVEN_ROW_BACKCOLOR = "evenRowsBackgroundColor";
	
	/** The Constant PN_DETAIL_ODD_ROW_FORECOLOR. */
	public static final String PN_DETAIL_ODD_ROW_FORECOLOR = "oddRowsForegroundColor";
	
	/** The Constant PN_DETAIL_ODD_ROW_BACKCOLOR. */
	public static final String PN_DETAIL_ODD_ROW_BACKCOLOR = "oddRowsBackgroundColor";
	
	
	/** The Constant DEFAULT_BAND_WIDTH. */
	public static final String DEFAULT_BAND_WIDTH = "530";	
	
	/** The Constant DEFAULT_HEADER_HEIGHT. */
	public static final String DEFAULT_HEADER_HEIGHT = "40";
	
	/** The Constant DEFAULT_PIXEL_PER_CHAR. */
	public static final String DEFAULT_PIXEL_PER_CHAR = "9";
	
	/** The Constant DEFAULT_PIXEL_PER_ROW. */
	public static final String DEFAULT_PIXEL_PER_ROW = "16";
	
	/** The Constant DEFAULT_MAXLINE_PER_ROW. */
	public static final String DEFAULT_MAXLINE_PER_ROW = "4";
	
	/** The Constant DEFAULT_HEADER_FONT. */
	public static final String DEFAULT_HEADER_FONT = "Helvetica-Bold";
	
	/** The Constant DEFAULT_HEADER_FONT_SIZE. */
	public static final String DEFAULT_HEADER_FONT_SIZE = "12";
	
	/** The Constant DEFAULT_HEADER_FONT_BOLD. */
	public static final String DEFAULT_HEADER_FONT_BOLD = "true";
	
	/** The Constant DEFAULT_HEADER_FONT_ITALIC. */
	public static final String DEFAULT_HEADER_FONT_ITALIC = "false";
	
	/** The Constant DEFAULT_HEADER_FORECOLOR. */
	public static final String DEFAULT_HEADER_FORECOLOR = "FFFFFF";
	
	/** The Constant DEFAULT_HEADER_BACKCOLOR. */
	public static final String DEFAULT_HEADER_BACKCOLOR = "#E4ECF2";
	
	/** The Constant DEFAULT_ROW_FONT. */
	public static final String DEFAULT_ROW_FONT = "Times-Roman";	
	
	/** The Constant DEFAULT_ROW_FONT_SIZE. */
	public static final String DEFAULT_ROW_FONT_SIZE = "10";
	
	/** The Constant DEFAULT_DETAIL_EVEN_ROW_FORECOLOR. */
	public static final String DEFAULT_DETAIL_EVEN_ROW_FORECOLOR = "#000000";
	
	/** The Constant DEFAULT_DETAIL_EVEN_ROW_BACKCOLOR. */
	public static final String DEFAULT_DETAIL_EVEN_ROW_BACKCOLOR = "#EEEEEE";
	
	/** The Constant DEFAULT_DETAIL_ODD_ROW_FORECOLOR. */
	public static final String DEFAULT_DETAIL_ODD_ROW_FORECOLOR = "#000000";
	
	/** The Constant DEFAULT_DETAIL_ODD_ROW_BACKCOLOR. */
	public static final String DEFAULT_DETAIL_ODD_ROW_BACKCOLOR = "#FFFFFF";	

	/** The Constant DEFAULT_NUMBER_PATTERN. */
	public static final String DEFAULT_NUMBER_PATTERN = "#,##0.##";
	
	
	public TemplateBuilder(String query, 
			   Vector queryFields, 
			   Map params, 
			   File baseTemplateFile) {

		this.query = query;
		this.queryFields = queryFields;
		this.params = params;
		this.baseTemplateFile = baseTemplateFile;
	}
	
	
	/**
	 * Gets the param value.
	 * 
	 * @param paramName the param name
	 * @param paramDefaultValue the param default value
	 * 
	 * @return the param value
	 */
	private String getParamValue(String paramName, String paramDefaultValue) {
		String paramValue = null;
		
		paramValue = (String)params.get(paramName);
		paramValue = (paramValue != null)? paramValue: paramDefaultValue;
		
		return paramValue;
	}
	
	public String buildTemplate() {
		String templateStr = getTemplateTemplate();
		if(getParamValue("pagination", "false").equalsIgnoreCase("true")) {
			templateStr = replaceParam(templateStr, "pagination", 
					"isIgnorePagination=\"true\"");
		} else {
			templateStr = replaceParam(templateStr, "pagination", "");
		}
		templateStr = replaceParam(templateStr, "lang", "sql");
		String escapedQuery = escape(query);
		templateStr = replaceParam(templateStr, "query", escapedQuery);
		templateStr = replaceParam(templateStr, "fields", getFieldsBlock());
		templateStr = replaceParam(templateStr, "body", getColumnHeaderBlock() + getDetailsBlock());
		
		return templateStr;
	}
		
	
	
	
	/**
	 * Gets the fields block.
	 * 
	 * @return the fields block
	 */
	public String getFieldsBlock() {
		StringBuffer buffer = new StringBuffer();
				
		for(int i = 0; i < queryFields.size(); i++) {
			Field field = (Field)queryFields.get(i);
			buffer.append("<field name=\"" + field.getName() + "\" class=\"" + field.getClassType() + "\"/>\n");
			
		}
		
		
		return buffer.toString();
	}
	
	/** The Constant DETAIL_HEIGHT. */
	public static final int DETAIL_HEIGHT = 20;
	
	/** The Constant DETAIL_WIDTH. */
	public static final int DETAIL_WIDTH = 530;
	
	/**
	 * Gets the details block.
	 * 
	 * @return the details block
	 */
	public String getDetailsBlock() {
		StringBuffer buffer = new StringBuffer();
		
		int totalWidth = Integer.parseInt(getParamValue(PN_BAND_WIDTH, DEFAULT_BAND_WIDTH ));
		int detailHeight = getRowHeight(Integer.parseInt(DEFAULT_HEADER_HEIGHT));
		
		buffer.append("<detail>\n");
		buffer.append("<band " + 
					  "height=\"" + detailHeight + "\"  " + 
					  "isSplitAllowed=\"true\" >\n");
		
		int[] columnWidth = getColumnWidth(totalWidth);	
		int x = 0;
		
		int i=0;
		for(i = 0; i < queryFields.size(); i++) {
			Field field = (Field)queryFields.get(i);
			
			if( !field.isVisible() ) {
				continue;
			}
			
			boolean isANumber = false;
			String className = field.getClassType();
			Class fieldClass = Object.class;
			try {
				fieldClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				logger.error("Class type not recognized: [" + className + "]", e);
			}
			if (Number.class.isAssignableFrom(fieldClass)){
				isANumber = true;
			}
			
			buffer.append("<textField isStretchWithOverflow=\"true\" isBlankWhenNull=\"false\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ");
			if (isANumber) {
				String pattern = field.getPattern();
				buffer.append(" pattern=\"" + ((pattern != null) ? pattern : DEFAULT_NUMBER_PATTERN) + "\"");
			}
			buffer.append(" >\n");
			
			buffer.append("<reportElement " + 
						  		"mode=\"" + "Opaque" + "\" " + 
						  		"x=\"" + x + "\" " + 
						  		"y=\"" + 0 + "\" " + 
						  		"width=\"" + columnWidth[i] + "\" " + 
						  		"height=\"" + detailHeight + "\" " + 
						  		"forecolor=\"" + getParamValue(PN_DETAIL_EVEN_ROW_FORECOLOR, DEFAULT_DETAIL_EVEN_ROW_FORECOLOR ) + "\" " + 
						  		"backcolor=\"" + getParamValue(PN_DETAIL_EVEN_ROW_BACKCOLOR, DEFAULT_DETAIL_EVEN_ROW_BACKCOLOR) + "\" " + 
						  		"key=\"textField\">\n");
			
			buffer.append("<printWhenExpression><![CDATA[new Boolean(\\$V\\{REPORT_COUNT\\}.intValue() % 2 == 0)]]></printWhenExpression>");
			buffer.append("</reportElement>");
			buffer.append("<box leftPadding=\"2\" rightPadding=\"2\" topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/>\n");

			
			buffer.append("<textElement " +
								"textAlignment=\"" + (isANumber ? "Right": "Left") + "\" " +
								"verticalAlignment=\"Middle\"> " +
								"<font pdfFontName=\"" + getParamValue(PN_ROW_FONT, DEFAULT_ROW_FONT)+ "\" " +
									  "size=\"" + getParamValue(PN_ROW_FONT_SIZE, DEFAULT_ROW_FONT_SIZE)+ "\"/>" +
						  "</textElement>\n");
			
			if(field.getClassType().equalsIgnoreCase("java.sql.Date")) {
				buffer.append("<textFieldExpression   " + 
						  "class=\"java.lang.String\"> " + 
						  "<![CDATA[\\$F\\{" + field.getName() + "\\}.toString()]]>\n" +
						  "</textFieldExpression>\n");
			} else {
				buffer.append("<textFieldExpression   " + 
						  "class=\"" + field.getClassType() + "\"> " + 
						  "<![CDATA[\\$F\\{" + field.getName() + "\\}]]>\n" +
						  "</textFieldExpression>\n");
			}
		
			
			buffer.append("</textField>\n\n");	
			
			
			
			
			buffer.append("<textField isStretchWithOverflow=\"true\" isBlankWhenNull=\"false\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" ");
			if (isANumber) {
				String pattern = field.getPattern();
				buffer.append(" pattern=\"" + ((pattern != null) ? pattern : DEFAULT_NUMBER_PATTERN) + "\"");
			}
			buffer.append(" >\n");
			buffer.append("<reportElement " + 
					      		"mode=\"" + "Opaque" + "\" " + 
					      		"x=\"" +  x  + "\" " + 
					      		"y=\"" + 0 + "\" " + 
					      		"width=\"" + columnWidth[i] + "\" " + 
					      		"height=\"" + detailHeight + "\" " + 
					      		"forecolor=\"" + getParamValue(PN_DETAIL_ODD_ROW_FORECOLOR, DEFAULT_DETAIL_ODD_ROW_FORECOLOR ) + "\" " + 
						  		"backcolor=\"" + getParamValue(PN_DETAIL_ODD_ROW_BACKCOLOR, DEFAULT_DETAIL_ODD_ROW_BACKCOLOR) + "\" " + 
						  		"key=\"textField\">\n");
			buffer.append("<printWhenExpression><![CDATA[new Boolean(\\$V\\{REPORT_COUNT\\}.intValue() % 2 != 0)]]></printWhenExpression>");
			buffer.append("</reportElement>");
			buffer.append("<box leftPadding=\"2\" rightPadding=\"2\" topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/>\n");

			buffer.append("<textElement " +
								"textAlignment=\"" + (isANumber ? "Right": "Left") + "\" " +
								"verticalAlignment=\"Middle\"> " +
								"<font pdfFontName=\"" + getParamValue(PN_ROW_FONT, DEFAULT_ROW_FONT)+ "\" " +
								  "size=\"" + getParamValue(PN_ROW_FONT_SIZE, DEFAULT_ROW_FONT_SIZE)+ "\"/>" +
			  			  "</textElement>\n");
			
			if(field.getClassType().equalsIgnoreCase("java.sql.Date")) {
				buffer.append("<textFieldExpression   " + 
						  "class=\"java.lang.String\"> " + 
						  "<![CDATA[\\$F\\{" + field.getName() + "\\}.toString()]]>\n" +
						  "</textFieldExpression>\n");
			} else {
				buffer.append("<textFieldExpression   " + 
						  "class=\"" + field.getClassType() + "\"> " + 
						  "<![CDATA[\\$F\\{" + field.getName() + "\\}]]>\n" +
						  "</textFieldExpression>\n");
			}
			
			buffer.append("</textField>\n\n\n");	
			
			x += columnWidth[i];		
		}
		
		
		buffer.append("</band>");
		buffer.append("</detail>");
		
		
		return buffer.toString();
	}
	
	/**
	 * Gets the column header block.
	 * 
	 * @return the column header block
	 */
	public String getColumnHeaderBlock(){
		StringBuffer buffer = new StringBuffer();		
		
		int totalWidth = Integer.parseInt(getParamValue(PN_BAND_WIDTH, DEFAULT_BAND_WIDTH ));
		
		buffer.append("<columnHeader>\n");
		buffer.append("<band " + 
					  "height=\"" + getParamValue(PN_HEADER_HEIGHT, DEFAULT_HEADER_HEIGHT) + "\"  " + 
					  "isSplitAllowed=\"true\" >\n");
		
		int[] columnWidth = getColumnWidth(totalWidth);		
		int x = 0;
		
		int i=0;
		for(i = 0; i < queryFields.size(); i++) {
			Field field = (Field)queryFields.get(i);
			
			if( !field.isVisible() ) {
				continue;
			}
			
			buffer.append("<staticText>\n");
			buffer.append("<reportElement " + 
			  		"mode=\"" + "Opaque" + "\" " + 
			  		"x=\"" + x + "\" " + 
			  		"y=\"" + 0 + "\" " + 
			  		"width=\"" + columnWidth[i] + "\" " + 
			  		"height=\"" + getParamValue(PN_HEADER_HEIGHT, DEFAULT_HEADER_HEIGHT ) + "\" " + 
			  		"forecolor=\"" + getParamValue(PN_HEADER_FORECOLOR, DEFAULT_HEADER_FORECOLOR ) + "\" " + 
			  		"backcolor=\"" + getParamValue(PN_HEADER_BACKCOLOR, DEFAULT_HEADER_BACKCOLOR ) + "\" " + 
			  		"key=\"staticText\"/>\n");	

			buffer.append("<box leftPadding=\"2\" rightPadding=\"2\" topBorder=\"None\" topBorderColor=\"#000000\" leftBorder=\"None\" leftBorderColor=\"#000000\" rightBorder=\"None\" rightBorderColor=\"#000000\" bottomBorder=\"None\" bottomBorderColor=\"#000000\"/>\n");

			buffer.append("<textElement " +
					"textAlignment=\"" + (field.getClassType().equalsIgnoreCase("java.lang.String")? "Left": "Left") + "\" " +
					"verticalAlignment=\"Middle\"> " +
						"<font pdfFontName=\"" + getParamValue(PN_HEADER_FONT, DEFAULT_HEADER_FONT) + "\" " +
							  "size=\"" + getParamValue(PN_HEADER_FONT_SIZE, DEFAULT_HEADER_FONT_SIZE) + "\" " +
							  "isBold=\""+getParamValue(PN_HEADER_FONT_BOLD, DEFAULT_HEADER_FONT_BOLD)+"\" "+
							  "isItalic=\""+getParamValue(PN_HEADER_FONT_ITALIC, DEFAULT_HEADER_FONT_ITALIC)+"\"/> " +
			  "</textElement>\n");

			String escapedAlias = escape(field.getAlias());
			buffer.append("<text><![CDATA[" + escapedAlias + "]]></text>\n");

			buffer.append("</staticText>\n\n");		

			x += columnWidth[i];	
		}
		
		buffer.append("</band>");
		buffer.append("</columnHeader>");
		
		return buffer.toString();
	}	
	
	/**
	 * Gets the column width.
	 * 
	 * @param totalWidth the total width
	 * 
	 * @return the column width
	 */
	public int[] getColumnWidth(int totalWidth) {
		int[] columnWidthInPixel = new int[queryFields.size()];
		
		int pixelPerChar = Integer.parseInt(getParamValue(PN_PIXEL_PER_CHAR, DEFAULT_PIXEL_PER_CHAR ));
		
		int freePixels = 0;
		int overflowFieldNum = queryFields.size();
		int pixelPerColumn = totalWidth/(queryFields.size());
		int remainderPixels = totalWidth%(queryFields.size());
		
		for(int i = 0; i < queryFields.size(); i++) {
			int fieldRequiredWidthInPixel = ((Field)queryFields.get(i)).getDisplySize() * pixelPerChar;
			if(fieldRequiredWidthInPixel < pixelPerColumn) {
				columnWidthInPixel[i] = fieldRequiredWidthInPixel;
				freePixels += (pixelPerColumn-fieldRequiredWidthInPixel);
				overflowFieldNum--;
			} else {
				columnWidthInPixel[i] = pixelPerColumn;
			}
			
		}
		
		if(overflowFieldNum > 0 && freePixels > 0) {
			freePixels += remainderPixels;
			pixelPerColumn = freePixels/overflowFieldNum;
			remainderPixels = freePixels%overflowFieldNum;
			for(int i = 0; i < queryFields.size(); i++) {
				int fieldRequiredWidthInPixel = ((Field)queryFields.get(i)).getDisplySize() * pixelPerChar;
				if(fieldRequiredWidthInPixel > columnWidthInPixel[i]) {
					columnWidthInPixel[i] += pixelPerColumn;
					if(fieldRequiredWidthInPixel > columnWidthInPixel[i]) overflowFieldNum--;
					freePixels -= pixelPerColumn;
				}
			}
			freePixels -= remainderPixels;			
		} 
		columnWidthInPixel[queryFields.size()-1] += remainderPixels;
				
		return columnWidthInPixel;
	}
	
	/**
	 * Gets the row height.
	 * 
	 * @param totalWidth the total width
	 * 
	 * @return the row height
	 */
	public int getRowHeight(int totalWidth) {
		
		int pixelPerChar = Integer.parseInt(getParamValue(PN_PIXEL_PER_CHAR, DEFAULT_PIXEL_PER_CHAR ));
		int pixelPerRow = Integer.parseInt(getParamValue(PN_PIXEL_PER_ROW, DEFAULT_PIXEL_PER_ROW ));
		
		int rowHeight = pixelPerRow;
		int[] columnWidthInPixel = new int[queryFields.size()];
		
		int freePixels = 0;
		int overflowFieldNum = queryFields.size();
		int pixelPerColumn = totalWidth/(queryFields.size());
		int remainderPixels = totalWidth%(queryFields.size());
		
		for(int i = 0; i < queryFields.size(); i++) {
			int fieldRequiredWidthInPixel = ((Field)queryFields.get(i)).getDisplySize() * pixelPerChar;
			if(fieldRequiredWidthInPixel < pixelPerColumn) {
				columnWidthInPixel[i] = fieldRequiredWidthInPixel;
				freePixels += (pixelPerColumn-fieldRequiredWidthInPixel);
				overflowFieldNum--;
			} else {
				columnWidthInPixel[i] = pixelPerColumn;
			}
			
		}
		
		int lines = 1;
		if(overflowFieldNum > 0 && freePixels > 0) {
			freePixels += remainderPixels;
			pixelPerColumn = freePixels/overflowFieldNum;
			remainderPixels = freePixels%overflowFieldNum;
			for(int i = 0; i < queryFields.size(); i++) {
				int fieldRequiredWidthInPixel = ((Field)queryFields.get(i)).getDisplySize() * pixelPerChar;
				if(fieldRequiredWidthInPixel > columnWidthInPixel[i]) {
					columnWidthInPixel[i] += pixelPerColumn;
					if(fieldRequiredWidthInPixel < columnWidthInPixel[i]) overflowFieldNum--;
					else {
						int l = fieldRequiredWidthInPixel/columnWidthInPixel[i];
						if(fieldRequiredWidthInPixel%columnWidthInPixel[i] > 0) l += 1;
						if(l > lines) lines = l;
					}
					freePixels -= pixelPerColumn;
				}
			}
			freePixels -= remainderPixels;			
		} 
		columnWidthInPixel[queryFields.size()-1] += remainderPixels;
		
		int maxLinesPerRow = Integer.parseInt(getParamValue(PN_MAXLINE_PER_ROW, DEFAULT_MAXLINE_PER_ROW));
		lines = (lines>maxLinesPerRow)? maxLinesPerRow: lines;
		rowHeight = lines * pixelPerRow;
		
		return (rowHeight);
	}
	
	
	
	
	
	/**
	 * Gets the template template.
	 * 
	 * @return the template template
	 */
	private String getTemplateTemplate() {
		StringBuffer buffer = new StringBuffer();
		
		
		InputStream is = null;
		if(baseTemplateFile!=null && baseTemplateFile.exists()) {
			try {
				is = new FileInputStream(baseTemplateFile);
			} catch (FileNotFoundException e1) {
				is = null;
			}
		}
		
		if(is == null)	is = getClass().getClassLoader().getResourceAsStream("template.jrxml");
		
		BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
		String line = null;
		try {
			while( (line = reader.readLine()) != null) {
				buffer.append(line + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	/**
	 * Replace param.
	 * 
	 * @param template the template
	 * @param pname the pname
	 * @param pvalue the pvalue
	 * 
	 * @return the string
	 */
	private String replaceParam(String template, String pname, String pvalue) {
		int index = -1;
		while( (index = template.indexOf("${" + pname + "}")) != -1) {
			template = template.replaceAll("\\$\\{" + pname + "\\}", pvalue);
		}
		
		return template;
	}
	

	private String escape(String pvalue) {
		pvalue = pvalue.replace("\\", "\\\\");
		pvalue = pvalue.replace("$", "\\$");
		return pvalue;
	}
	
}
