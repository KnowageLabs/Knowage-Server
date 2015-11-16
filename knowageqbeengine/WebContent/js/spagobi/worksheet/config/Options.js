/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
Ext.ns("Sbi.worksheet.config");

Sbi.worksheet.config.options = {
	'attributes' : 
		[
		 {
			  name : 'attributePresentation'
			, description : 'An attribute can be displayed using its code or its description or both'
			, type : 'radiogroup'
			, applyTo : 'datasetdata'
			, label : LN('sbi.worksheet.config.options.attributepresentation.label')
			, items: 
				[
	                 {boxLabel: LN('sbi.worksheet.config.options.attributepresentation.code'), inputValue: "code", checked: true}
	               , {boxLabel: LN('sbi.worksheet.config.options.attributepresentation.description'), inputValue: "description"}
	               , {boxLabel: LN('sbi.worksheet.config.options.attributepresentation.both'), inputValue: "both"}
			    ]
		 }
		]
	,
	'measures' :
		[
		 {
			  name : 'measureScaleFactor'
			, description : 'The scale factor of a measure'
			, type : 'radiogroup'
			, applyTo : 'datasetdata'
			, label : LN('sbi.worksheet.config.options.measurepresentation.scalefactor')
			, items: 
				[
	                 {boxLabel: LN('sbi.worksheet.config.options.measurepresentation.NONE'), inputValue: "NONE", checked: true}
	               , {boxLabel: LN('sbi.worksheet.config.options.measurepresentation.K'), inputValue: "K"}
	               , {boxLabel: LN('sbi.worksheet.config.options.measurepresentation.M'), inputValue: "M"}
	               , {boxLabel: LN('sbi.worksheet.config.options.measurepresentation.G'), inputValue: "G"}
			    ]
		 }
		]
};