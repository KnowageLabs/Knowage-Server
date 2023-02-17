<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary dataPreparationSaveDialog" :visible="visibility" footer="footer" :header="$t('managers.workspaceManagement.dataPreparation.savePreparedDataset')" :closable="false" modal>
        <div class="p-grid p-m-0">
            <div class="p-col-12">
                <div class="p-d-flex">
                    <span class="p-float-label kn-flex p-mr-2">
                        <InputText
                            v-model.trim="v$.preparedDataset.name.$model"
                            class="kn-material-input"
                            type="text"
                            :disabled="!isFirstSave"
                            :class="{
                                'p-invalid': v$.preparedDataset.name.$invalid
                            }"
                            max-length="100"
                            @change="touched = true"
                        />
                        <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.name') }}</label>
                        <KnValidationMessages
                            :v-comp="v$.preparedDataset.name"
                            :additional-translate-params="{
                                fieldName: $t('managers.configurationManagement.headers.name')
                            }"
                        ></KnValidationMessages>
                    </span>
                </div>

                <span class="p-float-label">
                    <Textarea
                        v-model.trim="v$.preparedDataset.description.$model"
                        class="kn-material-input p-mb-1"
                        type="text"
                        :disabled="!isFirstSave"
                        :class="{
                            'p-invalid': v$.preparedDataset.description.$invalid
                        }"
                        rows="3"
                        max-length="10000"
                        @blur="touched = true"
                    />
                    <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.description') }}</label>
                    <KnValidationMessages
                        :v-comp="v$.preparedDataset.description"
                        :additional-translate-params="{
                            fieldName: $t('managers.configurationManagement.headers.description')
                        }"
                    ></KnValidationMessages>
                </span>
            </div>
            <div class="schedulerContainer">
                <KnScheduler
                    class="p-m-1"
                    :cron-expression="currentCronExpression"
                    :cron-expression-type="cronExpressionType"
                    :descriptor="schedulerDescriptor"
                    :logs-visible="false"
                    :schedulation-enabled="schedulationEnabled"
                    :schedulation-paused="schedulationPaused"
                    @touched="touched = true"
                    @update:schedulationPaused="updateSchedulationPaused"
                    @update:schedulationEnabled="updateSchedulationEnabled"
                    @update:currentCronExpression="updateCurrentCronExpression"
                    @update:cronExpressionType="updateCronExpressionType"
                />
            </div>
        </div>
        <template #footer>
            <Button class="kn-button--secondary" :label="$t('common.cancel')" @click="cancel" />

            <Button v-t="'common.save'" class="kn-button--primary" :disabled="saveButtonDisabled" @click="savePreparedDataset()" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'

import { createValidations } from '@/helpers/commons/validationHelper'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import Textarea from 'primevue/textarea'
import DataPreparationDescriptor from './DataPreparationDescriptor.json'
import useValidate from '@vuelidate/core'
import DataPreparationValidationDescriptor from './DataPreparationValidationDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import { IDataPreparationDataset, IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'

import dataPreparationMonitoringDescriptor from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoringDescriptor.json'
import KnScheduler from '@/components/UI/KnScheduler/KnScheduler.vue'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'data-preparation-detail-save-dialog',
    components: { Dialog, KnScheduler, KnValidationMessages, Textarea },
    props: {
        originalDataset: {} as any,
        config: {} as any,
        columns: [] as PropType<IDataPreparationColumn[]>,
        instanceId: {} as any,
        processId: {} as any,
        preparedDsMeta: {} as any,
        visibility: Boolean
    },
    emits: ['update:visibility', 'update:instanceId', 'update:processId'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            descriptor: DataPreparationDescriptor,
            preparedDataset: {} as IDataPreparationDataset,
            v$: useValidate() as any,
            validationDescriptor: DataPreparationValidationDescriptor,
            schedulerDescriptor: dataPreparationMonitoringDescriptor,
            currentCronExpression: '',
            isFirstSave: true,
            touched: false,
            schedulationPaused: false,
            schedulationEnabled: false,
            cronExpressionType: ''
        }
    },
    computed: {
        saveButtonDisabled(): any {
            return this.v$.$invalid || !this.preparedDataset.name
        }
    },

    watch: {
        preparedDsMeta: {
            handler() {
                if (Object.keys(this.preparedDsMeta).length > 0) {
                    this.preparedDataset = this.preparedDsMeta
                    this.cronExpressionType = this.preparedDsMeta.config?.type
                    this.currentCronExpression = this.preparedDsMeta.config?.cron ? this.preparedDsMeta.config.cron : ''

                    this.schedulationPaused = this.preparedDsMeta.config?.schedulationPaused || false

                    this.schedulationEnabled = this.preparedDsMeta.config?.cron ? true : false
                }
            },
            deep: true
        }
    },
    updated() {
        if (this.processId && this.processId != '') this.isFirstSave = false
    },

    validations() {
        return {
            preparedDataset: createValidations('preparedDataset', this.validationDescriptor.validations.configuration)
        }
    },

    created() {
        this.loadTranslations()
    },
    methods: {
        savePreparedDataset(): void {
            const processDefinition = this.createProcessDefinition()
            this.saveOrUpdateProcess(processDefinition).then(
                (response: AxiosResponse<any>) => {
                    const processId = response.data.id
                    this.$emit('update:processId', processId)
                    const datasetDefinition = this.createDatasetDefinition()
                    this.saveOrUpdateInstance(processId, datasetDefinition).then(
                        (response: AxiosResponse<any>) => {
                            this.$emit('update:instanceId', response.data.id)
                            this.store.setInfo({ title: 'Saved successfully' })
                        },
                        () => {
                            this.store.setError({ title: 'Save error', msg: 'Cannot add process instance' })
                        }
                    )
                },
                () => {
                    this.store.setError({ title: 'Save error', msg: 'Cannot create process' })
                }
            )
            this.resetAndClose()
        },
        saveOrUpdateProcess(processDefinition) {
            if (this.processId && this.processId != '') return this.$http.put(import.meta.env.VITE_DATA_PREPARATION_PATH + `1.0/process/${this.processId}`, processDefinition)
            else return this.$http.post(import.meta.env.VITE_DATA_PREPARATION_PATH + '1.0/process', processDefinition)
        },
        saveOrUpdateInstance(processId, datasetDefinition) {
            if (this.instanceId && this.instanceId != '') return this.$http.post(import.meta.env.VITE_DATA_PREPARATION_PATH + `1.0/instance/${this.instanceId}`, datasetDefinition)
            else return this.$http.post(import.meta.env.VITE_DATA_PREPARATION_PATH + '1.0/process/' + processId + '/instance', datasetDefinition)
        },
        createDatasetDefinition() {
            const toReturn = {}
            toReturn['config'] = {}
            toReturn['config']['paused'] = this.schedulationPaused

            if (this.schedulationEnabled) {
                toReturn['config']['cron'] = this.currentCronExpression
                toReturn['config']['type'] = this.cronExpressionType
            }

            toReturn['dataSetLabel'] = this.originalDataset.label
            toReturn['dataSetId'] = this.originalDataset.id
            const d = new Date()
            if (this.preparedDataset.label) {
                toReturn['destinationDataSetLabel'] = this.preparedDataset.label
            } else {
                toReturn['destinationDataSetLabel'] = 'ds__' + (d.getTime() % 10000000)
            }
            toReturn['destinationDataSetName'] = this.preparedDataset.name
            toReturn['destinationDataSetDescription'] = this.preparedDataset.description
            toReturn['meta'] = this.createMetaDefinition()
            toReturn['dsId'] = this.preparedDataset.id
            return toReturn
        },
        createMetaDefinition() {
            const meta = [] as Array<any>
            this.columns?.forEach((col) => {
                const item = {}
                item['displayedName'] = col.fieldAlias
                item['name'] = col.header
                item['fieldType'] = col.fieldType
                item['type'] = col.Type
                meta.push(item)
            })
            return meta
        },
        createProcessDefinition() {
            const toReturn = {}
            if (this.config && this.config.transformations) toReturn['definition'] = this.config.transformations
            return toReturn
        },
        cancel() {
            if (this.touched) {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesHeader'),
                    header: this.$t('common.toast.unsavedChangesMessage'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.resetAndClose()
                })
            } else {
                this.resetAndClose()
            }
        },
        resetAndClose(): void {
            this.cronExpressionType = this.preparedDsMeta.config?.type
            this.currentCronExpression = this.preparedDsMeta.config?.cron ? this.preparedDsMeta.config.cron : ''

            this.schedulationPaused = this.preparedDsMeta.config?.schedulationPaused || false

            this.schedulationEnabled = this.preparedDsMeta.config?.cron ? true : false
            this.closeDialog()
        },
        closeDialog(): void {
            this.$emit('update:visibility', false)
        },
        loadTranslations(): void {
            this.descriptor.dataPreparation.refreshRate.options.forEach((element) => {
                element.name = this.$t(element.name)
            })
        },

        updateSchedulationPaused(newSchedulationPaused) {
            this.schedulationPaused = newSchedulationPaused
        },
        updateSchedulationEnabled(newSchedulationEnabled) {
            this.schedulationEnabled = newSchedulationEnabled
        },
        updateCurrentCronExpression(newCronExpression) {
            this.currentCronExpression = newCronExpression
        },
        updateCronExpressionType(newCronExpressionType) {
            this.cronExpressionType = newCronExpressionType
        }
    }
})
</script>

<style lang="scss">
.dataPreparationSaveDialog {
    min-width: 600px !important;
    width: 600px !important;
    max-width: 600px !important;
}

.schedulerContainer {
    width: 100%;
}
</style>
