import { isConditionMet } from './TableWidgetHelper'

export default class CellRenderer {
    eGui!: HTMLSpanElement
    setStyle(style: any) {
        for (const property in style) {
            if (style[property]) this.eGui.style[property] = style[property]
        }
    }

    init(params) {
        this.eGui = document.createElement('span')

        let visType = {} as any
        //visualization types
        if (params.propWidget.settings.visualization.visualizationTypes.enabled) {
            const visualizationTypes = params.propWidget.settings.visualization.visualizationTypes.types
            //"all" target visualization types
            const all = visualizationTypes.filter((i) => i.target === 'all')[0]
            if (all) visType = all
            if (visualizationTypes.filter((i) => Array.isArray(i.target) && i.target.includes(params.colId))) {
                const types = visualizationTypes.filter((i) => Array.isArray(i.target) && i.target.includes(params.colId))
                for (const type of types) {
                    visType = { ...visType, ...type }
                }
            }
        }
        //conditional styling
        if (params.propWidget.settings.conditionalStyles?.enabled && params.propWidget.settings.conditionalStyles.conditions) {
            const columnConditions = params.propWidget.settings.conditionalStyles.conditions.filter((i) => i.target === params.colId)
            if (columnConditions.length > 0) {
                columnConditions.forEach((element) => {
                    if (isConditionMet(element.condition, params.value, params.dashboardVariables, params.dashboardDrivers)) {
                        this.setStyle(element.properties)
                    }
                })
            }
        }

        if (visType.type) {
            if (visType.type.toLowerCase() === 'text') this.eGui.innerHTML = `${visType.prefix}${params.value}${visType.suffix}`
            if (visType.type.toLowerCase() === 'bar') {
                const minValue = visType.min || 0
                const maxValue = visType.max || 100
                let percentage = Math.round(((params.value - minValue) / (maxValue - minValue)) * 100)
                if (percentage < 0) percentage = 0
                if (percentage > 100) percentage = 100
                this.eGui.innerHTML = `<div class="barContainer" style="background-color:${visType['background-color']};justify-content:${visType['alignment']}">
                                        <div class="innerBar" style="width:${percentage}%;background-color:${visType.color}"></div>
                                      </div>`
            }
        } else this.eGui.innerHTML = `${params.value}`
        //change the rendered element depending on type
    }

    getGui() {
        return this.eGui
    }

    refresh(params) {
        return false
    }
}
