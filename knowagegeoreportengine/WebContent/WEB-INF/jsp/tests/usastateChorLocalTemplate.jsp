<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
 <script language="javascript" type="text/javascript">
	Sbi.template = {
		mapName: "WATSONs",

		analysisType: "choropleth",
		
		feautreInfo: [["State Name","STATE_NAME"], ["Extension (KM)","LAND_KM"], ["Population", "PERSONS"]],
		indicators: [["unit_sales", "Unit sales"],["store_sales", "Sales"], ["store_cost", "Cost"]],

		businessId: "sales_state", //it links to alphanumeric data into spagobi dataset
		geoId: "STATE_ABBR", //it links to geometires 

		targetLayerConf: {
			text: 'States'
			, name: 'usa_states'
			//, url: 'http://localhost:8080/geoserver/wfs'	
			, data: 'usa_states.json'
		},
		
		inlineDocumentConf: {
			label: 'DIALCHART_simpledial'
			, staticParams: {
				param1: 'andrea'
			}
			, dynamicParams: {
				state: 'STATE_NAME'
			}
			, displayToolbar: 'false'
			, displaySliders: 'false'
		},

		detailDocumentConf: [{
			text: 'Link1'
			, label: 'DepartmentList'
			, staticParams: {
				departmentId: '3'
			}
			, dynamicParams: {
				state: 'STATE_NAME'
			}
			, displayToolbar: 'false'
			, displaySliders: 'false'
		}, {
			text: 'Link2'
				, label: 'DepartmentList'
				, staticParams: {
					departmentId: '3'
				}
				, dynamicParams: {
					state: 'STATE_NAME'
				}
				, displayToolbar: 'false'
				, displaySliders: 'false'
			}] ,
			    
		role: "spagobi/admin",
				
			    
		lon: -96.800,
		lat: 40.800,
		zoomLevel: 4
	};

</script>