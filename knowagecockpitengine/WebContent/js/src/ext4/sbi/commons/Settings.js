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

Ext.ns("Sbi.settings");

Sbi.settings.cockpit = {
		layout : {
			useRelativeDimensions: false
		}

};


Sbi.settings.mydata = {
	/**
	 * This options will set the default active
	 * filter used the first time the MyData page is opened
	 * Possibile values are:
	 * -'MyDataSet'
	 * -'EnterpriseDataSet'
	 * -'SharedDataSet'
	 * -'AllDataSet'
	 *
	 * Make attention that the default filter selected must be
	 * a visible filter, so for example if
	 * defaultFilter:'MyDataSet'
	 * showMyDataSetFilter must be true
	 */
	  defaultFilter: 'AllDataSet' //'UsedDataSet'

	, showUsedDataSetFilter: false
	, showMyDataSetFilter: false
	, showEnterpriseDataSetFilter: false
	, showSharedDataSetFilter: false
	, showAllDataSetFilter: true
	/**
	 * MY DATA :
	 * put false for previous behavior (all USER public ds + owned)
	 * put true for showing only owned datasets
	 */
	, showOnlyOwner: false
	/**
	 * Visibility of MyData tabs
	 */
	, showDataSetTab: true
	, showModelsTab: false
	/**
	 * Visibility of MyData TabToolbar (this hide the whole tab toolbar)
	 */
	, showTabToolbar: true
};

Sbi.settings.widgets = {
		//Details for specific file upload management (ex: img for document preview,...)
	   FileUploadPanel: {
			imgUpload: {
				maxSizeFile: 10485760
			  , directory: '/preview/images' //starting from /resources directory
			  , extFiles: ['BMP', 'IMG', 'JPG', 'PNG', 'GIF']
			}
		}
};