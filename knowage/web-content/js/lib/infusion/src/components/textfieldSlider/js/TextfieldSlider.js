/*
Copyright 2013-2016 OCAD University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function ($, fluid) {
    "use strict";

    /********************
     * Textfield Slider *
     ********************/

    fluid.defaults("fluid.textfieldSlider", {
        gradeNames: ["fluid.viewComponent", "fluid.contextAware"],
        components: {
            textfield: {
                type: "fluid.textfieldSlider.textfield",
                container: "{textfieldSlider}.dom.textfield",
                options: {
                    model: "{textfieldSlider}.model",
                    range: "{textfieldSlider}.options.range"
                }
            },
            slider: {
                container: "{textfieldSlider}.dom.slider",
                options: {
                    model: "{fluid.textfieldSlider}.model",
                    range: "{fluid.textfieldSlider}.options.range",
                    sliderOptions: "{fluid.textfieldSlider}.options.sliderOptions"
                }
            }
        },
        contextAwareness: {
            sliderVariety: {
                checks: {
                    jQueryUI: {
                        contextValue: "{fluid.prefsWidgetType}",
                        equals: "jQueryUI",
                        gradeNames: "fluid.textfieldSlider.jQueryUI"
                    }
                },
                defaultGradeNames: "fluid.textfieldSlider.nativeHTML"
            }
        },
        selectors: {
            textfield: ".flc-textfieldSlider-field",
            slider: ".flc-textfieldSlider-slider"
        },
        model: {
            value: null
        },
        modelRelay: {
            target: "value",
            singleTransform: {
                type: "fluid.transforms.limitRange",
                input: "{that}.model.value",
                min: "{that}.options.range.min",
                max: "{that}.options.range.max"
            }
        },
        range: {
            min: 0,
            max: 100
        },
        sliderOptions: {
            orientation: "horizontal",
            step: 1.0
        }
    });

    fluid.defaults("fluid.textfieldSlider.nativeHTML", {
        components: {
            slider: {
                type: "fluid.slider.native"
            }
        }
    });

    fluid.defaults("fluid.textfieldSlider.jQueryUI", {
        components: {
            slider: {
                type: "fluid.slider.jQuery"
            }
        }
    });

    fluid.defaults("fluid.textfieldSlider.textfield", {
        gradeNames: ["fluid.viewComponent"],
        range: {}, // should be used to specify the min, max range e.g. {min: 0, max: 100}
        modelRelay: {
            target: "value",
            singleTransform: {
                type: "fluid.transforms.stringToNumber",
                input: "{that}.model.stringValue"
            }
        },
        modelListeners: {
            value: {
                "this": "{that}.container",
                "method": "val",
                args: ["{change}.value"]
            },
            // TODO: This listener is to deal with the issue that, when the input field receives a invalid input such as a string value,
            // ignore it and populate the field with the previous value.
            // This is an area in which UX has spilled over into our model configuration, which to some extent we should try to prevent.
            // Whenever we receive a "change" event or some other similar checkpoint, if these updates occurred any faster, the user would
            // be infuriated by being unable to type into the field. This situation doesn't occur at the moment because the change event is
            // only fired when users leave the input feild. At the very least, we need to give a namespace to this listener - unfortunately
            // the current dataBinding implementation will ignore it. Having this listener here represents an interaction decision rather
            // than an implementation decision. This issue needs to be revisited.
            stringValue: {
                "this": "{that}.container",
                "method": "val",
                args: ["{that}.model.value"]
            }
        },
        listeners: {
            "onCreate.bindChangeEvt": {
                "this": "{that}.container",
                "method": "change",
                "args": ["{that}.setModel"]
            }
        },
        invokers: {
            setModel: {
                changePath: "stringValue",
                value: "{arguments}.0.target.value"
            }
        }
    });

    // Base slider grade
    fluid.defaults("fluid.slider", {
        gradeNames: ["fluid.viewComponent"],
        range: {} // should be used to specify the min, max range e.g. {min: 0, max: 100}
    });

    fluid.defaults("fluid.slider.native", {
        gradeNames: ["fluid.slider"],
        modelRelay: {
            target: "value",
            singleTransform: {
                type: "fluid.transforms.stringToNumber",
                input: "{that}.model.stringValue"
            }
        },
        invokers: {
            setModel: {
                changePath: "stringValue",
                value: {
                    expander: {
                        "this": "{that}.container",
                        "method": "val"
                    }
                }
            }
        },
        listeners: {
            "onCreate.initSliderAttributes": {
                "this": "{that}.container",
                method: "attr",
                args: [{
                    "min": "{that}.options.range.min",
                    "max": "{that}.options.range.max",
                    "step": "{that}.options.sliderOptions.step",
                    "type": "range",
                    "value": "{that}.model.value"
                }]
            },
            "onCreate.bindSlideEvt": {
                "this": "{that}.container",
                "method": "on",
                "args": ["input", "{that}.setModel"]
            },
            "onCreate.bindRangeChangeEvt": {
                "this": "{that}.container",
                "method": "on",
                "args": ["change", "{that}.setModel"]
            }
        },
        modelListeners: {
            "value": [{
                "this": "{that}.container",
                "method": "val",
                args: ["{change}.value"],
                // If we don't exclude init, the value can get
                // set before onCreate.initSliderAttributes
                // sets min / max / step, which messes up the
                // initial slider rendering
                excludeSource: "init"
            }]
        }
    });

    fluid.defaults("fluid.slider.jQuery", {
        gradeNames: ["fluid.slider"],
        selectors: {
            thumb: ".ui-slider-handle"
        },
        styles: {
            handle: "fl-slider-handle",
            range: "fl-slider-range"
        },
        members: {
            slider: {
                expander: {
                    "this": "{that}.container",
                    method: "slider",
                    args: ["{that}.combinedSliderOptions"]
                }
            },
            combinedSliderOptions: {
                expander: {
                    funcName: "fluid.slider.combineSliderOptions",
                    args: ["{that}.options.sliderOptions", "{that}.options.range"]
                }
            }
        },
        sliderOptions: {
            orientation: "horizontal",
            step: 1.0,
            classes: {
                "ui-slider-handle": "{that}.options.styles.handle",
                "ui-slider-range": "{that}.options.styles.range"
            }
        },
        invokers: {
            setSliderValue: {
                "this": "{that}.slider",
                "method": "slider",
                args: ["value", "{arguments}.0"]
            },
            setSliderAriaValueNow: {
                "this": "{that}.dom.thumb",
                "method": "attr",
                args: ["aria-valuenow", "{arguments}.0"]
            },
            setModel: {
                changePath: "value",
                value: "{arguments}.1.value"
            }
        },
        listeners: {
            // This can be removed once the jQuery UI slider has built in ARIA
            "onCreate.initSliderAria": {
                "this": "{that}.dom.thumb",
                method: "attr",
                args: [{
                    role: "slider",
                    "aria-valuenow": "{that}.combinedSliderOptions.value",
                    "aria-valuemin": "{that}.combinedSliderOptions.min",
                    "aria-valuemax": "{that}.combinedSliderOptions.max"
                }]
            },
            "onCreate.bindSlideEvt": {
                "this": "{that}.slider",
                "method": "on",
                "args": ["slide", "{that}.setModel"]
            }
        },
        modelListeners: {
            "value": [{
                listener: "{that}.setSliderValue",
                args: ["{change}.value"]
            }, {
                listener: "{that}.setSliderAriaValueNow",
                args: ["{change}.value"]
            }]
        }
    });

    fluid.slider.combineSliderOptions = function (sliderOptions, model, range) {
        return $.extend(true, {}, sliderOptions, model, range);
    };

})(jQuery, fluid_2_0_0);
