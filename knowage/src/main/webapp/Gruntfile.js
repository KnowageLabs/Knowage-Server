module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    concat: {
        options: {
          banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ' + '<%= grunt.template.today("yyyy-mm-dd") %> */',
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
	        		 'js/lib/angular/color-picker/angularjs-color-picker.js'
	        	 ],
	        	 'dist/knowage-modules-bundle.js': [
	        		 'node_modules/moment/min/moment-with-locales.min.js',
	        		 'node_modules/toastify-js/src/toastify.js',
	        		 'node_modules/jsonformatter/dist/json-formatter.min.js',
	        		 'node_modules/xregexp/xregexp-all.js',
	        		 'node_modules/angular-xregexp/angular-xregexp.js',
	        		 'node_modules/angular-tree-control/angular-tree-control.js',
	        		 'node_modules/angular-tree-control/context-menu.js',
	        		 'node_modules/ng-wysiwyg/dist/wysiwyg.min.js'
	        	 ],
	        	 'dist/knowage-modules-styles-bundle.css' : [
	        		 'themes/commons/css/reset_2018.css',
	        		 'js/lib/angular/angular-material_1.1.0/angular-material.min.css',
	        		 'js/lib/angular/angular-tree/angular-ui-tree.min.css',
	        		 'node_modules/toastify-js/src/toastify.css',
	        		 'themes/sbi_default/css/angular-toastr.css',
	        		 'js/lib/angular/color-picker/angularjs-color-picker.min.css',
	        		 'js/lib/angular/color-picker/mdColorPickerPersonalStyle.css',
	        		 'node_modules/ng-wysiwyg/dist/editor.min.css',
	        		 'node_modules/angular-tree-control/css/tree-control.css'
	        	 ]
        	}
        }
    }
  });

  // Load the plugin that provides the "concat" task.
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.registerTask('knowage-concat', ['concat']);

};