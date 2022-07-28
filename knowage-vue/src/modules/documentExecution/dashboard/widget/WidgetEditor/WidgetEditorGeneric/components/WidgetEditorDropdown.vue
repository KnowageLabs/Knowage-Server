<template>
    <div v-if="visible" :class="class" :style="style">
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
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        property: { type: String, required: true },
        class: { type: String },
        style: { type: String },
        label: { type: String },
        options: { type: Array },
        settings: { type: Object, required: true },
        visibilityCondition: { type: String },
        initialValue: { type: String }
    },
    emits: ['change'],
    data() {
        return {
            modelValue: '' as any,
            visible: false
        }
    },
    watch: {
        initialValue() {
            this.loadValue()
        }
    },
    async created() {
        this.loadValue()
        this.$watch(
            'widgetModel.' + this.property,
            () => {
                this.loadValue()
            },
            { deep: true }
        )
        if (this.settings.watchers) {
            for (let i = 0; i < this.settings.watchers.length; i++) {
                this.$watch('widgetModel.' + this.settings.watchers[i], () => this.fieldIsVisible(), { deep: true })
            }
        }
    },
    methods: {
        loadValue() {
            console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> LOAD VALUE CALLED")
            if ((this.initialValue || this.initialValue === '') && !this.property) {
                this.modelValue = this.initialValue
                this.visible = true
            } else if (!this.property) {
                this.modelValue = ''
            } else {
                this.modelValue = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? ''
            }

            this.fieldIsVisible()
        },
        onChange() {
            this.$emit('change', this.modelValue)
        },
        fieldIsVisible() {
            // console.log(' >>>>>> fieldIsVisible DROPDOWN 1', this.settings.visibilityCondition)
            if (!this.settings.visibilityCondition) return (this.visible = true)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.visibilityCondition, 'getValue', null)
            // console.log(' >>>>>> fieldIsVisible DROPDOWN 2', tempFunction(this.widgetModel))
            if (tempFunction && typeof tempFunction === 'function') return (this.visible = tempFunction(this.widgetModel))
        }
    }
})
</script>
