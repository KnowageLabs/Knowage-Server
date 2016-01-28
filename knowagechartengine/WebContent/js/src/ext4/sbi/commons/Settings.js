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

/**
 * Configurations for the Designer. 
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
Sbi.settings.chart = 
{
	/**
	 * Set the visibility state of the charts' border. This is used for 
	 * BAR, LINE, RADAR and SCATTER chart types.
	 */
	borderVisible: false,
		
	/**
	 * Customization for the left panel of the Designer (on the left side 
	 * of the separating line that splits the Designer into two parts) 
	 * that contains the GUI elements for the chart style, chart type and
	 * collections of measures and attributes.
	 */
	leftDesignerContainer:
	{
		/**
		 * The width percentage of all GUI items in the left Designer's 
		 * panel container. 
		 */
		widthPercentageOfItem: "100%"
	},
		
	configurationStep:
	{
		/**
		 * Property for the sign that will represent mandatory fields on
		 * the Designer.
		 */
		htmlForMandatoryFields: "<span style='color: rgb(255, 0, 0);'> [&#9873]</span>",
		/**
		 * All GUI fields that appear inside the Designer (in its panels 
		 * and popups) should take this width.
		 */
		widthOfFields: 280,
		
		/**
		 * Padding for fields that are out of fieldsets and that are lying
		 * on three different positions: on the top, in the middle (inner)
		 * or at the end of the panel. 
		 */
		paddingOfTopFields: "0 0 5 0",			
		paddingOfInnerFields: "5 0 5 0",		
		paddingOfBottomFields: "5 0 0 0",
		
		/**
		 * Margin for fields that are inside of fieldsets and that are lying
		 * on three different positions.
		 */		
		marginOfTopFieldset: '5 0 2.5 0',
		marginOfTopFieldsetButtons: "5 0 5 10",
		
		marginOfInnerFieldset:  '2.5 0 2.5 0',		
		marginOfInnerFieldsetButtons: "0 0 5 10",
		
		marginOfBottomFieldset:  '2.5 0 5 0',
			
		/**
		 * Layout that all fields in the same panel should follow.
		 */
		layoutFieldsInMainPanel: 
    	{
        	type:'hbox',
        	align:"center"
		},
	},
	
	parallel:
	{
		tooltip:
		{
			/**
			 * This parameter is used for the threshold that is used for
			 * determining if the text on the PARALLEL's tooltip is going
			 * to be black (when the tooltip's background is lighter color)
			 * or white (when the background is too dark).
			 */
			darknessThreshold: 0.7
		}
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