/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.

 **/

package it.eng.knowage.serialization;

import it.eng.knowage.initializer.BusinessModelInitializer;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelPackage;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessModelFactory;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalForeignKey;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalModelFactory;
import it.eng.knowage.meta.model.physical.PhysicalTable;

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

	public static final String SPAGOBI_MODEL_URI = "it.eng.spagobi";

	public void serialize(Model model, File file) {
		FileOutputStream outputStream;

		try {
			outputStream = new FileOutputStream(file);
			serialize(model, outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to serialize model [" + model.getName() + "] to file [" + file.getName() + "]", t);
		}
	}

	public void serialize(Model model, OutputStream outputStream) {

		// Create a resource set.
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the default resource factory -- only needed for stand-alone!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		// Get the URI of the model file.
		// URI uri = URI.createFileURI(new File("mylibrary.xmi").getAbsolutePath());
		URI uri = URI.createURI(SPAGOBI_MODEL_URI);

		// Create a resource for this file.
		Resource resource = resourceSet.createResource(uri);

		// Add the book and writer objects to the contents.
		resource.getContents().add(model);

		// Save the contents of the resource to the file system.
		try {
			// resource.save(Collections.EMPTY_MAP);
			resource.save(outputStream, Collections.EMPTY_MAP);
		} catch (IOException e) {
			throw new RuntimeException("Impossible to serialize model [" + model.getName() + "]", e);
		}
	}

	@Override
	public Model deserialize(File file) {
		Model model;
		FileInputStream inputStream;

		model = null;
		try {
			inputStream = new FileInputStream(file);
			model = deserialize(inputStream);
			inputStream.close();
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to serialize model [" + model.getName() + "] to file [" + file.getName() + "]", t);
		}

		return model;
	}

	@Override
	public Model deserialize(InputStream inputStream) {
		Model model;
		// Create a resource set.
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the default resource factory -- only needed for stand-alone!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		// Register the package -- only needed for stand-alone!
		ModelPackage libraryPackage = ModelPackage.eINSTANCE;

		// Get the URI of the model file.
		// URI uri = URI.createFileURI(new File("mylibrary.xmi").getAbsolutePath());
		URI uri = URI.createURI(SPAGOBI_MODEL_URI);

		// Demand load the resource for this file.
		Resource resource = resourceSet.createResource(uri);

		model = null;
		try {
			resource.load(inputStream, Collections.EMPTY_MAP);
			// resource.load(Collections.EMPTY_MAP);
			model = (Model) resource.getContents().get(0);

			// resource.save(System.out, Collections.EMPTY_MAP);
		} catch (Throwable e) {
			throw new RuntimeException("Impossible to serialize model [" + model.getName() + "]", e);
		}

		return model;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		ModelFactory FACTORY = ModelFactory.eINSTANCE;
		PhysicalModelFactory PFACTORY = PhysicalModelFactory.eINSTANCE;
		BusinessModelFactory BFACTORY = BusinessModelFactory.eINSTANCE;

		Model spagobiModel = FACTORY.createModel();
		spagobiModel.setName("testModel");

		// physical model
		PhysicalModel physicalModel = PFACTORY.createPhysicalModel();
		physicalModel.setName("physicalModel");
		spagobiModel.getPhysicalModels().add(physicalModel);
		System.out.println(physicalModel.getParentModel());

		for (int i = 0; i < 300; i++) {
			PhysicalTable table = PFACTORY.createPhysicalTable();
			table.setName("table_" + (i + 1));
			physicalModel.getTables().add(table);
			for (int j = 0; j < 15; j++) {
				PhysicalColumn column = PFACTORY.createPhysicalColumn();
				column.setName("column_" + (j + 1));
				table.getColumns().add(column);
			}
		}

		PhysicalForeignKey fk = PFACTORY.createPhysicalForeignKey();
		fk.setSourceTable(physicalModel.getTable("table_1"));
		fk.setDestinationTable(physicalModel.getTable("table_2"));
		fk.getSourceColumns().add(physicalModel.getTable("table_1").getColumn("column_1"));
		fk.getDestinationColumns().add(physicalModel.getTable("table_2").getColumn("column_1"));
		fk.getSourceColumns().add(physicalModel.getTable("table_1").getColumn("column_2"));
		fk.getDestinationColumns().add(physicalModel.getTable("table_2").getColumn("column_2"));
		physicalModel.getForeignKeys().add(fk);

		fk = PFACTORY.createPhysicalForeignKey();
		fk.setSourceTable(physicalModel.getTable("table_2"));
		fk.setDestinationTable(physicalModel.getTable("table_3"));
		fk.getSourceColumns().add(physicalModel.getTable("table_2").getColumn("column_3"));
		fk.getDestinationColumns().add(physicalModel.getTable("table_3").getColumn("column_3"));
		fk.getSourceColumns().add(physicalModel.getTable("table_2").getColumn("column_4"));
		fk.getDestinationColumns().add(physicalModel.getTable("table_3").getColumn("column_4"));
		physicalModel.getForeignKeys().add(fk);

		// business model
		// BusinessModel businessModel = BFACTORY.createBusinessModel();
		// businessModel.setName("businessModel");
		BusinessModelInitializer modelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = modelInitializer.initialize("businessModel", physicalModel);

		System.out.println(spagobiModel.getPropertyTypes().size());

		spagobiModel.getBusinessModels().add(businessModel);

		EmfXmiSerializer serializer = new EmfXmiSerializer();
		serializer.serialize(spagobiModel, new File("bigemfmodel.xmi"));
	}

}
