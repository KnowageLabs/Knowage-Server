package it.eng.spagobi.calendar.utils;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import it.eng.knowage.tools.servermanager.importexport.ExportUtilities;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

public class CalendarUtilities {
	static private Logger logger = Logger.getLogger(ExportUtilities.class);

	public static SessionFactory getHibSessionAida() throws EMFUserError {
		logger.debug("IN");
		Configuration conf = new Configuration();
		String resource = "it/eng/spagobi/tools/calendar/metadata/mapping/hibernate.calendar.cfg.xml";
		conf = conf.configure(resource);

		IDataSource datasource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel("aida_oracle");
		try {
			conf.setProperty("hibernate.connection.url", datasource.getUrlConnection());
			conf.setProperty("hibernate.connection.password", datasource.getPwd());
			conf.setProperty("hibernate.connection.username", datasource.getUser());
			conf.setProperty("hibernate.connection.driver_class", datasource.getDriver());
			conf.setProperty("hibernate.dialect", datasource.getHibDialectClass());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SessionFactory sessionFactory = conf.buildSessionFactory();
		logger.debug("OUT");
		return sessionFactory;
	}
}
