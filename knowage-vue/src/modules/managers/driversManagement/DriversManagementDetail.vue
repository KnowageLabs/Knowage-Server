<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #left>{{ title }} </template>
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center" style="overflow:auto">
        {{ driver }}
        <DriversDetailCard :selectedDriver="driver"></DriversDetailCard>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import DriversDetailCard from './DriversDetailCard.vue'
//import { createValidations } from '@/helpers/commons/validationHelper'
//import axios from 'axios'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
//import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'metadata-management-detail',
    components: { DriversDetailCard },
    props: {
        selectedDriver: {
            type: Object,
            required: false
        }
    },
    computed: {
        buttonDisabled(): any {
            return false //this.v$.$invalid
        },
        title(): any {
            return this.driver.id ? this.driver.name : this.$t('common.new')
        }
    },
    data() {
        return {
            driver: {} as any
        }
    },
    watch: {
        selectedDriver() {
            //this.v$.$reset()
            this.driver = { ...this.selectedDriver } as any
        }
    },
    mounted() {
        if (this.driver) {
            this.driver = { ...this.selectedDriver } as any
        }
    },

    methods: {
        handleSubmit() {},
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
