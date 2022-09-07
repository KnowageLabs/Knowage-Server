<template>
    <MultiSelect v-model="modelValue" :options="options" :optionLabel="optionLabel" :optionValue="optionsValue" :disabled="disabled" @change="$emit('change', $event)"> </MultiSelect>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-visualization-type-multiselect',
    components: { MultiSelect },
    props: { value: { type: Array }, availableTargetOptions: { type: Array, required: true }, widgetColumnsAliasMap: { type: Object, required: true }, optionLabel: { type: String }, optionsValue: { type: String }, disabled: { type: Boolean } },
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
            this.modelValue.forEach((target: string) => {
                const tempColumn = { id: target, alias: this.widgetColumnsAliasMap[target] }
                if (tempColumn) targetOptions.push(tempColumn)
            })
            return targetOptions.concat(this.availableTargetOptions as any)
        }
    },
    methods: {
        loadValue() {
            this.modelValue = this.value as any[]
        }
    }
})
</script>
