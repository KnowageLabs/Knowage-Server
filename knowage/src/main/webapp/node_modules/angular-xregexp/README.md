angular-xregexp
==============

XRegExp validator for AngularJS.
This library decorates ngPattern to use [XRegExp](http://xregexp.com/) for more complex regular expression.


Installation
------------

You can choose your preferred method of installation:
* Through bower: `bower install angular-xregexp --save`
* Through npm: `npm install angular-xregexp xregexp --save`
* Download from github: [angular-xregexp.min.js](https://raw.github.com/cosimomeli/angular-xregexp/master/angular-xregexp.min.js)

Usage
-----
Include both **xregexp-all.js** and **angular-xregexp.js** in your application.

```html
<script src="components/xregexp/xegexp-all.js"></script>
<script src="components/angular-xregexp/angular-xregexp.js"></script>
```

Add the module `angularXRegExp` as a dependency to your app module:

```js
var myapp = angular.module('myapp', ['angularXRegExp']);
```
Then just use `ng-pattern` as always.

License
----

Released under the terms of the [MIT License](LICENSE).
