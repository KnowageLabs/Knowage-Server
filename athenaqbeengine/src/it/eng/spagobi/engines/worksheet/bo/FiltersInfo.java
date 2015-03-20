/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.bo;

import it.eng.qbe.query.WhereField;
import it.eng.spagobi.engines.worksheet.bo.AttributePresentationOption.AdmissibleValues;
import it.eng.spagobi.engines.worksheet.exceptions.WrongConfigurationForFiltersOnDomainValuesException;
import it.eng.spagobi.tools.dataset.bo.AbstractCustomDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FiltersInfo {

	private WorkSheetDefinition workSheetDefinition = null;
	private IDataSet dataSet = null;
	private List<WhereField> additionalFilters = null;
	
	public FiltersInfo(WorkSheetDefinition workSheetDefinition, IDataSet dataSet) {
		this.workSheetDefinition = workSheetDefinition;
		this.dataSet = dataSet;
	}
	
	/**
	 * Returns a map where keys are fields' technical names (not seen by the user), and values are fields values (not decoded)
	 * @param sheetName The sheet name
	 * @return Returns a map where keys are fields' technical names (not seen by the user), and values are fields values (not decoded)
	 * @throws WrongConfigurationForFiltersOnDomainValuesException
	 */
	public Map<String, List<String>> getFiltersInfoAsRawValuesMap(String sheetName) throws WrongConfigurationForFiltersOnDomainValuesException {
		List<Attribute> globalFilters = workSheetDefinition.getGlobalFilters();
		Sheet sheet = workSheetDefinition.getSheet(sheetName);
		List<Attribute> sheetFilters = sheet.getFiltersOnDomainValues();
		return WorkSheetDefinition.mergeDomainValuesFilters(globalFilters, sheetFilters);
	}
	
	/**
	 * Returns a map where keys are fields' labels (as seen by the user), and values are fields decoded values (as seen by the user)
	 * @param sheetName The sheet name
	 * @return Returns a map where keys are fields' labels (as seen by the user), and values are fields decoded values (as seen by the user)
	 * @throws WrongConfigurationForFiltersOnDomainValuesException
	 */
	public Map<String, List<String>> getFiltersInfoAsMap(String sheetName) throws WrongConfigurationForFiltersOnDomainValuesException {
		Map<String, List<String>> rawValues = this.getFiltersInfoAsRawValuesMap(sheetName);
		this.addAdditionalFilters(rawValues);
		Map<String, List<String>> decodedValues = this.getDecodedValues(rawValues);
		Map<String, List<String>> toReturn = this.decodeKeys(decodedValues);
		return toReturn;
	}

	private void addAdditionalFilters(Map<String, List<String>> rawValues) {
		if (this.additionalFilters != null && !additionalFilters.isEmpty()) {
			Iterator<WhereField> it = this.additionalFilters.iterator();
			while (it.hasNext()) {
				WhereField field = it.next();
				String fieldName = field.getLeftOperand().values[0];
				String[] values = field.getRightOperand().values;
				List<String> valuesAsList = Arrays.asList(values);
				rawValues.put(fieldName, valuesAsList);
			}
		}
	}

	private Map<String, List<String>> decodeKeys(
			Map<String, List<String>> decodedValues) {
		IMetaData metadata = dataSet.getMetadata();
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		Iterator<String> iterator = decodedValues.keySet().iterator();
		while (iterator.hasNext()) {
			String fieldName = iterator.next();
			int index = metadata.getFieldIndex(fieldName);
			String key = metadata.getFieldMeta(index).getAlias();
			if (key == null || key.trim().equals("")) {
				key = fieldName;
			}
			List<String> values = decodedValues.get(fieldName);
			toReturn.put(key, values);
		}
		return toReturn;
	}

	private Map<String, List<String>> getDecodedValues(
			Map<String, List<String>> rawValues) {
		Map<String, List<String>> toReturn = rawValues;
		if (dataSet instanceof AbstractCustomDataSet) {
			AbstractCustomDataSet abstractCustomDataSet = (AbstractCustomDataSet) dataSet;
			Map<String, List<String>> toBeDecoded = getCodesToBeDecoded(rawValues);
			Map<String, List<String>> decoded = abstractCustomDataSet.getDomainDescriptions(toBeDecoded);
			toReturn = substituteCodeWithDescriptions(rawValues, decoded);
		}
		return toReturn;
	}
	
	private Map<String, List<String>> substituteCodeWithDescriptions(Map<String, List<String>> codesMap,
			Map<String, List<String>> descriptionsMap) {
		Map<String, List<String>> toReturn = codesMap;
		Iterator<String> it = descriptionsMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			AdmissibleValues presentation = getAttributePresentation(key);
			if (presentation.equals(AdmissibleValues.description)) {
				toReturn.put(key, descriptionsMap.get(key));
			}
			if (presentation.equals(AdmissibleValues.both)) {
				List<String> codes = codesMap.get(key);
				List<String> descriptions = descriptionsMap.get(key);
				List<String> newValues = new ArrayList<String>();
				for ( int i = 0 ; i < codes.size() ; i++ ) {
					String aCode = codes.get(i);
					String aDescription = descriptions.get(i);
					newValues.add(aCode + " - " + aDescription);
				}
				toReturn.put(key, newValues);
			} 
		}
		return toReturn;
	}
	
	private Map<String, List<String>> getCodesToBeDecoded(
			Map<String, List<String>> filters) {
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		Iterator<String> iterator = filters.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			AdmissibleValues presentation = getAttributePresentation(key);
			if (!presentation.equals(AdmissibleValues.code)) {
				toReturn.put(key, filters.get(key));
			}
		}
		return toReturn;
	}
	
	
	private AdmissibleValues getAttributePresentation(String fieldId) {
		WorksheetFieldsOptions options = workSheetDefinition.getFieldsOptions();
		FieldOptions fieldOptions = options.getOptionsForFieldByFieldId(fieldId);
		if (fieldOptions != null) {
			List<FieldOption> list = fieldOptions.getOptions();
			Iterator<FieldOption> it = list.iterator();
			while (it.hasNext()) {
				FieldOption option = it.next();
				if (option instanceof AttributePresentationOption) {
					AttributePresentationOption theOption = (AttributePresentationOption) option;
					String valueStr = theOption.getValue().toString();
					AdmissibleValues value = AdmissibleValues.valueOf(valueStr);
					return value;
				}
			}
		}
		return AdmissibleValues.description; // default value is description
	}

	public void setAdditionalFilters(List<WhereField> additionalFilters) {
		this.additionalFilters = additionalFilters;
	}

	public List<WhereField> getAdditionalFilters() {
		return additionalFilters;
	}
	
}
