<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
//import { createValidations } from '@/helpers/commons/validationHelper'
//import axios from 'axios'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
//import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'metadata-management-detail',
    components: {},
    computed: {
        buttonDisabled(): any {
            return false //this.v$.$invalid
        }
    },
    methods: {
        handleSubmit() {},
        closeTemplate() {}
    }
})
</script>
