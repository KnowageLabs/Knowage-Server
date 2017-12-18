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