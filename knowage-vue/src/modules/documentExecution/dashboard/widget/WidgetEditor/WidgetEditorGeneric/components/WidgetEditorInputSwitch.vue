<template>
    <div v-if="visible" :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2">{{ $t(label) }}</label>
        <InputSwitch v-model="checked" :style="style" @change="onChange"></InputSwitch>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import { IWidget } from '../../../../Dashboard'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'widget-editor-input-switch',
    components: { InputSwitch },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        property: { type: String, required: true },
        label: { type: String },
        class: { type: String },
        style: { type: Object },
        settings: { type: Object, required: true },
        initialValue: { type: Boolean },
        itemIndex: { type: Number }
    },
    emits: ['change'],
    data() {
        return {
            checked: false,
            visible: false
        }
    },
    async created() {
        this.loadValue()
        this.fieldIsVisible()
        this.$watch(
            'widgetModel.' + this.property,
            () => {
                this.loadValue()
                this.fieldIsVisible()
            },
            { deep: true }
        )
    },
    methods: {
        loadValue() {
            if ((this.initialValue || this.initialValue === false) && !this.property) {
                this.checked = this.initialValue
                this.visible = true
            } else if (!this.property) {
                this.checked = false
            } else {
                this.checked = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? false
            }
        },
        onChange() {
            this.$emit('change', this.checked)
            if (this.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel)
            }
        },
        fieldIsVisible() {
            //
            if (!this.settings.visibilityCondition) return (this.visible = true)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.visibilityCondition, 'getValue', null)
            //
            if (tempFunction && typeof tempFunction === 'function') return (this.visible = tempFunction(this.widgetModel))
        }
    }
})
</script>
