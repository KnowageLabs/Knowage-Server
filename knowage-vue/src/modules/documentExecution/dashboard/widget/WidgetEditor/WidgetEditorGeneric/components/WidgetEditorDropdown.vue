<template>
    <div :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <Dropdown class="kn-material-input" v-model="modelValue" :options="options" :optionLabel="settings.optionLabel" :optionValue="settings.optionValue" @change="onChange"></Dropdown>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'widget-editor-dropdown',
    components: {},
    props: { value: { type: String }, label: { type: String }, options: { type: Array }, settings: { type: Object, required: true } },
    emits: ['change'],
    data() {
        return {
            modelValue: '' as any
        }
    },
    watch: {
        value() {
            this.loadValue()
        }
    },
    async created() {
        this.loadValue()
    },
    methods: {
        loadValue() {
            this.modelValue = this.value ?? ''
        },
        onChange() {
            this.$emit('change', this.modelValue)
        }
    }
})
</script>
