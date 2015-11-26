var app = angular.module('geoManager', [ 'ngMaterial','geoModule' ]);

app.controller('mapCtrl', [ "$scope",funzione ]);

function funzione($scope){
	$scope.mydata={"root":[{"layerId":9,"name":"   <md-icon md-font-icon=\"fa fa-bullseye\" ></md-icon>","descr":"America WMS","type":"WMS","label":"<md-icon md-font-icon=\"fa fa-bullseye\" ></md-icon>","baseLayer":false,"layerDef":"eyJsYXllcklkIjoiQW1lcmljYSBXTVMiLCJsYXllckxhYmVsIjoiQW1lcmljYSBXTVMiLCJsYXllck5hbWUiOiJBbWVyaWNhIFdNUyIsImxheWVyX2ZpbGUiOm51bGwsInByb3BlcnRpZXMiOiIiLCJsYXllcl91cmwiOiJodHRwOi8vZGVtby5ib3VuZGxlc3NnZW8uY29tL2dlb3NlcnZlci93bXMiLCJsYXllcl96b29tIjpudWxsLCJsYXllcl9jZXRyYWxfcG9pbnQiOm51bGwsImxheWVyX29wdGlvbnMiOiJ7fSIsImxheWVyX3BhcmFtcyI6IntcIkxBWUVSU1wiOiBcInRvcHA6c3RhdGVzXCJ9IiwibGF5ZXJfb3JkZXIiOjF9","pathFile":null,"layerLabel":"America WMS","layerName":"America WMS","layerIdentify":"America WMS","layerURL":"http://demo.boundlessgeo.com/geoserver/wms","layerOptions":"{}","layerParams":"{\"LAYERS\": \"topp:states\"}","layerOrder":1,"category_id":207,"category":{"commonInfo":{"userIn":"biadmin","userUp":"biadmin","userDe":null,"sbiVersionIn":"4.0","sbiVersionUp":"4.0","sbiVersionDe":null,"organization":"SPAGOBI","timeIn":1445957105000,"timeUp":1445957121000,"timeDe":null},"valueId":207,"domainCd":"GEO_CATEGORY","domainNm":"GEO Category","valueCd":"Cartografia_Tematica","valueNm":"Cartografia Tematica","valueDs":"geo.cartografia.tematica"},"roles":null,"properties":null,"filebody":null},{"layerId":10,"name":"v_at_gis_limite_comunale_wgs84","descr":"v_at_gis_limite_comunale_wgs84","type":"WFS","label":"v_at_gis_limite_comunale_wgs84","baseLayer":false,"layerDef":"eyJsYXllcklkIjoidl9hdF9naXNfbGltaXRlX2NvbXVuYWxlX3dnczg0IiwibGF5ZXJMYWJlbCI6InZfYXRfZ2lzX2xpbWl0ZV9jb211bmFsZV93Z3M4NCIsImxheWVyTmFtZSI6InZfYXRfZ2lzX2xpbWl0ZV9jb211bmFsZV93Z3M4NCIsImxheWVyX2ZpbGUiOm51bGwsInByb3BlcnRpZXMiOiIiLCJsYXllcl91cmwiOiJodHRwOi8vcGFjd2ViLmVuZy5pdC9hc3R1dG8tZ2Vvc2VydmVyL0FUZVNPL293cz9zZXJ2aWNlPVdGUyZ2ZXJzaW9uPTEuMC4wJnJlcXVlc3Q9R2V0RmVhdHVyZSZ0eXBlTmFtZT1BVGVTTzp2X2F0X2dpc19saW1pdGVfY29tdW5hbGVfd2dzODQmbWF4RmVhdHVyZXM9NTAmb3V0cHV0Rm9ybWF0PWFwcGxpY2F0aW9uL2pzb24iLCJsYXllcl96b29tIjpudWxsLCJsYXllcl9jZXRyYWxfcG9pbnQiOm51bGwsImxheWVyX3BhcmFtcyI6bnVsbCwibGF5ZXJfb3B0aW9ucyI6bnVsbCwibGF5ZXJfb3JkZXIiOjJ9","pathFile":null,"layerLabel":"v_at_gis_limite_comunale_wgs84","layerName":"v_at_gis_limite_comunale_wgs84","layerIdentify":"v_at_gis_limite_comunale_wgs84","layerURL":"http://pacweb.eng.it/astuto-geoserver/ATeSO/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=ATeSO:v_at_gis_limite_comunale_wgs84&maxFeatures=50&outputFormat=application/json","layerOptions":null,"layerParams":null,"layerOrder":2,"category_id":207,"category":{"commonInfo":{"userIn":"biadmin","userUp":"biadmin","userDe":null,"sbiVersionIn":"4.0","sbiVersionUp":"4.0","sbiVersionDe":null,"organization":"SPAGOBI","timeIn":1445957105000,"timeUp":1445957121000,"timeDe":null},"valueId":207,"domainCd":"GEO_CATEGORY","domainNm":"GEO Category","valueCd":"Cartografia_Tematica","valueNm":"Cartografia Tematica","valueDs":"geo.cartografia.tematica"},"roles":null,"properties":null,"filebody":null},{"layerId":12,"name":"usa_states_file","descr":"usa_states_file","type":"File","label":"usa_states_file","baseLayer":false,"layerDef":"eyJwcm9wZXJ0aWVzIjpbXSwibGF5ZXJJZCI6InVzYV9zdGF0ZXNfZmlsZSIsImxheWVyTGFiZWwiOiJ1c2Ffc3RhdGVzX2ZpbGUiLCJsYXllck5hbWUiOiJ1c2Ffc3RhdGVzX2ZpbGUiLCJsYXllcl9maWxlIjoiQzpcXHdvcmtcXGF0aGVuYVxcYXBhY2hlLXRvbWNhdC03LjAuNTlcXHJlc291cmNlc1xcU1BBR09CSVxcTGF5ZXJcXHVzYV9zdGF0ZXNfZmlsZSIsImxheWVyX3VybCI6Im51bGwiLCJsYXllcl96b29tIjoibnVsbCIsImxheWVyX2NldHJhbF9wb2ludCI6Im51bGwiLCJsYXllcl9wYXJhbXMiOiJudWxsIiwibGF5ZXJfb3B0aW9ucyI6Im51bGwiLCJsYXllcl9vcmRlciI6M30=","pathFile":"C:\\work\\athena\\apache-tomcat-7.0.59\\resources\\SPAGOBI\\Layer\\usa_states_file","layerLabel":"usa_states_file","layerName":"usa_states_file","layerIdentify":"usa_states_file","layerURL":null,"layerOptions":null,"layerParams":null,"layerOrder":3,"category_id":207,"category":{"commonInfo":{"userIn":"biadmin","userUp":"biadmin","userDe":null,"sbiVersionIn":"4.0","sbiVersionUp":"4.0","sbiVersionDe":null,"organization":"SPAGOBI","timeIn":1445957105000,"timeUp":1445957121000,"timeDe":null},"valueId":207,"domainCd":"GEO_CATEGORY","domainNm":"GEO Category","valueCd":"Cartografia_Tematica","valueNm":"Cartografia Tematica","valueDs":"geo.cartografia.tematica"},"roles":null,"properties":[],"filebody":null},{"layerId":13,"name":"mylayer","descr":"mylayer","type":"WFS","label":"mylayer","baseLayer":false,"layerDef":"eyJsYXllcklkIjoibXlsYXllciIsImxheWVyTGFiZWwiOiJteWxheWVyIiwibGF5ZXJOYW1lIjoibXlsYXllciIsImxheWVyX2ZpbGUiOm51bGwsInByb3BlcnRpZXMiOiIiLCJsYXllcl91cmwiOiJodHRwOi8vc2lmLnJlZ2lvbmUuc2ljaWxpYS5pdC9hc3R1dG8tZ2Vvc2VydmVyL0FUZVNPL293cz9zZXJ2aWNlPVdGUyZ2ZXJzaW9uPTEuMC4wJnJlcXVlc3Q9R2V0RmVhdHVyZSZ0eXBlTmFtZT1BVGVTTzp2X2F0X2dpc19saW1pdGVfY29tdW5hbGUmbWF4RmVhdHVyZXM9NTAmb3V0cHV0Rm9ybWF0PWFwcGxpY2F0aW9uL2pzb24iLCJsYXllcl96b29tIjpudWxsLCJsYXllcl9jZXRyYWxfcG9pbnQiOm51bGwsImxheWVyX3BhcmFtcyI6bnVsbCwibGF5ZXJfb3B0aW9ucyI6bnVsbCwibGF5ZXJfb3JkZXIiOjF9","pathFile":null,"layerLabel":"mylayer","layerName":"mylayer","layerIdentify":"mylayer","layerURL":"http://sif.regione.sicilia.it/astuto-geoserver/ATeSO/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=ATeSO:v_at_gis_limite_comunale&maxFeatures=50&outputFormat=application/json","layerOptions":null,"layerParams":null,"layerOrder":1,"category_id":206,"category":{"commonInfo":{"userIn":"biadmin","userUp":"biadmin","userDe":null,"sbiVersionIn":"4.0","sbiVersionUp":"4.0","sbiVersionDe":null,"organization":"SPAGOBI","timeIn":1445957069000,"timeUp":1445957131000,"timeDe":null},"valueId":206,"domainCd":"GEO_CATEGORY","domainNm":"GEO Category","valueCd":"Cartografia_di_base","valueNm":"Cartografia di base","valueDs":"geo.cartografia.di.base"},"roles":null,"properties":null,"filebody":null},{"layerId":14,"name":"usastates","descr":"usastates","type":"WFS","label":"usastates","baseLayer":false,"layerDef":"eyJwcm9wZXJ0aWVzIjpbXSwibGF5ZXJJZCI6InVzYXN0YXRlcyIsImxheWVyTGFiZWwiOiJ1c2FzdGF0ZXMiLCJsYXllck5hbWUiOiJ1c2FzdGF0ZXMiLCJsYXllcl9maWxlIjoibnVsbCIsImxheWVyX3VybCI6Imh0dHA6Ly8xNjEuMjcuMjEzLjEwNTo0ODA4MS9nZW9zZXJ2ZXIvdG9wcC9vd3M/c2VydmljZT1XRlMmdmVyc2lvbj0xLjAuMCZyZXF1ZXN0PUdldEZlYXR1cmUmdHlwZU5hbWU9dG9wcDpzdGF0ZXMmbWF4RmVhdHVyZXM9NTAmb3V0cHV0Rm9ybWF0PWFwcGxpY2F0aW9uJTJGanNvbiIsImxheWVyX3pvb20iOiJudWxsIiwibGF5ZXJfY2V0cmFsX3BvaW50IjoibnVsbCIsImxheWVyX3BhcmFtcyI6Im51bGwiLCJsYXllcl9vcHRpb25zIjoibnVsbCIsImxheWVyX29yZGVyIjoyfQ==","pathFile":null,"layerLabel":"usastates","layerName":"usastates","layerIdentify":"usastates","layerURL":"http://161.27.213.105:48081/geoserver/topp/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=topp:states&maxFeatures=50&outputFormat=application%2Fjson","layerOptions":null,"layerParams":null,"layerOrder":2,"category_id":206,"category":{"commonInfo":{"userIn":"biadmin","userUp":"biadmin","userDe":null,"sbiVersionIn":"4.0","sbiVersionUp":"4.0","sbiVersionDe":null,"organization":"SPAGOBI","timeIn":1445957069000,"timeUp":1445957131000,"timeDe":null},"valueId":206,"domainCd":"GEO_CATEGORY","domainNm":"GEO Category","valueCd":"Cartografia_di_base","valueNm":"Cartografia di base","valueDs":"geo.cartografia.di.base"},"roles":null,"properties":[],"filebody":null}]};


	$scope.pageChanged=function(newPageNumber,itemsPerPage,searchValue, columnOrdering, reverseOrdering){
		console.log("newPageNumber",newPageNumber)
		console.log("itemsPerPage",itemsPerPage)
		console.log("searchValue",searchValue)
		console.log("columnOrdering",columnOrdering)
		console.log("reverseOrdering",reverseOrdering)

	}

	$scope.menuOpt = 
		[{
			label : 'action1',
			action : function(row,col,event) {
				console.log("row",row)
				console.log("col",col)
			}
		},
		{
			label : 'action2',
			action : function(item,event) {
				myfunction2 (event,item);
			}
		}];


	$scope.SpeedMenuOpt  = [
	                        {
	                        	label : 'action1',
	                        	icon:'fa fa-pencil' , 
	                        	backgroundColor:'red', 
	                        	color:'green',		
	                        	action : function(item,event) {
	                        		console.log("action1")
	                        	}
	                        },
	                        {
	                        	label : 'action2',
	                        	icon:'fa fa-trash' , 
	                        	backgroundColor:'green', 
	                        	color:'black',		
	                        	action : function(item,event) {
	                        		console.log("action2")
	                        	}
	                        }
	                        ,
	                        {
	                        	label : 'action3',
	                        	icon:'fa fa-trash' , 
	                        	backgroundColor:'green', 
	                        	color:'black',		
	                        	action : function(item,event) {
	                        		console.log("action2")
	                        	}
	                        }
	                        ,
	                        {
	                        	label : 'action4',
	                        	icon:'fa fa-trash' , 
	                        	backgroundColor:'green', 
	                        	color:'black',		
	                        	action : function(item,event) {
	                        		console.log("action2")
	                        	}
	                        }
	                        ];
};