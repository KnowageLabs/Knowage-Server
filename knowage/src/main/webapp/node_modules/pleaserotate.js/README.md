pleaserotate.js
===============

A simple way to force mobile users to view your site in portrait or landscape
mode.  Include this js file and it will replace your site with a friendly
message until the users' device is in the proper orientation.

![Example](https://raw.githubusercontent.com/arscan/pleaserotate.js/master/demo.gif "Example")

View it in action [over here](http://www.robscanlon.com/encom-boardroom/) (mobile device
required to see it).

### Basic Usage

Just include the script file anywhere in your doc:

```html
<script src="pleaserotate.js"></script>
```

Style using CSS.  Configure by creating a `window.PleaseRotateOptions` object
before you include the script.

```html
<style>
    /* style the elements with CSS */
    #pleaserotate-graphic{
        fill: #fff;
    }

    #pleaserotate-backdrop {
        color: #fff;
        background-color: #000;
    }
</style>

<script>
    /* you can pass in options here */
    PleaseRotateOptions = {
        forcePortrait: true // if you would prefer to force portrait mode
    };
</script>
<script src="pleaserotate.js"></script>
```

### Using AMD or CommonJS

This supports AMD and CommonJS.  Require ```pleaserotate.js``` and call
```PleaseRotate.start(options)``` to use.

### Options

The current default options are:

```javascript
var PleaseRotateOptions = {
    startOnPageLoad: true,
    onHide: function(){},
    onShow: function(){},
    forcePortrait: false,
    message: "Please Rotate Your Device",
    subMessage: "(or click to continue)",
    allowClickBypass: true,
    onlyMobile: true,
    zIndex: 1000,
    iconNode: null
};
```

### Note Regarding Startup

Depending on where you including the javascript file, other parts of your site
could render before pleaserotate.js has a chance to block them out.  To help
you  work around that, pleaserotate.js attaches classes to the ```<html>```
element while running.  Look for ```pleaserotate-showing``` and
```pleaserotate-hiding``` classes, which indicate that pleaserotate.js has
finished loading and if it is currently showing the "Please Rotate" message or
not.  You can use CSS to hide parts of your page until those classes show up.
