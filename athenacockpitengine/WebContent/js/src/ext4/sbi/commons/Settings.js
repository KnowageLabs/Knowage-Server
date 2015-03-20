/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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

	, showUsedDataSetFilter: true
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