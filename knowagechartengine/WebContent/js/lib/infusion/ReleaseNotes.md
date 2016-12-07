# Release Notes for Fluid Infusion 2.0 #

[Main Project Site](http://fluidproject.org)

[Documentation](https://github.com/fluid-project/infusion-docs)

## What's New in 2.0.0? ##

See [API Changes from 1.5 to 2.0](http://docs.fluidproject.org/infusion/development/APIChangesFrom1_5To2_0.html) on the Infusion docs site, as well as
[Deprecations in 1.5](http://docs.fluidproject.org/infusion/development/DeprecationsIn1_5.html).

### New Features

* [FLUID-5249: Instantiation of all components in a single-rooted component tree](https://issues.fluidproject.org/browse/FLUID-5249)
* [FLUID-5495: IoCSS expressions matching upwards in the first component, and support for root context /](https://issues.fluidproject.org/browse/FLUID-5495)
* [FLUID-5241: New "ContextAwareness" grades and API](https://issues.fluidproject.org/browse/FLUID-5241)
    * [FLUID-5264: progressiveCheckerForComponent does not respond at instantiation](https://issues.fluidproject.org/browse/FLUID-5264) (implementation is abolished)
* [FLUID-5733: `fluid.notImplemented` function for implementing abstract grades](https://issues.fluidproject.org/browse/FLUID-5733)
* [FLUID-4884: New `FluidViewDebugging.js` implementation for DOM-directed browsing of IoC tree](https://issues.fluidproject.org/browse/FLUID-4884)
* [FLUID-5506: Constraint-based priority scheme to compute order of notifying listeners (and others)](https://issues.fluidproject.org/browse/FLUID-5506)

### Bug Fixes and Improvements

* [FLUID-5668: Corruption when material written for `members` requires merging](https://issues.fluidproject.org/browse/FLUID-5668)
* [FLUID-5714: The framework does not correctly merge invoker specifications when both "func" and "funcName" are used](https://issues.fluidproject.org/browse/FLUID-5714)
    * [FLUID-5184: Cannot override a this-ist invoker with a that-ist one](https://issues.fluidproject.org/browse/FLUID-5184)
* [FLUID-5621: Improve `distributeOptions` so that priority of distributions can be precisely controlled](https://issues.fluidproject.org/browse/FLUID-5621)
* [FLUID-5587: Framework should support namespaced options distributions rather than an array](https://issues.fluidproject.org/browse/FLUID-5587)
* [FLUID-5226: `fluid.isPlainObject()` is not powerful to detect objects which are only exotic by virtue of constructor](https://issues.fluidproject.org/browse/FLUID-5226)
* [FLUID-5717: Merge policies contributed via dynamic grades do not operate correctly](https://issues.fluidproject.org/browse/FLUID-5717)
* [FLUID-5615: Grade closure algorithm for dynamic grades is faulty](https://issues.fluidproject.org/browse/FLUID-5615) (partial fix)
* [FLUID-5742: Dynamic components do not respond to dynamic `typeName` or `gradeNames`](https://issues.fluidproject.org/browse/FLUID-5742)
* [FLUID-5694: Diagnostic when injecting component to its own location](https://issues.fluidproject.org/browse/FLUID-5694)
* [FLUID-5696: Failure to gingerly instantiate injection records](https://issues.fluidproject.org/browse/FLUID-5696)
* [FLUID-5664: Renderer fails to respect rebinding of model reference](https://issues.fluidproject.org/browse/FLUID-5664)
* [FLUID-5671: Memory exhaustion possible during `fluid.fail` on node.js (`fluid.prettyPrintJSON`)](https://issues.fluidproject.org/browse/FLUID-5671)
* [FLUID-5582: Improve flexibility of `fluid.fail` handling further](https://issues.fluidproject.org/browse/FLUID-5582)
* [FLUID-5667: Circularity in material sent for options expansion is not detected](https://issues.fluidproject.org/browse/FLUID-5667)
* [FLUID-5711: The Infusion core framework doesn't work when run in a Web Worker](https://issues.fluidproject.org/browse/FLUID-5711)
* [FLUID-5509: Storing NaN via a "new-style applier" will cause infinite recursion in DataBinding](https://issues.fluidproject.org/browse/FLUID-5509)
* [FLUID-5675: Text To Speech enactor should be supplied with a mock implementation to facilitate integration testing](https://issues.fluidproject.org/browse/FLUID-5675)

### Removals

* [FLUID-5616: "init functions" removed from the framework](https://issues.fluidproject.org/browse/FLUID-5616)
* Also removed: the static and dynamic environments, dynamic invokers, old ChangeApplier, autoInit, etc.

## What's New in 1.9 ##

**Note** 1.9 was never (so far) an official release of Infusion, but was a "last ditch" branching point to record the state of the framework should
we ever in future require to make a release or backport fixes to a version of Infusion API compatible with the latest 1.x line, most particularly 1.5.x.
It is stored in github at [Infusion 1.9.x](https://github.com/fluid-project/infusion/tree/1.9.x)

This list of JIRAs is nonexhaustive since work has accumulated in trunk for several years since the 1.5 Infusion release. These are some of the more significant recent fixes,
currently not including work on the preferences framework:

### New Features

* [FLUID-5513: Implement "micropromises" to ease manipulation of asynchronous code sequences](https://issues.fluidproject.org/browse/FLUID-5513)

### Bug Fixes and Improvements

* [FLUID-5662: fluid.fetchResources stalls if defaultLocale and locale are identical, and forceCache option is supplied](https://issues.fluidproject.org/browse/FLUID-5662)
* [FLUID-5559: onTestCaseStart event can fire multiple times in IoC Testing fixture](https://issues.fluidproject.org/browse/FLUID-5559)
* [FLUID-5575: Timing of onTestCaseStart event is incorrect](https://issues.fluidproject.org/browse/FLUID-5575)
* [FLUID-5268: Newly implemented "afterDestroy" event does not function](https://issues.fluidproject.org/browse/FLUID-5268)
* [FLUID-5475: Improve diagnostics when framework is used within node.js](https://issues.fluidproject.org/browse/FLUID-5475)
* [FLUID-5512: Allow valueMapper to handle the defaultOutputValue when the transformation is based upon compound input values](https://issues.fluidproject.org/browse/FLUID-5512)
* [FLUID-5673: Tooltips are not closed by closeAll and accumulate endlessly if some descendents are not part of "items" option](https://issues.fluidproject.org/browse/FLUID-5673)
* [FLUID-5659: Failure to notify multiple relay rules](https://issues.fluidproject.org/browse/FLUID-5659)
* [FLUID-5599: Expand the message bundle system to be able to locate the bundle for a requested language](https://issues.fluidproject.org/browse/FLUID-5599)

## Downloading Infusion ##

You can dowload the source code for Infusion from [GitHub](https://github.com/fluid-project/infusion).

You can create your own custom build of Infusion using the [grunt build script](README.md#how-do-i-create-an-infusion-package):

## Demos ##

Infusion ships with a demos for seeing all of the components in action. You can
find them in the _**demos**_ folder in the release bundle or on our [build site](http://build.fluidproject.org/).

When run from a local file system, several of these demos require you to enable local file AJAX
in Firefox and Chrome:

* https://wiki.fluidproject.org/display/fluid/Browser+settings+to+support+local+Ajax+calls
* http://kb.mozillazine.org/Security.fileuri.strict_origin_policy
* http://ejohn.org/blog/tightened-local-file-security/

## License ##

Fluid Infusion is licensed under both the ECL 2.0 and new BSD licenses.

More information is available in our [wiki](http://wiki.fluidproject.org/display/fluid/Fluid+Licensing).


## Third Party Software in Infusion ##

This is a list of publicly available software that is redistributed with Fluid Infusion,
categorized by license:

### MIT License ###
* [Foundation v6.2.3](http://foundation.zurb.com/index.html)
* [HTML5 Boilerplate v4.3](http://html5boilerplate.com/)
* [jQuery v3.1.0](http://jquery.com/)
* [jQuery UI (Core; Interactions: draggable, resizable; Widgets: button, checkboxradio, controlgroup, dialog, mouse, slider, tabs, and tooltip) v1.12.1](http://ui.jquery.com/)
* [jQuery QUnit v1.12.0](http://qunitjs.com)
* [jQuery QUnit Composite v1.0.1](https://github.com/jquery/qunit-composite)
* [jQuery Mockjax v2.2.1](https://github.com/jakerella/jquery-mockjax)
* [jQuery scrollTo v1.4.2](http://flesler.blogspot.com/2007/10/jqueryscrollto.html)
* [jQuery Touch Punch v0.2.2](http://touchpunch.furf.com/)
* [jquery.simulate](https://github.com/eduardolundgren/jquery-simulate)
* [jquery.selectbox v0.5 (forked)](https://github.com/fluid-project/jquery.selectbox)
* [Micro Clearfix](http://nicolasgallagher.com/micro-clearfix-hack/)
* [Normalize v4.1.1](https://necolas.github.io/normalize.css/)
* [Buzz v1.1.0](http://buzz.jaysalvat.com)
* [html5shiv v3.7.2](https://code.google.com/p/html5shiv/)

### zlib/libpng License ###
* [fastXmlPull is based on XML for Script's Fast Pull Parser v3.1](http://wiki.fluidproject.org/display/fluid/Licensing+for+fastXmlPull.js)

### ECL 2.0 ###
* Sample markup and stylesheets from [Sakai v2.5](http://sakaiproject.org)

### Apache 2.0 ###
* [Open Sans Light font](http://www.google.com/fonts/specimen/Open+Sans)

### Public Domain ###
* Douglas Crockford's [JSON.js (from 2007-11-06)](http://www.json.org/)

## Documentation ##

We are in the process of migrating our documentation to a new space. The markdown files for the documentation can be found in [github](https://github.com/fluid-project/infusion-docs).

The new space is dedicated to only Infusion documentation, and provides improved navigation.

Some of our documentation remains in the wiki space: Links to these pages are indicated with _**wiki**_. From any of these pages, you can return to the main documentation space using your
browser's Back button.

## Supported Browsers ##

Infusion 2.0 was tested with the following browsers:

* Chrome current (versions 43-44)
* Firefox current (versions 39-40)
* Internet Explorer versions 10 and 11
* Safari versions ???

For more information see the [Fluid Infusion browser support](http://wiki.fluidproject.org/display/docs/Browser+Support) wiki page.


## Status of Components and Framework Features ##

### Production ###

Supported by tested browsers, and stable for production usage across a wide range of applications and use cases

* Infusion Framework Core
* Inline Edit: Simple Text
* Renderer
* Reorderer: List, Grid, Layout, Image
* Undo

### Preview ###

Still growing, but with broad browser support. Expect new features in upcoming releases

* IoC
* Pager
* Preferences Framework
* Progress
* UI Options
* Uploader

### Sneak Peek ###
In development; APIs will change. Share your feedback, ideas, and code

* Inline Edit: Dropdown
* Inline Edit: Rich Text
* Table of Contents
* Model Relay
* Model Transformation
* Progressive Enhancement
* etc.

## Known Issues ##

The Fluid Project uses a [JIRA](http://issues.fluidproject.org) website to track bugs. Some of the known issues in this release are described here:

### Framework ###

* [FLUID-5519: Timing of "initial transaction" in new model relay system is problematic](https://issues.fluidproject.org/browse/FLUID-5519)

### Inline Edit ###

* [FLUID-1600: Pressing the "Tab" key to exit edit mode places focus on the wrong item](http://issues.fluidproject.org/browse/FLUID-1600)

### Layout Reorderer ###
* [FLUID-3864: Layout Reorderer failed to move portlets back to the first column in three-columns view with keyboard](http://issues.fluidproject.org/browse/FLUID-3864)
* [FLUID-3089: If columns become stacked, can't drag item into lower column](http://issues.fluidproject.org/browse/FLUID-3089)

### Renderer ###

* [FLUID-3493: Renderer appears to corrupt templates containing empty tags on Opera (maybe others)](http://issues.fluidproject.org/browse/FLUID-3493)
* [FLUID-4322: Renderer can corrupt tag nesting structure in some cases with branch containers](http://issues.fluidproject.org/browse/FLUID-4322)

### Reorderer ###

* [FLUID-3925: With no wrapping on, the keyboard movement keystrokes are captured by the browser where a wrap would have occurred.](http://issues.fluidproject.org/browse/FLUID-3925)

### UI Options / Preferences Framework ###

* [FLUID-4394: Fat Panel UI Options' iFrame HTML page doesn't play nice with a concatenated build of Infusion](http://issues.fluidproject.org/browse/FLUID-4394)
* [FLUID-4426: Sliding Panel needs ARIA and/or to move focus to beginning of panel when opened to alert screen readers of new content](http://issues.fluidproject.org/browse/FLUID-4426)
* [FLUID-4491: Line spacing doesn't affect elements that have a line-height style set](http://issues.fluidproject.org/browse/FLUID-4491)
* [FLUID-5066: UIO Integrators shouldn't have to edit Infusion's copy of html templates to add panels, css](http://issues.fluidproject.org/browse/FLUID-5066)
* [FLUID-5218: Prefs editor requires iFrame template to be in the same place as panel templates; it probably shouldn't](http://issues.fluidproject.org/browse/FLUID-5218)
* [FLUID-5223: If there's exactly one text field in the prefs editor, pressing enter on most inputs causes the form to submit](http://issues.fluidproject.org/browse/FLUID-5223)
* [FLUID-5255: ToC template path is not configurable by UIO integrators](http://issues.fluidproject.org/browse/FLUID-5255)

### Undo ###

* [FLUID-3697: Undo hard-codes selector classes instead of using user-configured values](http://issues.fluidproject.org/browse/FLUID-3697)
