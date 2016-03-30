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
package it.eng.knowage.generator;

import it.eng.knowage.common.TestConstants;
import it.eng.knowage.meta.generator.jpamapping.JpaMappingClassesGenerator;
import it.eng.knowage.meta.generator.jpamapping.JpaMappingCodeGenerator;
import it.eng.knowage.meta.generator.jpamapping.JpaMappingJarGenerator;

import java.io.File;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TestGeneratorFactory {
	public static JpaMappingCodeGenerator createCodeGenerator() {
		File generatorProjectRootFolder = new File(TestConstants.workspaceFolder, "it.eng.knowage.meta.generator");
		File generatorProjectTemplateFolder = new File(generatorProjectRootFolder, "templates");
		JpaMappingCodeGenerator.defaultTemplateFolderPath = generatorProjectTemplateFolder.toString();
		JpaMappingCodeGenerator jpaMappingCodeGenerator = new JpaMappingCodeGenerator();
		File generatorProjectSrcFolder = new File(TestConstants.workspaceFolder, "src");
		jpaMappingCodeGenerator.setSrcDir(generatorProjectSrcFolder);
		File generatorProjectDistFolder = new File(TestConstants.workspaceFolder, "dist");
		jpaMappingCodeGenerator.setDistDir(generatorProjectDistFolder);
		return jpaMappingCodeGenerator;
	}

	public static JpaMappingClassesGenerator createClassesGenerator() {
		File generatorProjectRootFolder = new File(TestConstants.workspaceFolder, "it.eng.knowage.meta.generator");
		File generatorProjectTemplateFolder = new File(generatorProjectRootFolder, "templates");
		File generatorProjectLibFolder = new File(TestConstants.libFolder, "eclipselink");

		JpaMappingClassesGenerator jpaMappingClassesGenerator;
		JpaMappingClassesGenerator.defaultTemplateFolderPath = generatorProjectTemplateFolder.toString();
		jpaMappingClassesGenerator = new JpaMappingJarGenerator();

		jpaMappingClassesGenerator.setLibDir(generatorProjectLibFolder);
		jpaMappingClassesGenerator.setLibs(new String[] { "org.eclipse.persistence.core_2.1.2.jar", "javax.persistence_2.0.1.jar" });
		return jpaMappingClassesGenerator;
	}

	public static JpaMappingJarGenerator createJarGenerator() {
		JpaMappingJarGenerator jpaMappingJarGenerator;
		JpaMappingJarGenerator.defaultTemplateFolderPath = "D:/Sviluppo/Athena/knowagemeta-unit-test/workspaces/metadata/it.eng.knowage.meta.generator/templates";
		jpaMappingJarGenerator = new JpaMappingJarGenerator();
		File projectRootFolder = new File("D:/Sviluppo/Athena/knowagemeta-unit-test/workspaces/metadata/it.eng.knowage.meta.generator");
		jpaMappingJarGenerator.setLibDir(new File(projectRootFolder, "libs/eclipselink"));
		jpaMappingJarGenerator.setLibs(new String[] { "org.eclipse.persistence.core_2.1.2.jar", "javax.persistence_2.0.1.jar" });
		return jpaMappingJarGenerator;
	}
}
