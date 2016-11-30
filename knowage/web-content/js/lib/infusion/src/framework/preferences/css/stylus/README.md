## Overview ##

The "[stylus](./)" directory contains Stylus files for generating the Preference Framework stylesheets.

### How to add a new theme ###

Define your own themes variable that looks like:

```
    contrastThemes = {
        "theme-selector": {
            foregroundColor: #000000,
            backgroundColor: #ffffff
        }
        ...
    }
```

When calling Stylus mixins defined in "[utils/Themes.styl](./utils/Themes.styl)", pass in your own themes variable as a parameter, such as:

```
    build-themes-Enactors(contrastThemes);
```

### How to prevent grunt from compiling utility Stylus files ###
Some Stylus files may only contain mixins or functions for other Stylus files to import. Those files should not be compiled into CSS. To prevent the grunt task `grunt buildStylus` from compiling them, place these files in the "[utils](./utils)" directory.
