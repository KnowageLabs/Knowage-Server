/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.commons.monitor;

import java.util.concurrent.TimeUnit;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Monitor {
	
	String name;
	long startTime;
	long stopTime;
	
	private Monitor(String name) {
		this.name = name;
		this.startTime = System.currentTimeMillis();
		this.stopTime = -1;
	}
	
	public long start() {
		startTime = System.currentTimeMillis();
		stopTime = -1;
		return startTime;
	}
	
	public long stop() {
		stopTime = System.currentTimeMillis();
		return stopTime;
	}
	
	public long elapsed() {
		long stop = (this.stopTime == -1)? System.currentTimeMillis(): this.stopTime;
		long elapsed = stop - startTime;
		return elapsed;
	}
	
	public String elapsedAsString() {
		return formatInterval(elapsed());
	}
	
	
	
	public static Monitor start(String name) {
		return new Monitor(name);
	}
	
	private static String formatInterval(final long l) {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }
	
}
