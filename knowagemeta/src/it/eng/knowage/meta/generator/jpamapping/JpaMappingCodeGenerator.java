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
package it.eng.knowage.meta.generator.jpamapping;

import it.eng.knowage.meta.generator.GenerationException;
import it.eng.knowage.meta.generator.IGenerator;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaTable;
import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaView;
import it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaModel;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.IMondrianDimension;
import it.eng.knowage.meta.generator.mondrianschema.wrappers.MondrianModel;
import it.eng.knowage.meta.generator.utils.StringUtils;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.olap.OlapModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JpaMappingCodeGenerator implements IGenerator {

	/**
	 * The base output folder as passed to the method generate (this class is not thread safe!)
	 */
	protected File baseOutputDir;

	protected File srcDir;

	protected File distDir;

	/**
	 * The velocity template directory
	 */
	private File templateDir;

	private String persistenceUnitName;

	// =======================================================================================
	// VELOCITY TEMPALTES
	// =======================================================================================

	/**
	 * The template used to map business table to java class
	 */
	private File tableTemplate;

	/**
	 * The template used to map business view to json mapping file
	 */
	private File viewTemplate;

	/**
	 * The template used to map business table's composed key to a java class
	 */
	private File keyTemplate;

	/**
	 * The template used to generate the persistence.xml file
	 */
	private File persistenceUnitTemplate;

	/**
	 * The template used to generate labels.properties file
	 */
	private File labelsTemplate;

	/**
	 * The template used to generate qbe.properties file
	 */
	private File propertiesTemplate;

	/**
	 * The template used to generate cfileds.xml file
	 */
	private File cfieldsTemplate;

	/**
	 * The template used to generate relationshipsTemplate.json file
	 */
	private File relationshipsTemplate;

	/**
	 * The template used to generate hierarchies.properties file
	 */
	private File hierarchiesTemplate;

	// -- STATICS --------------------------------------------------

	public static String defaultTemplateFolderPath = "templates";
	public static final String DEFAULT_SRC_DIR = "src";
	public static final String DEFAULT_DIST_DIR = "dist";
	/**
	 * TODO REVIEW FOR PORTING
	 */
	// private static final IResourceLocator RL = SpagoBIMetaGeneratorPlugin.getInstance().getResourceLocator();

	private static Logger logger = LoggerFactory.getLogger(JpaMappingCodeGenerator.class);

	public JpaMappingCodeGenerator() {
		String templatesDirRelativePath;

		logger.trace("IN");

		templatesDirRelativePath = null;
		try {
			/**
			 * TODO REVIEW FOR PORTING
			 */
			// templatesDirRelativePath = RL.getPropertyAsString("jpamapping.templates.dir", defaultTemplateFolderPath);
			templatesDirRelativePath = "/it/eng/knowage/meta/generator/templates";

			// templateDir = RL.getFile(templatesDirRelativePath);
			templateDir = new File(getClass().getResource("/it/eng/knowage/meta/generator/templates").getFile());

			logger.debug("Template dir is equal to [{}]", templateDir);
			Assert.assertTrue("Template dir [" + templateDir + "] does not exist", templateDir.exists());

			tableTemplate = new File(templateDir, "sbi_table.vm");
			logger.trace("[Table] template file is equal to [{}]", tableTemplate);
			Assert.assertTrue("[Table] template file [" + tableTemplate + "] does not exist", tableTemplate.exists());

			viewTemplate = new File(templateDir, "sbi_view.vm");
			logger.trace("[View] template file is equal to [{}]", viewTemplate);
			Assert.assertTrue("[View] template file [" + viewTemplate + "] does not exist", viewTemplate.exists());

			keyTemplate = new File(templateDir, "sbi_pk.vm");
			logger.trace("[Key] template file is equal to [{}]", keyTemplate);
			Assert.assertTrue("[Key] template file [" + keyTemplate + "] does not exist", keyTemplate.exists());

			persistenceUnitTemplate = new File(templateDir, "sbi_persistence_unit.vm");
			logger.trace("[PersistenceUnit] template file is equal to [{}]", persistenceUnitTemplate);
			Assert.assertTrue("[PersistenceUnit] template file [" + persistenceUnitTemplate + "] does not exist", persistenceUnitTemplate.exists());

			labelsTemplate = new File(templateDir, "sbi_labels.vm");
			logger.trace("[Labels] template file is equal to [{}]", labelsTemplate);
			Assert.assertTrue("[Labels] template file [" + labelsTemplate + "] does not exist", labelsTemplate.exists());

			propertiesTemplate = new File(templateDir, "sbi_properties.vm");
			logger.trace("[Properties] template file is equal to [{}]", propertiesTemplate);
			Assert.assertTrue("[Properties] template file [" + propertiesTemplate + "] does not exist", propertiesTemplate.exists());

			cfieldsTemplate = new File(templateDir, "sbi_cfields.vm");
			logger.trace("[Calculated Fields] template file is equal to [{}]", cfieldsTemplate);
			Assert.assertTrue("[Calculated Fields] template file [" + cfieldsTemplate + "] does not exist", cfieldsTemplate.exists());

			relationshipsTemplate = new File(templateDir, "sbi_relationships.vm");
			logger.trace("[Relationships] template file is equal to [{}]", relationshipsTemplate);
			Assert.assertTrue("[Relationships] template file [" + relationshipsTemplate + "] does not exist", relationshipsTemplate.exists());

			hierarchiesTemplate = new File(templateDir, "hierarchies.vm");
			logger.trace("[Hierarchies] template file is equal to [{}]", relationshipsTemplate);
			Assert.assertTrue("[Relationships] template file [" + relationshipsTemplate + "] does not exist", relationshipsTemplate.exists());

		} catch (Throwable t) {
			logger.error("Impossible to resolve folder [" + templatesDirRelativePath + "]", t);
		} finally {
			logger.trace("OUT");
		}

	}

	@Override
	public void generate(ModelObject o, String outputDir) {
		generate(o, outputDir, false);
	}

	@Override
	public void generate(ModelObject o, String outputDir, boolean isUpdatableMapping) {
		BusinessModel model;

		logger.trace("IN");

		if (o instanceof BusinessModel) {
			model = (BusinessModel) o;
			try {

				baseOutputDir = new File(outputDir);
				deleteFile(baseOutputDir);
				baseOutputDir = new File(outputDir);

				logger.debug("Output dir is equal to [{}]", baseOutputDir);

				srcDir = (srcDir == null) ? new File(baseOutputDir, DEFAULT_SRC_DIR) : srcDir;
				logger.debug("src dir is equal to [{}]", srcDir);

				distDir = (distDir == null) ? new File(baseOutputDir, DEFAULT_DIST_DIR) : distDir;
				logger.debug("dist dir is equal to [{}]", distDir);

				if (distDir.mkdirs()) {
					logger.debug("Created directory [{}]", distDir);
				}

				generateJpaMapping(model, isUpdatableMapping);

				logger.info("Jpa mapping code generated succesfully");
			} catch (Exception e) {
				logger.error("An error occur while generating jpa mapping code", e);
				throw new GenerationException("An error occur while generating JPA mapping", e);
			}
		} else {
			throw new GenerationException("Impossible to generate JPA mapping from an object of type [" + o.getClass().getName() + "]");
		}

		logger.trace("OUT");
	}

	/**
	 * Delete the file and all it's children
	 *
	 * @param file
	 */
	private void deleteFile(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteFile(files[i]);
			}
		}

		boolean fileDeletionResult = file.delete();
		// if (!fileDeletionResult) {
		// logger.error("Can't delete the file [{}] the file is writtable? [{}]", file.getAbsolutePath(), file.canWrite());
		// throw new KnowageMetaException("Can't delete the file " + file.getAbsolutePath() + " the file is writable? " + file.canWrite() + " "
		// + file.canExecute());
		//
		// }
	}

	/**
	 * Generate the JPA Mapping of one BusinessModel in one outputFile
	 *
	 * @param model
	 *            BusinessModel
	 * @param outputFile
	 *            File
	 */
	public void generateJpaMapping(BusinessModel model, boolean isUpdatableMapping) throws Exception {

		logger.trace("IN");

		Velocity.setProperty("file.resource.loader.path", getTemplateDir().getAbsolutePath());

		JpaModel jpaModel = new JpaModel(model);
		generateBusinessTableMappings(jpaModel.getTables(), isUpdatableMapping);
		logger.info("Java files for tables of model [{}] succesfully created", model.getName());

		generateBusinessViewMappings(jpaModel.getViews(), isUpdatableMapping);
		logger.info("Java files for views of model [{}] succesfully created", model.getName());

		createLabelsFile(labelsTemplate, jpaModel);
		logger.info("Labels file for model [{}] succesfully created", model.getName());

		createPropertiesFile(propertiesTemplate, jpaModel);
		logger.info("Properties file for model [{}] succesfully created", model.getName());

		generatePersistenceUnitMapping(jpaModel);
		logger.info("Persistence unit for model [{}] succesfully created", model.getName());

		createCfieldsFile(cfieldsTemplate, jpaModel);
		logger.info("Calculated fields file for model [{}] succesfully created", model.getName());

		createRelationshipFile(relationshipsTemplate, jpaModel);
		logger.info("Relationships file for model [{}] succesfully created", model.getName());

		generateHierarchiesFile(hierarchiesTemplate, model);
		logger.info("Hierarchies file for model [{}] succesfully created", model.getName());

		logger.trace("OUT");
	}

	public void generateBusinessTableMappings(List<IJpaTable> jpaTables, boolean isUpdatableMapping) throws Exception {
		// logger.debug("Creating mapping for business class [{}]", table.getName());

		for (IJpaTable jpaTable : jpaTables) {
			createJavaFile(tableTemplate, jpaTable, jpaTable.getClassName(), isUpdatableMapping);
			logger.debug("Mapping for table [" + jpaTable.getName() + "] succesfully created");
			if (jpaTable.hasCompositeKey()) {
				createJavaFile(keyTemplate, jpaTable, jpaTable.getCompositeKeyClassName(), isUpdatableMapping);
				logger.debug("Mapping for composite PK of business table [{}] succesfully", jpaTable.getName());
			}
		}

		// logger.debug("Mapping for business class [{}] created succesfully", table.getName());
	}

	public void generateBusinessViewMappings(List<IJpaView> jpaViews, boolean isUpdatableMapping) throws Exception {
		for (IJpaView jpaView : jpaViews) {
			generateBusinessTableMappings(jpaView.getInnerTables(), isUpdatableMapping);
		}
		createViewsFile(jpaViews);
	}

	public void generatePersistenceUnitMapping(JpaModel model) throws Exception {
		model.setPersistenceUnitName(persistenceUnitName);
		createPersistenceUnitFile(persistenceUnitTemplate, model);
	}

	/**
	 * This method create a single java class file
	 *
	 * @param templateFile
	 * @param businessTable
	 * @param jpaTable
	 */
	private void createJavaFile(File templateFile, IJpaTable jpaTable, String className, boolean isUpdatableMapping) {

		VelocityContext velocityContext;

		velocityContext = new VelocityContext();
		velocityContext.put("jpaTable", jpaTable); //$NON-NLS-1$
		if (jpaTable.getPhysicalType().equalsIgnoreCase("View")) {
			velocityContext.put("isUpdatable", false); //$NON-NLS-1$
		} else {
			velocityContext.put("isUpdatable", isUpdatableMapping); //$NON-NLS-1$
		}

		File outputDir = new File(srcDir, StringUtils.strReplaceAll(jpaTable.getPackage(), ".", "/"));
		outputDir.mkdirs();
		File outputFile = new File(outputDir, className + ".java");

		createFile(templateFile, outputFile, velocityContext);

		// logger.debug("Created mapping file [{}] for table [{}]", outputFile, jpaTable.getClassName());
	}

	/**
	 * This method create a single java class file
	 *
	 * @param templateFile
	 * @param businessTable
	 * @param views
	 */
	private void createViewsFile(List<IJpaView> jpaViews) {

		VelocityContext context;

		logger.trace("IN");

		try {

			context = new VelocityContext();
			context.put("jpaViews", jpaViews); //$NON-NLS-1$

			File outputFile = new File(srcDir, "views.json");

			createFile(viewTemplate, outputFile, context);
		} catch (Throwable t) {
			logger.error("Impossible to create mapping", t);
		} finally {
			logger.trace("OUT");
		}

	}

	/**
	 * This method create a single java class file
	 *
	 * @param templateFile
	 * @param businessTable
	 * @param jpaView
	 */
	private void createPersistenceUnitFile(File templateFile, JpaModel model) {

		VelocityContext context;

		logger.trace("IN");

		try {
			context = new VelocityContext();
			List<IJpaTable> jpaTables = new ArrayList<IJpaTable>();
			jpaTables.addAll(model.getTables());
			List<IJpaView> jpaViews = model.getViews();
			for (IJpaView jpaView : jpaViews) {
				jpaTables.addAll(jpaView.getInnerTables());
			}

			context.put("jpaTables", jpaTables); //$NON-NLS-1$
			context.put("model", model);

			File outputDir = new File(srcDir, "META-INF");
			outputDir.mkdirs();

			File outputFile = new File(outputDir, "persistence.xml");

			createFile(templateFile, outputFile, context);
		} catch (Throwable t) {
			logger.error("Impossible to create persitance.xml", t);
		} finally {
			logger.trace("OUT");
		}
	}

	private void createLabelsFile(File templateFile, JpaModel model) {

		VelocityContext context;

		logger.trace("IN");

		try {
			logger.debug("Create labels.properties");

			context = new VelocityContext();
			List<IJpaTable> tables = new ArrayList<IJpaTable>();
			tables.addAll(model.getTables());
			for (IJpaView jpaView : model.getViews()) {
				tables.addAll(jpaView.getInnerTables());
			}

			context.put("jpaTables", tables); //$NON-NLS-1$
			context.put("jpaViews", model.getViews());

			File outputFile = new File(srcDir, "label.properties");

			createFile(templateFile, outputFile, context);
		} catch (Throwable t) {
			logger.error("Impossible to create label.properties", t);
		} finally {
			logger.trace("OUT");
		}
	}

	private void createPropertiesFile(File templateFile, JpaModel model) {

		VelocityContext context;

		logger.trace("IN");

		try {
			logger.debug("Create qbe.properties");

			context = new VelocityContext();

			List<IJpaTable> tables = new ArrayList<IJpaTable>();
			tables.addAll(model.getTables());
			// old implementation
			// for (IJpaView jpaView : model.getViews()) {
			// tables.addAll(jpaView.getInnerTables());
			// }

			context.put("jpaTables", tables); //$NON-NLS-1$
			context.put("jpaViews", model.getViews()); //$NON-NLS-1$

			File outputFile = new File(srcDir, "qbe.properties");

			createFile(templateFile, outputFile, context);
		} catch (Throwable t) {
			logger.error("Impossible to create qbe.properties", t);
		} finally {
			logger.trace("OUT");
		}
	}

	private void createCfieldsFile(File templateFile, JpaModel model) {
		VelocityContext context;

		logger.trace("IN");

		try {
			logger.debug("Create cfields_meta.xml");

			context = new VelocityContext();
			context.put("jpaTables", model.getTables()); //$NON-NLS-1$
			context.put("jpaViews", model.getViews()); //$NON-NLS-1$

			// File outputFile = new File(distDir, "cfields_meta.xml");
			// move cFields inside datamart.jar
			File outputFile = new File(srcDir, "cfields_meta.xml");

			createFile(templateFile, outputFile, context);
		} catch (Throwable t) {
			logger.error("Impossible to create cfields_meta.xml", t);
		} finally {
			logger.trace("OUT");
		}
	}

	private void createRelationshipFile(File templateFile, JpaModel model) {
		VelocityContext context;

		logger.trace("IN");

		try {
			logger.debug("Create relationships.json");

			context = new VelocityContext();
			context.put("jpaTables", model.getTables()); //$NON-NLS-1$
			context.put("jpaViews", model.getViews()); //$NON-NLS-1$

			File outputFile = new File(srcDir, "relationships.json");

			createFile(templateFile, outputFile, context);
			BufferedReader r = new BufferedReader(new FileReader(outputFile));
			String content = "", l = "";
			while ((l = r.readLine()) != null)
				content += l + "\n";
			JSONObject o = new JSONObject(content);
			r.close();
		} catch (Throwable t) {
			logger.error("Impossible to create relationships.json", t);
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

	private void generateHierarchiesFile(File templateFile, BusinessModel businessModel) {
		VelocityContext context;
		Model model = businessModel.getParentModel();
		if (model.getOlapModels() != null && model.getOlapModels().size() > 0) {
			OlapModel olapModel = model.getOlapModels().get(0);

			logger.trace("IN");

			try {
				logger.debug("Create hierarchies.xml");
				MondrianModel mondrianModel = new MondrianModel(olapModel);

				List<IMondrianDimension> dimensions = new ArrayList<IMondrianDimension>();
				dimensions.addAll(mondrianModel.getDimensions());

				context = new VelocityContext();
				context.put("model", mondrianModel); //$NON-NLS-1$
				context.put("dimensions", dimensions); //$NON-NLS-1$

				File outputFile = new File(srcDir, "hierarchies.xml");

				createFile(templateFile, outputFile, context);
			} catch (Throwable t) {
				logger.error("Impossible to create hierarchies.xml", t);
			} finally {
				logger.trace("OUT");
			}
		}
	}

	// =======================================================================
	// ACCESSOR METHODS
	// =======================================================================

	@Override
	public void hideTechnicalResources() {
		logger.debug("IN");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			if (baseOutputDir != null && baseOutputDir.exists()) {
				IProject proj = workspace.getRoot().getProject(baseOutputDir.getParentFile().getParentFile().getName());
				workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				IFolder iFolder = proj.getFolder(baseOutputDir.getParentFile().getName() + "\\" + baseOutputDir.getName());
				if (iFolder.exists()) {
					iFolder.setHidden(true);
					iFolder.setTeamPrivateMember(true);
					iFolder.setDerived(true, null);
					iFolder.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
					proj.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
			} else {
				logger.warn("Exception occurred before creating baseoutputDir: no resource to hide");
			}
		} catch (Exception e) {
			logger.error("Error in hiding technical model folders ", e);
			throw new GenerationException("Error in hiding technical model folders", e);
		}
		logger.debug("OUT");

	}

	public File getTemplateDir() {
		return templateDir;
	}

	public void setTemplateDir(File templateDir) {
		this.templateDir = templateDir;
	}

	public File getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(File srcDir) {
		this.srcDir = srcDir;
	}

	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public File getBaseOutputDir() {
		return baseOutputDir;
	}

	public void setBaseOutputDir(File baseOutputDir) {
		this.baseOutputDir = baseOutputDir;
	}

	public File getDistDir() {
		return distDir;
	}

	public void setDistDir(File distDir) {
		this.distDir = distDir;
	}

}
