<template>
    <div v-if="visible" :class="class" class="color-picker-container">
        <label v-if="label" class="kn-material-input-label p-mr-2">{{ $t(label) }}</label>
        <ColorPicker class="p-ml-auto" v-model="modelValue" :inline="settings.inline" :format="settings.format ?? 'rgb'" :disabled="disabled" @change="onChange"></ColorPicker>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import { IWidget } from '../../../../Dashboard'
import ColorPicker from 'primevue/colorpicker'

export default defineComponent({
    name: 'widget-editor-color-picker',
    components: { ColorPicker },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        property: { type: String, required: true },
        label: { type: String },
        class: { type: String },
        settings: { type: Object, required: true },
        initialValue: { type: String },
        itemIndex: { type: Number }
    },
    emits: [],
    data() {
        return {
            modelValue: null as any,
            visible: false,
            disabled: false
        }
    },
    async created() {
        this.loadValue()
        this.fieldIsDisabled()
        this.fieldIsVisible()
        this.setWatchers()
    },
    methods: {
        loadValue() {
            if ((this.initialValue || this.initialValue === '') && !this.property) {
                this.modelValue = this.initialValue as string
                this.visible = true
            } else if (!this.property) {
                this.modelValue = null
            } else {
                this.modelValue = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? null
            }

            console.log('TEEEEEEEEEST: ', typeof this.modelValue === 'string')
            if (typeof this.modelValue === 'string' && this.modelValue !== '') this.modelValue = this.getRGBColorFromString(this.modelValue)
            console.log('LOADED MODEL VALUE: ', this.modelValue)
        },
        onChange(event: any) {
            console.log('EVENT: ', event)
            if (!event.value) return
            if (this.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel, this.modelValue)
            }
        },
        fieldIsDisabled() {
            if (!this.settings.disabledCondition) return (this.disabled = false)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.disabledCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(this.widgetModel, this.itemIndex))
        },
        fieldIsVisible() {
            if (!this.settings.visibilityCondition) return (this.visible = true)
            const tempFunction = getModelProperty(this.widgetModel, this.settings.visibilityCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.visible = tempFunction(this.widgetModel))
        },
        setWatchers() {
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
        },
        getRGBColorFromString(color: string) {
            console.log('----- getRGBColorFromString: ', color)
            const temp = color
                ?.trim()
                ?.substring(4, color.length - 1)
                ?.split(',')
            console.log('TEMP: ', temp)
            if (temp) {
                return { r: temp[0], g: temp[1], b: temp[2] }
            } else return { r: 0, g: 0, b: 0 }
        }
    }
})
</script>

<style lang="scss" scoped>
.color-picker-container {
    border: 1px solid #c2c2c2;
    border-radius: 4px;
    padding: 0.5rem;
    display: flex;
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
    min-width: 300px;
}
</style>
