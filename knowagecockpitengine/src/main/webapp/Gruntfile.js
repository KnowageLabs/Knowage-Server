module.exports = function(grunt) {
	var knowageContext = '../../../../knowage/src/main/webapp/';
  
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    concat: {
        options: {
          banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ' + '<%= grunt.template.today("yyyy-mm-dd") %> */',
          stripBanners: true,
          nonull: true,
        },
        basic_and_extras: {
        	files: {
	        	 'dist/knowagecockpit-lib-bundle.js' : [
	        		 knowageContext + 'js/lib/angular/angular-gridster/angular-gridster.min.js',
	        		 knowageContext + 'js/lib/angular/angular-drag-and-drop-lists/angular-drag-and-drop-lists.js',
	        		 knowageContext + 'js/lib/angular/angular-json-tree/json-tree.js',
	        		 knowageContext + 'js/lib/openlayers/6.1.1/ol.js',
	        		 knowageContext + 'js/lib/mathjs/4.0.1/math.min.js'
	        	 ],
	        	 'dist/knowagecockpit-modules-bundle.js': [
					knowageContext + 'node_modules/ng-wysiwyg/dist/wysiwyg.min.js',	
					knowageContext + 'node_modules/jsonformatter/dist/json-formatter.min.js',	
					knowageContext + 'node_modules/codemirror/lib/codemirror.js',
					knowageContext + 'node_modules/codemirror/addon/mode/simple.js',
					knowageContext + 'node_modules/codemirror/mode/xml/xml.js',
					knowageContext + 'node_modules/codemirror/mode/css/css.js',
					knowageContext + 'node_modules/codemirror/mode/r/r.js',
					knowageContext + 'node_modules/codemirror/mode/javascript/javascript.js',
					knowageContext + 'node_modules/angular-ui-codemirror/src/ui-codemirror.js',
					knowageContext + 'node_modules/ag-grid-community/dist/ag-grid-community.min.js',
					knowageContext + 'node_modules/toastify-js/src/toastify.js',		
					knowageContext + 'node_modules/moment/min/moment-with-locales.min.js',
					knowageContext + 'node_modules/canvg/dist/browser/canvg.min.js',
					knowageContext + 'node_modules/html2canvas/dist/html2canvas.min.js',
					knowageContext + 'node_modules/jspdf/dist/jspdf.min.js',
					knowageContext + 'node_modules/file-saver/dist/FileSaver.min.js'
	        	 ],
	        	 'dist/knowagecockpit-modules-styles-bundle.css' : [
					knowageContext +  'node_modules/@fortawesome/fontawesome-free/css/v4-shims.min.css',
					knowageContext +  'js/lib/angular/angular-material_1.1.0/angular-material.min.css',
					knowageContext +  'js/lib/angular/angular-tree/angular-ui-tree.min.css',
					knowageContext + 'js/lib/angular/angular-gridster/angular-gridster.min.css',
					knowageContext +  'node_modules/toastify-js/src/toastify.css',
					knowageContext +  'js/lib/angular/color-picker/angularjs-color-picker.min.css',
					knowageContext +  'js/lib/angular/color-picker/mdColorPickerPersonalStyle.css',
					knowageContext +  'node_modules/angular-tree-control/css/tree-control.css',
					knowageContext +  'node_modules/codemirror/lib/codemirror.css',
					knowageContext +  'node_modules/codemirror/theme/eclipse.css',
					knowageContext + 'node_modules/jsonformatter/dist/json-formatter.min.css',
					knowageContext +  'js/lib/angular/angular-json-tree/json-tree.css',
					knowageContext +  'js/lib/openlayers/6.1.1/ol.css'
	        	 ],
	        	 'dist/knowagecockpit-sources-bundle_<%= pkg.version %>.js' : [
					'js/src/angular_1.4/tools/commons/angular-table/AngularTable.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/angular-table/utils/daff.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/component-tree/componentTree.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/upload-file/FileUpload.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js',
					'js/src/angular_1.4/cockpit/directives/commons/calculated-field/calculatedFieldMode.js'
	        	 ],
	        	 'dist/knowagecockpit-sources-cockpit-bundle_<%= pkg.version %>.js' : [
					'js/src/angular_1.4/cockpit/cockpit.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-sheet/cockpitSheet.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-grid/cockpitGrid.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-widget/cockpitWidget.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-toolbar/cockpitToolbar.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-style-configurator/cockpitStyleConfigurator.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-cross-configurator/cockpitCrossConfigurator.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-columns-configurator/cockpitColumnsConfigurator.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-column-variables/cockpitColumnVariables.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-selector-configurator/cockpitSelectorConfigurator.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-filters-configuration/cockpitFiltersConfiguration.js',
					'js/src/angular_1.4/cockpit/directives/cockpit-text-configuration/cockpitTextConfiguration.js',
					'js/src/angular_1.4/cockpit/services/*.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/services/knModule.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/services/knModule_fontIconsService.js',
					knowageContext + 'js/src/angular_1.4/tools/commons/services/knModule_aggridLabels.js',
					knowageContext + 'js/src/angular_1.4/tools/driversexecution/driversExecutionModule.js',
					knowageContext + 'js/src/angular_1.4/tools/driversexecution/driversExecutionService.js',
					knowageContext + 'js/src/angular_1.4/tools/driversexecution/driversDependencyService.js',
					knowageContext + 'js/src/angular_1.4/tools/driversexecution/*.js',
					knowageContext + 'js/src/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementController.js',
					knowageContext + 'js/src/angular_1.4/tools/businessmodelopening/*.js',
					knowageContext + 'js/src/angular_1.4/tools/driversexecution/renderparameters/renderParameters.js',
					'js/src/angular_1.4/cockpit/factory/*.js'
	        	 ],
	        	 'dist/knowagecockpit-sources-widgets-bundle_<%= pkg.version %>.js' : [
	        		 'js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/*Widget/*.js'
	        	 ],
	        	 'dist/knowagecockpit-sources-chart-bundle_<%= pkg.version %>.js': [
        			'js/src/angular_1.4/chart/designer/directives/custom_directives/customDirectives.js',
        			'js/src/angular_1.4/chart/designer/directives/custom_directives/chart-tab/chart-tab.js',
        			'js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/configuration-tab.js',
        			'js/src/angular_1.4/chart/designer/directives/custom_directives/advanced-tab/advanced-tab.js',
        			'js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/chartstructure-tab.js',
	        		'js/src/angular_1.4/chart/commons/Settings.js',
	        		'js/src/angular_1.4/chart/designer/directives/common_directives/commonDirectives.js',
	        		'js/src/angular_1.4/chart/designer/directives/third_party/thirdPartyDirectives.js',
	        		'js/src/angular_1.4/chart/designer/directives/chartDirectives.js',
	        		'js/src/angular_1.4/chart/designer/services/chartDesignerServices.js',
	        		'js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibilityModule.js',
	        		'js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibilityModule.js',
        			'js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibility/chartBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/gaugeBackwardCompatibility/gaugeBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/sunburstBackwardCompatibility/sunburstBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/scatterBackwardCompatibility/scatterBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/parallelBackwardCompatibility/parallelBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/treemapBackwardCompatibility/treemapBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/heatmapBackwardCompatibility/heatmapBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/barBackwardCompatibility/barBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/lineBackwardCompatibility/lineBackwardCompatibility.js',
        			'js/src/angular_1.4/chart/designer/services/backward/radarBackwardCompatibility/radarBackwardCompatibility.js',
	        		'js/src/angular_1.4/chart/designer/services/channelMessaging.js',
	        		'js/src/angular_1.4/chart/designer/chartDesigner.js',
	        		'js/src/angular_1.4/chart/treemap/treemap.js',
	        		'js/src/angular_1.4/chart/heatmap/heatmap.js'
	        	 ],
	        	 'dist/knowagecockpit-sources-chartrender-bundle_<%= pkg.version %>.js': [
	        		'js/src/angular_1.4/chart/chartInitializer/chartInitializerModule.js',
	        		'js/src/angular_1.4/chart/designer/services/chartDesignerServices.js',
	        		'js/src/angular_1.4/chart/chartInitializer/services/*.js',
	        		'js/src/angular_1.4/chart/chartRenderer/chartRendererModule.js',
	        		'js/src/angular_1.4/chart/chartRenderer/directives/chartRenderer.js',
	        		'js/src/angular_1.4/chart/JsonChartTemplateService/JsonChartTemplateServiceModule.js',
	        		'js/src/angular_1.4/chart/JsonChartTemplateService/services/JsonChartTemplateService.js',
	        		'js/src/angular_1.4/chart/chartRenderer/services/chartConfMergeService.js',
	        		'js/src/angular_1.4/chart/chartRenderer/services/chartSonifyService.js',
	        		'js/lib/d3/d3.js',
	        		'js/lib/chartJs/Chart.js',
	        		'js/src/angular_1.4/chart/d3/*.js',
	        		'js/src/angular_1.4/chart/commons/Settings.js'	        		
	        	 ]
        	}
        }
    },
    uglify: {
        options: {
          mangle: false
        },
        target: {
          files: {
            'dist/knowagecockpit-lib-bundle.min.js': ['dist/knowagecockpit-lib-bundle.js'],
            'dist/knowagecockpit-modules-bundle.min.js': ['dist/knowagecockpit-modules-bundle.js'],
            'dist/knowagecockpit-sources-bundle_<%= pkg.version %>.min.js': ['dist/knowagecockpit-sources-bundle_<%= pkg.version %>.js'],
            'dist/knowagecockpit-sources-cockpit-bundle_<%= pkg.version %>.min.js' : ['dist/knowagecockpit-sources-cockpit-bundle_<%= pkg.version %>.js'],
            'dist/knowagecockpit-sources-widgets-bundle_<%= pkg.version %>.min.js' : ['dist/knowagecockpit-sources-widgets-bundle_<%= pkg.version %>.js'],
            'dist/knowagecockpit-sources-chart-bundle_<%= pkg.version %>.min.js' : ['dist/knowagecockpit-sources-chart-bundle_<%= pkg.version %>.js'],
            'dist/knowagecockpit-sources-chartrender-bundle_<%= pkg.version %>.min.js' : ['dist/knowagecockpit-sources-chartrender-bundle_<%= pkg.version %>.js'],
          }
        }
      },
      cssmin: {
    	  target: {
			  files: {
				  'dist/knowagecockpit-modules-styles-bundle.min.css': ['dist/knowagecockpit-modules-styles-bundle.css']
		    }
    	  }
    	}
  });

  // Load the plugin that provides the "concat" task.
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.registerTask('knowagecockpit-concat', ['concat']);

};