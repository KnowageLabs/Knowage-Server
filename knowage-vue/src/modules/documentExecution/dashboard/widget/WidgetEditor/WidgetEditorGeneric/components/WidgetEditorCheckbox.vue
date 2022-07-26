<template>
    <div v-if="visible" :class="class" class="p-field-checkbox">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <Checkbox v-model="modelValue" :binary="settings.binary" :disabled="disabled" @change="onChange" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import { IWidget } from '../../../../Dashboard'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    name: 'widget-editor-checkbox',
    components: { Checkbox },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        property: { type: String, required: true },
        label: { type: String },
        class: { type: String },
        settings: { type: Object, required: true }
    },
    emits: ['change'],
    data() {
        return {
            modelValue: '' as any,
            disabled: false,
            visible: false
        }
    },
    watch: {},
    async created() {
        this.loadValue()
        this.$watch('widgetModel.' + this.property, () => this.loadValue(), { deep: true })
        if (this.settings.watchers) {
            for (let i = 0; i < this.settings.watchers.length; i++) {
                console.log('TEMP: ', this.settings.watchers[i])
                this.$watch(
                    'widgetModel.' + this.settings.watchers[i],
                    () => {
                        this.fieldIsDisabled()
                        this.fieldIsVisible()
                    },
                    { deep: true }
                )
            }
        }
    },
    methods: {
        loadValue() {
            this.modelValue = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? ''

            this.fieldIsDisabled()
            this.fieldIsVisible()
        },
        onChange() {
            this.$emit('change', this.modelValue)
            if (this.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel)
            }
        },
        fieldIsDisabled() {
            // console.log(' >>>>>> fieldIsDisabled  1', this.settings.visibilityCondition)
            if (!this.settings.disabledCondition) return (this.disabled = false)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.disabledCondition, 'getValue', null)
            // console.log(' >>>>>> fieldIsDisabled  2', tempFunction)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(this.widgetModel))
        },
        fieldIsVisible() {
            console.log(' >>>>>> fieldIsVisible INPUT 1', this.settings.visibilityCondition)
            if (!this.settings.visibilityCondition) return (this.visible = true)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.visibilityCondition, 'getValue', null)
            console.log(' >>>>>> fieldIsVisible INPUT 2', tempFunction(this.widgetModel))
            if (tempFunction && typeof tempFunction === 'function') return (this.visible = tempFunction(this.widgetModel))
        }
    }
})
</script>
