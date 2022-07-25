<template>
    <div :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <InputText :class="inputClass" :type="settings.type ?? 'text'" v-model="modelValue" :maxLength="maxLength" :disabled="disabled" @input="onInput" @change="onChange" @blur="$emit('blur')" />
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
        maxLength: { type: String },
        settings: { type: Object, required: true }
    },
    emits: ['input', 'change', 'blur'],
    data() {
        return {
            modelValue: '' as string,
            disabled: false
        }
    },
    watch: {},
    async created() {
        this.loadValue()
        this.$watch('widgetModel.' + this.property, () => this.loadValue(), { deep: true })
        if (this.settings.watchers) {
            for (let i = 0; i < this.settings.watchers.length; i++) {
                console.log('TEMP: ', this.settings.watchers[i])
                this.$watch('widgetModel.' + this.settings.watchers[i], () => this.fieldIsDisabled(), { deep: true })
            }
        }
    },
    methods: {
        loadValue() {
            this.modelValue = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? ''

            this.fieldIsDisabled()
        },
        onInput() {
            this.$emit('input', this.modelValue)
            if (this.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel)
            }
        },
        onChange() {
            this.$emit('change', this.modelValue)
        },
        fieldIsDisabled() {
            console.log(' >>>>>> fieldIsDisabled  1', this.settings.visibilityCondition)
            if (!this.settings.disabledCondition) return (this.disabled = false)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.disabledCondition, 'getValue', null)
            console.log(' >>>>>> fieldIsDisabled  2', tempFunction)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(this.widgetModel))
        }
    }
})
</script>
