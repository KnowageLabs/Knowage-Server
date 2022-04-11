<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary dataPreparationSaveDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.workspaceManagement.dataPreparation.savePreparedDataset')" :closable="false" modal>
        <div class="p-grid p-m-0">
            <div class="p-col-12 ">
                <div class="p-d-flex">
                    <span class="p-float-label kn-flex p-mr-2">
                        <InputText
                            class="kn-material-input"
                            type="text"
                            :disabled="!isFirstSave"
                            v-model.trim="v$.preparedDataset.name.$model"
                            :class="{
                                'p-invalid': v$.preparedDataset.name.$invalid
                            }"
                            maxLength="100"
                            @change="touched = true"
                        />
                        <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.name') }}</label>
                        <KnValidationMessages
                            :vComp="v$.preparedDataset.name"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.configurationManagement.headers.name')
                            }"
                        ></KnValidationMessages>
                    </span>
                </div>

                <span class="p-float-label">
                    <Textarea
                        class="kn-material-input p-mb-1"
                        type="text"
                        :disabled="!isFirstSave"
                        v-model.trim="v$.preparedDataset.description.$model"
                        :class="{
                            'p-invalid': v$.preparedDataset.description.$invalid
                        }"
                        rows="3"
                        maxLength="10000"
                        @blur="touched = true"
                    />
                    <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.description') }}</label>
                    <KnValidationMessages
                        :vComp="v$.preparedDataset.description"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.configurationManagement.headers.description')
                        }"
                    ></KnValidationMessages>
                </span>
            </div>
            <KnScheduler
                class="p-m-1"
                :cronExpression="currentCronExpression"
                :descriptor="schedulerDescriptor"
                @touched="touched = true"
                :logsVisible="false"
                :schedulationEnabled="schedulationEnabled"
                :schedulationPaused="schedulationPaused"
                @update:schedulationPaused="updateSchedulationPaused"
                @update:schedulationEnabled="updateSchedulationEnabled"
                @update:currentCronExpression="updateCurrentCronExpression"
            />
        </div>
        <template #footer>
            <Button class="kn-button--secondary" :label="$t('common.cancel')" @click="resetAndClose" />

            <Button class="kn-button--primary" v-t="'common.save'" :disabled="saveButtonDisabled" @click="savePreparedDataset()" />
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

    export default defineComponent({
        name: 'data-preparation-detail-save-dialog',
        props: {
            originalDataset: {} as any,
            config: {} as any,
            columns: [] as PropType<IDataPreparationColumn[]>,
            instanceId: {} as any,
            processId: {} as any,
            preparedDsMeta: {} as any,
            visibility: Boolean
        },
        components: { Dialog, KnScheduler, KnValidationMessages, Textarea },
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
                schedulationEnabled: false
            }
        },
        updated() {
            if (this.processId && this.processId != '') this.isFirstSave = false
        },
        emits: ['update:visibility', 'update:instanceId', 'update:processId'],

        validations() {
            return {
                preparedDataset: createValidations('preparedDataset', this.validationDescriptor.validations.configuration)
            }
        },
        computed: {
            saveButtonDisabled(): any {
                return this.v$.$invalid || !this.touched
            }
        },
        methods: {
            savePreparedDataset(): void {
                let processDefinition = this.createProcessDefinition()
                this.saveOrUpdateProcess(processDefinition).then(
                    (response: AxiosResponse<any>) => {
                        let processId = response.data.id
                        this.$emit('update:processId', processId)
                        let datasetDefinition = this.createDatasetDefinition()
                        this.saveOrUpdateInstance(processId, datasetDefinition).then(
                            (response: AxiosResponse<any>) => {
                                this.$emit('update:instanceId', response.data.id)
                                this.$store.commit('setInfo', { title: 'Saved successfully' })
                            },
                            () => {
                                this.$store.commit('setError', { title: 'Save error', msg: 'Cannot add process instance' })
                            }
                        )
                    },
                    () => {
                        this.$store.commit('setError', { title: 'Save error', msg: 'Cannot create process' })
                    }
                )
                this.resetAndClose()
            },
            saveOrUpdateProcess(processDefinition) {
                if (this.processId && this.processId != '') return this.$http.put(process.env.VUE_APP_DATA_PREPARATION_PATH + `1.0/process/${this.processId}`, processDefinition)
                else return this.$http.post(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process', processDefinition)
            },
            saveOrUpdateInstance(processId, datasetDefinition) {
                if (this.instanceId && this.instanceId != '') return this.$http.patch(process.env.VUE_APP_DATA_PREPARATION_PATH + `1.0/instance/${this.instanceId}`, datasetDefinition)
                else return this.$http.patch(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process/' + processId + '/instance', datasetDefinition)
            },
            createDatasetDefinition() {
                let toReturn = {}
                toReturn['config'] = {}
                toReturn['config']['paused'] = this.schedulationPaused

                if (this.schedulationEnabled) toReturn['config']['cron'] = this.currentCronExpression

                toReturn['dataSetLabel'] = this.originalDataset.label
                var d = new Date()
                toReturn['destinationDataSetLabel'] = 'ds__' + (d.getTime() % 10000000)
                toReturn['destinationDataSetName'] = this.preparedDataset.name
                toReturn['destinationDataSetDescription'] = this.preparedDataset.description
                toReturn['meta'] = this.createMetaDefinition()
                return toReturn
            },
            createMetaDefinition() {
                let meta = [] as Array<any>
                this.columns?.forEach((col) => {
                    let item = {}
                    item['displayedName'] = col.fieldAlias
                    item['name'] = col.header
                    item['fieldType'] = col.fieldType
                    item['type'] = col.Type
                    meta.push(item)
                })
                return meta
            },
            createProcessDefinition() {
                let toReturn = {}
                if (this.config && this.config.transformations) toReturn['definition'] = this.config.transformations
                return toReturn
            },
            resetAndClose(): void {
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
            }
        },

        watch: {
            preparedDsMeta: {
                handler() {
                    if (Object.keys(this.preparedDsMeta).length > 0) {
                        this.preparedDataset = this.preparedDsMeta
                        this.currentCronExpression = this.preparedDsMeta.config?.cron ? this.preparedDsMeta.config.cron : ''

                        this.schedulationPaused = this.preparedDsMeta.config?.schedulationPaused || false

                        this.schedulationEnabled = this.preparedDsMeta.config?.cron ? true : false
                    }
                },
                deep: true
            }
        },

        created() {
            this.loadTranslations()
        }
    })
</script>

<style lang="scss">
    .dataPreparationSaveDialog {
        min-width: 600px !important;
        width: 600px !important;
        max-width: 600px !important;
    }
</style>
