/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.query;

// TODO: Auto-generated Javadoc
/**
 * The Class Field.
 * 
 * @author Gioia
 */
public class Field {
	
	/** The name. */
	private String name;
	
	private String alias;
	
	private boolean visible;
	
	/** The class type. */
	private String classType;
	
	/** The disply size. */
	private int displySize;
	
	/** The format pattern (used for numbers) */
	private String pattern;
	
	/**
	 * Instantiates a new field.
	 * 
	 * @param name the name
	 * @param classType the class type
	 * @param displySize the disply size
	 */
	public  Field(String name, String classType, int displySize) {
		this.name = name;
		this.classType = classType;
		this.displySize = displySize;
	}

	/**
	 * Gets the class type.
	 * 
	 * @return the class type
	 */
	public String getClassType() {
		return classType;
	}

	/**
	 * Sets the class type.
	 * 
	 * @param classType the new class type
	 */
	public void setClassType(String classType) {
		this.classType = getFieldType(classType);
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Return the correct field type...     *
	 * 
	 * @param type the type
	 * 
	 * @return the field type
	 */
    public static String getFieldType(String type)
    {

        if (type == null) return "java.lang.Object";

        if (type.equals("java.lang.Boolean") || type.equals("boolean")) return "java.lang.Boolean";
        if (type.equals("java.lang.Byte") || type.equals("byte")) return "java.lang.Byte";
        if (type.equals("java.lang.Integer") || type.equals("int")) return "java.lang.Integer";
        if (type.equals("java.lang.Long") || type.equals("long")) return "java.lang.Long";
        if (type.equals("java.lang.Double") || type.equals("double")) return "java.lang.Double";
        if (type.equals("java.lang.Float") || type.equals("float")) return "java.lang.Float";
        if (type.equals("java.lang.Short") || type.equals("short")) return "java.lang.Short";
        if (type.startsWith("[")) return "java.lang.Object";
       
        return type;
    }

	/**
	 * Gets the disply size.
	 * 
	 * @return the disply size
	 */
	public int getDisplySize() {
		return displySize;
	}

	/**
	 * Sets the disply size.
	 * 
	 * @param displySize the new disply size
	 */
	public void setDisplySize(int displySize) {
		this.displySize = displySize;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
