export function group(expression) {
    const group = {} as any;
    group.type = "NODE_OP";
    group.value = "PAR";
    group.childNodes = [];
    group.childNodes.push(expression);

    return group;
}

export function expression(leftOperant, operator, rightOperand) {
    const expression = {} as any
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
    const operator = {} as any;
    operator.type = "NODE_OP";
    operator.value = value;
    operator.childNodes = [];

    return operator;
}

export function filter(name) {
    const filter = {} as any;
    filter.type = "NODE_CONST";
    filter.value = name;
    filter.childNodes = [];

    return filter;
}
