<template>
    <div :class="class">
        <label v-if="label" class="kn-material-input-label p-mr-2">{{ $t(label) }}</label>
        <InputSwitch v-model="checked" :style="style" @change="onChange"></InputSwitch>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'widget-editor-input-switch',
    components: { InputSwitch },
    props: { initalValue: { type: Boolean }, label: { type: String }, class: { type: String }, style: { type: Object } },
    emits: ['change'],
    data() {
        return {
            checked: false
        }
    },
    async created() {
        this.checked = this.initalValue
    },
    methods: {
        onChange() {
            this.$emit('change', this.checked)
        }
    }
})
</script>
