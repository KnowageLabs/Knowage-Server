/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
Ext.ns("Sbi.settings");

Sbi.settings.tools = {
	dataset: {
		filedatasetpanel: {
			supportedEncodings: [
			   ['windows-1252', 'windows-1252']
			   , ['UTF-8', 'UTF-8']
			   , ['UTF-16','UTF-16']
			   , ['US-ASCII','US-ASCII']
			   , ['ISO-8859-1','ISO-8859-1']
       	    ]
			, defaultEncoding: "UTF-8"
		}
	}
};

Sbi.settings.mydata = {
	// the toolbar that appears when a new document is created over a dataset
	toolbar: {
		hide: false
	}
	, hiddenActions: []
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
	, defaultFilter: 'MyDataSet'

	, showMyDataSetFilter: true
	, showEnterpriseDataSetFilter: true
	, showSharedDataSetFilter: true
	, showAllDataSetFilter: true
	, showCkanDataSetFilter: true
	/**
	 * MY DATA :
	 * put false for previous behavior (all USER public ds + owned)
	 * put true for showing only owned datasets
	 */
	, showOnlyOwner: true
	/**
	 * Visibility of MyData tabs
	 */
	, showDataSetTab: true
	, showModelsTab: true
	, showSmartFilterTab: true
	/**
	 * Visibility of MyData TabToolbar (this hide the whole tab toolbar)
	 */
	, showTabToolbar: true
};

/**
 * My Analysis
 */
Sbi.settings.myanalysis = {
		/**
		 * This options will set the default active
		 * filter used the first time the MyAnalysis page is opened
		 * Possibile values are:
		 * -'Report'
		 * -'Cockpit'
		 * -'Map'
		 * -'All'
		 * 
		 * Make attention that the default filter selected must be 
		 * a visible filter, so for example if 
		 * defaultFilter:'Report'
		 * showReportFilter must be true
		 */
		 defaultFilter: 'All'

		, showReportFilter: true
		, showCockpitFilter: true
		, showMapFilter: true
		, showAllFilter: true		
};

/**
 * Execution  panel settings
 */
Sbi.settings.execution = {
		
		executionPanel : {
			border : false    	    // true to display a border around document execution panel
		  , popupWindowHeight: 300 //#px for height of window when document is executed as a popup
		  , popupWindowWidth:  500 //#px for width of window when document is executed as a popup
		}

		, parametersPanel: {
			columnNo: 3
			, mandatoryFieldAdditionalString: '*' // a String that will be added in the label of the mandatory fields
			, columnWidth: 310
			, labelAlign: 'left'
			, fieldWidth: 180	
			, maskOnRender: false
			, fieldLabelWidth: 100
			, moveInMementoUsingCtrlKey: false
			, maxFieldHeight : 300  // max height to be available for rendering an input field; in case the field exceeds, scroll bars will appear
			, labelSeparator : ':'   // separator between the driver's label and the input field, e.g. ':', an empty string or others...
		}

		, shortcutsPanel: {
			panelsOrder: {
				subobjects: 1
				, snapshots: 2
			}
			, height: 205
		}
		
		, toolbar:{
			hideForEngineLabels:[]	//list of engines without toolbar 
		}
		
		, parametersselectionpage : {
			//parametersRegion : "east" // admissible values: east/north, now in document detail with values top and right
			parametersSliderWidth : 320 // valid in case of east region
			, parametersSliderHeight : 200 // valid in case of north region
			, parametersSliderCollapsed : false // applied only in case there are no parameters to fill
			, parametersSliderFloatable : false // true to allow clicking on collapsed parameters panel's bar to display the parameters' panel floated above the layout, 
												// false to force the user to fully expand parameters' panel by clicking the expand button to see it again
		}
};

/**
 * Details for save window GUI (final user side)
Sbi.settings.saveWindow = {
		showScopeInfo: false
};
 */

/**
 * Document browser settings
 */
Sbi.settings.browser = {
		mexport: {
			massiveExportWizard: {
				resizable: true
			}
			, massiveExportWizardOptionsPage: {
				
			}, massiveExportWizardParametersPage: {
				
			}
			, massiveExportWizardTriggerPage: {
				showJobDetails: false
			}
		}
	  , showLeftPanels: true
	  , showBreadCrumbs: true
//	  , maxNumberOfExecutionTabs: 1 	 //the maximum number of tabs to open on execution of documents if valorized
	  , typeLayout: 'tab'				 //possible values: 'tab' or 'card'
	  , showTitle: true 
	  , hideGoBackToolbar: false			//hide (if true) the bottom bar with goBack links
	  , showCreateButton: true
} 

Sbi.settings.invisibleParameters = {
	remove : true
};

/**
 * KPI
 */
Sbi.settings.kpi = {
		goalModelInstanceTreeUI: {
			goalCustom: false
		}
};

/**
 * WIDGETS
 */
Sbi.settings.widgets = {
		TreeLookUpField : {
			//true to allow the selection of the internal node of the tree driver
			//false to allow the selection only foe the leafs
			allowInternalNodeSelection: true
		}
};

// Specific IE settings
Ext.ns("Sbi.settings.IE");

// Workaround: on IE, it takes a long time to destroy the stacked execution wizards.
// If the Sbi.settings.IE.destroyExecutionWizardWhenClosed is false, stacked execution wizards are not destroyed but only hidden;
// if the Sbi.settings.IE.destroyExecutionWizardWhenClosed is true, stacked execution wizards are destroyed instead (this may cause the IE 
// warning message "A script on this page is causing Internet Explorer to run slowly")
Sbi.settings.IE.destroyExecutionWizardWhenClosed = false;



