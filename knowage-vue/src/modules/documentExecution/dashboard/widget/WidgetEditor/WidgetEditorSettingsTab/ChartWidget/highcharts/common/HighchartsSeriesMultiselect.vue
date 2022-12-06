<template>
    <MultiSelect v-model="modelValue" :options="options" :disabled="disabled" @change="$emit('change', $event)"> </MultiSelect>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'widget-editor-series-multiselect',
    components: { MultiSelect },
    props: {
        value: { type: Array },
        availableSeriesOptions: { type: Array as PropType<string[]>, required: true },
        disabled: { type: Boolean }
    },
    emits: ['change'],
    data() {
        return {
            modelValue: [] as string[]
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
            const targetOptions = [] as string[]
            this.modelValue.forEach((serieName: string) => {
                targetOptions.push(serieName)
            })
            return targetOptions.concat(this.availableSeriesOptions as any)
        }
    },
    methods: {
        loadValue() {
            this.modelValue = this.value as string[]
        }
    }
})
</script>
