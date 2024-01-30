package it.eng.spagobi.analiticalmodel.document.dao;

public enum SpagoBiEngineDriver {

	  KPI_DRIVER("it.eng.spagobi.engines.drivers.kpi.KpiDriver")
	 ,BIRT_REPORT_DRIVER("it.eng.spagobi.engines.drivers.birt.BirtReportDriver")
	 ,JASPER_REPORT_DRIVER("it.eng.spagobi.engines.drivers.jasperreport.JasperReportDriver")
	 ,QBE_DRIVER_DRIVER("it.eng.spagobi.engines.drivers.qbe.QbeDriver")
	 ,TALEND_DRIVER("it.eng.spagobi.engines.drivers.talend.TalendDriver")
	 ,COMMONJ_DRIVER("it.eng.spagobi.engines.drivers.commonj.CommonjDriver")
	 ,GIS_DRIVER("it.eng.spagobi.engines.drivers.gis.GisDriver")
	 ,SVG_VIEWER__DRIVER("it.eng.spagobi.engines.drivers.svgviewer.SvgViewerDriver")
	 ,COCKPIT_DRIVER("it.eng.spagobi.engines.drivers.cockpit.CockpitDriver")
	 ,DASHBOARD_DRIVER("it.eng.spagobi.engines.drivers.dashboard.DashboardDriver")
	 ,WHATIF_DRIVER("it.eng.spagobi.engines.drivers.whatif.WhatIfDriver")
	 ,OLAP_DRIVER("it.eng.spagobi.engines.drivers.whatif.OlapDriver")
	 ,DOSSIER_DRIVER("it.eng.spagobi.engines.drivers.dossier.DossierDriver");

	 private String driverName;
	 
	 
	 private SpagoBiEngineDriver(String driverName) {
		 this.driverName = driverName;
	 }
	 
	 public String getDriverName() {
		 return this.driverName;
	 }
	 
	 public static SpagoBiEngineDriver fromDriverName(String dName) {
		for(SpagoBiEngineDriver driver : SpagoBiEngineDriver.values()) {
			if(driver.driverName.equalsIgnoreCase(dName)) {
				return driver;
			}
		} 
		throw new IllegalArgumentException("Driver [" + dName + "] not supported");
	 }
	 
}
