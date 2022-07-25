<template>
    <div :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <Dropdown class="kn-material-input" v-model="modelValue" :options="options" :optionLabel="settings.optionLabel ?? 'label'" :optionValue="settings.optionValue ?? 'value'" @change="onChange"></Dropdown>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import { IWidget } from '../../../../Dashboard'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-dropdown',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, property: { type: String, required: true }, label: { type: String }, options: { type: Array }, settings: { type: Object, required: true } },
    emits: ['change'],
    data() {
        return {
            modelValue: '' as any
        }
    },
    watch: {},
    async created() {
        this.loadValue()
        this.$watch('widgetModel.' + this.property, () => this.loadValue(), { deep: true })
    },
    methods: {
        loadValue() {
            this.modelValue = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? ''
        },
        onChange() {
            this.$emit('change', this.modelValue)
        }
    }
})
</script>
