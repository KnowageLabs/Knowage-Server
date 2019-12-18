CodeMirror.defineSimpleMode("calculatedFieldMode", {
    start: [{
            regex: /(Sum|Locate|Min|Max|Count|Length|Avg|Concat|Substring)/,
            token: ["keyword"]
        },{
            regex: /((?:AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\()(\"[a-zA-Z0-9\-\_\s]*\")(\))/,
            token: ["keyword", "field", "keyword"]
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