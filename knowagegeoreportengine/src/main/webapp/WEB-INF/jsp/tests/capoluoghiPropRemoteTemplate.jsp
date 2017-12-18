<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


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
