<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="handleSubmit" />
            <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <name-card :selectedAlert="selectedAlert" :listeners="listeners" @valueChanged="updateAlert" :vcomp="v$.selectedAlert"></name-card>
        <events-card :selectedAlert="selectedAlert" @valueChanged="updateAlert"></events-card>
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
import EventsCard from './Cards/EventsCard.vue'
export default defineComponent({
    name: 'alert-details',
    components: { NameCard, EventsCard },
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
        } else {
            this.selectedAlert = { id: null, singleExecution: false }
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
            selectedAlert: createValidations('alert', alertValidationDescriptor.validations.alert)
        }
    },
    methods: {
        async loadAlert() {
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/' + this.id + '/load')
                .then((response) => {
                    this.selectedAlert = { ...response.data }
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
            console.log(event)
            if (event.fieldName == 'singleExecution') {
                this.selectedAlert.singleExecution = !this.selectedAlert.singleExecution
            } else {
                this.selectedAlert[event.fieldName] = event.value
            }

            //this.setDirty()
        },
        handleSubmit() {
            console.log(this.selectedAlert)
            //console.log('>>>>>', this.v$)
        },
        async checkId() {
            if (this.id) {
                await this.loadAlert()
            } else {
                this.selectedAlert = { id: null, singleExecution: false }
            }
            this.v$.$reset()
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
