<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="handleSubmit" />
            <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <div class="p-col-9">
            <name-card :selectedAlert="selectedAlert" :listeners="listeners" @valueChanged="updateAlert" :vcomp="v$.alert"></name-card>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iAlert, iListener } from './Alert'
import axios from 'axios'
import useValidate from '@vuelidate/core'
import NameCard from './Cards/NameCard.vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import alertValidationDescriptor from './AlertValidationDescriptor.json'
export default defineComponent({
    name: 'alert-details',
    components: { NameCard },
    props: {
        id: {
            type: String,
            required: false
        }
    },
    watch: {
        async id() {
            await this.checkId()
        }
    },
    created() {
        if (this.id) {
            this.loadAlert()
        }
        this.loadListener()
    },
    data() {
        return {
            selectedAlert: {} as iAlert,
            listeners: [] as iListener[],
            alertValidationDescriptor: alertValidationDescriptor,
            v$: useValidate() as any
        }
    },
    validations() {
        return {
            alert: createValidations('alert', alertValidationDescriptor.validations.alert)
        }
    },
    methods: {
        async loadAlert() {
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/' + this.id + '/load')
                .then((response) => {
                    this.selectedAlert = response.data
                })
                .finally(() => console.log('selected', this.selectedAlert))
        },
        async loadListener() {
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/listListener')
                .then((response) => {
                    this.listeners = response.data
                })
                .finally(() => console.log('selected', this.selectedAlert))
        },
        updateAlert(event) {
            this.selectedAlert[event.fieldName] = event.value
            //this.setDirty()
        },
        handleSubmit() {
            console.log(this.selectedAlert)
        },
        async checkId() {
            if (this.id) {
                await this.loadAlert()
            } else {
                this.selectedAlert = { id: null }
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
