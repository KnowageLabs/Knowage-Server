<template>
    <div :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2"> {{ $t(label) }}</label>
        <InputText :class="inputClass" :type="type" v-model="modelValue" :maxLength="maxLength" @input="onInput" @change="onChange" @blur="$emit('blur')" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'widget-editor-input-text',
    components: {},
    props: { value: { type: String }, label: { type: String }, class: { type: String }, inputClass: { type: String }, type: { type: String }, maxLength: { type: String } },
    emits: ['input', 'change', 'blur'],
    data() {
        return {
            modelValue: '' as string
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
        onInput() {
            this.$emit('input', this.modelValue)
        },
        onChange() {
            this.$emit('change', this.modelValue)
        }
    }
})
</script>
