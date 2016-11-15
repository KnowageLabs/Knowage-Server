package it.eng.spagobi.calendar.utils;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class CalendarUtilities {
	static private Logger logger = Logger.getLogger(CalendarUtilities.class);

	public static SessionFactory getHibSessionAida() throws EMFUserError {
		logger.debug("IN");
		Configuration conf = new Configuration();
		String resource = "it/eng/spagobi/tools/calendar/metadata/mapping/hibernate.calendar.cfg.xml";
		conf = conf.configure(resource);
		try {

			IDataSource datasource = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(
					SingletonConfig.getInstance().getConfigValue("dwh.calendar.datasource.label"));

			if (datasource.checkIsJndi()) {
				conf.setProperty("hibernate.connection.datasource", datasource.getJndi());
			} else {
				conf.setProperty("hibernate.connection.url", datasource.getUrlConnection());
				conf.setProperty("hibernate.connection.password", datasource.getPwd());
				conf.setProperty("hibernate.connection.username", datasource.getUser());
				conf.setProperty("hibernate.connection.driver_class", datasource.getDriver());
			}

			conf.setProperty("hibernate.validator.apply_to_ddl", "false");
			conf.setProperty("hibernate.validator.autoregister_listeners", "false");
			conf.setProperty("hibernate.dialect", datasource.getHibDialectClass());

		} catch (Exception e) {
			logger.error("Error trying to open a new Calendar connection", e);
		}

		SessionFactory sessionFactory = conf.buildSessionFactory();
		logger.debug("OUT");
		return sessionFactory;
	}
}
