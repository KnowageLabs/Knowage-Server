module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        watch: {
            options: {
                livereload: true
            },
            tasks: ['uglify'],
            files: ['pleaserotate.js', 'Gruntfile.js', 'index.html']
        },
        uglify: {
            options: {
                banner: "/*pleaserotate.js by Rob Scanlon, MIT license. http://github.com/arscan/pleaserotate.js*/\n\n"
            },
            main: {
                files: {
                    'pleaserotate.min.js': 'pleaserotate.js'
                }
            }
        }
        
    });

    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-uglify');


};
