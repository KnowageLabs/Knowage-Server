/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.resourcemanager.resource.dto;

import it.eng.knowage.boot.validation.AlphanumericWithBrackets;
import it.eng.knowage.boot.validation.Xss;

public class MetadataDTO {

	@Xss
	@AlphanumericWithBrackets
	private String name;

	@Xss
	@AlphanumericWithBrackets
	private String version;

	@Xss
	@AlphanumericWithBrackets
	private String typeOfAnalytics;

	private boolean openSource;

	@Xss
	@AlphanumericWithBrackets
	private String description;

	@Xss
	@AlphanumericWithBrackets
	private String accuracyAndPerformance;

	@Xss
	@AlphanumericWithBrackets
	private String usageOfTheModel;

	@Xss
	@AlphanumericWithBrackets
	private String formatOfData;

	private String image;

	public MetadataDTO() {
		super();
	}

	public MetadataDTO(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTypeOfAnalytics() {
		return typeOfAnalytics;
	}

	public void setTypeOfAnalytics(String typeOfAnalytics) {
		this.typeOfAnalytics = typeOfAnalytics;
	}

	public boolean isOpenSource() {
		return openSource;
	}

	public void setOpenSource(boolean openSource) {
		this.openSource = openSource;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAccuracyAndPerformance() {
		return accuracyAndPerformance;
	}

	public void setAccuracyAndPerformance(String accuracyAndPerformance) {
		this.accuracyAndPerformance = accuracyAndPerformance;
	}

	public String getUsageOfTheModel() {
		return usageOfTheModel;
	}

	public void setUsageOfTheModel(String usageOfTheModel) {
		this.usageOfTheModel = usageOfTheModel;
	}

	public String getFormatOfData() {
		return formatOfData;
	}

	public void setFormatOfData(String formatOfData) {
		this.formatOfData = formatOfData;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

}