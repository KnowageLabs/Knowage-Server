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
package it.eng.knowage.meta.model.serializer;

import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class EmfXmiSerializer implements IModelSerializer {
	
	public static final String SPAGOBI_MODEL_URI = "it.eng.knowage";

	
	public void serialize(Model model, File file)  {
		FileOutputStream outputStream;
		
		try {
			outputStream = new FileOutputStream(file);
	        serialize(model, outputStream);
	        outputStream.flush();
	        outputStream.close();
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to serialize model [" + model.getName() + "] to file [" + file.getName() + "]", t);
		}
	}
	
	@Override
	public void serialize(Model model, OutputStream outputStream) {
		
		// Create a resource set.
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the default resource factory -- only needed for stand-alone!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
			  Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl()
		);

		// Get the URI of the model file.
		//URI uri = URI.createFileURI(new File("mylibrary.xmi").getAbsolutePath());
		URI uri = URI.createURI(SPAGOBI_MODEL_URI);
		  
		// Create a resource for this file.
		Resource resource = resourceSet.createResource(uri);
		  
		// Add the book and writer objects to the contents.
		resource.getContents().add(model);
	
		// Save the contents of the resource to the file system.
		try {
			//resource.save(Collections.EMPTY_MAP);
			resource.save(outputStream, Collections.EMPTY_MAP);
		} catch (IOException e) {
		  throw new RuntimeException("Impossible to serialize model [" + model.getName() + "]", e);
		}	
	}

	public Model deserialize(File file) {
		Model model;
		FileInputStream inputStream;
		
		model = null;
		try {
			inputStream = new FileInputStream(file);
	        model = deserialize(inputStream);
	        inputStream.close();
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to deserialize model [" + model.getName() + "] to file [" + file.getName() + "]", t);
		}
		
		return model;
	}
	
	@Override
	public Model deserialize(InputStream inputStream) {
		Model model;
		// Create a resource set.
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the default resource factory -- only needed for stand-alone!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl()
		);

		// Register the package -- only needed for stand-alone!
		ModelPackage libraryPackage = ModelPackage.eINSTANCE;

		// Get the URI of the model file.
		//URI uri = URI.createFileURI(new File("mylibrary.xmi").getAbsolutePath());
		URI uri = URI.createURI(SPAGOBI_MODEL_URI);
		  
		// Demand load the resource for this file.
		Resource resource = resourceSet.createResource(uri);
		
		model = null;
		try {
			resource.load(inputStream, Collections.EMPTY_MAP);
			//resource.load(Collections.EMPTY_MAP);
			model = (Model)resource.getContents().get(0);
			
			//resource.save(System.out, Collections.EMPTY_MAP);
		} catch (Throwable e) {
			throw new RuntimeException("Impossible to deserialize model [" + model.getName() + "]", e);
		}
		
		return model;
	}
}
