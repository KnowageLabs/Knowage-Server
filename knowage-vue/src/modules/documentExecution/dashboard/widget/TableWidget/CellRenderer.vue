<template>
    <div class="custom-cell-container p-d-flex kn-height-full" :style="getCellStyle()">
        <div class="custom-cell-label">{{ params.value }} {{ params.data.span > 1 ? params.data.span : '' }}</div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { getColumnConditionalStyles } from './TableWidgetHelper'
import helpersDecriptor from '../WidgetEditor/helpers/tableWidget/TableWidgetHelpersDescriptor.json'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object
        }
    },
    data() {
        return { helpersDecriptor }
    },
    created() {
        this.getCellStyle()
    },
    methods: {
        getColumnStyle() {
            var columnStyles = this.params.propWidget.settings.style.columns
            var columnStyleString = null as any

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
            var rowStyles = this.params.propWidget.settings.style.rows
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
        getCellStyle() {
            var columnStyleString = Object.entries(this.helpersDecriptor.defaultColumnStyles.styles[0].properties)
                .map(([k, v]) => `${k}:${v}`)
                .join(';')

            // console.group('getCellStyle')
            // console.log('CONDITIONAL \n', this.getConditionalStyle())
            // console.log('COLUMN \n', this.getColumnStyle() == columnStyleString)
            // console.log('ROW \n', this.getRowspanRowColor())
            // console.groupEnd()

            if (this.getConditionalStyle()) return this.getConditionalStyle()
            if (this.getColumnStyle() != columnStyleString) return this.getColumnStyle()
            if (this.getRowspanRowColor()) return this.getRowspanRowColor()
        }
    }
})
</script>
