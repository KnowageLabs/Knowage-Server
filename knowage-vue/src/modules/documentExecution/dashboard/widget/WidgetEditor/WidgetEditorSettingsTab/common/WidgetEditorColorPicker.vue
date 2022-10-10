<template>
    <div class="color-picker-container">
        <label v-if="label" class="kn-material-input-label p-mr-2">{{ $t(label) }}</label>
        <ColorPicker class="p-ml-auto" v-model="modelValue" :inline="false" :format="'rgb'" :disabled="disabled" @change="onChange"></ColorPicker>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import ColorPicker from 'primevue/colorpicker'
import { getRGBColorFromString } from '../../helpers/WidgetEditorHelpers'

export default defineComponent({
    name: 'widget-editor-color-picker',
    components: { ColorPicker },
    props: {
        initialValue: { type: String },
        label: { type: String },
        disabled: { type: Boolean }
    },
    emits: ['change'],
    data() {
        return {
            modelValue: null as any,
            color: '',
            colorPickTimer: null as any
        }
    },
    created() {
        this.loadValue()
    },
    methods: {
        loadValue() {
            this.modelValue = this.initialValue ? getRGBColorFromString(this.initialValue) : {}
            this.color = this.initialValue ?? ''
        },
        onChange(event: any) {
            if (this.colorPickTimer) {
                clearTimeout(this.colorPickTimer)
                this.colorPickTimer = null
            }
            this.colorPickTimer = setTimeout(() => {
                if (!this.modelValue) return
                this.color = `rgb(${this.modelValue.r}, ${this.modelValue.g}, ${this.modelValue.b})`
                this.$emit('change', this.color)
            }, 200)
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
    min-width: 100px;
}
</style>
