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
package it.eng.spagobi.pivot4j.ui.html;

import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;
import org.pivot4j.sort.SortCriteria;
import org.pivot4j.transform.PlaceMembersOnAxes;
import org.pivot4j.ui.CellTypes;
import org.pivot4j.ui.collector.NonInternalPropertyCollector;
import org.pivot4j.ui.command.DrillDownCommand;
import org.pivot4j.ui.command.UICommand;
import org.pivot4j.ui.command.UICommandParameters;
import org.pivot4j.ui.html.HtmlRenderCallback;
import org.pivot4j.ui.table.TableRenderContext;
import org.pivot4j.util.CssWriter;
import org.pivot4j.util.RenderPropertyUtils;

import it.eng.spagobi.engines.whatif.crossnavigation.CrossNavigationManager;
import it.eng.spagobi.engines.whatif.crossnavigation.SpagoBICrossNavigationConfig;
import it.eng.spagobi.engines.whatif.crossnavigation.TargetClickable;
import it.eng.spagobi.engines.whatif.model.PivotJsonHTMLSerializer;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class WhatIfHTMLRendereCallback extends HtmlRenderCallback {
	private boolean showProperties = false;
	private final HashMap<Member, Integer> memberPositions;
	private boolean measureOnRows;
	private Map<Integer, String> positionMeasureMap;
	private boolean initialized = false;
	private final Map<String, Object> properties;
	private static final String pathToImages = "../../../../knowage/themes/commons/img/olap/";

	public WhatIfHTMLRendereCallback(Writer writer) {
		super(writer);
		memberPositions = new HashMap<Member, Integer>();
		showProperties = true;
		setRowHeaderLevelPadding(20);
		properties = new HashMap<String, Object>();
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public void addProperty(String key, Object value) {
		properties.put(key, value);
	}

	/**
	 * Translate the ordinal from the system of subsetted mdx to the system of the plain mdx cell set
	 *
	 * @param ordinal
	 * @return
	 */
	private int getOrdinalNoSubset(int ordinal) {
		Integer columnOffset = (Integer) getProperty(PivotJsonHTMLSerializer.COLUMN_OFFSET);
		Integer rowOffset = (Integer) getProperty(PivotJsonHTMLSerializer.ROW_OFFSET);
		Integer axisLength = (Integer) getProperty(PivotJsonHTMLSerializer.AXIS_LENGTH);
		Integer axisSubsetLength = (Integer) getProperty(PivotJsonHTMLSerializer.SUBSET_AXIS_LENGTH);

		int rowNumber = rowOffset + Math.abs(ordinal / axisSubsetLength);
		int columnNumber = columnOffset + ordinal % axisSubsetLength;

		// translate on rows
		ordinal = axisLength * rowNumber;

		// translate on columns
		ordinal = ordinal + columnNumber;

		return ordinal;
	}

	@Override
	protected Map<String, String> getCellAttributes(TableRenderContext context) {
		StringWriter sw = new StringWriter();
		CssWriter cssw = new CssWriter(sw);
		Map<String, String> attributes = super.getCellAttributes(context);

		if (context.getCellType() == CellTypes.VALUE && context.getCell() != null) {

			initializeInternal(context);

			// need the name of the measure to check if it's editable
			String measureName = getMeasureName(context);

			int colId = context.getColumnIndex();
			int rowId = context.getRowIndex();
			int positionId = getOrdinalNoSubset(context.getCell().getOrdinal());
			String value = context.getCell().getFormattedValue();
			if (context.getCell().getValue() != null && context.getCell().getFormattedValue() != null) {
				String formatedValue = context.getCell().getFormattedValue();
				String style;
				ArrayList<String> fv = trimStyle(formatedValue);// formatedValue.split("=*\\s*\" | =*\\s*\'");

				for (int i = 0; i < fv.size(); i++) {
					String styles[] = fv.get(i).split("\\s*=\\s*");
					if (styles.length == 2) {
						cssw.writeStyle(styles[0], styles[1] + "!important");

					}
				}
				cssw.writeStyle("padding", "3px");
				style = sw.toString();
				attributes.put("style", style);
			}

			String id = positionId + "!" + rowId + "!" + colId + "!" + System.currentTimeMillis() % 1000;
			attributes.put("id", id);
			attributes.put("measureName", measureName);
			attributes.put("ordinal", String.valueOf(positionId));
			attributes.put("value", value);
			attributes.put("cell", null);
		} else if (context.getCellType() == CellTypes.LABEL) {
			String uniqueName = context.getMember().getUniqueName();
			String level = context.getMember().getLevel().getUniqueName();
			String dimensionType = null;
			String dimensionUniqueName = context.getMember().getDimension().getName();
			String hierarchyUniqueName = context.getMember().getHierarchy().getUniqueName();
			String position = context.getPosition().getMembers().toString();
			try {
				dimensionType = context.getMember().getDimension().getDimensionType().xmlaName();
			} catch (OlapException e) {

			}
			String parentMember = null;

			if (context.getMember().getParentMember() != null) {
				parentMember = context.getMember().getParentMember().getUniqueName();
			} else {
				parentMember = context.getMember().getUniqueName();
			}

			int axisOrdinal = context.getAxis().axisOrdinal();

			attributes.put("axisOrdinal", String.valueOf(axisOrdinal));
			attributes.put("dimensionUniqueName", dimensionUniqueName);
			attributes.put("dimensionType", dimensionType);
			attributes.put("parentMember", parentMember);
			attributes.put("uniqueName", uniqueName);
			attributes.put("level", level);
			attributes.put("hierarchyUniqueName", hierarchyUniqueName);
			attributes.put("position", position);
			attributes.put("member", null);

		}
		return attributes;
	}

	@Override
	public void renderCommands(TableRenderContext context, List<UICommand<?>> commands) {

		String drillMode = context.getRenderer().getDrillDownMode();

		if (!isEmptyNonPropertyCell(context)) {
			if (context.getCellType() == CellTypes.VALUE && context.getCell() != null) {

				int positionId = getOrdinalNoSubset(context.getCell().getOrdinal());
				String ordinal = String.valueOf(positionId);

				if (context.getRenderer().getEnableDrillThrough()) {

					Map<String, String> attributes = new TreeMap<String, String>();
					attributes.put("src", pathToImages + "ico_search.gif");
					attributes.put("id", "drillt");
					attributes.put("drillThrough", "(" + ordinal + ")");
					startElement("img", attributes);
					endElement("img");
				}
			}

			if (context.getMember() != null && context.getMember().getMemberType() != null
					&& !context.getMember().getMemberType().name().equalsIgnoreCase("Measure")) {
				Map<String, String> attributes = new TreeMap<String, String>();
				NonInternalPropertyCollector np = new NonInternalPropertyCollector();
				List<Property> properties = np.getProperties(context.getMember().getLevel());

				if (properties != null && !properties.isEmpty()) {
					Map<String, String> attributes1 = new TreeMap<String, String>();
					attributes.put("src", pathToImages + "show_props.png");
					attributes1.put("getProps", "('" + context.getMember().getUniqueName() + "')");
					startElement("img", attributes1);
					endElement("img");

				}

				if (commands != null && !commands.isEmpty()) {
					for (UICommand<?> command : commands) {
						String cmd = command.getName();
						UICommandParameters commandParams = command.createParameters(context);

						int axis = 0;
						if (context.getAxis() != null) {
							axis = context.getAxis().axisOrdinal();
						}
						int memb = 0;
						if (context.getPosition() != null) {
							memb = context.getAxis().axisOrdinal();
						}

						String uniqueName = context.getMember().getUniqueName();
						String positionUniqueName = context.getPosition().getMembers().toString();
						if (cmd != null) {
							if ((cmd.equalsIgnoreCase("collapsePosition") || cmd.equalsIgnoreCase("drillUp") || cmd.equalsIgnoreCase("collapseMember"))
									&& !drillMode.equals(DrillDownCommand.MODE_REPLACE)) {

								Map<String, String> drillUpAttributes = new TreeMap<String, String>();
								drillUpAttributes.put("axis", String.valueOf(commandParams.getAxisOrdinal()));
								drillUpAttributes.put("position", String.valueOf(commandParams.getMemberOrdinal()));
								drillUpAttributes.put("memberOrdinal", String.valueOf(memb));
								drillUpAttributes.put("uniqueName", uniqueName);
								drillUpAttributes.put("positionUniqueName", positionUniqueName);
								startElement("drillup", drillUpAttributes);
								endElement("drillup");

							} else if ((cmd.equalsIgnoreCase("expandPosition") || cmd.equalsIgnoreCase("drillDown") || cmd.equalsIgnoreCase("expandMember"))
									&& commandParams.getMemberOrdinal() > -1) {

								Map<String, String> drillDownAttributes = new TreeMap<String, String>();
								drillDownAttributes.put("axis", String.valueOf(axis));
								drillDownAttributes.put("position", String.valueOf(commandParams.getMemberOrdinal()));
								drillDownAttributes.put("memberOrdinal", String.valueOf(memb));
								drillDownAttributes.put("uniqueName", uniqueName);
								drillDownAttributes.put("positionUniqueName", positionUniqueName);
								startElement("drilldown", drillDownAttributes);
								endElement("drilldown");
							} else {
								if (context.getAxis() == Axis.ROWS && !isPropertyCell(context)) {

									attributes.put("src", pathToImages + "nodrill.png");
									attributes.put("style", "padding : 2px");
									startElement("img", attributes);
									endElement("img");
								}

							}
							if (cmd.equalsIgnoreCase("sort")) {
								setSortingCommand(context);

							}

						}
					}
				} else {
					if (context.getAxis() == Axis.ROWS && !isPropertyCell(context)) {

						attributes.put("src", pathToImages + "nodrill.png");
						attributes.put("style", "padding : 2px");
						startElement("img", attributes);
						endElement("img");
					}
				}
			} else if (context.getMember() != null && context.getMember().getMemberType() != null
					&& context.getMember().getMemberType().name().equalsIgnoreCase("Measure")) {

				for (UICommand<?> command : commands) {
					String cmd = command.getName();
					if (cmd.equalsIgnoreCase("sort")) {
						setSortingCommand(context);

					}

				}
			}
		}

	}

	@Override
	public void renderContent(TableRenderContext context, String label, Double value) {
		if (label != null && label.contains("|")) {
			label = formatValue(label, value);
		}
		String link = null;
		String propertyCategory = context.getRenderPropertyCategory();
		RenderPropertyUtils propertyUtils = getRenderPropertyUtils();
		link = propertyUtils.getString("link", propertyCategory, null);

		if (link == null) {
			Map<String, String> attributes = new TreeMap<String, String>();
			String drillMode = context.getRenderer().getDrillDownMode();
			if ((context.getCellType() == "title") && !label.equalsIgnoreCase("Measures")) {

				int colIdx = context.getColumnIndex();
				int rowIdx = context.getRowIndex();

				int axis = 0;
				if (context.getAxis() != null) {
					axis = context.getAxis().axisOrdinal();
				}
				int memb = 0;
				if (context.getAxis() == Axis.COLUMNS) {
					memb = rowIdx / 2;
				} else {
					memb = colIdx;
				}
				int pos = 0;
				if (context.getAxis() == Axis.COLUMNS) {
					pos = rowIdx;
				} else {
					pos = colIdx;
				}

				if (drillMode.equals(DrillDownCommand.MODE_REPLACE)) {
					Hierarchy h = context.getHierarchy();
					PlaceMembersOnAxes pm = context.getModel().getTransform(PlaceMembersOnAxes.class);

					List<Member> visibleMembers = pm.findVisibleMembers(h);
					int d = 0;
					for (Member m : visibleMembers) {
						Level l = m.getLevel();
						d = l.getDepth();
						if (d != 0) {
							break;
						}
					}

					// For drill replace the context.getPosition() and
					// context.getMember are empty.
					String uniqueName = "x";

					if (context != null) {
						if (context.getMember() != null) {
							uniqueName = context.getMember().getUniqueName();
						}

					}

					if (d != 0) {
						context.getMember();
						attributes.put("src", pathToImages + "arrow-up.png");
						attributes.put("drillUp",
								"(" + axis + " , " + pos + " , " + memb + ",'" + uniqueName + "','" + context.getHierarchy().getUniqueName() + "' )");
						startElement("img", attributes);
						endElement("img");
					}
					writeContent(label);

				} else if (!drillMode.equals(DrillDownCommand.MODE_REPLACE)) {
					writeContent(label);
				}
			} else {

				// start OSMOSIT cross nav button
				SpagoBIPivotModel sbiModel = (SpagoBIPivotModel) context.getModel();
				SpagoBICrossNavigationConfig crossNavigation = sbiModel.getCrossNavigation();

				if (crossNavigation != null && crossNavigation.isButtonClicked() && context.getCellType() == CellTypes.VALUE) {

					int colId = context.getColumnIndex();
					int rowId = context.getRowIndex();
					int positionId = getOrdinalNoSubset(context.getCell().getOrdinal());
					String id = positionId + "!" + rowId + "!" + colId + "!" + System.currentTimeMillis() % 1000;
					attributes.put("src", pathToImages + "cross-navigation.png");
					String coordinatesAsString = StringUtils.join(context.getCell().getCoordinateList(), ",");
					attributes.put("cellClickCreateCrossNavigationMenu", "('" + coordinatesAsString + "')");
					attributes.put("id", id);
					startElement("img", attributes);
					endElement("img");

					writeContent(label);
				} else {
					// TODO: OSMOSIT create member clickable
					List<TargetClickable> targetsClickable = sbiModel.getTargetsClickable();
					if (targetsClickable != null && targetsClickable.size() > 0) {
						Member member = context.getMember();
						if (member != null) {
							String url = CrossNavigationManager.buildClickableUrl(member, targetsClickable);
							if (url != null) {
								attributes.put("onClick", url);
								attributes.remove("src");
								attributes.put("href", "#");

								startElement("a", attributes);
								writeContent(label);
								endElement("a");

							} else {
								writeContent(label);
							}
						} else {

							writeContent(label);
						}
					} else {

						writeContent(label);
					}

				}

			}
		} else {
			Map<String, String> attributes = new HashMap<String, String>(1);
			attributes.put("href", link);

			startElement("a", attributes);
			writeContent(label);
			endElement("a");
		}

	}

	private boolean isPropertyCell(TableRenderContext context) {

		if (showProperties && context.getLevel() != null && isEmptyNonPropertyCell(context) && context.getRenderer().getPropertyCollector() != null) {
			List<Property> propertieds = context.getRenderer().getPropertyCollector().getProperties(context.getLevel());
			return (propertieds != null && propertieds.size() > 0);
		}
		return false;
	}

	private boolean isEmptyNonPropertyCell(TableRenderContext context) {

		Member member = context.getMember();

		if (member != null && showProperties) {
			Integer memberPosition = null;

			if (context.getAxis().axisOrdinal() == (Axis.ROWS.axisOrdinal())) {
				memberPosition = context.getColumnIndex();

			} else {
				memberPosition = context.getRowIndex();
			}
			if (!memberPositions.containsKey(member)) {
				memberPositions.put(member, memberPosition);

			}

			Integer previousPositions = memberPositions.get(member);

			if (previousPositions == memberPosition) {
				return false;
			}
			return true;
		}

		return false;
	}

	private String getMeasureName(TableRenderContext context) {
		int coordinate;
		if (this.measureOnRows) {
			coordinate = context.getRowIndex();
		} else {
			coordinate = context.getColumnIndex();
		}
		String measureName = this.positionMeasureMap.get(coordinate);

		if (measureName == null) {
			measureName = ((SpagoBICellWrapper) context.getCell()).getMeasureName();
			this.positionMeasureMap.put(coordinate, measureName);
		}

		return measureName;

	}

	private void initializeInternal(TableRenderContext context) {
		if (!this.initialized) {
			this.measureOnRows = true;
			this.initialized = true;
			this.positionMeasureMap = new HashMap<Integer, String>();

			// check if the measures are in the rows or in the columns
			List<Member> columnMembers = null;
			Position p = context.getColumnPosition();
			if (p != null) {
				columnMembers = p.getMembers();
			}

			try {
				if (columnMembers != null) {
					for (int i = 0; i < columnMembers.size(); i++) {
						Member member = columnMembers.get(i);
						if (member.getDimension().getDimensionType().equals(Dimension.Type.MEASURE)) {
							this.measureOnRows = false;
						}
					}
				}
			} catch (OlapException e) {
				throw new SpagoBIEngineRuntimeException("Erro getting the measure of a rendered cell ", e);
			}
		}
	}

	private void setSortingCommand(TableRenderContext context) {
		int axis = 0;
		if (context.getAxis() != null) {
			axis = context.getAxis().axisOrdinal();
		}

		int axisToSort = 0;
		if (axis == Axis.ROWS.axisOrdinal()) {
			axisToSort = Axis.COLUMNS.axisOrdinal();
		} else {
			axisToSort = Axis.ROWS.axisOrdinal();
		}

		Map<String, String> attributes = new TreeMap<String, String>();

		if (context.getRenderer().getEnableSort()) {
			if (context.getModel().isSorting(context.getPosition()) && context.getModel().getSortCriteria() != null) {
				if (context.getModel().getSortCriteria().equals(SortCriteria.ASC) || context.getModel().getSortCriteria().equals(SortCriteria.BASC)
						|| context.getModel().getSortCriteria().equals(SortCriteria.TOPCOUNT)) {
					if (axisToSort == Axis.ROWS.axisOrdinal()) {
						attributes.put("src", pathToImages + "DESC-rows.png");
					} else {
						attributes.put("src", pathToImages + "DESC-columns.png");
					}

					attributes.put("sort", "(" + axisToSort + " , " + axis + " , '" + context.getPosition().getMembers().toString() + "' )");
					startElement("img", attributes);
					endElement("img");
				} else if (context.getModel().getSortCriteria().equals(SortCriteria.DESC) || context.getModel().getSortCriteria().equals(SortCriteria.BDESC)
						|| context.getModel().getSortCriteria().equals(SortCriteria.BOTTOMCOUNT)) {

					if (axisToSort == Axis.ROWS.axisOrdinal()) {
						attributes.put("src", pathToImages + "ASC-rows.png");
					} else {
						attributes.put("src", pathToImages + "ASC-columns.png");
					}

					attributes.put("sort", "(" + axisToSort + " , " + axis + " , '" + context.getPosition().getMembers().toString() + "' )");
					startElement("img", attributes);
					endElement("img");
				}
			} else {
				context.getModel().setSorting(false);
				if (axisToSort == Axis.ROWS.axisOrdinal()) {
					attributes.put("src", pathToImages + "noSortRows.png");
				} else {
					attributes.put("src", pathToImages + "noSortColumns.png");
				}

				attributes.put("sort", "(" + axisToSort + " , " + axis + " , '" + context.getPosition().getMembers().toString() + "' )");

				startElement("img", attributes);
				endElement("img");
			}

		}

	}

	public static ArrayList<String> trimStyle(String formatedValue) {
		ArrayList<String> result = new ArrayList<String>();
		String results[] = formatedValue.split("\\s*\\|\\s*");

		for (int i = 0; i < results.length; i++) {
			if (!results[i].contains("format") && results[i].contains("=")) {
				result.add(results[i]);
			}
		}
		return result;
	}

	private String formatValue(String label, double value) {
		String resArr[] = label.split("\\|");

		for (int i = 0; i < resArr.length; i++) {
			if (resArr[i].contains("format")) {
				String result[] = resArr[i].split("\\s*=\\s*");
				String pattern = result[1];
				NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
				DecimalFormat df = (DecimalFormat) nf;
				df.applyPattern(pattern);
				return df.format(value);
			}
		}
		return value + "";
	}
}
