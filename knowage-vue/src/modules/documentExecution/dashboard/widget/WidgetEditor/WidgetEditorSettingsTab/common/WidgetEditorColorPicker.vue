<template>
    <div class="color-picker-container">
        <label v-if="label" class="kn-material-input-label p-mr-2">{{ $t(label) }}</label>
        <ColorPicker class="p-ml-auto" v-model="modelValue" :inline="false" :format="'rgb'" :disabled="disabled" @change="onChange"></ColorPicker>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import ColorPicker from 'primevue/colorpicker'

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
            color: ''
        }
    },
    created() {
        this.loadValue()
    },
    methods: {
        loadValue() {
            this.modelValue = this.initialValue ? this.getRGBColorFromString(this.initialValue) : {}
            this.color = this.initialValue ?? ''
        },
        onChange() {
            if (!this.modelValue) return
            this.color = `rgb(${this.modelValue.r}, ${this.modelValue.g}, ${this.modelValue.b})`
            this.$emit('change', this.color)
        },
        getRGBColorFromString(color: string) {
            const temp = color
                ?.trim()
                ?.substring(4, color.length - 1)
                ?.split(',')

            if (temp) {
                return { r: +temp[0], g: +temp[1], b: +temp[2] }
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
