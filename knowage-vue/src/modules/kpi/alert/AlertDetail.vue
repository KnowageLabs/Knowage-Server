<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <Message v-if="expiredCard" severity="warn" :closable="true" :style="alertDescriptor.styles.message">
            {{ $t('kpi.alert.expiredWarning') }}
        </Message>
        <name-card :selectedAlert="selectedAlert" :listeners="listeners" @valueChanged="updateAlert" :vcomp="v$.selectedAlert"></name-card>
        <events-card :selectedAlert="selectedAlert" @valueChanged="updateAlert"></events-card>
        <KpiCard v-if="isListenerSelected && actionList?.length > 0" :selectedAlert="selectedAlert" :kpiList="kpiList" :actionList="actionList" @showDialog="onShowActionDialog($event)" @kpiLoaded="updateKpi" />
    </div>
    <Button @click="dialogVisiable = true">Add action</Button>
    <add-action-dialog :dialogVisible="dialogVisiable" :kpi="kpi" :selectedAction="selectedAction" @close="dialogVisiable = false" @add="addAction"></add-action-dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iAction, iAlert, iListener } from './Alert'
import { createValidations } from '@/helpers/commons/validationHelper'
import axios from 'axios'
import Message from 'primevue/message'
import useValidate from '@vuelidate/core'
import NameCard from './Cards/NameCard.vue'
import KpiCard from './Cards/KpiCard.vue'
import alertValidationDescriptor from './AlertValidationDescriptor.json'
import EventsCard from './Cards/EventsCard.vue'
import AddActionDialog from './addActionDialog/AddActionDialog.vue'
import alertDescriptor from './AlertDescriptor.json'

export default defineComponent({
    name: 'alert-details',
    components: { NameCard, EventsCard, AddActionDialog, KpiCard, Message },
    props: { id: { type: String, required: false } },
    watch: {
        async id() {
            await this.checkId()
        }
    },
    computed: {
        isListenerSelected(): any {
            if (!this.selectedAlert.alertListener || this.selectedAlert.alertListener === this.emptyObject) {
                return false
            }
            return true
        },
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    created() {
        if (this.id) {
            this.loadAlert()
        } else {
            this.selectedAlert = { id: null, singleExecution: false }
        }
        this.loadListener()
        this.loadKpiList()
        this.loadActionList()
    },
    data() {
        return {
            selectedAlert: {} as iAlert,
            listeners: [] as iListener[],
            jsonOptions: {} as any,
            actions: [] as any[],
            selectedAction: {} as iAction,
            kpiList: [] as any,
            actionList: [] as any,
            kpi: {} as any,
            emptyObject: {} as any,
            alertValidationDescriptor: alertValidationDescriptor,
            dialogVisiable: false,
            v$: useValidate() as any,
            alertDescriptor,
            expiredCard: false
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
                .finally(() => (this.expiredCard = this.selectedAlert.jobStatus == 'EXPIRED'))
        },
        async loadListener() {
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/listListener')
                .then((response) => {
                    this.listeners = response.data
                })
                .finally(() => console.log('selected', this.selectedAlert))
        },
        async loadKpiList() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listKpi').then((response) => {
                this.kpiList = [...response.data]
            })
        },
        async loadActionList() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/listAction').then((response) => {
                this.actionList = [...response.data]
            })
        },
        updateAlert(event) {
            console.log(event)
            if (event.fieldName == 'singleExecution') {
                this.selectedAlert.singleExecution = !this.selectedAlert.singleExecution
            } else {
                this.selectedAlert[event.fieldName] = event.value
            }
        },
        async handleSubmit() {
            if (this.selectedAlert.jsonOptions) {
                this.selectedAlert.jsonOptions.actions = this.selectedAlert.jsonOptions.actions.map((action: any) => {
                    return {
                        jsonActionParameters: JSON.stringify(action.jsonActionParameters),
                        idAction: action.idAction,
                        thresholdValues: action.thresholdValues
                    }
                })
            }
            this.selectedAlert.jsonOptions = JSON.stringify(this.selectedAlert.jsonOptions)
            console.log(this.selectedAlert)

            let operation = this.selectedAlert.id ? 'update' : 'insert'

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/save', this.selectedAlert).then((response) => {
                if (response.data.errors != undefined && response.data.errors.length > 0) {
                    this.$store.commit('setError', {
                        title: this.$t('kpi.alert.savingError'),
                        msg: response.data.errors[0].message
                    })
                } else {
                    this.$store.commit('setInfo', {
                        title: this.$t(this.alertDescriptor.operation[operation].toastTitle),
                        msg: this.$t(this.alertDescriptor.operation.success)
                    })
                    this.$emit('saved', response.data.id)
                }
            })
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
        },
        updateKpi(event) {
            this.kpi = event
        },
        addAction(action) {
            this.dialogVisiable = false
            const option = { ...action, idAction: +action.idAction, data: this.actionList.find((ac) => action.idAction == ac.id) }
            option['thresholdData'] = option.thresholdValues.map((thresholdId) => {
                return this.kpi.threshold.thresholdValues.find((threshold) => threshold.id == thresholdId)
            })
            this.selectedAlert.jsonOptions.actions.push(option)
        },
        onShowActionDialog(action) {
            this.selectedAction = action ? { ...action, idAction: +action.idAction } : {}

            this.dialogVisiable = true
        }
    }
})
</script>
