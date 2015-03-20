/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.utils;

import java.awt.Color;
import java.awt.Font;

public class StyleLabel {

	private Font font;
	private String fontName;
	private int size;
	private Color color;
	private String orientation;
	
	public Font getFont() {
		return font;
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public String getOrientation() {
		return orientation;
	}
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public StyleLabel(String fontName, int size, Color color, String orientation) {
		super();
		this.fontName = fontName;
		this.size = size;
		this.color = color;
		this.orientation = orientation;
		font=new Font(fontName,Font.BOLD,size);
	}
	public StyleLabel(String fontName, int size, Color color) {
		super();
		this.fontName = fontName;
		this.size = size;
		this.color = color;
		font=new Font(fontName,Font.BOLD,size);
	}
	

	
	
}
