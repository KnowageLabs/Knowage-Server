<template>
    <MultiSelect v-model="modelValue" :options="options" :option-label="optionLabel" :option-value="optionsValue" :disabled="disabled" @change="$emit('change', $event)"> </MultiSelect>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'widget-editor-multiselect',
    components: { MultiSelect },
    props: { value: { type: Array }, availableTargetOptions: { type: Array, required: true }, widgetLayersNameMap: { type: Object, required: true }, optionLabel: { type: String }, optionsValue: { type: String }, disabled: { type: Boolean } },
    emits: ['change'],
    data() {
        return {
            modelValue: [] as any[]
        }
    },
    computed: {
        options() {
            const targetOptions = [] as { id: string; name: string }[]
            this.modelValue.forEach((target: string) => {
                const tempLayer = { id: this.widgetLayersNameMap[target], name: target }
                if (tempLayer) targetOptions.push(tempLayer)
            })
            const merged = this.mergeTargetOptionsWithAvailableTargetOptions(targetOptions)
            return Object.values(merged) ?? []
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
    methods: {
        loadValue() {
            this.modelValue = this.value as any[]
        },
        mergeTargetOptionsWithAvailableTargetOptions(targetOptions: { id: string; name: string }[]) {
            const merged = [...targetOptions, ...this.availableTargetOptions].reduce((acc: any, curr: any) => {
                if (!acc[curr.name]) {
                    acc[curr.name] = curr
                }
                return acc
            }, {}) as any
            return Object.values(merged)
        }
    }
})
</script>
