<template>
    <div :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <InputText :class="inputClass" :type="type" v-model="modelValue" :maxLength="maxLength" :disabled="disabled" @input="onInput" @change="onChange" @blur="$emit('blur')" />
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
        type: { type: String },
        maxLength: { type: String },
        disabled: { type: Boolean },
        settings: { type: Object, required: true }
    },
    emits: ['input', 'change', 'blur'],
    data() {
        return {
            modelValue: '' as string
        }
    },
    watch: {
        value: {
            handler() {
                this.loadValue()
            },
            deep: true
        }
    },
    async created() {
        this.loadValue()
        this.$watch('widgetModel.' + this.property, () => this.loadValue(), { deep: true })
    },
    methods: {
        loadValue() {
            this.modelValue = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? ''
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
        }
    }
})
</script>
