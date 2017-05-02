/*
Copyright 2010 University of Toronto
Copyright 2010-2011 OCAD University
Copyright 2013, 2016 Raising the Floor - International

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};
var fluid = fluid || fluid_2_0_0;

(function ($, fluid) {
    "use strict";

    fluid.registerNamespace("fluid.model.transform");
    fluid.registerNamespace("fluid.transforms");

    /**********************************
     * Standard transformer functions *
     **********************************/

    fluid.defaults("fluid.transforms.value", {
        gradeNames: "fluid.standardTransformFunction",
        invertConfiguration: "fluid.identity"
    });

    fluid.transforms.value = fluid.identity;

    // Export the use of the "value" transform under the "identity" name for FLUID-5293
    fluid.transforms.identity = fluid.transforms.value;
    fluid.defaults("fluid.transforms.identity", {
        gradeNames: "fluid.transforms.value"
    });

    // A helpful utility function to be used when a transform's inverse is the identity
    fluid.transforms.invertToIdentity = function (transformSpec) {
        transformSpec.type = "fluid.transforms.identity";
        return transformSpec;
    };

    fluid.defaults("fluid.transforms.literalValue", {
        gradeNames: "fluid.standardOutputTransformFunction"
    });

    fluid.transforms.literalValue = function (transformSpec) {
        return transformSpec.input;
    };

    fluid.defaults("fluid.transforms.stringToNumber", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.stringToNumber.invert"
    });

    fluid.transforms.stringToNumber = function (value) {
        var newValue = Number(value);
        return isNaN(newValue) ? undefined : newValue;
    };

    fluid.transforms.stringToNumber.invert = function (transformSpec) {
        transformSpec.type = "fluid.transforms.numberToString";
        return transformSpec;
    };

    fluid.defaults("fluid.transforms.numberToString", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.numberToString.invert"
    });

    fluid.transforms.numberToString = function (value) {
        return (typeof value !== "number") ? undefined : "" + value;
    };

    fluid.transforms.numberToString.invert = function (transformSpec) {
        transformSpec.type = "fluid.transforms.stringToNumber";
        return transformSpec;
    };

    fluid.defaults("fluid.transforms.count", {
        gradeNames: "fluid.standardTransformFunction"
    });

    fluid.transforms.count = function (value) {
        return fluid.makeArray(value).length;
    };


    fluid.defaults("fluid.transforms.round", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.invertToIdentity"
    });

    fluid.transforms.round = function (value) {
        return Math.round(value);
    };


    fluid.defaults("fluid.transforms.delete", {
        gradeNames: "fluid.transformFunction"
    });

    fluid.transforms["delete"] = function (transformSpec, transformer) {
        var outputPath = fluid.model.composePaths(transformer.outputPrefix, transformSpec.outputPath);
        transformer.applier.change(outputPath, null, "DELETE");
    };


    fluid.defaults("fluid.transforms.firstValue", {
        gradeNames: "fluid.standardOutputTransformFunction"
    });

    fluid.transforms.firstValue = function (transformSpec, transformer) {
        if (!transformSpec.values || !transformSpec.values.length) {
            fluid.fail("firstValue transformer requires an array of values at path named \"values\", supplied", transformSpec);
        }
        for (var i = 0; i < transformSpec.values.length; i++) {
            var value = transformSpec.values[i];
            // TODO: problem here - all of these transforms will have their side-effects (setValue) even if only one is chosen
            var expanded = transformer.expand(value);
            if (expanded !== undefined) {
                return expanded;
            }
        }
    };

    fluid.defaults("fluid.transforms.linearScale", {
        gradeNames: ["fluid.multiInputTransformFunction",
                     "fluid.standardTransformFunction",
                     "fluid.lens" ],
        invertConfiguration: "fluid.transforms.linearScale.invert",
        inputVariables: {
            factor: 1,
            offset: 0
        }
    });

    /* simple linear transformation */
    fluid.transforms.linearScale = function (input, extraInputs) {
        var factor = extraInputs.factor();
        var offset = extraInputs.offset();

        if (typeof(input) !== "number" || typeof(factor) !== "number" || typeof(offset) !== "number") {
            return undefined;
        }
        return input * factor + offset;
    };

    /* TODO: This inversion doesn't work if the value and factors are given as paths in the source model */
    fluid.transforms.linearScale.invert = function (transformSpec) {
        // delete the factor and offset paths if present
        delete transformSpec.factorPath;
        delete transformSpec.offsetPath;

        if (transformSpec.factor !== undefined) {
            transformSpec.factor = (transformSpec.factor === 0) ? 0 : 1 / transformSpec.factor;
        }
        if (transformSpec.offset !== undefined) {
            transformSpec.offset = -transformSpec.offset * (transformSpec.factor !== undefined ? transformSpec.factor : 1);
        }
        return transformSpec;
    };

    fluid.defaults("fluid.transforms.binaryOp", {
        gradeNames: [ "fluid.multiInputTransformFunction", "fluid.standardOutputTransformFunction" ],
        inputVariables: {
            left: null,
            right: null
        }
    });

    fluid.transforms.binaryLookup = {
        "===": function (a, b) { return fluid.model.isSameValue(a, b); },
        "!==": function (a, b) { return !fluid.model.isSameValue(a, b); },
        "<=": function (a, b) { return a <= b; },
        "<": function (a, b) { return a < b; },
        ">=": function (a, b) { return a >= b; },
        ">": function (a, b) { return a > b; },
        "+": function (a, b) { return a + b; },
        "-": function (a, b) { return a - b; },
        "*": function (a, b) { return a * b; },
        "/": function (a, b) { return a / b; },
        "%": function (a, b) { return a % b; },
        "&&": function (a, b) { return a && b; },
        "||": function (a, b) { return a || b; }
    };

    fluid.transforms.binaryOp = function (inputs, transformSpec, transformer) {
        var left = inputs.left();
        var right = inputs.right();

        var operator = fluid.model.transform.getValue(undefined, transformSpec.operator, transformer);

        var fun = fluid.transforms.binaryLookup[operator];
        return (fun === undefined || left === undefined || right === undefined) ?
            undefined : fun(left, right);
    };

    fluid.defaults("fluid.transforms.condition", {
        gradeNames: [ "fluid.multiInputTransformFunction", "fluid.standardOutputTransformFunction" ],
        inputVariables: {
            "true": null,
            "false": null,
            "condition": null
        }
    });

    fluid.transforms.condition = function (inputs) {
        var condition = inputs.condition();
        if (condition === null) {
            return undefined;
        }

        return inputs[condition ? "true" : "false"]();
    };

    fluid.defaults("fluid.transforms.valueMapper", {
        gradeNames: ["fluid.transformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.valueMapper.invert",
        collectInputPaths: "fluid.transforms.valueMapper.collect"
    });

    /* unsupported, NON-API function
     * sorts by the object's 'matchValue' property, where higher is better.
     * Tiebreaking is done via the `index` property, where a lower index takes priority
     */
    fluid.model.transform.compareMatches = function (speca, specb) {
        var matchDiff = specb.matchValue - speca.matchValue;
        return matchDiff === 0 ? speca.index - specb.index : matchDiff; // tiebreak using 'index'
    };

    fluid.transforms.valueMapper = function (transformSpec, transformer) {
        if (!transformSpec.match) {
            fluid.fail("valueMapper requires an array or hash of matches at path named \"match\", supplied ", transformSpec);
        }
        var value = fluid.model.transform.getValue(transformSpec.defaultInputPath, undefined, transformer);

        var matchedEntry = (fluid.isArrayable(transformSpec.match)) ? // long form with array of records?
            fluid.transforms.valueMapper.longFormMatch(value, transformSpec, transformer) :
            transformSpec.match[value];

        if (matchedEntry === undefined) { // if no matches found, default to noMatch
            matchedEntry = transformSpec.noMatch;
        }

        if (matchedEntry === undefined) { // if there was no noMatch directive, return undefined
            return;
        }

        var outputPath = matchedEntry.outputPath === undefined ? transformSpec.defaultOutputPath : matchedEntry.outputPath;
        transformer.outputPrefixOp.push(outputPath);

        var outputValue;
        if (fluid.isPrimitive(matchedEntry)) {
            outputValue = matchedEntry;
        } else if (matchedEntry.outputUndefinedValue) { // if outputUndefinedValue is set, outputValue `undefined`
            outputValue = undefined;
        } else {
            // get value from outputValue. If none is found set the outputValue to be that of defaultOutputValue (or undefined)
            outputValue = fluid.model.transform.resolveParam(matchedEntry, transformer, "outputValue", undefined);
            outputValue = (outputValue === undefined) ? transformSpec.defaultOutputValue : outputValue;
        }
        // output if we have a path and something to output
        if (typeof(outputPath) === "string" && outputValue !== undefined) {
            fluid.model.transform.setValue(undefined, outputValue, transformer, transformSpec.merge);
            outputValue = undefined; // make sure we don't also return value
        }
        transformer.outputPrefixOp.pop();
        return outputValue;
    };

    // unsupported, NON-API function
    fluid.transforms.valueMapper.longFormMatch = function (valueFromDefaultPath, transformSpec, transformer) {
        var o = transformSpec.match;
        if (o.length === 0) {
            fluid.fail("valueMapper supplied empty list of matches: ", transformSpec);
        }
        var matchPower = [];
        for (var i = 0; i < o.length; ++i) {
            var option = o[i];
            var value = option.inputPath ?
                fluid.model.transform.getValue(option.inputPath, undefined, transformer) : valueFromDefaultPath;

            var matchValue = fluid.model.transform.matchValue(option.inputValue, value, option.partialMatches);
            matchPower[i] = {index: i, matchValue: matchValue};
        }
        matchPower.sort(fluid.model.transform.compareMatches);
        return matchPower[0].matchValue <= 0 ? undefined : o[matchPower[0].index];
    };

    fluid.transforms.valueMapper.invert = function (transformSpec, transformer) {
        var match = [];
        var togo = {
            type: "fluid.transforms.valueMapper",
            match: match
        };
        var isArray = fluid.isArrayable(transformSpec.match);

        togo.defaultInputPath = fluid.model.composePaths(transformer.outputPrefix, transformSpec.defaultOutputPath);
        togo.defaultOutputPath = fluid.model.composePaths(transformer.inputPrefix, transformSpec.defaultInputPath);

        var def = fluid.firstDefined;
        fluid.each(transformSpec.match, function (option, key) {
            if (option.outputUndefinedValue === true) {
                return; // don't attempt to invert undefined output value entries
            }
            var outOption = {};
            var origInputValue = def(isArray ? option.inputValue : key, transformSpec.defaultInputValue);
            if (origInputValue === undefined) {
                fluid.fail("Failure inverting configuration for valueMapper - inputValue could not be resolved for record " + key + ": ", transformSpec);
            }
            outOption.outputValue = origInputValue;
            outOption.inputValue = !isArray && fluid.isPrimitive(option) ?
                option : def(option.outputValue, transformSpec.defaultOutputValue);

            if (option.outputPath) {
                outOption.inputPath = fluid.model.composePaths(transformer.outputPrefix, def(option.outputPath, transformSpec.outputPath));
            }
            if (option.inputPath) {
                outOption.outputPath = fluid.model.composePaths(transformer.inputPrefix, def(option.inputPath, transformSpec.inputPath));
            }
            match.push(outOption);
        });
        return togo;
    };

    fluid.transforms.valueMapper.collect = function (transformSpec, transformer) {
        var togo = [];
        fluid.model.transform.accumulateInputPath(transformSpec.defaultInputPath, transformer, togo);
        fluid.each(transformSpec.match, function (option) {
            fluid.model.transform.accumulateInputPath(option.inputPath, transformer, togo);
        });
        return togo;
    };

    /* -------- arrayToSetMembership and setMembershipToArray ---------------- */

    fluid.defaults("fluid.transforms.arrayToSetMembership", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.arrayToSetMembership.invert"
    });


    fluid.transforms.arrayToSetMembership = function (value, transformSpec, transformer) {
        var output = {};
        var options = transformSpec.options;

        if (!value || !fluid.isArrayable(value)) {
            fluid.fail("arrayToSetMembership didn't find array at inputPath nor passed as value.", transformSpec);
        }
        if (!options) {
            fluid.fail("arrayToSetMembership requires an options block set");
        }

        if (transformSpec.presentValue === undefined) {
            transformSpec.presentValue = true;
        }

        if (transformSpec.missingValue === undefined) {
            transformSpec.missingValue = false;
        }

        fluid.each(options, function (outPath, key) {
            // write to output object the value <presentValue> or <missingValue> depending on whether key is found in user input
            var outVal = (value.indexOf(key) !== -1) ? transformSpec.presentValue : transformSpec.missingValue;
            fluid.set(output, outPath, outVal, transformer.resolverSetConfig);
        });
        return output;
    };

    /**
     * NON-API function; Copies the entire transformSpec with the following modifications:
     * * A new type is set (from argument)
     * * each [key]=value entry in the options is swapped to be: [value]=key
     */
    fluid.transforms.arrayToSetMembership.invertWithType = function (transformSpec, transformer, newType) {
        transformSpec.type = newType;
        var newOptions = {};
        fluid.each(transformSpec.options, function (path, oldKey) {
            newOptions[path] = oldKey;
        });
        transformSpec.options = newOptions;
        return transformSpec;
    };

    fluid.transforms.arrayToSetMembership.invert = function (transformSpec, transformer) {
        return fluid.transforms.arrayToSetMembership.invertWithType(transformSpec, transformer,
            "fluid.transforms.setMembershipToArray");
    };

    fluid.defaults("fluid.transforms.setMembershipToArray", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.setMembershipToArray.invert"
    });

    fluid.transforms.setMembershipToArray = function (input, transformSpec, transformer) {
        var options = transformSpec.options;

        if (!options) {
            fluid.fail("setMembershipToArray requires an options block specified");
        }

        if (transformSpec.presentValue === undefined) {
            transformSpec.presentValue = true;
        }

        if (transformSpec.missingValue === undefined) {
            transformSpec.missingValue = false;
        }

        var outputArr = [];
        fluid.each(options, function (outputVal, key) {
            var value = fluid.get(input, key, transformer.resolverGetConfig);
            if (value === transformSpec.presentValue) {
                outputArr.push(outputVal);
            }
        });
        return outputArr;
    };

    fluid.transforms.setMembershipToArray.invert = function (transformSpec, transformer) {
        return fluid.transforms.arrayToSetMembership.invertWithType(transformSpec, transformer,
            "fluid.transforms.arrayToSetMembership");
    };

    /* -------- deindexIntoArrayByKey and indexArrayByKey -------------------- */

    /**
     * Transforms the given array to an object.
     * Uses the transformSpec.options.key values from each object within the array as new keys.
     *
     * For example, with transformSpec.key = "name" and an input object like this:
     *
     * {
     *   b: [
     *     { name: b1, v: v1 },
     *     { name: b2, v: v2 }
     *   ]
     * }
     *
     * The output will be:
     * {
     *   b: {
     *     b1: {
     *       v: v1
     *     }
     *   },
     *   {
     *     b2: {
     *       v: v2
     *     }
     *   }
     * }
     */
    fluid.model.transform.applyPaths = function (operation, pathOp, paths) {
        for (var i = 0; i < paths.length; ++i) {
            if (operation === "push") {
                pathOp.push(paths[i]);
            } else {
                pathOp.pop();
            }
        }
    };

    fluid.model.transform.expandInnerValues = function (inputPath, outputPath, transformer, innerValues) {
        var inputPrefixOp = transformer.inputPrefixOp;
        var outputPrefixOp = transformer.outputPrefixOp;
        var apply = fluid.model.transform.applyPaths;

        apply("push", inputPrefixOp, inputPath);
        apply("push", outputPrefixOp, outputPath);
        var expanded = {};
        fluid.each(innerValues, function (innerValue) {
            var expandedInner = transformer.expand(innerValue);
            if (!fluid.isPrimitive(expandedInner)) {
                $.extend(true, expanded, expandedInner);
            } else {
                expanded = expandedInner;
            }
        });
        apply("pop", outputPrefixOp, outputPath);
        apply("pop", inputPrefixOp, inputPath);

        return expanded;
    };


    fluid.defaults("fluid.transforms.indexArrayByKey", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens" ],
        invertConfiguration: "fluid.transforms.indexArrayByKey.invert"
    });

    /** Transforms an array of objects into an object of objects, by indexing using the option "key" which must be supplied within the transform specification.
    * The key of each element will be taken from the value held in each each original object's member derived from the option value in "key" - this member should
    * exist in each array element. The member with name agreeing with "key" and its value will be removed from each original object before inserting into the returned
    * object.
    * For example,
    * <code>fluid.transforms.indexArrayByKey([{k: "e1", b: 1, c: 2}, {k: "e2", b: 2: c: 3}], {key: "k"})</code> will output the object
    * <code>{e1: {b: 1, c: 2}, e2: {b: 2: c, 3}</code>
    * Note: This transform frequently arises in the context of data which arose in XML form, which often represents "morally indexed" data in repeating array-like
    * constructs where the indexing key is held, for example, in an attribute.
    */
    fluid.transforms.indexArrayByKey = function (arr, transformSpec, transformer) {
        if (transformSpec.key === undefined) {
            fluid.fail("indexArrayByKey requires a 'key' option.", transformSpec);
        }
        if (!fluid.isArrayable(arr)) {
            fluid.fail("indexArrayByKey didn't find array at inputPath.", transformSpec);
        }
        var newHash = {};
        var pivot = transformSpec.key;

        fluid.each(arr, function (v, k) {
            // check that we have a pivot entry in the object and it's a valid type:
            var newKey = v[pivot];
            var keyType = typeof(newKey);
            if (keyType !== "string" && keyType !== "boolean" && keyType !== "number") {
                fluid.fail("indexArrayByKey encountered untransformable array due to missing or invalid key", v);
            }
            // use the value of the key element as key and use the remaining content as value
            var content = fluid.copy(v);
            delete content[pivot];
            // fix sub Arrays if needed:
            if (transformSpec.innerValue) {
                content = fluid.model.transform.expandInnerValues([transformer.inputPrefix, transformSpec.inputPath, k.toString()],
                    [transformSpec.outputPath, newKey], transformer, transformSpec.innerValue);
            }
            newHash[newKey] = content;
        });
        return newHash;
    };

    fluid.transforms.indexArrayByKey.invert = function (transformSpec) {
        transformSpec.type = "fluid.transforms.deindexIntoArrayByKey";
        // invert transforms from innerValue as well:
        // TODO: The Model Transformations framework should be capable of this, but right now the
        // issue is that we use a "private contract" to operate the "innerValue" slot. We need to
        // spend time thinking of how this should be formalised
        if (transformSpec.innerValue) {
            var innerValue = transformSpec.innerValue;
            for (var i = 0; i < innerValue.length; ++i) {
                innerValue[i] = fluid.model.transform.invertConfiguration(innerValue[i]);
            }
        }
        return transformSpec;
    };


    fluid.defaults("fluid.transforms.deindexIntoArrayByKey", {
        gradeNames: [ "fluid.standardTransformFunction", "fluid.lens" ],
        invertConfiguration: "fluid.transforms.deindexIntoArrayByKey.invert"
    });

    /**
     * Transforms an object of objects into an array of objects, by deindexing by the option "key" which must be supplied within the transform specification.
     * The key of each object will become split out into a fresh value in each array element which will be given the key held in the transformSpec option "key".
     * For example:
     * <code>fluid.transforms.deindexIntoArrayByKey({e1: {b: 1, c: 2}, e2: {b: 2: c, 3}, {key: "k"})</code> will output the array
     * <code>[{k: "e1", b: 1, c: 2}, {k: "e2", b: 2: c: 3}]</code>
     *
     * This performs the inverse transform of fluid.transforms.indexArrayByKey.
     */
    fluid.transforms.deindexIntoArrayByKey = function (hash, transformSpec, transformer) {
        if (transformSpec.key === undefined) {
            fluid.fail("deindexIntoArrayByKey requires a \"key\" option.", transformSpec);
        }

        var newArray = [];
        var pivot = transformSpec.key;

        fluid.each(hash, function (v, k) {
            var content = {};
            content[pivot] = k;
            if (transformSpec.innerValue) {
                v = fluid.model.transform.expandInnerValues([transformSpec.inputPath, k], [transformSpec.outputPath, newArray.length.toString()],
                    transformer, transformSpec.innerValue);
            }
            $.extend(true, content, v);
            newArray.push(content);
        });
        return newArray;
    };

    fluid.transforms.deindexIntoArrayByKey.invert = function (transformSpec) {
        transformSpec.type = "fluid.transforms.indexArrayByKey";
        // invert transforms from innerValue as well:
        // TODO: The Model Transformations framework should be capable of this, but right now the
        // issue is that we use a "private contract" to operate the "innerValue" slot. We need to
        // spend time thinking of how this should be formalised
        if (transformSpec.innerValue) {
            var innerValue = transformSpec.innerValue;
            for (var i = 0; i < innerValue.length; ++i) {
                innerValue[i] = fluid.model.transform.invertConfiguration(innerValue[i]);
            }
        }
        return transformSpec;
    };

    fluid.defaults("fluid.transforms.limitRange", {
        gradeNames: "fluid.standardTransformFunction"
    });

    fluid.transforms.limitRange = function (value, transformSpec) {
        var min = transformSpec.min;
        if (min !== undefined) {
            var excludeMin = transformSpec.excludeMin || 0;
            min += excludeMin;
            if (value < min) {
                value = min;
            }
        }
        var max = transformSpec.max;
        if (max !== undefined) {
            var excludeMax = transformSpec.excludeMax || 0;
            max -= excludeMax;
            if (value > max) {
                value = max;
            }
        }
        return value;
    };

    fluid.defaults("fluid.transforms.indexOf", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.indexOf.invert"
    });

    fluid.transforms.indexOf = function (value, transformSpec) {
        // We do not allow a positive number as 'notFound' value, as it threatens invertibility
        if (typeof (transformSpec.notFound) === "number" && transformSpec.notFound >= 0) {
            fluid.fail("A positive number is not allowed as 'notFound' value for indexOf");
        }
        var offset = fluid.transforms.parseIndexationOffset(transformSpec.offset, "indexOf");
        var array = fluid.makeArray(transformSpec.array);
        var originalIndex = array.indexOf(value);
        return originalIndex === -1 && transformSpec.notFound ? transformSpec.notFound : originalIndex + offset;
    };

    fluid.transforms.indexOf.invert = function (transformSpec, transformer) {
        var togo = fluid.transforms.invertArrayIndexation(transformSpec, transformer);
        togo.type = "fluid.transforms.dereference";
        return togo;
    };

    fluid.defaults("fluid.transforms.dereference", {
        gradeNames: ["fluid.standardTransformFunction", "fluid.lens"],
        invertConfiguration: "fluid.transforms.dereference.invert"
    });

    fluid.transforms.dereference = function (value, transformSpec) {
        if (typeof (value) !== "number") {
            return undefined;
        }
        var offset = fluid.transforms.parseIndexationOffset(transformSpec.offset, "dereference");
        var array = fluid.makeArray(transformSpec.array);
        var index = value + offset;
        return array[index];
    };

    fluid.transforms.dereference.invert = function (transformSpec, transformer) {
        var togo = fluid.transforms.invertArrayIndexation(transformSpec, transformer);
        togo.type = "fluid.transforms.indexOf";
        return togo;
    };

    fluid.transforms.parseIndexationOffset = function (offset, transformName) {
        var parsedOffset = 0;
        if (offset !== undefined) {
            parsedOffset = fluid.parseInteger(offset);
            if (isNaN(parsedOffset)) {
                fluid.fail(transformName + " requires the value of \"offset\" to be an integer or a string that can be converted to an integer. " + offset + " is invalid.");
            }
        }
        return parsedOffset;
    };

    fluid.transforms.invertArrayIndexation = function (transformSpec) {
        if (!isNaN(Number(transformSpec.offset))) {
            transformSpec.offset = Number(transformSpec.offset) * (-1);
        }
        return transformSpec;
    };

    fluid.defaults("fluid.transforms.stringTemplate", {
        gradeNames: "fluid.standardOutputTransformFunction"
    });

    fluid.transforms.stringTemplate = function (transformSpec) {
        return fluid.stringTemplate(transformSpec.template, transformSpec.terms);
    };

    fluid.defaults("fluid.transforms.free", {
        gradeNames: "fluid.transformFunction"
    });

    fluid.transforms.free = function (transformSpec) {
        var args = fluid.makeArray(transformSpec.args);
        return fluid.invokeGlobalFunction(transformSpec.func, args);
    };

    fluid.defaults("fluid.transforms.quantize", {
        gradeNames: "fluid.standardTransformFunction"
    });

    /**
     * Quantize function maps a continuous range into discrete values. Given an input, it will
     * be matched into a discrete bucket and the corresponding output will be done.
     */
    fluid.transforms.quantize = function (value, transformSpec, transform) {
        if (!transformSpec.ranges || !transformSpec.ranges.length) {
            fluid.fail("fluid.transforms.quantize should have a key called ranges containing an array defining ranges to quantize");
        }
        // TODO: error checking that upper bounds are all numbers and increasing
        for (var i = 0; i < transformSpec.ranges.length; i++) {
            var rangeSpec = transformSpec.ranges[i];
            if (value <= rangeSpec.upperBound || rangeSpec.upperBound === undefined && value >= Number.NEGATIVE_INFINITY) {
                return fluid.isPrimitive(rangeSpec.output) ? rangeSpec.output : transform.expand(rangeSpec.output);
            }
        }
    };

    /**
     * inRange transformer checks whether a value is within a given range and returns true if it is,
     * and false if it's not.
     *
     * The range is defined by the two inputs: "min" and "max" (both inclusive). If one of these inputs
     * is not present it is considered -infinite and +infinite, respectively - In other words, if no
     * `min` value is defined, any value below or equal to the given "max" value will result in true.
     */
    fluid.defaults("fluid.transforms.inRange", {
        gradeNames: "fluid.standardTransformFunction"
    });

    fluid.transforms.inRange = function (value, transformSpec) {
        return (transformSpec.min === undefined || transformSpec.min <= value) &&
            (transformSpec.max === undefined ||  transformSpec.max >= value) ? true : false;
    };

})(jQuery, fluid_2_0_0);
