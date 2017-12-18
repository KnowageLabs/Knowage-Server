ngWYSIWYG
=========

Folks, for your judgement and, hopefully, contributions, here is the true angular WYSIWYG.
I took images and layout from the <a href="https://github.com/jessegreathouse/TinyEditor">tinyeditor</a>, so kudos to Michael Leigeber.

Here is the <a href="http://psergus.github.io/ngWYSIWYG/">Demo</a>

### Why iFrame?

A real rich text editor must reflect the true stage of the editing content. Any CSS and/or Javascript on the host page must not overide the specifics of the content.
Moreover, iframe allows to isolate your security issues (any possible Javascript code in the content may polute your window's scope).


Installation
=========================

## Requirements

1. `AngularJS` ≥ `1.2.x`
2. `Angular Sanitize` ≥ `1.2.x`

### Bower

````Shell
$ bower install ngWYSIWYG --save
```

Include the ngWYSIWYG files in your index.html:
````HTML
<link rel="stylesheet" href="bower_components/ngWYSIWYG/dist/editor.min.css" />
<script src="bower_components/ngWYSIWYG/dist/wysiwyg.min.js"></script>
```

Add it as module to your app.js:

````JavaScript
['ngWYSIWYG']
````

Use it wherever you want:

```HTML
<wysiwyg-edit content="your_variable"></wysiwyg-edit>
```

## Configuration

You can configure the editor for two options (will extend l8r). First option is if you want to sanitize input from the user and prevent XSS attacks. This option uses angular's 
$sanitize. The second option will allow to configure toolbar buttons. You will be able to configure which buttons you want to show. Please see example.

````JavaScript
angular.module('myApp', ['ngWYSIWYG']).
controller('demoController', ['$scope', '$q', '$timeout', function($scope, $q, $timeout) {
	$scope.your_variable = 'some HTML text here';
	$scope.api = {
		scope: $scope,
		$scope.editorConfig = {
		    sanitize: false,
		    toolbar: [
			{ name: 'basicStyling', items: ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript', '-', 'leftAlign', 'centerAlign', 'rightAlign', 'blockJustify', '-'] },
			{ name: 'paragraph', items: ['orderedList', 'unorderedList', 'outdent', 'indent', '-'] },
			{ name: 'doers', items: ['removeFormatting', 'undo', 'redo', '-'] },
			{ name: 'colors', items: ['fontColor', 'backgroundColor', '-'] },
			{ name: 'links', items: ['image', 'hr', 'symbols', 'link', 'unlink', '-'] },
			{ name: 'tools', items: ['print', '-'] },
			{ name: 'styling', items: ['font', 'size', 'format'] },
		    ]
		};
	};
}]);
````

```HTML
<wysiwyg-edit content="your_variable" config="editorConfig"></wysiwyg-edit>
```

## Custom content style

This option enables you to specify a custom CSS file to be used within the editor (the editable area).

````HTML
<wysiwyg-edit content="your_variable" config="editorConfig" content-style="some_style.css"></wysiwyg-edit>
```

If you specify a relative path, it is resolved in relation to the URL of the (HTML) file that includes ngWYSIWYG,
NOT relative to ngWYSIWYG itself. In the example above, if the HTML file is hosted at http://www.example.com/wysiwyg.html, 
then the css URL will be resolved to: http://www.example.com/some_style.css.

### Use case

This configuration is useful when you want your editor's content area to show the content exactly like its going to be
show in the destination, without adding inline css to it. For example, let's say that the destination has a black background color
with a white font-color. In this case your some_style.css file would have the following properties:

```CSS
html, body {
    background-color: black;
    color: #ffffff;
}
```

## API

There is an idea on the api functions to delegate some responsibilities to the customer's scope.
The first thing which is implemented is insert image delegation. By default the directive uses a simple prompt function to accept image's url. However,
there is a way to bring up a custom dialog box on the customer's side and return promise.

````JavaScript
angular.module('myApp', ['ngWYSIWYG']).
controller('demoController', ['$scope', '$q', '$timeout', function($scope, $q, $timeout) {
	$scope.your_variable = 'some HTML text here';
	$scope.api = {
		scope: $scope,
		$scope.editorConfig = {
		    sanitize: false,
		    toolbar: [
			{ name: 'basicStyling', items: ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript', '-', 'leftAlign', 'centerAlign', 'rightAlign', 'blockJustify', '-'] },
			{ name: 'paragraph', items: ['orderedList', 'unorderedList', 'outdent', 'indent', '-'] },
			{ name: 'doers', items: ['removeFormatting', 'undo', 'redo', '-'] },
			{ name: 'colors', items: ['fontColor', 'backgroundColor', '-'] },
			{ name: 'links', items: ['image', 'hr', 'symbols', 'link', 'unlink', '-'] },
			{ name: 'tools', items: ['print', '-'] },
			{ name: 'styling', items: ['font', 'size', 'format'] },
		    ]
		};
		insertImage: function() {
			var deferred = $q.defer();
			$timeout(function() {
				var val = prompt('Enter image url', 'http://');
				if(val) {
					deferred.resolve('<img src="' + val + '" style="width: 30%;">');
				}
				else {
				    deferred.reject(null);
				}
			}, 1000);
			return deferred.promise;
		}
	};
}]);
````
Make sure you feed the api object to the directive like this:

```HTML
<wysiwyg-edit content="your_variable" api="api"></wysiwyg-edit>
```

### Simple download (aka git clone/fork)

1. Include dist/wysiwyg.min.js in your project using script tag.
1. Include dist/editor.min.js in your project using link tag.
2. Add dependency to `ngWYSIWYG` to your app module. Example: ```angular.module('myApp', ['ngWYSIWYG'])```.
3. Add element ```<wysiwyg-edit content="your_variable"></wysiwyg-edit>```.

Maintenance
=========================

### Roadmap

- Current cursor/caret position style reflection on the toolbar
- Material Design
- Implement tests
- Look for the Angular 2.0

### Issues?

If you find any, please let me know by sumbitting an issue request. I will be working on it actively.

## Contributers

Contributions are welcome and special thanks to all the contributions!

## License

[MIT license](http://opensource.org/licenses/MIT)
