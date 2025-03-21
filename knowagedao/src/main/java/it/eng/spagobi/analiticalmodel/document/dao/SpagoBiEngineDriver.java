package it.eng.spagobi.analiticalmodel.document.dao;

public enum SpagoBiEngineDriver {

	  KPI_DRIVER("it.eng.spagobi.engines.drivers.kpi.KpiDriver")
	 ,BIRT_REPORT_DRIVER("it.eng.spagobi.engines.drivers.birt.BirtReportDriver")
	 ,JASPER_REPORT_DRIVER("it.eng.spagobi.engines.drivers.jasperreport.JasperReportDriver")
	 ,QBE_DRIVER_DRIVER("it.eng.spagobi.engines.drivers.qbe.QbeDriver")
	 ,TALEND_DRIVER("it.eng.spagobi.engines.drivers.talend.TalendDriver")
	 ,COCKPIT_DRIVER("it.eng.spagobi.engines.drivers.cockpit.CockpitDriver")
	 ,DASHBOARD_DRIVER("it.eng.spagobi.engines.drivers.dashboard.DashboardDriver")
	 ,OLAP_DRIVER("it.eng.spagobi.engines.drivers.whatif.OlapDriver")
	 ,DOSSIER_DRIVER("it.eng.spagobi.engines.drivers.dossier.DossierDriver");

	 private final String driverName;
	 
	 
	 SpagoBiEngineDriver(String driverName) {
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
