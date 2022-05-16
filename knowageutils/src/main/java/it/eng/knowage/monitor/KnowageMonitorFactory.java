/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.monitor;

import java.util.Optional;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.jamonapi.KnowageJamonMonitor;
import com.jamonapi.KnowageJamonMonitorFactory;
import com.jamonapi.MonitorFactory;

/**
 * Wrapper of @link {@link MonitorFactory}
 *
 * @author Marco Libanori
 */
public class KnowageMonitorFactory {

	static protected Logger logger = Logger.getLogger(KnowageMonitorFactory.class);

	private static KnowageMonitorFactory INSTANCE = new KnowageMonitorFactory();

	private IKnowageMonitorFactory factory = null;

	public static KnowageMonitorFactory getInstance() {
		return INSTANCE;
	}

	private KnowageMonitorFactory() {
		initFactory();
	}

	private void initFactory() {
		String monitorFactoryClassName = Optional.ofNullable(System.getProperty("knowage.monitorfactory.class", System.getenv("knowage.monitorfactory.class")))
				.orElse(KnowageJamonMonitor.class.getName());
		logger.debug("Monitor factory class name is [" + monitorFactoryClassName + "]");
		factory = instantiateMonitor(monitorFactoryClassName);
	}

	public IKnowageMonitor start(String monitorName) {
		return factory.start(monitorName);
	}

	protected static IKnowageMonitorFactory instantiateMonitor(String monitorFactoryClassName) {
		try {
			IKnowageMonitorFactory factory = (IKnowageMonitorFactory) Class.forName(monitorFactoryClassName).newInstance();
			logger.debug("Monitor factory class name is [" + monitorFactoryClassName + "]");
			return factory;
		} catch (Exception e) {
			LogMF.error(logger, e, "Cannot start monitor using factory class [{0}]", new String[] { monitorFactoryClassName });
			return initDefaultMonitorFactory();
		}
	}

	private static KnowageJamonMonitorFactory initDefaultMonitorFactory() {
		return new KnowageJamonMonitorFactory();
	}

}
