<template>
    <div class="custom-cell-container p-d-flex kn-height-full" :style="getConditionalStyle() ?? getColumnStyle()">
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
        return {}
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
        getConditionalStyle() {
            if (this.params.propWidget.settings.conditionalStyles.enabled) {
                return getColumnConditionalStyles(this.params.propWidget, this.params.colId, this.params.value, true)
            } else return null
        }
    }
})
</script>
