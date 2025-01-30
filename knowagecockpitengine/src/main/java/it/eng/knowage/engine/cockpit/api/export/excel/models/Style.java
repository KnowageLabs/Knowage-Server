package it.eng.knowage.engine.cockpit.api.export.excel.models;

import org.apache.poi.ss.usermodel.Sheet;

public class Style {
    private Sheet sheet;
    private String alignItems;
    private String justifyContent;
    private String backgroundColor;
    private String color;
    private String fontFamily;
    private String fontSize;
    private String fontStyle;
    private String fontWeight;

    public Style() {
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public String getAlignItems() {
        return alignItems;
    }

    public void setAlignItems(String alignItems) {
        this.alignItems = alignItems;
    }

    public String getJustifyContent() {
        return justifyContent;
    }

    public void setJustifyContent(String justifyContent) {
        this.justifyContent = justifyContent;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }
}
