<template>
    <div class="custom-cell-container p-d-flex" :style="getColumnConditionalStyles()">
        <div class="custom-cell-label">{{ params.value }}</div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            rowStyles: '' as string
        }
    },
    mounted() {
        // if (this.params) console.log('\n \n cell RENDERER PARAMS \n', this.params)
        this.getColumnConditionalStyles()
    },
    methods: {
        setCellStyle() {},
        getRowStyle() {
            var rowStyles = this.params.propWidget.settings.style.rows
            var rowIndex = this.params.node.rowIndex

            if (rowStyles.alternatedRows && rowStyles.alternatedRows.enabled) {
                if (rowStyles.alternatedRows.oddBackgroundColor && rowIndex % 2 === 0) {
                    return { background: rowStyles.alternatedRows.oddBackgroundColor }
                }
                if (rowStyles.alternatedRows.evenBackgroundColor && rowIndex % 2 != 0) {
                    return { background: rowStyles.alternatedRows.evenBackgroundColor }
                }
            } else return false
        },
        getColumnStyle() {
            var columnStyles = this.params.propWidget.settings.style.columns
            var columnStyleString = null as any
            columnStyleString = Object.entries(columnStyles[0].properties)
                .map(([k, v]) => `${k}:${v}`)
                .join(';')

            columnStyles.forEach((group) => {
                if (group.target.includes(this.params.colId)) {
                    columnStyleString = Object.entries(group.properties)
                        .map(([k, v]) => `${k}:${v}`)
                        .join(';')
                }
            })

            return columnStyleString
        },
        getColumnConditionalStyles() {
            var conditionalStyles = this.params.propWidget.settings.conditionalStyles
            var styleString = '' as any

            if (conditionalStyles.enabled) {
                var columnConditionalStyles = conditionalStyles.conditions.filter((condition) => condition.target.includes(this.params.colId))

                if (columnConditionalStyles.length > 0) console.log(`cond styles ---- ${this.params.colId}`, columnConditionalStyles)

                if (columnConditionalStyles.length > 0 && this.formatCondition(columnConditionalStyles[0].condition)) {
                    styleString = Object.entries(columnConditionalStyles[0].properties)
                        .map(([k, v]) => `${k}:${v}`)
                        .join(';')
                }

                return styleString
            }

            return false
        },
        formatCondition(condition) {
            var fullfilledCondition = false
            switch (condition.operator) {
                case '==':
                    fullfilledCondition = this.params.value == condition.value
                    break
                case '>=':
                    fullfilledCondition = this.params.value >= condition.value
                    break
                case '<=':
                    fullfilledCondition = this.params.value <= condition.value
                    break
                case 'IN':
                    fullfilledCondition = condition.value.split(',').indexOf(this.params.value) != -1
                    break
                case '>':
                    fullfilledCondition = this.params.value > condition.value
                    break
                case '<':
                    fullfilledCondition = this.params.value < condition.value
                    break
                case '!=':
                    fullfilledCondition = this.params.value != condition.value
                    break
            }

            return fullfilledCondition
        },
        getRowspanStyle() {}
    }
})
</script>
