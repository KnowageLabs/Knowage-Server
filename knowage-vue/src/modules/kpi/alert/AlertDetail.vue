<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <name-card :selectedAlert="selectedAlert" :listeners="listeners" @valueChanged="updateAlert" :vcomp="v$.selectedAlert"></name-card>
        <events-card :selectedAlert="selectedAlert" @valueChanged="updateAlert"></events-card>
    </div>
    <Button @click="dialogVisiable = true">Add action</Button>
    <add-action-dialog :dialogVisible="dialogVisiable" @close="dialogVisiable = false"></add-action-dialog>
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
import AddActionDialog from './addActionDialog/AddActionDialog.vue'
export default defineComponent({
    name: 'alert-details',
    components: { NameCard, EventsCard, AddActionDialog },
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
            jsonOptions: {} as any,
            actions: [] as any[],
            alertValidationDescriptor: alertValidationDescriptor,
            dialogVisiable: false,
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
                    this.selectedAlert.jsonOptions = JSON.parse(this.selectedAlert.jsonOptions ? this.selectedAlert.jsonOptions : '')

                    if (this.selectedAlert.jsonOptions) {
                        this.selectedAlert.jsonOptions.actions = this.selectedAlert.jsonOptions.actions.map((action: any) => {
                            return {
                                jsonActionParameters: JSON.parse(action.jsonActionParameters),
                                idAction: action.idAction,
                                thresholdValues: action.thresholdValues
                            }
                        })
                    }
                    console.log('jsonParse', this.selectedAlert.jsonOptions)
                })
                .finally(() => console.log('actions', this.actions))
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
                this.selectedAlert = {
                    id: null,
                    singleExecution: false,
                    frequency: {
                        cron: { type: 'minute', parameter: { numRepetition: '1' } },
                        startDate: new Date().valueOf(),
                        endDate: null,
                        startTime: new Date().valueOf(),
                        endTime: ''
                    }
                }
            }
            this.v$.$reset()
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
