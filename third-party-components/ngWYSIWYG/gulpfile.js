var gulp = require('gulp');
var git = require('gulp-git');
var filter = require('gulp-filter');
var tag_version = require('gulp-tag-version');
var gulpif = require('gulp-if');
var sass = require('gulp-sass');
var minifyCss = require('gulp-minify-css');
var clean = require('gulp-clean');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var webserver = require('gulp-webserver');
var bump = require('gulp-bump');
var rename = require('gulp-rename');
var protractor = require("gulp-protractor").protractor;
var _ = require('underscore');
var umd = require('gulp-umd');

var development = false;
var webserverInstance;

gulp.task('set-development-mode', function() {
	development = true;
});

gulp.task('watch', function() {
	gulp.watch(['./src/**/*'], ['minify', 'uglify', 'copy-images']);
});

gulp.task('webserver', function() {
	webserverInstance = gulp.src('./dev').pipe(webserver({ host: '0.0.0.0', port: 8000 }));
});

gulp.task('develop', ['set-development-mode', 'minify', 'uglify', 'copy-images', 'watch', 'webserver']);

function getDestination() {
	if (development) {
		return './dev';
	}
	return './dist';
}

function renameMin(path) {
	path.basename += ".min";
	return path;
}

gulp.task('sass', function() {
	return gulp.src('./src/css/**/*.sass')
		.pipe(sass().on('error', sass.logError))
		.pipe(gulp.dest(getDestination()));
});

gulp.task('minify', ['sass'], function() {
	return gulp.src(getDestination() + '/editor.css')
		.pipe(gulpif(development === false, rename(renameMin)))
		.pipe(minifyCss({compatibility: 'ie8'}))
		.pipe(gulp.dest(getDestination()));
});

gulp.task('clean-css', ['minify'], function () {
	return gulp.src(getDestination() + '/editor.css', {read: false})
		.pipe(clean());
});

gulp.task('concat-js', function() {
	return gulp.src([
			'./src/js/wysiwyg.js',
			'./src/js/ngpColorsGrid.js',
			'./src/js/ngpSymbolsGrid.js',
			'./src/js/ngpImageResizer.js',
			'./src/js/wysiwygEdit.js',
			'./src/js/ngpContentFrame.js',
			'./src/js/ngpResizable.js',
			'./src/js/ngpUtils.js'
		])
		.pipe(concat('wysiwyg.js'))
		.pipe(umd({
	        dependencies: function (file) {
	            return [{
	                name: 'angular',
	                amd: 'angular',
	                cjs: 'angular',
	                global: 'angular',
	                param: 'angular'
	            }];
	        },
	        exports: function (file) {
	            return "'ngWYSIWYG'";
	        },
	        //template: umdTemplates.returnExportsNoNamespace.path,
	        templateSource: '(function(root, factory) {\r\n' +
	                            'if (typeof exports === "object") {\r\n' +
	                                'module.exports = factory(<%= cjs %>);\r\n' +
	                            '} else if (typeof define === "function" && define.amd) {\r\n' +
	                                'define(<%= amd %>, factory);\r\n' +
	                            '} else{\r\n' +
	                                'factory(<%= global %>);\r\n' +
	                            '}\r\n' +
	                        '}(this, function(<%= param %>) {\r\n' +
	                            '<%= contents %>\r\n' +
	                            'return <%= exports %>;\r\n' +
	                        '}));'
	    }))
		.pipe(gulp.dest('./dev'));
});

gulp.task('uglify', ['concat-js'], function() {
	return gulp.src('./dev/wysiwyg.js')
		.pipe(gulpif(development === false, uglify({ mangle: true })))
		.pipe(gulpif(development === false, rename(renameMin)))
		.pipe(gulp.dest(getDestination()));
});

gulp.task('copy-images', function() {
	return gulp.src('./src/images/**/*')
		.pipe(gulp.dest(getDestination() + '/images/'));
});

gulp.task('run-tests', ['webserver'], function() {
	return gulp.src(['./src/tests/*.js', '!./src/tests/conf.js'])
		.pipe(protractor({
			configFile: './src/tests/conf.js',
			args: ['--baseUrl', 'http://127.0.0.1:8000']
		}))
		.on('error', function(e) { throw e; });
});

gulp.task('tests', ['run-tests'], function() {
	webserverInstance.emit('kill');
});

gulp.task('build', ['clean-css', 'uglify', 'copy-images']);

function inc(importance) {
	// get all the files to bump version in
	return gulp.src(['./package.json', './bower.json'])
		// bump the version number in those files
		.pipe(bump({type: importance}))
		// save it back to filesystem
		.pipe(gulp.dest('./'))
		// commit the changed version number
		.pipe(git.commit('bumps package version'))

		// read only one file to get the version number
		.pipe(filter('package.json'))
		// **tag it in the repository**
		.pipe(tag_version());
}

gulp.task('patch', function() { return inc('patch'); });
gulp.task('feature', function() { return inc('minor'); });
gulp.task('release', function() { return inc('major'); });

gulp.task('push', function() {
	var packageJson = require('./package.json');
	git.push('origin', 'v' + packageJson.version, function (err) {
		if (err) throw err;
	});
});