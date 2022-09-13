<template>
    <MultiSelect v-model="modelValue" :options="options" :optionLabel="optionLabel" :optionValue="optionsValue" :showToggleAll="false" :disabled="disabled" @change="onChange"> </MultiSelect>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-visualization-type-multiselect',
    components: { MultiSelect },
    props: { value: { type: Array }, availableTargetOptions: { type: Array, required: true }, widgetColumnsAliasMap: { type: Object, required: true }, allColumnsSelected: { type: Boolean }, optionLabel: { type: String }, optionsValue: { type: String }, disabled: { type: Boolean } },
    emits: ['change', 'allColumnsSelected'],
    data() {
        return {
            modelValue: [] as any[]
        }
    },
    watch: {
        value() {
            this.loadValue()
        }
    },
    created() {
        this.loadValue()
    },
    computed: {
        options() {
            let targetOptions = [] as (IWidgetColumn | { id: string; alias: string })[]
            if (!this.allColumnsSelected || this.modelValue[0] === 'All Columns') targetOptions.push({ id: 'All Columns', alias: 'All Columns' })
            if (this.modelValue[0] === 'All Columns') return targetOptions
            this.modelValue.forEach((target: string) => {
                const tempColumn = { id: target, alias: this.widgetColumnsAliasMap[target] }
                if (tempColumn && tempColumn.id !== 'All Columns') targetOptions.push(tempColumn)
            })
            targetOptions = targetOptions.concat(this.availableTargetOptions as any)

            return targetOptions
        }
    },
    methods: {
        loadValue() {
            this.modelValue = this.value as any[]
        },
        onChange(event: any) {
            if (this.checkIfAllColumnsSelected(event)) {
                this.onAllColumnsSelected(event)
            } else this.$emit('change', event)
        },
        checkIfAllColumnsSelected(event: any) {
            let selected = false
            for (let i = 0; i < event.value.length; i++) {
                if (event.value[i] === 'All Columns') {
                    selected = true
                    break
                }
            }
            return selected
        },
        onAllColumnsSelected() {
            this.modelValue = ['All Columns']
            this.$emit('allColumnsSelected')
        }
    }
})
</script>
