let filterTree = {}
const childProperty = 'childNodes';
const deepcopy = require('deepcopy');
const deepEqual = require('deep-equal')

// #region treeService.ts
export function getFilterTree() {
    console.log(' --- treeService - getFilterTree()')
    return filterTree
}

export function setFilterTree(expression) {
    console.log(' --- treeService - setFilterTree() - expression', expression)
    filterTree = deepcopy(expression)
}

export function contains(tree, nodeToFind) {
    console.log(' --- treeService - contains() - tree', tree, ', nodeToFind ', nodeToFind)
    let contains = false;
    traverseDF(tree, function (node) {
        if (node === nodeToFind) {
            // if (deepEqual(node, nodeToFind)) {
            contains = true
        }
    })

    return contains;
}

export function find(tree, toFind) {
    console.log(' --- treeService - find() - tree', tree, ', toFind ', toFind)
    var equalNode;
    traverseDF(tree, function (node) {
        // console.log(deepEqual(node, toFind))
        if (deepEqual(node, toFind)) {

            equalNode = node;
        }
    })

    return equalNode;
}


export function add(tree, node, parent) {
    console.log(' --- treeService - add() - tree', tree, ', node ', node, ', parent ', parent)
    nodeExistingCheck(tree, parent);
    parent[childProperty].unshift(node);
}


export function move(tree, source, destination) {
    console.log(' --- treeService - move() - tree', tree, ', source ', source, ', parent ', destination)
    nodeExistingCheck(tree, source);
    nodeExistingCheck(tree, destination);

    // angular.copy(source,destination)
    // destination = deepcopy(source)

    // MY START
    const tempNode = find(tree, destination)
    tempNode.childNodes = source.childNodes
    tempNode.value = source.value
    tempNode.type = source.type

    if (!source.details) delete tempNode.details
    else tempNode.details = source.details
    // MY END
}

export function swapNodes(node1, node2) {
    console.log(' --- treeService - swapNodes() - node1', node1, ', node2 ', node2)
    // var temp = deepcopy(node1)
    // node1 = deepcopy(node2)
    // node2 = deepcopy(temp)
    var temp = { childNodes: node1.childNodes, value: node1.value, type: node1.type, details: node1.details }
    node1.childNodes = node2.childNodes
    node1.value = node2.value
    node1.type = node2.type
    node1.details = node2.details
    node2.childNodes = temp.childNodes
    node2.value = temp.value
    node2.type = temp.type
    node2.details = temp.details

    if (!node1.details) delete node1.details
    if (!node2.details) delete node2.details
}

export function swapNodePropertyValues(node1, node2, properties) {
    console.log(' --- treeService - swapNodePropertyValues() - node1', node1, ', source ', node2, ', properties ', properties)
    for (var i = 0; i < properties.length; i++) {
        var temp = node1[properties[i]];
        node1[properties[i]] = node2[properties[i]];
        node2[properties[i]] = temp;

    }

}



export function removeNode(tree, nodeToRemove) {
    console.log(' --- treeService - removeNode() - tree', tree, ', nodeToRemove ', nodeToRemove)
    getSiblings(tree, nodeToRemove).splice(getNodeToRemoveIndex(tree, nodeToRemove), 1);
}

export function traverseDF(tree, callback) {

    (function recurse(currentNode) {
        // console.log(" bbb - CURRENT NODE: ", currentNode)
        callback(currentNode);
        for (var i = 0; i < currentNode[childProperty].length; i++) {
            recurse(currentNode[childProperty][i]);
        }

    })(tree)
    console.log(' --- treeService - traverseDF() - tree', tree, ', callback ', callback)
}

export function replace(tree, expression, node) {
    console.log(' --- treeService - replace() - tree', tree, ', expression ', expression, ', node ', node)
    nodeExistingCheck(tree, node);

    // MY START
    const tempNode = find(tree, node)
    console.log(' aaa - FOUND NODE: ', tempNode)
    console.log(' aaa - experssion: ', expression)
    tempNode.childNodes = expression.childNodes
    tempNode.value = expression.value
    tempNode.type = expression.type

    if (!expression.details) delete tempNode.details
    else tempNode.details = expression.details


    // if (expression.details) {
    //     tempNode.details = expression.details
    // }
    // MY END

    // angular.copy(expression, node)
    // node = deepcopy(expression)
}

export function getParent(tree, child) {
    console.log(' --- treeService - getParent() - tree', tree, ', child ', child)
    var parent;

    nodeExistingCheck(tree, child)

    traverseDF(tree, function (node) {

        if (findElementIndex(node[childProperty], child) > -1) parent = node;

    })
    //if(!parent)throw new Error('Parent does not exist.');

    return parent;

}


export function findElementIndex(array, element) {
    // console.log(' --- treeService - findElementIndex() - array', array, ', element ', element)
    for (var i = 0; i < array.length; i++) {
        // MY CHANGE node.value === nodeToFind.value
        // console.log(' ccc - element: ', element, ', array[i]: ', array[i])
        if (deepEqual(element, array[i])) {
            // console.log(' FOUND !!! ccc - element: ', element, ', array[i]: ', array[i])
            // if (element.value === array[i].value && element.details?.rightOperandValue === array[i].details?.rightOperandValue) {
            return i;
        }
    }
    return - 1;
}

export function getNodeToRemoveIndex(tree, nodeToRemove) {
    console.log(' --- treeService - findElementIndex() - tree', tree, ', nodeToRemove ', nodeToRemove)
    return findElementIndex(getSiblings(tree, nodeToRemove), nodeToRemove);
}

export function getSiblings(tree, node) {
    // console.log(' --- treeService - getSiblings() - tree', tree, ', node ', node)
    return getParent(tree, node)[childProperty];
}

export function nodeExistingCheck(tree, node) {
    console.log(' --- treeService - nodeExistingCheck() - tree', tree, ', node ', node)
    if (!contains(tree, node)) {

        throw new Error('Node does not exist.');
    }
}

export function findByName(tree, filterName) {
    console.log(' --- treeService - findByName() - tree', tree, ', filterName ', filterName)
    var equalNode;
    traverseDF(tree, function (node) {

        if (node.type == "NODE_CONST" && node.value == filterName) {
            equalNode = node;
        }
    })

    return equalNode;
}

export function removeInPlace(expression, filterName) {
    console.log(' --- treeService - removeInPlace() - expression', expression, ', filterName ', filterName)
    var currentNodeInExpression = findByName(expression, filterName);
    var parentOfCurrentNode = getParent(expression, currentNodeInExpression);
    var parentOfParent = null as any;

    // Get PAR node for filter group exactly two elements
    try {
        parentOfParent = getParent(expression, parentOfCurrentNode);
    } catch (err) {
        console.log("removeInPlace error 1: ", err)
    }

    try {
        var siblings = getSiblings(expression, currentNodeInExpression);

        var indexOfCurrentNode = siblings.indexOf(currentNodeInExpression);
        siblings.splice(indexOfCurrentNode, 1)

        if (siblings.length == 1 && parentOfParent && parentOfParent.value == "PAR") {
            try {
                parentOfCurrentNode = parentOfParent;
            } catch (err2) {
                console.log("removeInPlace error 2: ", err2)
            }

        }

        // If root nodes doesn't have children
        if (expression.childNodes.length == 0) {
            // angular.copy({}, expression);
            expression = {}
        } else if (parentOfParent != null) {
            replace(expression, siblings[0], parentOfCurrentNode);
        }

    } catch (err) {
        // no siblings
        // angular.copy({}, expression);
        expression = {}
    }

}
// #endregion


// #region advancedFilterService.ts
// #endregion


// #region groupUtilsService.ts
// #endregion

// #region operandUtilService.ts
export function getSibilng(filterTree, operand) {
    console.log("operandUtilService - getSibling() - filterTree ", filterTree, ', operand ', operand)

    const operator = getExpressionOperator(filterTree, operand)

    if (getLeftOperand(operator) === operand) {
        return getRightOperand(operator)
    }

    return getLeftOperand(operator);
}

export function getNextOperand(filterTree, operand) {
    console.log("operandUtilService - getNextOperand() - filterTree ", filterTree, ', operand ', operand)

    let nextOperand;
    const operator = getParent(filterTree, operand)

    traverseDF(filterTree,
        function (node) {
            if (deepEqual(getOperator(filterTree, node), operator)) {
                nextOperand = node;
            }
        })

    return nextOperand;
}

export function insertAfter(filterTree, operand, operator, beforeOperand) {
    let beforeOperandCopy = deepcopy(beforeOperand)
    replace(
        filterTree,
        createInsertExpression(filterTree, deepcopy(operand), operator, beforeOperand),
        getInsertPosition(filterTree, beforeOperand)
    )

    return find(filterTree, beforeOperandCopy)

}

export function createInsertExpression(filterTree, operand, operator, beforeOperand) {

    return expression(
        deepcopy(beforeOperand),
        operator,
        getInsertExpressionRightOperator(filterTree, deepcopy(operand), beforeOperand)
    )
}

export function getInsertExpressionRightOperator(filterTree, operand, beforeOperand) {
    if (!isInSimpleExpression(filterTree, beforeOperand)) {
        return subexpression(
            filterTree,
            deepcopy(operand),
            getNextOperand(filterTree, beforeOperand)
        )
    }

    return operand;
}

export function getInsertPosition(filterTree, beforeOperand) {
    if (!isInSimpleExpression(filterTree, beforeOperand)) {
        return getExpressionOperator(filterTree, beforeOperand);
    }
    return beforeOperand;
}

export function subexpression(filterTree, operand, nextOperand) {
    const leftOperand = operand;
    const tempOperator = operator(getOperator(filterTree, nextOperand).value);
    let rightOperand = nextOperand;
    if (!isInSimpleExpression(filterTree, rightOperand)) {
        rightOperand = getExpressionOperator(filterTree, nextOperand)
    }
    return expression(leftOperand, tempOperator, rightOperand)
}

export function remove(filterTree, operand) {
    console.log("operandUtilService - remove() - filterTree ", filterTree, ', operand ', operand)
    if (!getSibilng(filterTree, operand)) {
        removeNode(filterTree, operand)
        return;
    }

    if (!isInSimpleExpression(filterTree, operand)) {
        const nextOperand = getNextOperand(filterTree, operand);

        if (nextOperand && nextOperand.value != "PAR") {
            swapOperators(filterTree, nextOperand, operand);
        }
    }

    replace(filterTree,
        getSibilng(filterTree, operand), getExpressionOperator(filterTree, operand))
}



export function getExpressionOperator(filterTree, operand) {
    return getParent(filterTree, operand)
}

export function swapOperands(filterTree, operand1, operand2) {
    swapNodes(operand1, operand2);
    swapOperators(filterTree, operand1, operand2);
}

export function isInSimpleExpression(filterTree, operand) {
    return isOperatorFromSimple(getOperator(filterTree, operand), operand)
}

export function getFirstLevelOperands(filterTree) {
    const operands = [] as any[];
    traverseDF(filterTree, function (node) {
        if (!getGroup(filterTree, node) && !isOperator(node)) {
            operands.push(node)
        }
    })
    return operands;
}
// #endregion




// #region operatorService.ts
export function getOperator(filterTree, operand) {
    if (!filterTree) throw new Error('filterTree cannot be undefined.');
    if (!operand) throw new Error('operand cannot be undefined.');

    let operator;

    traverseDF(filterTree, function (node) {
        if (isOperator(node) && isOperatorFrom(node, operand)) {
            operator = node;
        }
    })

    return operator;
}

export function swapOperators(filterTree, operand1, operand2) {
    let operator1 = getOperator(filterTree, operand1);
    let operator2 = getOperator(filterTree, operand2);

    if (!operator1) {
        if (operator2) {
            operator1 = operator(operator2.value)
        } else {
            operator1 = operator("AND");
        }
    }

    if (!operator2) {
        if (operator1) {
            operator2 = operator(operator1.value)
        } else {
            operator2 = operator("AND");
        }

    }

    swapNodePropertyValues(operator1, operator2, ["type", "value"])
}

export function isOperator(node) {
    return isANDOperator(node) || isOROperator(node);
}

export function isOperatorFrom(operator, operand) {
    return isOperatorFromSimple(operator, operand) || isOperatorFromComplex(operator, operand);
}

// ???
export function isOperatorFromComplex(operator, operand) {
    return !isSimpeExpressionOperator(operator) && getLeftOperand(getRightOperand(operator)) === operand; // ILLOGICAL PLACE
}

// ???
export function isOperatorFromSimple(operator, operand) {
    return isSimpeExpressionOperator(operator) && isRightOperand(operator, operand);
}

// ???
export function isRightOperand(operator, operand) {
    return getRightOperand(operator) === operand; // ILLOGICAL PLACE
}


export function isSimpeExpressionOperator(operator) {
    return !isOperator(getLeftOperand(operator)) && !isOperator(getRightOperand(operator));
}

export function getLeftOperand(operator) {
    if (isOperator(operator)) {
        return operator.childNodes[0];
    }
}

export function getRightOperand(operator) {
    if (isOperator(operator)) {
        return operator.childNodes[1];
    }
}

export function hasChildren(node) {
    return node.childNodes && node.childNodes.length > 0;
}


export function isANDOperator(node) {
    return node && node.value && node.value === 'AND';
}

export function isOROperator(node) {
    return node && node.value && node.value === 'OR';
}

export function isConst(node) {
    return node && node.type && node.type === 'NODE_CONST';
}

export function isPar(node) {
    return node && node.value === 'PAR';
}
// #endregion

// #region filterTreeFactoryService.ts
export function group(expression) {
    return { type: "NODE_OP", value: "PAR", childNodes: [expression] };
}

export function expression(leftOperand, operator, rightOperand) {
    if (!leftOperand) throw new Error('leftOperand cannot be undefined.');
    if (!operator) throw new Error('operator cannot be undefined.');
    if (!rightOperand) throw new Error('rightOperand cannot be undefined.');

    return { type: operator.type, value: operator.value, childNodes: [leftOperand, rightOperand] };
}

export function operator(value) {
    return { type: "NODE_OP", value: value, childNodes: [] };
}

export function filter(name) {
    return { type: "NODE_CONST", value: name, childNodes: [] };
}
// #endregion