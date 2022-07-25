<template>
    <div :class="class">
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
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, property: { type: String, required: true }, label: { type: String }, class: { type: String }, style: { type: Object }, settings: { type: Object, required: true } },
    emits: ['change'],
    data() {
        return {
            checked: false
        }
    },
    async created() {
        this.loadValue()
        this.$watch('widgetModel.' + this.property, () => this.loadValue(), { deep: true })
    },
    methods: {
        loadValue() {
            this.checked = getModelProperty(this.widgetModel, this.property, 'getValue', null) ?? false
        },
        onChange() {
            this.$emit('change', this.checked)
            if (this.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel)
            }
        }
    }
})
</script>
