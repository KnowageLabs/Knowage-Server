<template>
    <div>
        <span class="p-float-label knValidatedFieldContainer">
            <VeeField :as="as" :name="name" :modelValue="modelValue" :class="cssClass" v-bind="$attrs" @input="$emit('modelUpdate', $event)" :validateOnChange="true" />
            <label :class="cssClass + '-label'" :for="label">{{ $t(label) }}</label>
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
