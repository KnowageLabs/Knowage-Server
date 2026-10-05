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
package it.eng.qbe.datasource.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import it.eng.qbe.model.structure.IModelGroupDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import junit.framework.TestCase;

public class FileDataSourceConfigurationGroupsTestCase extends TestCase {

	private final List<File> temporaryFiles = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		for (File file : temporaryFiles) {
			Files.deleteIfExists(file.toPath());
		}
		super.tearDown();
	}

	public void testLoadGroups() throws Exception {
		File jarFile = createJar("{\"groups\":["
					+ "{\"model\":\"Foodmart\",\"name\":\"Sales\",\"description\":\"Sales area\",\"entities\":[\"it.eng.knowage.meta.Product\",\"it.eng.knowage.meta.Store\"]},"
					+ "{\"model\":\"Foodmart\",\"name\":\"Customers\",\"entities\":[\"it.eng.knowage.meta.Customer\"]}"
					+ "]}");

		FileDataSourceConfiguration configuration = new FileDataSourceConfiguration("Foodmart", jarFile);
		List<IModelGroupDescriptor> groups = configuration.loadGroups();

		assertEquals(2, groups.size());
		assertEquals("Foodmart", groups.get(0).getModelName());
		assertEquals("Sales", groups.get(0).getName());
		assertEquals("Sales area", groups.get(0).getDescription());
		assertEquals(2, groups.get(0).getEntityTypes().size());
		assertEquals("it.eng.knowage.meta.Product", groups.get(0).getEntityTypes().get(0));
		assertEquals("Customers", groups.get(1).getName());
		assertEquals(1, groups.get(1).getEntityTypes().size());
	}

	public void testLegacyJarWithoutGroups() throws Exception {
		FileDataSourceConfiguration configuration = new FileDataSourceConfiguration("Legacy", createJar(null));
		assertTrue(configuration.loadGroups().isEmpty());
	}

	public void testEmptyGroups() throws Exception {
		FileDataSourceConfiguration configuration = new FileDataSourceConfiguration("Empty", createJar("{\"groups\":[]}"));
		assertTrue(configuration.loadGroups().isEmpty());
	}

	public void testRuntimeModelNameAndUtf8Labels() throws Exception {
		FileDataSourceConfiguration configuration = new FileDataSourceConfiguration("CatalogueName",
				createJar("{\"groups\":[{\"model\":\"OriginalName\",\"name\":\"Vendit\u00e8\","
						+ "\"uniqueName\":\"sales-id\",\"entities\":[]}]}"));
		IModelGroupDescriptor group = configuration.loadGroups().get(0);
		assertEquals("CatalogueName", group.getModelName());
		assertEquals("Vendit\u00e8", group.getName());
		assertEquals("sales-id", group.getUniqueName());
	}

	public void testCompositeGroupsKeepTheirModelScope() throws Exception {
		CompositeDataSourceConfiguration configuration = new CompositeDataSourceConfiguration();
		configuration.addSubConfiguration(new FileDataSourceConfiguration("First",
				createJar("{\"groups\":[{\"name\":\"Sales\",\"entities\":[\"example.Shared\"]}]}")));
		configuration.addSubConfiguration(new FileDataSourceConfiguration("Second",
				createJar("{\"groups\":[{\"name\":\"Sales\",\"entities\":[\"example.Shared\"]}]}")));
		List<IModelGroupDescriptor> groups = configuration.loadGroups();
		assertEquals(2, groups.size());
		assertEquals("First", groups.get(0).getModelName());
		assertEquals("Second", groups.get(1).getModelName());
	}

	public void testInvalidGroupsAreReported() throws Exception {
		for (String invalid : new String[] { "{invalid", "{}", "{\"groups\":[{\"entities\":[]}]}",
				"{\"groups\":[{\"name\":\"Sales\",\"entities\":[null]}]}" }) {
			try {
				new FileDataSourceConfiguration("Invalid", createJar(invalid)).loadGroups();
				fail("Invalid group metadata must not silently produce a flat tree");
			} catch (SpagoBIRuntimeException expected) {
				assertNotNull(expected.getCause());
			}
		}
	}

	public void testConfigurationsWithoutGroupsDAO() {
		assertTrue(new InMemoryDataSourceConfiguration("Memory").loadGroups().isEmpty());
		assertTrue(new DelegatingDataSourceConfiguration("Delegating").loadGroups().isEmpty());
	}

	private File createJar(String groupsJson) throws Exception {
		File jarFile = File.createTempFile("groups", ".jar");
		temporaryFiles.add(jarFile);
		try (JarOutputStream output = new JarOutputStream(new FileOutputStream(jarFile))) {
			if (groupsJson != null) {
				output.putNextEntry(new JarEntry("groups.json"));
				output.write(groupsJson.getBytes(StandardCharsets.UTF_8));
				output.closeEntry();
			}
		}
		return jarFile;
	}
}
