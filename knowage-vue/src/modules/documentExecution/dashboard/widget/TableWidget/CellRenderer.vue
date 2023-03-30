<template>
    <div class="custom-cell-container p-d-flex kn-height-full" :style="getCellStyle()">
        <div v-if="isColumnOfType('date')" class="custom-cell-label">{{ dateFormatter(params.value) }}</div>
        <div v-else-if="isColumnOfType('timestamp')" class="custom-cell-label">{{ dateTimeFormatter(params.value) }}</div>
        <div v-else-if="isIconColumn" class="custom-cell-label kn-cursor-pointer"><i :class="icon"></i></div>
        <div v-else class="custom-cell-label">{{ params.value }}</div>
        <span>{{ params.selectedColumn }}</span>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { getColumnConditionalStyles } from './TableWidgetHelper'
import helpersDecriptor from '../WidgetEditor/helpers/tableWidget/TableWidgetHelpersDescriptor.json'
import moment from 'moment'
import { getLocale } from '@/helpers/commons/localeHelper'
import { ITableWidgetVisualizationTypes } from '../../Dashboard'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object as any
        }
    },
    data() {
        return { helpersDecriptor }
    },
    computed: {
        isIconColumn() {
            return this.params?.colId === 'iconColumn'
        },
        icon() {
            if (!this.isIconColumn || !this.params.propWidget || !this.params.propWidget.settings.interactions) return ''

            return this.params.propWidget.settings.interactions.crossNavigation.icon
        }
    },
    watch: {
        params: {
            async handler() {},
            deep: true
        }
    },
    created() {
        this.getCellStyle()
    },
    methods: {
        getColumnStyle() {
            const columnStyles = this.params.propWidget.settings.style.columns
            let columnStyleString = null as any

            if (columnStyles.enabled) {
                columnStyleString = Object.entries(columnStyles.styles[0].properties)
                    .map(([k, v]) => `${k}:${v}`)
                    .join(';')

                columnStyles.styles.forEach((group) => {
                    if (group.target.includes(this.params.colId)) {
                        columnStyleString = Object.entries(group.properties)
                            .map(([k, v]) => `${k}:${v}`)
                            .join(';')
                    }
                })
            }

            return columnStyleString
        },
        getRowspanRowColor() {
            const rowStyles = this.params.propWidget.settings.style.rows
            if (rowStyles.alternatedRows && rowStyles.alternatedRows.enabled) {
                if (rowStyles.alternatedRows.oddBackgroundColor && this.params.node.rowIndex % 2 === 0) {
                    return `background-color: ${rowStyles.alternatedRows.oddBackgroundColor}`
                }
                if (rowStyles.alternatedRows.evenBackgroundColor && this.params.node.rowIndex % 2 != 0) {
                    return `background-color: ${rowStyles.alternatedRows.evenBackgroundColor}`
                }
            }
        },
        getConditionalStyle() {
            if (this.params.propWidget.settings.conditionalStyles.enabled) {
                return getColumnConditionalStyles(this.params.propWidget, this.params.colId, this.params.value, true)
            } else return null
        },
        getMultiselectStyle() {
            if (this.params.colDef.colId === 'indexColumn') return
            const selection = this.params.propWidget.settings.interactions.selection
            const celectedCellValues = this.params.multiSelectedCells
            const selectedColumn = this.params.selectedColumnArray[0]

            if (selection.enabled && selection.multiselection.enabled) {
                const multiselectStyle = Object.entries(selection.multiselection.properties)
                    .map(([k, v]) => `${k}:${v}`)
                    .join(';')
                if (selection.modalColumn && selection.modalColumn == this.params.colDef.colId && celectedCellValues.includes(this.params.value)) {
                    return multiselectStyle
                } else if (!selection.modalColumn && selectedColumn == this.params.colDef.field && celectedCellValues.includes(this.params.value)) {
                    return multiselectStyle
                }
            } else return null
        },
        getCellStyle() {
            if (this.isIconColumn) return 'justify-content: center; align-items: center;'
            const defaultColumnStyle = Object.entries(this.helpersDecriptor.defaultColumnStyles.styles[0].properties)
                .map(([k, v]) => `${k}:${v}`)
                .join(';')
            if (this.getMultiselectStyle()) return this.getMultiselectStyle()
            if (this.getConditionalStyle()) return this.getConditionalStyle()
            if (this.getColumnStyle() != defaultColumnStyle) return this.getColumnStyle()
            if (this.getRowspanRowColor()) return this.getRowspanRowColor()
        },
        isColumnOfType(columnType: string) {
            const widgetColumns = this.params.propWidget.columns
            const cellColumnId = this.params.colId
            const cellColumn = widgetColumns.find(({ id }) => id === cellColumnId)

            return cellColumn?.type.toLowerCase().includes(columnType)
        },
        getColumnVisualizationType(colId) {
            const visTypes = this.params.propWidget.settings.visualization.visualizationTypes as ITableWidgetVisualizationTypes

            const colVisType = visTypes.types.find((visType) => visType.target.includes(colId))
            if (colVisType) return colVisType
            else return visTypes.types[0]
        },
        dateFormatter(params) {
            const visType = this.getColumnVisualizationType(this.params.colId)

            const isDateValid = moment(params, 'DD/MM/YYYY').isValid()
            return isDateValid
                ? moment(params, 'DD/MM/YYYY')
                      .locale(getLocale(true))
                      .format(visType?.dateFormat || 'LL')
                : params
        },
        dateTimeFormatter(params) {
            const visType = this.getColumnVisualizationType(this.params.colId)

            const isDateValid = moment(params, 'DD/MM/YYYY HH:mm:ss.SSS').isValid()
            return isDateValid
                ? moment(params, 'DD/MM/YYYY HH:mm:ss.SSS')
                      .locale(getLocale(true))
                      .format(visType?.dateFormat || 'LLL')
                : params
        }
    }
})
</script>
