<template>
    <div v-if="visible" :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <InputText :class="inputClass" class="kn-material-input p-inputtext-sm" :type="settings.type ?? 'text'" v-model="modelValue" :maxLength="settings.maxLength" :disabled="disabled" @input="onInput" @change="onChange" @blur="$emit('blur')" />
        <small v-if="settings.hint">{{ $t(settings.hint, { placeholder: settings.hintPlaceholder }) }}</small>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import { IWidget } from '../../../../Dashboard'

export default defineComponent({
    name: 'widget-editor-input-text',
    components: {},
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        property: { type: String, required: true },
        label: { type: String },
        class: { type: String },
        inputClass: { type: String },
        settings: { type: Object, required: true }
    },
    emits: ['input', 'change', 'blur'],
    data() {
        return {
            modelValue: '' as string,
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
        onInput() {
            this.$emit('input', this.modelValue)
            this.callOnUpdateFunction()
        },
        onChange() {
            this.$emit('change', this.modelValue)
            this.callOnUpdateFunction()
        },
        fieldIsDisabled() {
            if (!this.settings.disabledCondition) return (this.disabled = false)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.disabledCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(this.widgetModel))
        },
        fieldIsVisible() {
            if (!this.settings.visibilityCondition) return (this.visible = true)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.visibilityCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.visible = tempFunction(this.widgetModel))
        },
        callOnUpdateFunction() {
            if (this.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel)
            }
        }
    }
})
</script>
