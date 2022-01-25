export function group(expression) {
    console.log("filterTreeFactoryService - group() - expression ", expression)
    var group = {} as any;
    group.type = "NODE_OP";
    group.value = "PAR";
    group.childNodes = [];
    group.childNodes.push(expression);

    return group;
}

export function createExpression(leftOperant, operator, rightOperand) {
    console.log("filterTreeFactoryService - createExpression() - leftOperant ", leftOperant, ', operator ', operator, ', rightOperand', rightOperand)
    var expression = {} as any
    if (!leftOperant) throw new Error('leftOperant cannot be undefined.');
    if (!operator) throw new Error('operator cannot be undefined.');
    if (!rightOperand) throw new Error('rightOperand cannot be undefined.');
    expression.type = operator.type;
    expression.value = operator.value;
    expression.childNodes = [];
    expression.childNodes.push(leftOperant);
    expression.childNodes.push(rightOperand);

    return expression;
}

export function operator(value) {
    console.log("filterTreeFactoryService - operator() - value ", value)
    var operator = {} as any;
    operator.type = "NODE_OP";
    operator.value = value;
    operator.childNodes = [];

    return operator;
}

export function filter(name) {
    console.log("filterTreeFactoryService - filter() - name ", name)
    var filter = {} as any;
    filter.type = "NODE_CONST";
    filter.value = name;
    filter.childNodes = [];

    return filter;
}
