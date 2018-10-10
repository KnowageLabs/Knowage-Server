CodeMirror.defineSimpleMode("customMode", {
    start: [{
            regex: /(Sum|Locate|Min|Max|Count|Length|Avg|Concat|Substring)/,
            token: ["keyword"]
        }, {
            regex: /\+|\-|\*|\//,
            token: ["keyword"]
        },
        {
            regex: /(test\()(.*)(\))/,
            token: ["keyword", "args", "keyword"]
        },
        {
            regex: /(\$F\{[a-zA-Z0-9\s\-\>]*\*\}){1}/,
            token: ["error"]
        },
        {
            regex: /(\$F\{[a-zA-Z0-9\s\-\>]*\}){1}/,
            token: ["atom"]
        }
    ]
});