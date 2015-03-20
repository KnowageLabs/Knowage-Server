<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 <script language="javascript" type="text/javascript">
	Sbi.template = {
		mapName: "WATSONs",
				

		analysisType: "proportionalSymbols",
		targetLayerName: "spagobi_capoluoghi",
		targetLayerLabel: "Capoluoghi",
		feautreInfo: [["REGIONE","gl_regione"], ["CAPOLUOGO","nome"]],
		indicators: [["numero_watson", "XNUMERO"],["valore_watson", "XVALORE"]],

		businessId: "id_capoluog", //it links to alphanumeric data into spagobi dataset
		geoId: "id_capoluog", //it links to geometires 

		targetLayerConf: {
			text: 'Capoluoghi'
			, name: 'spagobi_capoluoghi'
			, url: 'http://localhost:8080/geoserver/wfs'	
			//data: usa_states
		},
				
		geojsonUrl: "localhost:8080",


		inlineDocumentConf: {
			label: 'DIALCHART_simpledial'
			, staticParams: {
				param1: 'andrea'
			}
			, dynamicParams: {
				value: 'numero_watson'
			}
			, displayToolbar: 'false'
			, displaySliders: 'false'
		},

		detailDocumentConf: {
			label: 'DepartmentList'
			, staticParams: {
				departmentId: '3'
			}
			, dynamicParams: {
				regione: 'gl_regione'
			}
			, displayToolbar: 'false'
			, displaySliders: 'false'
		} ,
			    
		role: "spagobi/admin",
				
			    
		lon: 6.090,
		lat: 40.373,
		zoomLevel: 5
	};

</script>