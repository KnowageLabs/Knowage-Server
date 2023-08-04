module.exports = function(grunt){
	
	grunt.initConfig({
		pkg:grunt.file.readJSON('./package.json'),
		
		clean:{
			test:['./e2e/protractor/testresults','./node_modules/pageObjects']
	
		},
		copy:{
			pageObjects:{
				files:[{expand:true,cwd:'./e2e/protractor/',src:['pageObjects/**'],dest:'./node_modules/'}]
			}
		},
		protractor: {
		    options: {
		    	nodeBin:'./node/node',
		      configFile: "./e2e/protractor/conf.js", // Default config file 
		      keepAlive: false, // If false, the grunt process stops when the test fails. 
		      noColor: false, // If true, protractor will not use colors in its output. 
		      webdriverManagerUpdate:true,
		      args: {
		        // Arguments passed to the command 
		      }
		    },
		    basic: {   // Grunt requires at least one target to run so you can simply put 'all: {}' here too. 
		      options: {
		        configFile: "./e2e/protractor/conf.js", // Target-specific config file 
		        args: {} // Target-specific arguments
		      }
		    },
		    local: { 
			      options: {
			        configFile: "e2e/protractor/conf.local.js", // Target-specific config file 
			        args: {} // Target-specific arguments
			      }
			    },
		  },
		  
		  
		 
	})
	grunt.loadNpmTasks('grunt-protractor-runner');
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.registerTask('test',['clean:test','copy:pageObjects','protractor:basic']);
	grunt.registerTask('testlocal',['clean:test','copy:pageObjects','protractor:local']);
}