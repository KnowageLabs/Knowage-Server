/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class GeneratorFactory {
	private static List<GeneratorDescriptor> generatorDescriptorsCache = null;	
	
	public static Collection<GeneratorDescriptor> getGeneratorDescriptors() {
		if(generatorDescriptorsCache == null) {
			initGeneratorDescriptorsCache();
		}
		
		return new ArrayList<GeneratorDescriptor>(generatorDescriptorsCache);
	}
	
	public static GeneratorDescriptor getGeneratorDescriptorById(String id){
		if(generatorDescriptorsCache == null) {
			initGeneratorDescriptorsCache();
		}
		
		for(GeneratorDescriptor descriptor : generatorDescriptorsCache) {
			if(descriptor.getId().equals(id)) return descriptor;
		}
		return null;
	}
	
	private static void initGeneratorDescriptorsCache() {
		generatorDescriptorsCache = new ArrayList<GeneratorDescriptor>();
		
		IExtension[] extensions = Platform.getExtensionRegistry()
		.getExtensionPoint(SpagoBIMetaGeneratorPlugin.PLUGIN_ID, "generator")
		.getExtensions();
		
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				if (!configElements[j].getName().equals("generator")) continue;
				GeneratorDescriptor generatorDescriptor = buildGeneratorDescriptor(configElements[j]);
				if(generatorDescriptor != null) {
					generatorDescriptorsCache.add(generatorDescriptor);
				}
			}
		}
	}

	private static GeneratorDescriptor buildGeneratorDescriptor(IConfigurationElement configElement) {
		try {
			return new GeneratorDescriptor(configElement);
		} catch (Exception e) {
			String name = configElement.getAttribute("name");
			if (name == null) name = "[missing name attribute]";
			String msg =
				"Failed to load generator named "
				+ name
				+ " in "
				+ configElement.getDeclaringExtension().getNamespaceIdentifier();
				
			IStatus status = new Status(IStatus.ERROR, SpagoBIMetaGeneratorPlugin.PLUGIN_ID, IStatus.OK, msg, e);
			Bundle plugin = Platform.getBundle(SpagoBIMetaGeneratorPlugin.PLUGIN_ID);
			Platform.getLog(plugin).log(status);
			return null;
		}
	}
}
