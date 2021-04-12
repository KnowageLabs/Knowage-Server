CodeMirror.defineSimpleMode("calculatedFieldMode", {
    start: [{
            regex: /(Sum|Locate|Min|Max|Count|Length|Avg|Concat|Substring)/,
            token: ["keyword"]
        },{
        	// aggregations
            regex: /((?:AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\()(\"[a-zA-Z0-9\-\_\s]*\")(\))/,
            token: ["keyword", "field", "keyword"]
        },{
        	// totals
            regex: /((?:TOTAL_SUM|TOTAL_AVG|TOTAL_MIN|TOTAL_MAX|TOTAL_COUNT|TOTAL_COUNT_DISTINCT)\()(\"[a-zA-Z0-9\-\_\s]*\")(\))/,
            token: ["keyword", "field", "keyword"]
        },{
            regex: /((?:TOTAL_SUM|TOTAL_AVG|TOTAL_MIN|TOTAL_MAX|TOTAL_COUNT|TOTAL_COUNT_DISTINCT)\()([a-zA-Z0-9\-\+\/\*\_\s\$\{\}\"]*)(\))/,
            token: ["keyword", "field", "keyword"]
        },{
        	// functions
            regex: /((?:NULLIF)\()(\s?\"[a-zA-Z0-9\-\_\s]*\"|\$[a-zA-Z0-9\-\+\/\*\_\s\{\}\"]*)(\s?,\s?)([0-9]*\s?)(\))/,
            token: ["keyword", "field", "separator","value" , "keyword"]
        },{
            regex: /((?:AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\()([a-zA-Z0-9\-\+\/\*\_\s\$\{\}\"]*)(\))/,
            token: ["keyword", "", "keyword"]
        },{
            regex: /(\$V\{)([a-zA-Z0-9\-\_\s]*)(\})/,
            token: ["keyword", "field", "keyword"]
        }, {
            regex: /\+|\-|\*|\//,
            token: ["keyword"]
        },
        {
            regex: /(\"[a-zA-Z0-9\s\-\_\>]*\"){1}/,
            token: ["field"]
        }
    ]
});