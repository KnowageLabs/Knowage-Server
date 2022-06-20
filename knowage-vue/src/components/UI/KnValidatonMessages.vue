<template>
    <div v-if="vComp.$invalid && vComp.$dirty" class="p-error">
        <small class="p-col-12" v-for="(error, index) of vComp.$errors" :key="index">
            {{ $t(this.specificTranslateKeys && this.specificTranslateKeys[error.$validator] ? this.specificTranslateKeys[error.$validator] : `${this.defaultMessageTranslateBasePath}.${error.$validator}`, { ...error.$params, ...additionalTranslateParams }) }}
        </small>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-validation-messages',
    props: {
        vComp: {
            type: Object,
            required: true
        },
        /*  If you need to add specific parametrs for translation which are not part of validator parameters. Use it rarly. */
        additionalTranslateParams: {
            type: Object,
            default: () => {}
        },
        /* This is used if there is a need for specific key for some param. Use it rarly. */
        specificTranslateKeys: Object,
        defaultMessageTranslateBasePath: {
            type: String,
            default: import.meta.env.VUE_APP_VALIDATION_MESSAGES_BASE_KEY
        }
    }
})
</script>

<style lang="scss"></style>
