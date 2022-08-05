<template>
    <div v-if="visible" :class="class" :style="style">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <Dropdown class="kn-material-input" v-model="modelValue" :options="options" :optionValue="settings.optionValue ?? 'value'" :disabled="disabled" @change="onChange">
            <template #value="slotProps">
                <div v-if="slotProps.value">
                    <span>{{ slotProps.value }}</span>
                </div>
                <span v-else>
                    {{ slotProps.placeholder }}
                </span>
            </template>
            <template #option="slotProps">
                <div>
                    <span>{{ settings.translateLabels ? $t(slotProps.option.label) : slotProps.option.label }}</span>
                </div>
            </template>
        </Dropdown>
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
        initialValue: { type: String },
        item: { type: Object },
        itemIndex: { type: Number }
    },
    emits: ['change'],
    data() {
        return {
            modelValue: '' as any,
            disabled: false,
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
        this.setWatchers()
    },
    methods: {
        loadValue() {
            if ((this.initialValue || this.initialValue === '') && !this.property) {
                this.modelValue = this.initialValue
                this.visible = true
            } else if (!this.property) {
                this.modelValue = ''
            } else {
                this.modelValue = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? ''
            }

            this.fieldIsVisible()
            this.fieldIsDisabled()
        },
        onChange() {
            this.$emit('change', this.modelValue)
            this.callOnUpdateFunction()
        },
        callOnUpdateFunction() {
            if (this.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel, this.item, this.itemIndex)
            }
        },
        fieldIsVisible() {
            if (!this.settings.visibilityCondition) return (this.visible = true)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.visibilityCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.visible = tempFunction(this.widgetModel))
        },
        fieldIsDisabled() {
            if (!this.settings.disabledCondition) return (this.disabled = false)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.disabledCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(this.widgetModel, this.itemIndex))
        },
        setWatchers() {
            this.$watch(
                'widgetModel.' + this.property,
                () => {
                    this.loadValue(), this.fieldIsDisabled()
                },
                { deep: true }
            )
            if (this.settings.watchers) {
                for (let i = 0; i < this.settings.watchers.length; i++) {
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
        }
    }
})
</script>
