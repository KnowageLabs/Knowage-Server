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
package it.eng.spagobi.tools.importexportOLD.typesmanager;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.birt.BirtReportDriver;
import it.eng.spagobi.engines.drivers.jpivot.JPivotDriver;
import it.eng.spagobi.tools.importexportOLD.ExportManager;
import it.eng.spagobi.tools.importexportOLD.ExporterMetadata;

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
