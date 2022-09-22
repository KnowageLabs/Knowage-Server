<template>
    <div class="custom-cell-container p-d-flex kn-height-full" :style="rowStyles">
        <!-- <div class="custom-cell-container p-d-flex kn-height-full"> -->
        <div class="custom-cell-label">{{ params.value }}</div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { getColumnConditionalStyles } from './TableWidgetHelper'

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
        if (this.params.propWidget.settings.conditionalStyles.enabled) this.rowStyles = getColumnConditionalStyles(this.params.propWidget, this.params.colId, this.params.value, true)
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
        }
    }
})
</script>
