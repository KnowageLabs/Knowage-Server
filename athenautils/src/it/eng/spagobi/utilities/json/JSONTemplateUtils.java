/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.json;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.utilities.messages.IEngineMessageBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 * 
 * DATE            CONTRIBUTOR/DEVELOPER                        NOTE
 * 19-04-2013    Antonella Giachino (antonella.giachino@eng.it)      
 *               Andrea Fantappiè (andrea.fantappiè@eng.it)     Added internationalization management
 * 															    for highchart engine
 * 
 *     Utility Class to convert xml template in json template
 */

public class JSONTemplateUtils
{

	// template constants

	public static final String WIDTH = "WIDTH";
	public static final String HEIGHT = "HEIGHT";
	public static final String HIGHCHART_TYPE = "HIGHCHART";
	public static final String HIGH_CHART = "CHART";
	public static final String HIGH_NUMCHARTS = "NUMCHARTS";
	public static final String HIGH_SUBTYPE = "SUBTYPE";

	private static transient Logger logger = Logger.getLogger(JSONTemplateUtils.class);

	private String divWidth = "100%";
	private String divHeight = "100%";
	private String theme = "";
	private Integer numCharts = new Integer("1");
	private String subType = "";
	private boolean firstBlock = true;
	private JSONArray parametersJSON = null;
	private SourceBean template = null;

	// i18n support
	private IEngineMessageBuilder engineMessageBuilder;
	private Locale locale;

	/**
	 * Returns a JSONObject with the input configuration (xml format).
	 * 
	 * @param getTemplate
	 *            (). the template in xml language
	 * @param
	 * 
	 * @return JSONObject the same template in json format (because highcharts
	 *         uses json format)
	 */
	public JSONObject getJSONTemplateFromXml(SourceBean xmlTemplate, JSONArray parsJSON) throws JSONException {
		JSONObject toReturn = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStreamWriter ow = new OutputStreamWriter(out);
		parametersJSON = parsJSON;
		setTemplate(xmlTemplate);
		try {
			// the begin of all..
			ow.write("{\n");

			// dimension definition
			setDivWidth((String) getTemplate().getAttribute(WIDTH));
			setDivHeight((String) getTemplate().getAttribute(HEIGHT));

			if (isHighChart()) {
				// number of chart definition (for highchart lib)
				setNumCharts((getTemplate().getAttribute(HIGH_NUMCHARTS) != null) ? Integer.valueOf((String) getTemplate().getAttribute(HIGH_NUMCHARTS)) : 1);
				// subtype for master/detail chart
				setSubType((getTemplate().getAttribute(HIGH_CHART + "." + HIGH_SUBTYPE) != null) ? (String) getTemplate().getAttribute(HIGH_CHART + "." + HIGH_SUBTYPE) : "");

				getTemplate().delAttribute(WIDTH);
				getTemplate().delAttribute(HEIGHT);
				getTemplate().delAttribute(HIGH_NUMCHARTS);
				getTemplate().delAttribute(HIGH_SUBTYPE);
			}

			ow = getPropertiesDetail(this.getTemplate(), ow);
			ow.write("}\n");
			ow.flush();
			// System.out.println("*** template: " + out.toString());
			logger.debug("ChartConfig: " + out.toString());

		} catch (IOException ioe) {
			logger.error("Error while defining json chart template: " + ioe.getMessage());
		} catch (Exception e) {
			logger.error("Error while defining json chart template: " + e.getMessage());
		} finally {
			try {
				ow.close();
			} catch (IOException ioe2) {
				logger.error("Error while closing the output writer object: " + ioe2.getMessage());
			}

		}
		// replace dublicate , charachter
		String json = out.toString().replaceAll(", ,", ",");
		try {
			toReturn = ObjectUtils.toJSONObject(json);
		} catch (Exception e) {
			logger.error("Error while serializes the result: " + json + " - Error: " + e.getMessage());
		}

		return toReturn;
	}

	/**
	 * @return the template
	 */
	public SourceBean getTemplate() {
		return template;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(SourceBean template) {
		this.template = template;
	}

	/**
	 * @return the divWidth
	 */
	public String getDivWidth() {
		return divWidth;
	}

	/**
	 * @param divWidth
	 *            the divWidth to set
	 */
	public void setDivWidth(String divWidth) {
		this.divWidth = divWidth;
	}

	/**
	 * @return the divHeight
	 */
	public String getDivHeight() {
		return divHeight;
	}

	/**
	 * @param divHeight
	 *            the divHeight to set
	 */
	public void setDivHeight(String divHeight) {
		this.divHeight = divHeight;
	}

	/**
	 * @return the theme
	 */
	public String getTheme() {
		return theme;
	}

	/**
	 * @param theme
	 *            the theme to set
	 */
	public void setTheme(String theme) {
		this.theme = theme;
	}

	/**
	 * @return the numCharts
	 */
	public Integer getNumCharts() {
		return numCharts;
	}

	/**
	 * @param numCharts
	 *            the numCharts to set
	 */
	public void setNumCharts(Integer numCharts) {
		this.numCharts = numCharts;
	}

	/**
	 * @return the subType
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * @param subType
	 *            the subType to set
	 */
	public void setSubType(String subType) {
		this.subType = subType;
	}

	/**
	 * @return the firstBlock
	 */
	public boolean isFirstBlock() {
		return firstBlock;
	}

	/**
	 * @param firstBlock
	 *            the firstBlock to set
	 */
	public void setFirstBlock(boolean firstBlock) {
		this.firstBlock = firstBlock;
	}

	public IEngineMessageBuilder getEngineMessageBuilder() {
		return engineMessageBuilder;
	}

	public void setEngineMessageBuilder(IEngineMessageBuilder engineMessageBuilder) {
		this.engineMessageBuilder = engineMessageBuilder;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Returns an OutputStreamWriter with the json template
	 * 
	 * @param sbConfig
	 *            the sourcebean with the xml configuration
	 * @param ow
	 *            the current OutputStreamWriter
	 * @return
	 */
	private OutputStreamWriter getPropertiesDetail(Object sbConfig, OutputStreamWriter ow) {
		// template complete
		OutputStreamWriter toReturn = ow;

		if (sbConfig == null)
			return toReturn;

		try {
			List atts = ((SourceBean) sbConfig).getContainedAttributes();
			for (int i = 0; i < atts.size(); i++) {

				SourceBeanAttribute object = (SourceBeanAttribute) atts.get(i);

				// object.getValue();
				String key = (String) object.getKey();
				if (key.endsWith("_LIST")) {
					String arrayKey = key.substring(0, key.indexOf("_LIST"));
					toReturn.write("      " + convertKeyString(arrayKey) + ": [ \n");
					toReturn = getAllArrayAttributes(object, toReturn);
					toReturn.write("       ]\n");
				} else {
					if (object.getValue() instanceof SourceBean) {
						toReturn.write("      " + convertKeyString(key) + ": { \n");
						toReturn = getAllAttributes(object, ow);
						toReturn.write("       }\n");
					} else {
						// only for root node attributes
						if (key.endsWith("_list")) {
							String originalKey = key.replace("_list", "");
							String originalValue = ((String) object.getValue()).replace("'", "");
							toReturn.write("      " + originalKey + ": [" + originalValue + "] \n");
						} else {
							toReturn.write("      " + convertKeyString(key) + ": '");
							toReturn = getAllAttributes(object, ow);
							toReturn.write("'\n");
						}
					}
				}
				if (i != atts.size() - 1) {
					toReturn.write(", ");
				}
			}

		} catch (Exception e) {
			logger.error("Error while defining json chart template: " + e.getMessage());
		}
		return toReturn;
	}

	/**
	 * Returns an OutputStreamWriter with all details about a single key (ie.
	 * CHART tag)
	 * 
	 * @param key
	 * @param sb
	 * @param ow
	 * @return
	 */
	private OutputStreamWriter getAllAttributes(SourceBeanAttribute sb, OutputStreamWriter ow) {
		OutputStreamWriter toReturn = ow;

		try {
			if (sb.getValue() instanceof SourceBean) {
				// toReturn.write("      " + convertKeyString(sb.getKey())
				// +": { \n");
				SourceBean sbSubConfig = (SourceBean) sb.getValue();
				List subAtts = sbSubConfig.getContainedAttributes();
				List containedSB = sbSubConfig.getContainedSourceBeanAttributes();
				int numberOfSb = containedSB.size();
				int sbCounter = 1;
				// standard tag attributes
				for (int i = 0; i < subAtts.size(); i++) {
					SourceBeanAttribute object = (SourceBeanAttribute) subAtts.get(i);
					if (object.getValue() instanceof SourceBean) {

						String key = (String) object.getKey();

						if (key.endsWith("_LIST")) {
							String arrayKey = key.substring(0, key.indexOf("_LIST"));
							toReturn.write("      " + convertKeyString(arrayKey) + ": [ \n");
							toReturn = getAllArrayAttributes(object, toReturn);
							toReturn.write("       ]\n");
						} else {
							toReturn.write("      " + convertKeyString(key) + ": { \n");
							toReturn = getAllAttributes(object, toReturn);
							toReturn.write("       }\n");
						}
						if (i != subAtts.size() - 1) {
							toReturn.write("       , ");
						}
						sbCounter++;
					} else {
						SourceBeanAttribute subObject2 = (SourceBeanAttribute) subAtts.get(i);
						toReturn = writeTagAttribute(subObject2, toReturn, false);
						if (i != subAtts.size() - 1) {
							toReturn.write("       , ");
						}
					}
				}
				// toReturn.write("       }\n");
			} else {
				// puts the simple value attribute
				toReturn.write(String.valueOf(sb.getValue()));
			}

		} catch (IOException ioe) {
			logger.error("Error while defining json chart template: " + ioe.getMessage());
		} catch (Exception e) {
			logger.error("Error while defining json chart template: " + e.getMessage());
		}
		return toReturn;
	}

	private OutputStreamWriter getAllArrayAttributes(SourceBeanAttribute sb, OutputStreamWriter ow) {
		OutputStreamWriter toReturn = ow;

		try {
			if (sb.getValue() instanceof SourceBean) {
				SourceBean sbSubConfig = (SourceBean) sb.getValue();

				List containedSB = sbSubConfig.getContainedSourceBeanAttributes();
				int numberOfSb = containedSB.size();
				int sbCounter = 1;
				// standard tag attributes
				for (int i = 0; i < containedSB.size(); i++) {
					SourceBeanAttribute object = (SourceBeanAttribute) containedSB.get(i);
					Object o = object.getValue();
					SourceBean sb1 = SourceBean.fromXMLString(o.toString());
					String v = sb1.getCharacters();
					if (v != null) {
						if (!v.startsWith("[") && !v.startsWith("'")) {
							// adds ' only if the element isn't an array but a
							// simple value
							v = "'" + v + "'";
						}
						toReturn.write(v + "\n");

					} else {
						// attributes

						toReturn.write("{ 	\n");
						List atts = ((SourceBean) sb1).getContainedAttributes();
						toReturn = getAllAttributes(object, toReturn);
						toReturn.write("} 	\n");
					}
					if (i != containedSB.size() - 1) {
						toReturn.write("       , ");
					}
				}

			}
		} catch (Exception e) {
			logger.error("Error while defining json chart template: " + e.getMessage());
		}
		return toReturn;
	}

	/**
	 * Returns an object (String or Integer) with the value of the property.
	 * 
	 * @param key
	 *            the attribute key
	 * @param sbAttr
	 *            the soureBeanAttribute to looking for the value of the key
	 * @return
	 */
	private Object getAttributeValue(String key, SourceBeanAttribute sbAttr) {
		String value = new String((String) sbAttr.getValue());
		Object finalValue = "";
		if (value != null) {
			try {
				// checks if the value is a number
				// finalValue =Long.valueOf(value);
				finalValue = Double.valueOf(value);
			} catch (Exception e) {
				// checks if the value is a boolean
				if (key.equals("color") && value.startsWith("$P{")) {
					finalValue = decodeColorParameter(value);
				} else if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false") // boolean
						&& !value.startsWith("[")// not an array example for
													// categories..
				// && ! value.trim().startsWith("function")//not an array
				// example for categories..
				) {
					// replace parameters
					if (value.contains("$P{")) {
						boolean addFinalSpace = (key.equals("text") ? true : false);
						finalValue = replaceParametersInValue(value, addFinalSpace);
					}
					
					if(finalValue.equals(""))
						finalValue = value;
					
					if(value.contains("$R{")) {
						boolean addFinalSpace = (key.equals("text") ? true : false);
						finalValue = replaceMessagesInValue((String)finalValue, addFinalSpace);
					}
					
					if(finalValue.equals(""))
						finalValue = value;
					
					if (!value.startsWith("function"))
						finalValue = "'" + finalValue + "'";
					
				} else {
					// the value is not a string
					finalValue = value;
				}
			}
		}
		return finalValue;
	}

	private String replaceParametersInValue(String valueString, boolean addFinalSpace) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(valueString);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.indexOf("$P{") != -1) {
				String parName = tok.substring(tok.indexOf("$P{") + 3, tok.indexOf("}"));
				String remnantString = tok.substring(tok.indexOf("}") + 1);
				if (!parName.equals("")) {
					for (int i = 0; i < parametersJSON.length(); i++) {
						try {
							JSONObject objPar = (JSONObject) parametersJSON.get(i);
							if (((String) objPar.get("name")).equals(parName)) {
								String val = ((String) objPar.get("value")).replaceAll("'", "");
								if (!val.equals("%")) {
									sb.append(val);
								}
								if (remnantString != null && !remnantString.equals("")) {
									sb.append(remnantString);
									addFinalSpace = false;
								}
								if (addFinalSpace)
									sb.append(" ");
								break;
							}
						} catch (JSONException e1) {
							logger.error("Error while replacing parameters in value: " + e1.getMessage());
						}
					}
				}
			} else {
				sb.append(tok);
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	private String replaceMessagesInValue(String valueString, boolean addFinalSpace) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(valueString);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.indexOf("$R{") != -1) {
				String parName = tok.substring(tok.indexOf("$R{") + 3, tok.indexOf("}"));
				String remnantString = tok.substring(tok.indexOf("}") + 1);
				if (!parName.equals("")) {
					try {
						String val = engineMessageBuilder.getI18nMessage(locale, parName).replaceAll("'", "");
						if (!val.equals("%")) {
							sb.append(val);
						}
						if (remnantString != null && !remnantString.equals("")) {
							sb.append(remnantString);
							addFinalSpace = false;
						}
						if (addFinalSpace)
							sb.append(" ");
					} catch (Exception e1) {
						logger.error("Error while replacing message in value: " + e1.getMessage());
					}
				}
			} else {
				sb.append(tok);
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	private String decodeColorParameter(String valueString) {
		logger.debug("Parsing color attribute: " + valueString);
		String parName = valueString.substring(valueString.indexOf("$P{") + 3, valueString.indexOf("}"));

		for (int i = 0; i < parametersJSON.length(); i++) {
			try {
				JSONObject objPar = (JSONObject) parametersJSON.get(i);
				if (((String) objPar.get("name")).equals(parName)) {
					String val = ((String) objPar.get("value")).replaceAll("'", "");

					String[] colors = valueString.substring(valueString.indexOf("(") + 1, valueString.length() - 1).split(",");
					for (int c = 0; c < colors.length; c++) {
						String color[] = colors[c].split("=");
						if (color[0].equals(val)) {
							return "'" + color[1] + "'";
						}
					}
				}
			} catch (JSONException e1) {
				logger.error("Error while replacing parameters in value: " + e1.getMessage());
			}
		}

		return "''";
	}

	/**
	 * @param sb
	 * @param toReturn
	 * @return
	 * @throws IOException
	 */
	private OutputStreamWriter writeTagAttribute(SourceBeanAttribute sb, OutputStreamWriter toReturn, boolean isTag) throws IOException {

		Object subValue = getAttributeValue(sb.getKey(), sb);
		if (subValue != null) {
			if (isTag) {
				toReturn.write("      " + convertKeyString(sb.getKey()) + ": " + subValue + "\n");
			} else {
				// attribute with list of values
				if (sb.getKey().endsWith("_list")) {
					String originalKey = sb.getKey().replace("_list", "");
					// String originalValue = ((String)subValue).replace("'",
					// "");
					String originalValue = getListOfValues(((String) subValue).replace("'", ""));
					toReturn.write("      " + originalKey + ": [" + originalValue + "] \n");
				} else {
					toReturn.write("      " + sb.getKey() + ": " + subValue + "\n");
				}
			}

		}
		return toReturn;
	}

	/**
	 * Splits the list of values and add ' around the single value. Necessary
	 * with Jackson library!
	 * 
	 * @param string
	 *            with the original list of values
	 * @param toReturn
	 * @return
	 * @throws IOException
	 */
	private String getListOfValues(String source) throws IOException {
		String toReturn = "";
		if (source == null)
			return source;
		String[] values = source.split(",");
		for (int i = 0, l = values.length; i < l; i++) {
			// add ' only if missing
			if (values[i].startsWith("'"))
				continue;
			toReturn += "'" + values[i] + "'";
			if (i < (l - 1)) {
				toReturn += ",";
			}
		}
		return toReturn;
	}

	private String convertKeyString(String xmlTag) {
		String jsonKey = xmlTag.toLowerCase();
		StringBuffer sb = new StringBuffer();
		int count = 0;
		for (String s : xmlTag.split("_")) {
			if (count == 0) {
				sb.append(Character.toLowerCase(s.charAt(0)));
			} else {
				sb.append(Character.toUpperCase(s.charAt(0)));
			}
			if (s.length() > 1) {
				sb.append(s.substring(1, s.length()).toLowerCase());
			}
			count++;
		}

		if (!sb.toString().equals("")) {
			jsonKey = sb.toString();
		}

		return jsonKey;

	}

	private boolean isHighChart() {
		boolean toReturn = (getTemplate().getName().equals(HIGHCHART_TYPE)) ? true : false;
		return toReturn;
	}
}
