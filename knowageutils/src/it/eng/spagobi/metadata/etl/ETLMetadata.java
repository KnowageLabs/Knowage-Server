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
package it.eng.spagobi.metadata.etl;

import java.util.Set;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ETLMetadata {

	Set<ETLRDBMSSource> rdbmsSources;
	Set<ETLComponent> sourceTables;
	Set<ETLComponent> targetTables;
	Set<String> sourceFiles;
	Set<String> targetFiles;

	public ETLMetadata(Set<ETLRDBMSSource> rdbmsSources, Set<ETLComponent> sourceTables, Set<ETLComponent> targetTables, Set<String> sourceFiles,
			Set<String> targetFiles) {
		super();
		this.rdbmsSources = rdbmsSources;
		this.sourceTables = sourceTables;
		this.targetTables = targetTables;
		this.sourceFiles = sourceFiles;
		this.targetFiles = targetFiles;
	}

	public Set<ETLRDBMSSource> getRdbmsSources() {
		return rdbmsSources;
	}

	public void setRdbmsSources(Set<ETLRDBMSSource> rdbmsSources) {
		this.rdbmsSources = rdbmsSources;
	}

	public Set<ETLComponent> getSourceTables() {
		return sourceTables;
	}

	public void setSourceTables(Set<ETLComponent> sourceTables) {
		this.sourceTables = sourceTables;
	}

	public Set<ETLComponent> getTargetTables() {
		return targetTables;
	}

	public void setTargetTables(Set<ETLComponent> targetTables) {
		this.targetTables = targetTables;
	}

	public Set<String> getSourceFiles() {
		return sourceFiles;
	}

	public void setSourceFiles(Set<String> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}

	public Set<String> getTargetFiles() {
		return targetFiles;
	}

	public void setTargetFiles(Set<String> targetFiles) {
		this.targetFiles = targetFiles;
	}

	/**
	 * Get the ETLRDBMSSource object corresponding to the passed component name
	 *
	 * @param componentName
	 * @return the corresponding ETLRDBMSSource, otherwise null
	 */
	public ETLRDBMSSource getRDBMSSourceByComponentName(String componentName) {
		for (ETLRDBMSSource rdbmsSource : rdbmsSources) {
			if (rdbmsSource.getComponentName().equalsIgnoreCase(componentName)) {
				// found
				return rdbmsSource;
			}
		}
		// not found
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ETLRDBMSSource content : rdbmsSources) {
			sb.append("RDBMS Source > Component Name: " + content.getComponentName() + " DB Name: " + content.getDatabaseName() + " Host: " + content.getHost()
					+ " JDBC Url: " + content.getJdbcUrl() + " Label: " + content.getLabel() + " Schema: " + content.getSchema());
		}

		sb.append("Source Tables >");
		for (ETLComponent sourceTable : sourceTables) {
			sb.append(sourceTable.toString());
		}
		sb.append("Target Tables >");
		for (ETLComponent targetTable : targetTables) {
			sb.append(targetTable.toString());
		}
		sb.append("Source Files >");
		for (String sourceFile : sourceFiles) {
			sb.append(sourceFile);
		}
		sb.append("Target Files >");
		for (String targetFile : targetFiles) {
			sb.append(targetFile);
		}

		return sb.toString();
	}

}
