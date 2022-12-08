<template>
    <div ref="knowageStyleIcon" class="color-picker-container">
        <label v-if="label" class="kn-material-input-label p-mr-2">{{ $t(label) }}</label>
        <Button :disabled="disabled" class="kn-button kn-button--primary click-outside" :style="`background-color:${color}; padding: 0`" @click="colorPickerVisible = !colorPickerVisible"></Button>
        <ColorPicker v-if="colorPickerVisible" class="dashboard-color-picker click-outside" theme="light" :color="color" :sucker-hide="true" @changeColor="changeColor" />
    </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import 'vue-color-kit/dist/vue-color-kit.css'
import { useClickOutside } from './styleToolbar/useClickOutside'
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
            colorPickTimer: null as any,
            useClickOutside
        }
    },
    setup() {
        const knowageStyleIcon = ref(null)
        let colorPickerVisible = ref(false)
        let contextMenuVisible = ref(false)
        useClickOutside(knowageStyleIcon, () => {
            colorPickerVisible.value = false
            contextMenuVisible.value = false
        })
        return { colorPickerVisible, contextMenuVisible, knowageStyleIcon }
    },
    created() {
        this.loadValue()
    },
    methods: {
        changeColor(color) {
            const { r, g, b, a } = color.rgba

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
        }
    }
})
</script>

<style lang="scss">
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
    position: absolute;
    right: 45px;
    width: 220px !important;
}
</style>
