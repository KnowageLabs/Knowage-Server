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
package it.eng.knowage.meta.generator.mondrianschema;

import it.eng.knowage.meta.generator.GenerationException;
import it.eng.knowage.meta.generator.IGenerator;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianCube;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianDimension;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.MondrianModel;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.olap.OlapModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it), Marco Cortella (marco.cortella@eng.it)
 *
 */
public class MondrianSchemaGenerator implements IGenerator {
	/**
	 * TODO REVIEW FOR PORTING
	 */
	// private static final IResourceLocator RL = SpagoBIMetaGeneratorPlugin.getInstance().getResourceLocator();

	public static String defaultTemplateFolderPath = "templates";

	private static Logger logger = LoggerFactory.getLogger(MondrianSchemaGenerator.class);

	/**
	 * The velocity template directory
	 */
	private File templateDir;

	/**
	 * The velocity template used to map olap model to Mondrian Schema
	 */
	private File mondrianTemplate;

	public MondrianSchemaGenerator() {
		String templatesDirRelativePath;

		logger.trace("IN");
		templatesDirRelativePath = null;
		try {
			/**
			 * TODO REVIEW FOR PORTING
			 */
			// templatesDirRelativePath = RL.getPropertyAsString("mondrianschema.templates.dir", defaultTemplateFolderPath);

			logger.debug("Template dir is equal to [{}]", templateDir);
			Assert.assertTrue("Template dir [" + templateDir + "] does not exist", templateDir.exists());

			mondrianTemplate = new File(templateDir, "mondrian_template.vm");
			logger.trace("[Mondrian] template file is equal to [{}]", mondrianTemplate);
			Assert.assertTrue("[Mondrian] template file [" + mondrianTemplate + "] does not exist", mondrianTemplate.exists());
		} catch (Throwable t) {
			logger.error("Impossible to resolve folder [" + templatesDirRelativePath + "]", t);
		} finally {
			logger.trace("OUT");
		}

	}

	@Override
	public void generate(ModelObject o, String outputdir) throws GenerationException {
		OlapModel model;

		if (o instanceof OlapModel) {
			model = (OlapModel) o;
			generateMondrianSchema(model, outputdir);
		} else {
			throw new GenerationException("Impossible to create Mondrian Template from an object of type [" + o.getClass().getName() + "]");
		}
	}

	public void generateMondrianSchema(OlapModel model, String outputFilePath) throws GenerationException {
		logger.trace("IN");

		Velocity.setProperty("file.resource.loader.path", getTemplateDir().getAbsolutePath());

		MondrianModel mondrianModel = new MondrianModel(model);

		VelocityContext context;

		try {
			logger.debug("Create Mondrian Template");

			context = new VelocityContext();
			List<IMondrianCube> cubes = new ArrayList<IMondrianCube>();
			cubes.addAll(mondrianModel.getCubes());

			List<IMondrianDimension> dimensions = new ArrayList<IMondrianDimension>();
			dimensions.addAll(mondrianModel.getDimensions());

			// **** Check Model validity
			if (cubes.isEmpty()) {
				throw new GenerationException("No cubes found, must define at least one cube");
			} else {
				for (IMondrianCube cube : cubes) {
					if (cube.getMeasures().isEmpty()) {
						throw new GenerationException("Must define at least one measure for each cube");
					}
					if (cube.getCubeDimensions().isEmpty()) {
						throw new GenerationException("Cannot find dimensions linked to a cube, must define at least one dimension for cube");
					}
				}
			}
			if (dimensions.isEmpty()) {
				throw new GenerationException("No dimensions found, must define at least one dimension for cube");
			} else {
				for (IMondrianDimension dimension : dimensions) {
					if (dimension.getHierarchies().isEmpty()) {
						throw new GenerationException("Must define at least one hierarchy for each dimension");
					}
				}
			}
			// *****************

			String name = model.getName();
			context.put("name", name); //$NON-NLS-1$
			context.put("dimensions", dimensions); //$NON-NLS-1$
			context.put("cubes", cubes); //$NON-NLS-1$
			context.put("model", model);//$NON-NLS-1$

			File outputFile = new File(outputFilePath);
			createFile(mondrianTemplate, outputFile, context);

			logger.info("Mondrian template file of model [{}] succesfully created", model.getName());

		} catch (GenerationException t) {
			logger.error("Impossible to create mapping, Generate Exception", t);
			throw t;
		} catch (Throwable t) {
			logger.error("Impossible to create mapping", t);

		} finally {
			logger.trace("OUT");
		}

	}

	private void createFile(File templateFile, File outputFile, VelocityContext context) {
		Template template;

		try {
			template = Velocity.getTemplate(templateFile.getName());
		} catch (Throwable t) {
			throw new GenerationException("Impossible to load template file [" + templateFile + "]");
		}

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(outputFile);

			template.merge(context, fileWriter);

			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			logger.error("Impossible to generate output file from template file [" + templateFile + "]", e);
			throw new GenerationException("Impossible to generate output file from template file [" + templateFile + "]");
		}
	}

	// =======================================================================
	// ACCESSOR METHODS
	// =======================================================================

	@Override
	public void hideTechnicalResources() {
		// TODO Auto-generated method stub

	}

	public File getTemplateDir() {
		return templateDir;
	}

	public void setTemplateDir(File templateDir) {
		this.templateDir = templateDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.knowage.meta.generator.IGenerator#generate(it.eng.knowage.meta.model.ModelObject, java.lang.String, boolean)
	 */
	@Override
	public void generate(ModelObject o, String outputDir, boolean isUpdatableMapping) {
		// TODO Auto-generated method stub

	}

}
