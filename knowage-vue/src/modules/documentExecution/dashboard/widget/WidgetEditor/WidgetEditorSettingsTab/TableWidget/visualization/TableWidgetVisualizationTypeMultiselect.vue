<template>
    <MultiSelect v-model="modelValue" :options="options" :optionLabel="optionLabel" :optionValue="optionsValue" :disabled="disabled" @change="onChange"> </MultiSelect>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-visualization-type-multiselect',
    components: { MultiSelect },
    props: { value: { type: Array }, availableTargetOptions: { type: Array, required: true }, widgetColumnsAliasMap: { type: Object, required: true }, allColumnsSelected: { type: Boolean }, optionLabel: { type: String }, optionsValue: { type: String }, disabled: { type: Boolean } },
    emits: ['change'],
    data() {
        return {
            modelValue: [] as any[]
        }
    },
    watch: {},
    created() {
        this.loadValue()
    },
    computed: {
        options() {
            const targetOptions = [] as (IWidgetColumn | { id: string; alias: string })[]
            if (!this.allColumnsSelected) targetOptions.push({ id: 'All Columns', alias: 'All Columns' })
            this.modelValue.forEach((target: string) => {
                const tempColumn = { id: target, alias: this.widgetColumnsAliasMap[target] }
                if (tempColumn) targetOptions.push(tempColumn)
            })
            targetOptions.concat(this.availableTargetOptions as any)
            return targetOptions
        }
    },
    methods: {
        loadValue() {
            this.modelValue = this.value as any[]
        },
        onChange(event: any) {
            console.log('MODEL VALUE: ', this.modelValue)
            console.log('EVENT: ', event.value)
            this.$emit('change', event)
        }
        //            if (this.checkIfAllColumnsSelected(visualizationType)) {
        //                 this.onAllColumnsSelected(visualizationType)
        // }
        //                     onAllColumnsSelected(visualizationType: ITableWidgetVisualizationType) {
        //             console.log('onAllColumnsSelected visualizationType: ', visualizationType)
        //             const forRemoval = visualizationType.target.filter((target: string) => target !== 'All Columns')
        //             this.onColumnsRemovedFromMultiselect(forRemoval)
        //             visualizationType.target = ['All Columns']
        //         },
    }
})
</script>
