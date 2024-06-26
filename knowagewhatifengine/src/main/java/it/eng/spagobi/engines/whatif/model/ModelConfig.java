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
package it.eng.spagobi.engines.whatif.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pivot4j.PivotModel;
import org.pivot4j.transform.NonEmpty;
import org.pivot4j.ui.command.DrillDownCommand;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.eng.spagobi.engines.whatif.crossnavigation.SpagoBICrossNavigationConfig;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.writeback4j.SbiAliases;
import it.eng.spagobi.writeback4j.SbiScenario;
import it.eng.spagobi.writeback4j.SbiScenarioVariable;
import it.eng.spagobi.writeback4j.WriteBackEditConfig;

public class ModelConfig implements Serializable {

	private static final long serialVersionUID = 2687163910212567575L;
	private String drillType;
	private Boolean showParentMembers;
	private Boolean hideSpans;
	private Boolean showProperties;
	private Boolean showCompactProperties;
	private Boolean suppressEmpty;
	private Boolean enableDrillThrough;

	private Boolean sortingEnabled;
	private int axisToSort;
	private int axis;
	private String sortingPositionUniqueName;
	private String sortMode;
	private int topBottomCount;

	private int startRow;
	private int rowsSet;
	private int rowCount;
	private int startColumn;
	private int columnSet;
	private int columnCount;

	private int pageSize;
	
	private boolean pagination;



	public boolean isPagination() {
		return pagination;
	}
	
	public void setPagination(boolean isPagination){
		this.pagination = isPagination;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public int getRowsSet() {
		return rowsSet;
	}

	public void setRowsSet(int rowsSet) {
		this.rowsSet = rowsSet;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}

	public int getColumnSet() {
		return columnSet;
	}

	public void setColumnSet(int columnSet) {
		this.columnSet = columnSet;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public Boolean getSortingEnabled() {
		return sortingEnabled;
	}

	public void setSortingEnabled(Boolean sortingEnabled) {
		this.sortingEnabled = sortingEnabled;
	}

	public int getAxisToSort() {
		return axisToSort;
	}

	public void setAxisToSort(int axisToSort) {
		this.axisToSort = axisToSort;
	}

	public int getAxis() {
		return axis;
	}

	public void setAxis(int axis) {
		this.axis = axis;
	}

	public String getSortingPositionUniqueName() {
		return sortingPositionUniqueName;
	}

	public void setSortingPositionUniqueName(String sortingPositionUniqueName) {
		this.sortingPositionUniqueName = sortingPositionUniqueName;
	}

	public String getSortMode() {
		return sortMode;
	}

	public void setSortMode(String sortMode) {
		this.sortMode = sortMode;
	}

	public int getTopBottomCount() {
		return topBottomCount;
	}

	public void setTopBottomCount(int topBottomCount) {
		this.topBottomCount = topBottomCount;
	}

	private Integer actualVersion = null;
	private SbiScenario scenario = null;
	private SbiAliases aliases = null;

	private Integer artifactId;
	private String status;
	private String locker;

	private List<String> toolbarVisibleButtons;
	private List<String> toolbarMenuButtons;
	private List<String> toolbarClickedButtons;

	private Map<String, String> dimensionHierarchyMap;

	private SpagoBICrossNavigationConfig crossNavigation;

	public ModelConfig() {

	}

	public ModelConfig(PivotModel pivotModel) {
		drillType = DrillDownCommand.MODE_POSITION;
		showParentMembers = false;
		hideSpans = false;
		showProperties = false;
		enableDrillThrough = false;
		showCompactProperties = false;
		sortingEnabled = false;
		startRow = 0;
		rowsSet = 50;
		rowCount = 1;
		startColumn = 0;
		columnSet = 50;
		columnCount = 1;
		NonEmpty transformNonEmpty = pivotModel.getTransform(NonEmpty.class);
		suppressEmpty = transformNonEmpty.isNonEmpty();

		dimensionHierarchyMap = new HashMap<String, String>();
	}

	public Boolean getEnableDrillThrough() {
		return enableDrillThrough;
	}

	public void setEnableDrillThrough(Boolean enableDrillThrough) {
		this.enableDrillThrough = enableDrillThrough;
	}

	public Boolean getShowCompactProperties() {
		return showCompactProperties;
	}

	public void setShowCompactProperties(Boolean showCompactProperties) {
		this.showCompactProperties = showCompactProperties;
	}

	public Boolean getSuppressEmpty() {
		return suppressEmpty;
	}

	public void setSuppressEmpty(Boolean suppressEmpty) {
		this.suppressEmpty = suppressEmpty;
	}

	public Boolean getShowProperties() {
		return showProperties;
	}

	public void setShowProperties(Boolean showProperties) {
		this.showProperties = showProperties;
	}

	public Boolean getHideSpans() {
		return hideSpans;
	}

	public void setHideSpans(Boolean hideSpans) {
		this.hideSpans = hideSpans;
	}

	public Boolean getShowParentMembers() {
		return showParentMembers;
	}

	public void setShowParentMembers(Boolean showParentMembers) {
		this.showParentMembers = showParentMembers;
	}

	public String getDrillType() {
		return drillType;
	}

	public void setDrillType(String drillType) {
		this.drillType = drillType;
	}

	public Map<String, String> getDimensionHierarchyMap() {
		return dimensionHierarchyMap;
	}

	public void setDimensionHierarchyMap(Map<String, String> dimensionHierarchyMap) {
		this.dimensionHierarchyMap = dimensionHierarchyMap;
	}

	public void setDimensionHierarchy(String dimensionUniqueName, String hierarchyUniqueName) {
		this.dimensionHierarchyMap.put(dimensionUniqueName, hierarchyUniqueName);
	}

	public Integer getActualVersion() {
		// if(actualVersion==null && scenario!=null &&
		// scenario.getWritebackEditConfig()!=null ){
		// return scenario.getWritebackEditConfig().getInitialVersion();
		// }
		return actualVersion;
	}

	public void setActualVersion(Integer actualVersion) {
		this.actualVersion = actualVersion;
	}

	public WriteBackEditConfig getWriteBackConf() {
		if (scenario == null) {
			return null;
		}
		return scenario.getWritebackEditConfig();
	}

	public void setWriteBackConf(WriteBackEditConfig writebackEditConfig) {
		if (scenario != null) {
			scenario.setWritebackEditConfig(writebackEditConfig);
		}
	}

	public void setScenario(SbiScenario scenario) {
		this.scenario = scenario;
	}

	public List<String> getToolbarVisibleButtons() {
		return toolbarVisibleButtons;
	}

	public void setToolbarVisibleButtons(List<String> toolbarVisibleButtons) {
		this.toolbarVisibleButtons = toolbarVisibleButtons;
	}

	public List<String> getToolbarMenuButtons() {
		return toolbarMenuButtons;
	}

	public void setToolbarMenuButtons(List<String> toolbarMenuButtons) {
		this.toolbarMenuButtons = toolbarMenuButtons;
	}

	@JsonIgnore
	public SbiScenario getScenario() {
		return scenario;
	}

	@JsonIgnore
	public Object getVariableValue(String variableName) {
		if (scenario == null) {
			return null;
		}
		SbiScenarioVariable var = scenario.getVariable(variableName);
		if (var != null) {
			String value = var.getValue();
			return var.getType().getTypedType(value);
		} else {
			// if isn't a variable it could be a generic alias
			String value = aliases.getGenericNameFromAlias(variableName);
			if (value != null) {
				return value;
			} else {
				throw new SpagoBIEngineRuntimeException("Cannot calculate Value, Variable or Alias not found: " + variableName);
			}
		}

	}

	public Integer getArtifactId() {
		return artifactId;
	}

	public void setArtifactID(Integer artifactId) {
		this.artifactId = artifactId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLocker() {
		return locker;
	}

	public void setLocker(String locker) {
		this.locker = locker;
	}

	/**
	 * @return the aliases
	 */
	@JsonIgnore
	public SbiAliases getAliases() {
		return aliases;
	}

	/**
	 * @param aliases
	 *            the aliases to set
	 */
	@JsonIgnore
	public void setAliases(SbiAliases aliases) {
		this.aliases = aliases;
	}

	public boolean isWhatIfScenario() {
		return this.scenario != null;
	}

	// for the deserializer
	public void setWhatIfScenario(boolean bool) {
	}

	public SpagoBICrossNavigationConfig getCrossNavigation() {
		return crossNavigation;
	}

	public void setCrossNavigation(SpagoBICrossNavigationConfig crossNavigation) {
		this.crossNavigation = crossNavigation;
	}

	public List<String> getToolbarClickedButtons() {
		return toolbarClickedButtons;
	}

	public void setToolbarClickedButtons(List<String> toolbarClickedButtons) {
		this.toolbarClickedButtons = toolbarClickedButtons;
	}

	/**
	 * Updates the values of the object coping the values of another
	 * configuration.. Not all the modification are copied, id est Scenario and
	 * aliases
	 */
	public void update(ModelConfig source) {
		this.drillType = source.drillType;
		this.showParentMembers = source.showParentMembers;
		this.hideSpans = source.hideSpans;
		this.showProperties = source.showProperties;
		this.suppressEmpty = source.suppressEmpty;
		this.actualVersion = source.actualVersion = null;
		this.sortingEnabled = source.sortingEnabled;
		this.axisToSort = source.axisToSort;
		this.axis = source.axis;
		this.sortingPositionUniqueName = source.sortingPositionUniqueName;
		this.topBottomCount = source.topBottomCount;
		this.status = source.status;
		this.locker = source.locker;
		this.pagination = source.pagination;
		this.toolbarVisibleButtons = source.toolbarVisibleButtons;
		this.toolbarMenuButtons = source.toolbarMenuButtons;
		this.toolbarClickedButtons = source.toolbarClickedButtons;

		this.dimensionHierarchyMap = source.dimensionHierarchyMap;
	}

	

}
