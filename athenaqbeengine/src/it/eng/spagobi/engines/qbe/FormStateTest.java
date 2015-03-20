/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe;

import it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder.FormStateLoaderFactory;
import it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder.IFormStateLoader;
import it.eng.spagobi.engines.qbe.template.QbeJSONTemplateParser;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Test class for the FormState class
 * @author Ghedin Alberto
 *
 */
public class FormStateTest {
	
	public static transient Logger logger = Logger.getLogger(FormStateTest.class);
	
	public static void main(String[] args){
		try {
			String formStateString= "{\"staticOpenFilters\":[{\"field\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvCategoryDes\",\"id\":\"openFilter-1\",\"queryType\":\"custom\",\"singleSelection\":true,\"lookupQuery\":\"q2\",\"text\":\"Category\",\"orderBy\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvCategoryDes\",\"orderType\":\"ASC\",\"maxSelectedNumber\":\"1\",\"operator\":\"EQUALS TO\"},{\"field\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvServiceDes\",\"id\":\"openFilter-2\",\"queryType\":\"custom\",\"lookupQuery\":\"q3\",\"text\":\"Service\",\"orderBy\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvServiceDes\",\"orderType\":\"ASC\",\"maxSelectedNumber\":\"5\",\"operator\":\"EQUALS TO\"},{\"field\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvRequestAreaCod\",\"id\":\"openFilter-3\",\"queryType\":\"custom\",\"lookupQuery\":\"q4\",\"text\":\"Request Area\",\"orderBy\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvRequestAreaCod\",\"maxSelectedNumber\":\"5\",\"operator\":\"EQUALS TO\"},{\"field\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvConfitemDes\",\"id\":\"openFilter-4\",\"queryType\":\"custom\",\"lookupQuery\":\"q5\",\"text\":\"Configuration Item\",\"orderBy\":\"it.eng.modm.VwDmServiceTempAcq:id.dsvConfitemDes\",\"orderType\":\"ASC\",\"maxSelectedNumber\":\"5\",\"operator\":\"EQUALS TO\"},{\"field\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"id\":\"openFilter-5\",\"queryType\":\"custom\",\"lookupQuery\":\"q7\",\"text\":\"Process\",\"orderBy\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"orderType\":\"ASC\",\"maxSelectedNumber\":\"5\",\"operator\":\"EQUALS TO\"},{\"field\":\"it.eng.modm.VwProcessDm:id.dpImportanceDes\",\"id\":\"openFilter-6\",\"queryType\":\"custom\",\"lookupQuery\":\"q6\",\"text\":\"Importance\",\"orderBy\":\"it.eng.modm.VwProcessDm:id.dpImportanceDes\",\"orderType\":\"DESC\",\"maxSelectedNumber\":\"6\",\"operator\":\"EQUALS TO\"},{\"field\":\"it.eng.modm.FtTicket::dmOrigination(tikm_origination_key):doOriginationDes\",\"id\":\"openFilter-7\",\"singleSelection\":true,\"text\":\"Origination\",\"orderBy\":\"it.eng.modm.FtTicket::dmOrigination(tikm_origination_key):doOriginationDes\",\"orderType\":\"DESC\",\"maxSelectedNumber\":\"1\",\"operator\":\"EQUALS TO\"}],\"groupingVariables\":[{\"id\":\"groupingVariable-1\",\"admissibleFields\":[{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYear\",\"text\":\"Year\"},{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYearMonth\",\"text\":\"Year-Month\"},{\"field\":\"it.eng.modm.FtTicket::dmCompany(tikm_company_key):dcyCompanyDes\",\"text\":\"Customer\"},{\"field\":\"it.eng.modm.FtTicket::dmCompany(tikm_company_key):dcyOrgl2Des\",\"text\":\"Organization\"},{\"field\":\"it.eng.modm.FtTicket::dmCompany(tikm_company_key):dcyLocationDes\",\"text\":\"Location\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvCategoryDes\",\"text\":\"Category\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvServiceDes\",\"text\":\"Service\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvRequestAreaCod\",\"text\":\"Request Area\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvConfitemDes\",\"text\":\"Configuration Item\"},{\"field\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"text\":\"Process\"},{\"field\":\"it.eng.modm.VwProcessDm:id.dpImportanceDes\",\"text\":\"Importance\"},{\"field\":\"it.eng.modm.FtTicket::dmOrigination(tikm_origination_key):doOriginationDes\",\"text\":\"Origination\"}]},{\"id\":\"groupingVariable-2\",\"admissibleFields\":[{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYear\",\"text\":\"Year\"},{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYearMonth\",\"text\":\"Year-Month\"},{\"field\":\"it.eng.modm.FtTicket::dmCompany(tikm_company_key):dcyCompanyDes\",\"text\":\"Customer\"},{\"field\":\"it.eng.modm.FtTicket::dmCompany(tikm_company_key):dcyOrgl2Des\",\"text\":\"Organization\"},{\"field\":\"it.eng.modm.FtTicket::dmCompany(tikm_company_key):dcyLocationDes\",\"text\":\"Location\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvCategoryDes\",\"text\":\"Category\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvServiceDes\",\"text\":\"Service\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvRequestAreaCod\",\"text\":\"Request Area\"},{\"field\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvConfitemDes\",\"text\":\"Configuration Item\"},{\"field\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"text\":\"Process\"},{\"field\":\"it.eng.modm.VwProcessDm:id.dpImportanceDes\",\"text\":\"Importance\"},{\"field\":\"it.eng.modm.FtTicket::dmOrigination(tikm_origination_key):doOriginationDes\",\"text\":\"Origination\"}]}],\"dynamicFilters\":[{\"id\":\"dynamicFilter-1\",\"title\":\"Anno (YYYY)\",\"admissibleFields\":[{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYear\",\"text\":\"Year\"}],\"operator\":\"EQUALS TO\"},{\"id\":\"dynamicFilter-2\",\"title\":\"Anno-Mese (YYYY-MM)\",\"admissibleFields\":[{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYearMonth\",\"text\":\"Year-Month\"}],\"operator\":\"BETWEEN\"},{\"id\":\"dynamicFilter-3\",\"title\":\"Trimestre (YYYY-Q)\",\"admissibleFields\":[{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYearQuarter\",\"text\":\"Year-Quarter\"}],\"operator\":\"BETWEEN\"}],\"staticClosedFilters\":[{\"id\":\"xorFilter-1\",\"singleSelection\":true,\"title\":\"Processo\",\"allowNoSelection\":true,\"noSelectionText\":\"Tutti\",\"options\":[{\"expression\":{\"value\":\"$F{TzLAqsgr}\",\"childNodes\":[],\"type\":\"NODE_CONST\"},\"id\":\"option-1\",\"text\":\"Incident Management\",\"filters\":[{\"id\":\"TzLAqsgr\",\"leftOperandDescription\":\"Process\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"rightOperandValue\":[\"Inc\"],\"operator\":\"STARTS WITH\"}]},{\"expression\":{\"value\":\"$F{CnJyenyL}\",\"childNodes\":[],\"type\":\"NODE_CONST\"},\"id\":\"option-2\",\"text\":\"Change Management\",\"filters\":[{\"id\":\"CnJyenyL\",\"leftOperandDescription\":\"Process\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"rightOperandValue\":[\"Cha\"],\"operator\":\"STARTS WITH\"}]},{\"expression\":{\"value\":\"$F{IbmIQtbB}\",\"childNodes\":[],\"type\":\"NODE_CONST\"},\"id\":\"option-3\",\"text\":\"Query Management\",\"filters\":[{\"id\":\"IbmIQtbB\",\"leftOperandDescription\":\"Process\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"rightOperandValue\":[\"Que\"],\"operator\":\"STARTS WITH\"}]},{\"expression\":{\"value\":\"$F{vKrblZBH}\",\"childNodes\":[],\"type\":\"NODE_CONST\"},\"id\":\"option-4\",\"text\":\"Problem Management\",\"filters\":[{\"id\":\"vKrblZBH\",\"leftOperandDescription\":\"Process\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.VwProcessDm:id.dpProcessDes\",\"rightOperandValue\":[\"Pro\"],\"operator\":\"STARTS WITH\"}]},{\"expression\":{\"value\":\"AND\",\"childNodes\":[{\"value\":\"$F{IiVbacxi}\",\"childNodes\":[],\"type\":\"NODE_CONST\"},{\"value\":\"$F{FPvzzqEZ}\",\"childNodes\":[],\"type\":\"NODE_CONST\"},{\"value\":\"$F{xgRMVfDP}\",\"childNodes\":[],\"type\":\"NODE_CONST\"},{\"value\":\"$F{qgVczWhq}\",\"childNodes\":[],\"type\":\"NODE_CONST\"}],\"type\":\"NODE_OP\"},\"id\":\"option-5\",\"text\":\"Altro\",\"filters\":[{\"id\":\"IiVbacxi\",\"leftOperandDescription\":\"dpProcessCod\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.FtTicket::dmProcess(tikm_process_key):dpProcessCod\",\"rightOperandValue\":[\"INC\"],\"operator\":\"NOT EQUALS TO\"},{\"id\":\"FPvzzqEZ\",\"leftOperandDescription\":\"dpProcessCod\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.FtTicket::dmProcess(tikm_process_key):dpProcessCod\",\"rightOperandValue\":[\"CHA\"],\"operator\":\"NOT EQUALS TO\"},{\"id\":\"xgRMVfDP\",\"leftOperandDescription\":\"dpProcessCod\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.FtTicket::dmProcess(tikm_process_key):dpProcessCod\",\"rightOperandValue\":[\"QUE\"],\"operator\":\"NOT EQUALS TO\"},{\"id\":\"qgVczWhq\",\"leftOperandDescription\":\"dpProcessCod\",\"booleanConnector\":\"AND\",\"leftOperandValue\":\"it.eng.modm.FtTicket::dmProcess(tikm_process_key):dpProcessCod\",\"rightOperandValue\":[\"PRO\"],\"operator\":\"NOT EQUALS TO\"}]}]}]}";
			String formValueString = "{\"staticOpenFilters\":{\"staticOpenFiltersCategory\":[],\"staticOpenFiltersService\":[],\"staticOpenFiltersRequest Area\":[],\"staticOpenFiltersImportance\":[\"Severity 2\"],\"staticOpenFiltersOrigination\":[],\"staticOpenFiltersConfiguration Item\":[],\"staticOpenFiltersProcess\":[]},\"groupingVariables\":{\"groupingVariable-1\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYearMonth\",\"groupingVariable-2\":\"it.eng.modm.FtTicket::dmService(tikm_service_key):dsvServiceDes\"},\"dynamicFilters\":{\"dynamicFiltersAnno (YYYY)\":{\"field\":\"\",\"value\":\"\"},\"dynamicFiltersAnno-Mese (YYYY-MM)\":{\"field\":\"\",\"value\":\"\"},\"dynamicFiltersTrimestre (YYYY-Q)\":{\"field\":\"it.eng.modm.FtTicket::dmTime(tikm_time_key):dtYearQuarter\",\"tovalue\":\"2010-4\",\"fromvalue\":\"2010-3\"}},\"staticClosedFilters\":{\"onOffFilters\":{},\"xorFilters\":{\"staticClosedFiltersProcesso\":\"noSelection\"}}}";

			FormState formState = createFormState(  formStateString,  formValueString);

			JSONObject jo = formState.getFormStateValues();
			logger.info(jo);
			
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * Create an instance of the object FormState
	 * @param formStateString
	 * @param formValueString
	 * @return
	 * @throws JSONException
	 */
	private static FormState createFormState( String formStateString, String formValueString) throws JSONException{
		JSONObject formStateJSON = null;
		JSONObject rowDataJSON = null;
		JSONObject valuesDataJSON = null;
		String encodingFormatVersion;
		FormState formState = new FormState();

		rowDataJSON = new JSONObject(formStateString);
		valuesDataJSON = new JSONObject(formValueString);

		try {
			encodingFormatVersion = rowDataJSON.getString("version");
		} catch (JSONException e) {
			encodingFormatVersion = "0";
		}

		if (encodingFormatVersion.equalsIgnoreCase(formState.CURRENT_VERSION)) {				
			formStateJSON = rowDataJSON;
		} else {
			IFormStateLoader formViewerStateLoader;
			formViewerStateLoader = FormStateLoaderFactory.getInstance().getLoader(encodingFormatVersion);

			formStateJSON = (JSONObject) formViewerStateLoader.load(formStateString);
		}

		QbeJSONTemplateParser.addAdditionalInfo(formStateJSON);
		formState.setProperty(formState.FORM_STATE,  formStateJSON);
		formState.setFormStateValues(valuesDataJSON);
		formState.setIdNameMap();
		return formState;
		
	}
	
	
}
