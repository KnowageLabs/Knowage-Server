module.exports = function(grunt) {

  // Project configuration.
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
	        	'dist/angular-bundle.js': [
	        	  'js/lib/angular/angular_1.4/angular.min.js',
	        	  'js/lib/angular/angular_1.4/angular-animate.min.js',
	        	  'js/lib/angular/angular_1.4/angular-aria.min.js',
	        	  'js/lib/angular/angular_1.4/angular-sanitize.min.js',
	        	  'js/lib/angular/angular_1.4/angular-messages.min.js',
	        	  'js/lib/angular/angular_1.4/angular-cookies.js'
	        	  ],
	        	 'dist/polyfills-bundle.js': [
	        		 'polyfills/*/*polyfill.js',
	        		 'polyfills/canvas-toBlob/canvas-toBlob.js'
	              ],
	        	 'dist/knowage-lib-bundle.js' : [
	        		 'js/lib/angular/angular-material_1.1.0/angular-material.min.js',
	        		 'js/lib/angular/angular-tree/angular-ui-tree.js',
	        		 'js/lib/angular/contextmenu/ng-context-menu.js',
	        		 'js/lib/angular/pagination/dirPagination.js',
	        		 'js/lib/angular/expander-box/expanderBox.js',
	        		 'js/lib/angular/color-picker/tinycolor-min.js',
	        		 'js/lib/angular/color-picker/tinygradient.min.js',
	        		 'js/lib/angular/color-picker/angularjs-color-picker.js',
	        		 'js/lib/xml2js/xml2json.js',
	        		 'js/lib/angular/angular-base64/angular-base64.min.js'
	        	 ],
	        	 'dist/knowage-modules-bundle.js': [
	        		 'node_modules/moment/min/moment-with-locales.min.js',
	        		 'node_modules/toastify-js/src/toastify.js',
	        		 'node_modules/jsonformatter/dist/json-formatter.min.js',
	        		 'node_modules/xregexp/xregexp-all.js',
	        		 'node_modules/angular-xregexp/angular-xregexp.js',
	        		 'node_modules/angular-tree-control/angular-tree-control.js',
	        		 'node_modules/angular-tree-control/context-menu.js',
	        		 'node_modules/ng-wysiwyg/dist/wysiwyg.min.js',
	        		 'node_modules/ag-grid-community/dist/ag-grid-community.min.js'
	        	 ],
	        	 'dist/knowage-modules-styles-bundle.css' : [
	        		 'themes/commons/css/reset_2018.css',
	        		 'node_modules/@fortawesome/fontawesome-free/css/v4-shims.min.css',
	        		 'js/lib/angular/angular-material_1.1.0/angular-material.min.css',
	        		 'js/lib/angular/angular-tree/angular-ui-tree.min.css',
	        		 'node_modules/toastify-js/src/toastify.css',
	        		 'themes/sbi_default/css/angular-toastr.css',
	        		 'js/lib/angular/color-picker/angularjs-color-picker.min.css',
	        		 'js/lib/angular/color-picker/mdColorPickerPersonalStyle.css',
	        		 'node_modules/angular-tree-control/css/tree-control.css'
	        	 ],
	        	 'dist/knowage-sources-bundle_<%= pkg.version %>.js' : [
	        		 'js/src/angular_1.4/tools/commons/sbiModule_services/sbiModule_dateServices.js',
	        		 'js/src/angular_1.4/tools/commons/AngularList.js',
	        		 'js/src/angular_1.4/tools/commons/angular-table/AngularTable.js',
	        		 'js/src/angular_1.4/tools/commons/angular-table/utils/daff.js',
	        		 'js/src/angular_1.4/tools/commons/kn-table/knTable.js',
	        		 'js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js',
	        		 'js/src/angular_1.4/tools/commons/component-tree/componentTree.js',
	        		 'js/src/angular_1.4/tools/commons/upload-file/FileUpload.js',
	        		 'js/src/angular_1.4/tools/commons/upload-file/FileUploadBase64.js',
	        		 'js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js',
	        		 'js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js',
	        		 'js/src/angular_1.4/tools/commons/angular-list-detail/angular2Col.js',
	        		 'js/src/angular_1.4/tools/commons/angular-toastr.tpls.js',
	        		 'js/src/angular_1.4/tools/workspace/scripts/services/qbeViewer.js',
	        		 'js/src/angular_1.4/tools/workspace/scripts/services/qbeViewerCommunicationService.js',
	        		 'js/src/angular_1.4/tools/workspace/scripts/directive/dataset-save/datasetSaveModule.js',
	        		 'js/src/angular_1.4/tools/workspace/scripts/directive/dataset-save/datasetSave.js',
	        		 'js/src/angular_1.4/tools/workspace/scripts/services/datasetSave_service.js',
	        		 'js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetSchedulerModule.js',
	        		 'js/src/angular_1.4/tools/scheduler/dataset-scheduler/datasetScheduler_service.js',
	        		 'js/src/angular_1.4/tools/scheduler/dataset-scheduler/schedulerTimeUnit.js',
	        		 'js/src/angular_1.4/tools/commons/services/knModule_aggridLabels.js'
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
            'dist/angular-bundle.min.js': ['dist/angular-bundle.js'],
            'dist/polyfills-bundle.min.js': ['dist/polyfills-bundle.js'],
            'dist/knowage-lib-bundle.min.js': ['dist/knowage-lib-bundle.js'],
            'dist/knowage-modules-bundle.min.js': ['dist/knowage-modules-bundle.js'],
            'dist/knowage-sources-bundle_<%= pkg.version %>.min.js': ['dist/knowage-sources-bundle_<%= pkg.version %>.js']
          }
        }
      },
      cssmin: {
    	  target: {
			  files: {
				  'dist/knowage-modules-styles-bundle.min.css': ['dist/knowage-modules-styles-bundle.css']
		    }
    	  }
    	}
  });

  // Load the plugin that provides the "concat" task.
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.registerTask('knowage-concat', ['concat','cssmin']);

};