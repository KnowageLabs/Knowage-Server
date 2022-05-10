interface Config {
    alias: string
    expression: string
    format?: string
    nature: string
    type: string
}

export function buildCalculatedField(calcFieldOutput, selectedQueryFields) {
    let calculatedField = {} as any
    let addedParameters = {} as any

    addedParameters.alias = calcFieldOutput.alias
    addedParameters.type = calcFieldOutput.type
    addedParameters.nature = calcFieldOutput.nature
    addedParameters.expressionSimple = calcFieldOutput.expression
    addedParameters.expression = formatCalcFieldFormula(calcFieldOutput.expression, selectedQueryFields)
    addedParameters.type === 'DATE' ? (addedParameters.format = calcFieldOutput.format) : ''

    calculatedField.id = addedParameters
    calculatedField.alias = addedParameters.alias
    calculatedField.nature = addedParameters.nature
    calculatedField.type = 'inline.calculated.field'
    calculatedField.distinct = false
    addedParameters.type === 'DATE' ? (calculatedField.format = calcFieldOutput.format) : ''
    calculatedField.fieldType = calcFieldOutput.nature.toLowerCase()
    calculatedField.entity = calcFieldOutput.alias
    calculatedField.field = calcFieldOutput.alias
    calculatedField.funct = calcFieldOutput.nature == 'MEASURE' ? 'SUM' : ''
    calculatedField.group = calcFieldOutput.nature == 'ATTRIBUTE' ? true : false
    calculatedField.order = ''
    calculatedField.include = true
    calculatedField.inUse = true
    calculatedField.visible = true
    calculatedField.id.expression = cleanExpression(calculatedField.id.expression)
    calculatedField.id.expressionSimple = cleanExpression(calculatedField.id.expressionSimple)
    calculatedField.formula = cleanExpression(addedParameters.expression)
    calculatedField.expression = cleanExpression(addedParameters.expressionSimple)
    calculatedField.longDescription = cleanExpression(addedParameters.expression)

    return calculatedField
}

function formatCalcFieldFormula(formula, selectedQueryFields) {
    var fullRegex = /(\$F\{[a-zA-Z0-9\s\->]*\}){1}/g
    var regEx = /(\$F\{[a-zA-Z0-9\s\->]*\}){1}/
    var regExGroups = /(\$F\{)([a-zA-Z0-9\s\->]*)(\}){1}/

    if (formula.match(fullRegex)) {
        var fieldsNum = formula.match(fullRegex).length
    }

    for (var i = 0; i < fieldsNum; i++) {
        var tempReplace = ''
        var match = regExGroups.exec(formula) as any
        selectedQueryFields.forEach((value) => {
            if (value.alias == match[2]) {
                tempReplace = value.id
                return
            }
        })

        formula = formula.replace(regEx, tempReplace)
    }
    return formula
}

function cleanExpression(expression) {
    expression = expression.replaceAll(/\u00a0/g, ' ')
    return expression
}
