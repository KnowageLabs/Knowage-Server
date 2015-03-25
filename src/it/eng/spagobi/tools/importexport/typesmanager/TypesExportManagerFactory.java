/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.typesmanager;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.birt.BirtReportDriver;
import it.eng.spagobi.engines.drivers.jpivot.JPivotDriver;
import it.eng.spagobi.tools.importexport.ExportManager;
import it.eng.spagobi.tools.importexport.ExporterMetadata;

import org.apache.log4j.Logger;

public class TypesExportManagerFactory {

	static private Logger logger = Logger.getLogger(TypesExportManagerFactory.class);

	/**
	 *  Types ghandled by specific export managers TODO with all types 
	 */
	private static final String KPI = "KPI";
	private static final String CONSOLE = "CONSOLE";
	private static final String DATAMART = "DATAMART";
	private static final String WORKSHEET = "WORKSHEET";
	private static final String SMART_FILTER = "SMART_FILTER";
	private static final String OLAP = "OLAP";

	
	
	
	private static String getObjType(BIObject biobj, Engine engine){
		if (biobj.getBiObjectTypeCode().equalsIgnoreCase(KPI) 
				&& engine.getClassName() != null && engine.getClassName().equals("it.eng.spagobi.engines.kpi.SpagoBIKpiInternalEngine")) {
			return KPI;
		}

		if (biobj.getBiObjectTypeCode().equalsIgnoreCase(CONSOLE) ) {
			return CONSOLE;
		}

		if (biobj.getBiObjectTypeCode().equalsIgnoreCase(DATAMART) ) {
			return DATAMART;
		}

		if (biobj.getBiObjectTypeCode().equalsIgnoreCase(WORKSHEET) ) {
			return WORKSHEET;
		}

		if (biobj.getBiObjectTypeCode().equalsIgnoreCase(SMART_FILTER) ) {
			return SMART_FILTER;
		}

		if (biobj.getBiObjectTypeCode().equalsIgnoreCase(OLAP) ) {
			return OLAP;
		}

		return null;
	}

	public static ITypesExportManager createTypesExportManager(BIObject biobj, Engine engine, ExporterMetadata exporter,
			ExportManager manager){

		logger.debug("IN");
		String type = getObjType(biobj, engine);

		ITypesExportManager toReturn = null;

		if (type != null){

			if(type.equals(KPI)){
				logger.debug("kpi export manager");
				toReturn = new KPIExportManager(type, exporter, manager);
			}

			if(type.equals(CONSOLE)){
				logger.debug("console export manager");
				toReturn = new ConsoleExportManager(type, exporter, manager);
			}

			if(type.equals(WORKSHEET) || type.equals(DATAMART) || type.equals(SMART_FILTER)){
				logger.debug("Meta model export manager");
				toReturn = new MetaModelsNeedExportManager(type, exporter, manager);
			}
			
			if(type.equals(OLAP)
					&&
					JPivotDriver.class.getName().equals(engine.getDriverName())
					){
				logger.debug("Olap export manager");
				toReturn = new OlapExportManager(type, exporter, manager);
			}

		}
		
		if (BirtReportDriver.class.getName().equals(engine.getDriverName())) {
			toReturn = new BirtReportExportManager(type, exporter, manager);
		}

		// type has not a specific export manager
		if (toReturn == null) logger.debug("type has not a specific export manager");

		logger.debug("OUT");

		return toReturn;


	}





}
