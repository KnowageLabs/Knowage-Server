
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	
	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	
	<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<%@include file="/WEB-INF/jsp/commons/angular/svgViewerImport.jsp"%>
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/svgviewer/svgViewerController.js"></script>
	
	<!-- TODO: IMPORTANT - TO REMOVE -->
	<script src="https://code.highcharts.com/mapdata/countries/us/us-all.js"></script>
	
	<!-- TODO: to remove, fake example -->
	<style>
#container {
    height: 100%;
    width: 100%;
    margin: 0 auto;
}

.loading {
    margin-top: 10em;
    text-align: center;
    color: gray;
}
</style>
<script type="text/javascript">
var myData = [ 
{
    "name": "Muri"
},
{
    "name": "Condizionatori"
},
{
    "name": "Interruttori"
},
{
    "name": "Rack",
    "value": 333
}, 
{
    "name": "Rack 1",
    "value": 500
},
{
    "name": "Rack 1 copia",
    "value": 1000
},
{
    "name": "Rack 1 copia copia",
    "value": 200
},
{
    "name": "Rack 1 copia copia copia",
    "value": 700
}];

var mapPont = [{
    "color": "gray",
    "name": "Muri",
    "path": "M13,-791,13,-766,53,-766,53,-17,28,-17,28,8,892,8,892,-17,891,-17,891,-600,865,-600,865,-571,847,-590,824,-567,865,-523,865,-17,75,-17,75,-766,866,-766,866,-718,898,-719,899,-791,877,-790,877,-791,13,-791zM868,-602,950,-602C950,-602,952,-619,935,-639,923,-654,893,-656,893,-656,893,-656,920,-657,937,-677C954,-697,949,-716,949,-716L869,-716,873,-715"
}, {
    "color":"green",
    "name": "Condizionatori",
    "path": "M75,-375L130,-375,130,-196,75,-196,75,-375M75,-638L130,-638,130,-459,75,-459,75,-638M137,-708L137,-764,316,-764,316,-708,137,-708M623,-708L623,-764,802,-764,802,-708,623,-708",
}, {

    "drilldown":"us-ca",
    "name": "Rack",
    "path": "M401,-249,409,-249,409,-238,413,-238,405,-225,397,-238,401,-238zM451,-249,459,-249,459,-238,463,-238,455,-225,447,-238,451,-238zM451,-249,459,-249,459,-238,462,-238,455,-225,447,-238,451,-238zM500,-249,508,-249,508,-238,512,-238,504,-225,496,-238,500,-238zM550,-249,558,-249,558,-238,562,-238,554,-225,546,-238,550,-238zM600,-249,608,-249,608,-238,612,-238,604,-225,596,-238,600,-238zM650,-249,658,-249,658,-238,661,-238,654,-225,646,-238,650,-238zM657,-194,665,-194,665,-184,668,-184,660,-170,653,-184,657,-184zM607,-194,615,-194,615,-184,618,-184,610,-170,603,-184,607,-184zM607,-194,615,-194,615,-184,619,-184,611,-170,603,-184,607,-184zM557,-194,565,-194,565,-184,569,-184,561,-170,553,-184,557,-184zM508,-194,515,-194,515,-184,519,-184,511,-170,504,-184,508,-184zM458,-194,466,-194,466,-184,469,-184,462,-170,454,-184,458,-184zM408,-194,416,-194,416,-184,420,-184,412,-170,404,-184,408,-184zM299,-249,307,-249,307,-238,311,-238,303,-225,296,-238,299,-238zM350,-249,357,-249,357,-238,361,-238,353,-225,346,-238,350,-238zM349,-249,357,-249,357,-238,361,-238,353,-225,345,-238,349,-238zM356,-194,364,-194,364,-184,368,-184,360,-170,352,-184,356,-184zM306,-194,314,-194,314,-184,318,-184,310,-170,302,-184,306,-184zM365,-638L417,-638,417,-560,365,-560,365,-638M380,-330L432,-330,432,-252,380,-252,380,-330M380,-330L429,-330,429,-252,380,-252,380,-330M430,-330L479,-330,479,-252,430,-252,430,-330M430,-330L479,-330,479,-252,430,-252,430,-330M479,-330L529,-330,529,-252,479,-252,479,-330M529,-330L578,-330,578,-252,529,-252,529,-330M579,-330L628,-330,628,-252,579,-252,579,-330M629,-330L678,-330,678,-252,629,-252,629,-330M678,-113L626,-113,626,-191,678,-191,678,-113M678,-113L629,-113,629,-191,678,-191,678,-113M628,-113L579,-113,579,-191,628,-191,628,-113M628,-113L579,-113,579,-191,628,-191,628,-113M578,-113L529,-113,529,-191,578,-191,578,-113M529,-113L479,-113,479,-191,529,-191,529,-113M479,-113L430,-113,430,-191,479,-191,479,-113M429,-113L380,-113,380,-191,429,-191,429,-113M278,-330L331,-330,331,-252,278,-252,278,-330M278,-330L328,-330,328,-252,278,-252,278,-330M329,-330L378,-330,378,-252,329,-252,329,-330M328,-330L377,-330,377,-252,328,-252,328,-330M377,-113L328,-113,328,-191,377,-191,377,-113M327,-113L278,-113,278,-191,327,-191,327,-113"
}, {
    "name": "Rack 1",

    "path": "M386,-556,394,-556,394,-546,398,-546,390,-533,382,-546,386,-546zM365,-638L414,-638,414,-560,365,-560,365,-638"
}, {
    "name": "Rack 1 copia",
    "path": "M437,-556,445,-556,445,-546,449,-546,441,-533,433,-546,437,-546zM416,-638L465,-638,465,-560,416,-560,416,-638"
}, {
    "name": "Rack 1 copia copia",
    "path": "M485,-556,493,-556,493,-546,496,-546,489,-533,481,-546,485,-546zM464,-638L513,-638,513,-560,464,-560,464,-638"
}, {
    "name": "Rack 1 copia copia copia",
    "path": "M533,-556,540,-556,540,-546,544,-546,536,-533,529,-546,533,-546zM512,-638L561,-638,561,-560,512,-560,512,-638"
}, {
    "color": "red", 
    "name": "Interruttori",
    "path": "M378,-765L454,-765,454,-732,378,-732,378,-765M829,-302L866,-302,866,-220,829,-220,829,-302"
}, {
    "name": "rect5872",
    "path": "M0,-992L45,-992,45,-947,0,-947,0,-992"
}];

$(function() {
    // Initiate the chart
    $('#container').highcharts('Map', {

        chart : {
            events: {
                drilldown: function (e) {

                    if (!e.seriesOptions) {
                        var chart = this,
                            mapKey = 'countries/us/' + e.point.drilldown + '-all',
                            // Handle error, the timeout is cleared on success
                            fail = setTimeout(function () {
                                if (!Highcharts.maps[mapKey]) {
                                    chart.showLoading('<i class="icon-frown"></i> Failed loading ' + e.point.name);

                                    fail = setTimeout(function () {
                                        chart.hideLoading();
                                    }, 1000);
                                }
                            }, 3000);

                        // Show the spinner
                        chart.showLoading('<i class="icon-spinner icon-spin icon-3x"></i>'); // Font Awesome spinner

                        // Load the drilldown map
                        $.getScript('https://code.highcharts.com/mapdata/' + mapKey + '.js', function () {

                            data = Highcharts.geojson(Highcharts.maps[mapKey]);

                            // Set a non-random bogus value
                            $.each(data, function (i) {
                                this.value = i;
                            });

                            // Hide loading and add series
                            chart.hideLoading();
                            clearTimeout(fail);
                            chart.addSeriesAsDrilldown(e.point, {
                                name: e.point.name,
                                data: data,
                                dataLabels: {
                                    enabled: true,
                                    format: '{point.name}'
                                }
                            });
                        });
                    }


                    this.setTitle(null, { text: e.point.name });
                },
                drillup: function () {
                    this.setTitle(null, { text: 'USA' });
                }
            }
        },

        colorAxis: {
            min: 0,
            minColor: '#E6E7E8',
            maxColor: '#005645'
        },
        title: {
            text: 'Pont Bunker'
        },
        plotOptions: {
            series: {
                /* events: {
                                   click: function (e) {
                                       alert("Hai scelto: "+e.point.name);
                                   }
                               },*/
                tooltip: {
                    headerFormat: '',
                    pointFormat: '{point.name}'
                },
                /* dataLabels: {
                   enabled: true,
                   color: "black",
                   format: '{point.name}',
                   allowOverlap: true
                 }*/
            }
        },
        mapNavigation: {
            enabled: true
        },

        series: [

            {

                //"color": "red",
                "name": "Pont",
                "states": {
                    hover: {
                        color: 'blue'
                    }
                },
                "type": "map",
                data: myData,
                joinBy: ['name', 'name'],
                "mapData": mapPont
            },

        ]
    });
});
</script>
	
	<!-- END FAKE EXAMPLE -->
	
	<title>SVG Viewer</title>
	
	</head>
	
	<body>

        Testing the SVG Viewer Engine!
			<div ng-app="myapp">
			    <div layout="column" ng-controller="MyController">
			        <md-sidenav md-component-id="left" md-is-open="isSidenavOpen" class="md-sidenav-left">
			            Left Nav!
			        </md-sidenav>
			         <md-content>
			            <md-button ng-click="openLeftMenu()">
			              Open Side Nav
			            </md-button>
			            <div id="container">
						</div>
			          </md-content>
			    </div>
			</div>        
        	
	
	</body>

</html>
	
	
	
	
	
    