package com.jamonapi;

/**
 * Wrapper of @link {@link MonitorFactory}
 *
 * @author Marco Libanori
 */
public class KnowageMonitorFactory extends MonitorFactory {

	public static KnowageMonitor start(String label) {
		return new KnowageMonitor(MonitorFactory.start(label));
	}

}
