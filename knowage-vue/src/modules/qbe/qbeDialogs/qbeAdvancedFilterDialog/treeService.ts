const childProperty = 'childNodes';

export const filterTree = {}

export function contains(tree, nodeToFind) {
    var contains = false;
    traverseDF(tree, function (node) {

        if (node === nodeToFind) {
            contains = true
        }
    })

    return contains;
}

export function find(tree, toFind) {
    var equalNode;
    traverseDF(tree, function (node) {
        // TODO
        console.log(node + ' === ' + toFind)
        if (node === toFind) {
            equalNode = node;
        }
    })

    return equalNode;
}

// TODO Ubagovan
export function add(tree, node, parent) {
    nodeExistingCheck(tree, parent);
    //  parent[childProperty].unshift(node);
}


export function move(tree, source, destination) {

    nodeExistingCheck(tree, source);
    nodeExistingCheck(tree, destination);

    // angular.copy(source,destination)
    source = { ...destination }
}

export function swapNodes(node1, node2) {
    var temp = { ...node1 }
    node1 = { ...node2 }
    node2 = { ...temp }

}

export function swapNodePropertyValues(node1, node2, properties) {
    for (var i = 0; i < properties.length; i++) {
        var temp = node1[properties[i]];
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
        for (var i = 0; i < currentNode[childProperty].length; i++) {
            recurse(currentNode[childProperty][i]);
        }

    })(tree)
}

export function replace(tree, expression, node) {
    nodeExistingCheck(tree, node);
    // angular.copy(expression, node)
    node = { ...expression }
}

export function getParent(tree, child) {
    var parent;

    nodeExistingCheck(tree, child)

    // TODO
    traverseDF(tree, function (node) {

        if (findElementIndex(node[childProperty], child) > -1) parent = node;

    })
    //if(!parent)throw new Error('Parent does not exist.');

    return parent;

}


export function findElementIndex(array, element) {
    for (var i = 0; i < array.length; i++) {
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
    var equalNode;
    traverseDF(tree, function (node) {

        if (node.type == "NODE_CONST" && node.value == filterName) {
            equalNode = node;
        }
    })

    return equalNode;
}

export function removeInPlace(expression, filterName) {
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
