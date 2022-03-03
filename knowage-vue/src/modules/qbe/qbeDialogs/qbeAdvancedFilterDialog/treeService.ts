const deepEqual = require('deep-equal')

const childProperty = 'childNodes';
const deepcopy = require('deepcopy');

let filterTree = {}

export function getFilterTree() {
    return filterTree
}

export function setFilterTree(expression) {
    filterTree = deepcopy(expression)
}

export function contains(tree, nodeToFind) {
    let contains = false;
    traverseDF(tree, function (node) {
        if (node === nodeToFind) {
            contains = true
        }
    })

    return contains;
}

export function find(tree, toFind) {
    let equalNode;
    traverseDF(tree, function (node) {
        if (deepEqual(node, toFind)) {

            equalNode = node;
        }
    })

    return equalNode;
}


export function add(tree, node, parent) {
    nodeExistingCheck(tree, parent);
    parent[childProperty].unshift(node);
}


export function move(tree, source, destination) {
    nodeExistingCheck(tree, source);
    nodeExistingCheck(tree, destination);

    const tempNode = find(tree, destination)
    tempNode.childNodes = source.childNodes
    tempNode.value = source.value
    tempNode.type = source.type

    if (!source.details) delete tempNode.details
    else tempNode.details = source.details
}

export function swapNodes(node1, node2) {
    const temp = { childNodes: node1.childNodes, value: node1.value, type: node1.type, details: node1.details }
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
    for (let i = 0; i < properties.length; i++) {
        const temp = node1[properties[i]];
        node1[properties[i]] = node2[properties[i]];
        node2[properties[i]] = temp;

    }

}



export function removeNode(tree, nodeToRemove) {
    getSiblings(tree, nodeToRemove).splice(getNodeToRemoveIndex(tree, nodeToRemove), 1);
}

export function traverseDF(tree, callback) {

    (function recurse(currentNode) {
        callback(currentNode);
        for (let i = 0; i < currentNode[childProperty].length; i++) {
            recurse(currentNode[childProperty][i]);
        }

    })(tree)
}

export function replace(tree, expression, node) {
    nodeExistingCheck(tree, node);

    const tempNode = find(tree, node)
    tempNode.childNodes = expression.childNodes
    tempNode.value = expression.value
    tempNode.type = expression.type

    if (!expression.details) delete tempNode.details
    else tempNode.details = expression.details
}

export function getParent(tree, child) {
    let parent;

    nodeExistingCheck(tree, child)

    traverseDF(tree, function (node) {

        if (findElementIndex(node[childProperty], child) > -1) parent = node;

    })

    return parent;

}


export function findElementIndex(array, element) {
    for (let i = 0; i < array.length; i++) {
        if (element === array[i]) {
            return i;
        }
    }
    return - 1;
}

export function getNodeToRemoveIndex(tree, nodeToRemove) {
    return findElementIndex(getSiblings(tree, nodeToRemove), nodeToRemove);
}

export function getSiblings(tree, node) {
    return getParent(tree, node)[childProperty];
}

export function nodeExistingCheck(tree, node) {
    if (!contains(tree, node)) {

        throw new Error('Node does not exist.');
    }
}

export function findByName(tree, filterName) {
    let equalNode;
    traverseDF(tree, function (node) {

        if (node.type == "NODE_CONST" && node.value == filterName) {
            equalNode = node;
        }
    })

    return equalNode;
}

export function removeInPlace(expression, filterName) {
    const currentNodeInExpression = findByName(expression, filterName);
    let parentOfCurrentNode = getParent(expression, currentNodeInExpression);
    let parentOfParent = null as any;

    try {
        parentOfParent = getParent(expression, parentOfCurrentNode);
    } catch (err) {
        console.log(err)
    }

    try {
        const siblings = getSiblings(expression, currentNodeInExpression);

        const indexOfCurrentNode = siblings.indexOf(currentNodeInExpression);
        siblings.splice(indexOfCurrentNode, 1)

        if (siblings.length == 1 && parentOfParent && parentOfParent.value == "PAR") {
            try {
                parentOfCurrentNode = parentOfParent;
            } catch (err2) {
                console.log(err2)
            }

        }

        if (expression.childNodes.length == 0) {
            expression = {}
        } else if (parentOfParent != null) {
            replace(expression, siblings[0], parentOfCurrentNode);
        }

    } catch (err) {
        expression = {}
    }

}
