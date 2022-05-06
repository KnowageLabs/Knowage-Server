/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

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
package com.jamonapi;

import it.eng.knowage.monitor.IKnowageMonitor;

/**
 * Decorate {@link Monitor} with new functionalities.
 *
 * @author Marco Libanori
 */
public class KnowageJamonMonitor implements IKnowageMonitor {

	private static final long serialVersionUID = -581630377501687248L;

	private final Monitor monitor;

	public KnowageJamonMonitor(Monitor monitor) {
		super();
		this.monitor = monitor;
	}

	@Override
	public void stop() {
		this.monitor.stop();
	}

	@Override
	public void stop(Throwable t) {
		this.monitor.skip();
	}

}
