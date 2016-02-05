/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/

package it.eng.spagobi.utilities.locks;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class DistributedLockFactory {

	@SuppressWarnings("rawtypes")
	public static IMap getDistributedMap(String instanceName, String mapName) {
		com.hazelcast.config.Config cfg = new com.hazelcast.config.Config();
		cfg.setInstanceName(instanceName);
		HazelcastInstance hz = Hazelcast.getOrCreateHazelcastInstance(cfg);
		return hz.getMap(mapName);
	}
}
