import { iParameter, } from './KnParameterSidebar'

export function setVisualDependency(loadedParameters: { filterStatus: iParameter[], isReadyForExecution: boolean }, parameter: iParameter) {
    if (parameter.dependencies.visual.length !== 0) {
        parameter.dependencies.visual.forEach((dependency: any) => {
            const index = loadedParameters.filterStatus.findIndex((param: any) => {
                return param.urlName === dependency.parFatherUrlName
            })
            if (index !== -1) {
                const tempParameter = loadedParameters.filterStatus[index]
                parameter.dependsOnParameters ? parameter.dependsOnParameters.push(tempParameter) : (parameter.dependsOnParameters = [tempParameter])
                tempParameter.dependentParameters ? tempParameter.dependentParameters.push(parameter) : (tempParameter.dependentParameters = [parameter])
            }
        })
    }
}

export function updateVisualDependency(parameter: iParameter) {
    parameter.dependentParameters?.forEach((dependentParameter: iParameter) => visualDependencyCheck(dependentParameter, parameter))
}

export function visualDependencyCheck(parameter: iParameter, changedParameter: any) {
    let showOnPanel = 'false'
    for (let i = 0; i < parameter.dependencies.visual.length; i++) {
        let itemFound = false
        const visualDependency = parameter.dependencies.visual[i]

        if (parameter.dependsOnParameters) {
            const index = parameter.dependsOnParameters.findIndex((el: any) => {
                return el.urlName === visualDependency.parFatherUrlName
            })
            const parentParameter = parameter.dependsOnParameters[index]

            for (let i = 0; i < parentParameter?.parameterValue.length; i++) {
                if (parentParameter.parameterValue[i].value === visualDependency.compareValue) {
                    if (changedParameter.urlName === visualDependency.parFatherUrlName) {
                        parameter.label = visualDependency.viewLabel
                    }
                    itemFound = true
                    break
                }
            }

            if (visualDependency.operation === 'not contains') {
                if (itemFound) {
                    showOnPanel = 'false'
                    break;

                } else {
                    showOnPanel = 'true'
                }
            } else {
                showOnPanel = itemFound ? 'true' : 'false'
            }

        }
    }
    parameter.showOnPanel = showOnPanel
}