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
package it.eng.spagobi.commons.services;

import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import it.eng.knowage.encryption.DataEncryptionInitializer;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.initializers.caching.CachingInitializer;
import it.eng.spagobi.commons.initializers.metadata.CategoriesInitializer;
import it.eng.spagobi.commons.initializers.metadata.MetadataInitializer;
import it.eng.spagobi.commons.initializers.metadata.TreeInitializer;
import it.eng.spagobi.security.init.SecurityInitializer;
import it.eng.spagobi.tools.scheduler.init.CleanAuditQuartzInitializer;
import it.eng.spagobi.tools.scheduler.init.CleanCacheQuartzInitializer;
import it.eng.spagobi.tools.scheduler.init.QuartzInitializer;
import it.eng.spagobi.tools.scheduler.init.ResourceExportFolderSchedulerInitializer;
import it.eng.spagobi.tools.scheduler.init.RestEventQuartzInitializer;


public class InitializerKnowageServlet extends HttpServlet {

	private static transient Logger logger = Logger.getLogger(InitializerKnowageServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("Initializing InitializerKnowageServlet...");

		MetadataInitializer metadataInitializer = new MetadataInitializer();
		metadataInitializer.init(null);

		CategoriesInitializer categoriesInitializer = new CategoriesInitializer();
		categoriesInitializer.init(null);

		TreeInitializer treeInitializer = new TreeInitializer();
		treeInitializer.init(null);

		DataEncryptionInitializer dataEncryptionInitializer = new DataEncryptionInitializer();
		dataEncryptionInitializer.init(null);

		SecurityInitializer securityInitializer = new SecurityInitializer();
		securityInitializer.init(null);

		QuartzInitializer quartzInitializer = new QuartzInitializer();
		quartzInitializer.init(null);

		CleanCacheQuartzInitializer cleanCacheQuartzInitializer = new CleanCacheQuartzInitializer();
		cleanCacheQuartzInitializer.init(null);

		RestEventQuartzInitializer restEventQuartzInitializer = new RestEventQuartzInitializer();
		restEventQuartzInitializer.init(null);

		CachingInitializer cachingInitializer = new CachingInitializer();
		cachingInitializer.init(null);

		ResourceExportFolderSchedulerInitializer resourceExportFolderSchedulerInitializer = new ResourceExportFolderSchedulerInitializer();
		resourceExportFolderSchedulerInitializer.init(null);

		CleanAuditQuartzInitializer cleanAuditQuartzInitializer = new CleanAuditQuartzInitializer();
		cleanAuditQuartzInitializer.init(null);

		this.invokeCockpitStatisticsInitializer("it.eng.spagobi.commons.initializers.metadata.CockpitStatisticsInitializer");

	}

	private void invokeCockpitStatisticsInitializer(String fqcn) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		try {

			Class<?> clazz = Class.forName(fqcn, true, cl);

			Object instance = clazz.getDeclaredConstructor().newInstance();

			Method init = clazz.getMethod("init", SourceBean.class);

			init.invoke(instance, (Object) null);


		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			logger.info(String.format("Class %s non found", fqcn));
		} catch (NoSuchMethodException e) {
			logger.warn(String.format("Method init(Object) non found su %s.", fqcn));
		} catch (ReflectiveOperationException e) {
			logger.error(String.format("Error invocation %s.init(null): %s", fqcn, e.getMessage()));
		} catch (Throwable t) {
			logger.error("Generic error reflection: " + t.getMessage());
		}
	}


}
