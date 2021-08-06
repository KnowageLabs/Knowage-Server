<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #left>{{ title }} </template>
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center" style="overflow:auto">
        <DriversDetailCard :selectedDriver="driver" :types="types"></DriversDetailCard>
        <UseMode v-if="modes" :propModes="modes"></UseMode>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import DriversDetailCard from './DriversDetailCard.vue'
import UseMode from './useModes/UseMode.vue'
//import { createValidations } from '@/helpers/commons/validationHelper'
import axios from 'axios'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
//import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'metadata-management-detail',
    components: { DriversDetailCard, UseMode },
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
            driver: {} as any,
            types: [] as any[],
            modes: [] as any[]
        }
    },
    watch: {
        selectedDriver() {
            //this.v$.$reset()
            this.driver = { ...this.selectedDriver } as any
            this.getModes()
        }
    },
    mounted() {
        if (this.driver) {
            this.driver = { ...this.selectedDriver } as any
            this.getModes()
        }
        this.loadAll()
    },

    methods: {
        async getTypes() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '/domains/listValueDescriptionByType?DOMAIN_TYPE=PAR_TYPE').then((response) => (this.types = response.data))
        },
        async getModes() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers/' + this.driver.id + '/modes/').then((response) => (this.modes = response.data))
        },
        loadAll() {
            this.getTypes()
        },
        handleSubmit() {},
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
