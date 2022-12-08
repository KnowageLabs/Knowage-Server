<template>
    <div class="color-picker-container">
        <!-- {{ modelValue }} -->
        <label v-if="label" class="kn-material-input-label p-mr-2">{{ $t(label) }}</label>
        <!-- <ColorPicker class="p-ml-auto" v-model="modelValue" :inline="false" :format="'rgb'" :disabled="disabled" @change="onChange"></ColorPicker> -->
        <!-- <Button class="kn-button kn-button--primary" @click="colorPickerVisible = !colorPickerVisible"> {{ $t('common.close') }}</Button> -->
        <Button class="kn-button kn-button--primary" :style="`background-color:${color}; padding: 0`" @click="colorPickerVisible = !colorPickerVisible"></Button>
        <ColorPicker v-if="colorPickerVisible" class="dashboard-color-picker" theme="light" :color="color" :sucker-hide="true" @changeColor="changeColor" />
        <!-- <div style="width: 35px; height: 35px; background-color: orange">test</div> -->
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import ColorPicker from 'primevue/colorpicker'
import 'vue-color-kit/dist/vue-color-kit.css'

import { ColorPicker } from 'vue-color-kit'
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
            color: '#59c7f9',
            colorPickerVisible: false,
            colorPickTimer: null as any
        }
    },
    created() {
        this.loadValue()
    },
    mounted() {},
    methods: {
        changeColor(color) {
            const { r, g, b, a } = color.rgba
            // this.color = `rgba(${r}, ${g}, ${b}, ${a})`

            if (this.colorPickTimer) {
                clearTimeout(this.colorPickTimer)
                this.colorPickTimer = null
            }
            this.colorPickTimer = setTimeout(() => {
                if (!this.modelValue) return
                this.color = `rgba(${r}, ${g}, ${b}, ${a})`
                this.$emit('change', this.color)
            }, 200)
        },
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
.dashboard-color-picker {
    position: fixed;
    width: 220px !important;
    top: 7%;
}
</style>
