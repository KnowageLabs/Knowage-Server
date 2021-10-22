<template>
    <div>
        <span class="knValidatedFieldContainer">
            <VeeField :as="as" :name="name" :label="label" :modelValue="modelValue" :class="cssClass" v-bind="$attrs" @input="$emit('modelUpdate', $event)" :validateOnChange="true" />
            <label class="kn-material-input-label" :for="label">{{ label }}</label>
        </span>
        <VeeErrorMessage :name="name" class="kn-text-error" />
    </div>
</template>

<script>
    import { defineComponent } from 'vue'

    export default defineComponent({
        name: 'kn-validated-field',

        props: {
            name: { type: String, required: true },
            label: { type: String, required: true },
            modelValue: { type: String, default: '' },
            as: { type: String, required: true },
            cssClass: { type: String, required: true }
        }
    })
</script>

<style lang="scss" scoped>
    .knValidatedFieldContainer {
        display: flex;
    }
</style>
