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
package it.eng.spagobi.engines.qbe.analysisstateloaders.worksheet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class WorksheetStateLoaderFactory {
	
	private static Map loaderRegistry;
	
	static {
		loaderRegistry = new HashMap();
		AbstractWorksheetStateLoader l0 = new Version0WorksheetStateLoader();
		AbstractWorksheetStateLoader l1 = new Version1WorksheetStateLoader();
		l0.setNextLoader(l1);
		loaderRegistry.put("0", l0);
		loaderRegistry.put("1", l1);
	}
	
	private static WorksheetStateLoaderFactory instance;
	public static WorksheetStateLoaderFactory getInstance() {
		if(instance == null) {
			instance = new WorksheetStateLoaderFactory();
		}
		return instance;
	}
	
	private WorksheetStateLoaderFactory() {}
	
	public IWorksheetStateLoader getLoader(String encodingFormatVersion) {
		return (IWorksheetStateLoader) loaderRegistry.get(encodingFormatVersion);
	}
}
