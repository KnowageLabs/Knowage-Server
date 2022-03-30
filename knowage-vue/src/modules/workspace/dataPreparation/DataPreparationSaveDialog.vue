<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary dataPreparationSaveDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.workspaceManagement.dataPreparation.savePreparedDataset')" :closable="false" modal>
        <div class="p-grid p-m-0">
            <div class="p-col-12 ">
                <div class="p-d-flex">
                    <span class="p-float-label kn-flex p-mr-2">
                        <InputText
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.preparedDataset.label.$model"
                            :class="{
                                'p-invalid': v$.preparedDataset.label.$invalid
                            }"
                            maxLength="100"
                        />
                        <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.label') }}</label>
                        <KnValidationMessages
                            :vComp="v$.preparedDataset.label"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.configurationManagement.headers.label')
                            }"
                        ></KnValidationMessages>
                    </span>
                    <span class="p-float-label kn-flex p-mr-2">
                        <InputText
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.preparedDataset.name.$model"
                            :class="{
                                'p-invalid': v$.preparedDataset.name.$invalid
                            }"
                            maxLength="100"
                        />
                        <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.name') }}</label>
                        <KnValidationMessages
                            :vComp="v$.preparedDataset.name"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.configurationManagement.headers.name')
                            }"
                        ></KnValidationMessages>
                    </span>
                    <div class="kn-flex p-d-flex p-ai-center">
                        <span> {{ $t('managers.workspaceManagement.dataPreparation.dataset.enableSchedulation') }}</span> <InputSwitch v-model="enableSchedulation" />
                    </div>
                </div>

                <span class="p-float-label">
                    <Textarea
                        class="kn-material-input p-mb-1"
                        type="text"
                        v-model.trim="v$.preparedDataset.description.$model"
                        :class="{
                            'p-invalid': v$.preparedDataset.description.$invalid
                        }"
                        rows="3"
                        maxLength="10000"
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

            <Card class="p-col-12 kn-card no-padding" v-if="enableSchedulation"
                ><template #content>
                    <KnScheduler class="p-m-1" :cronExpression="currentCronExpression" :descriptor="schedulerDescriptor" @touched="touched = true" :readOnly="!enableSchedulation" />
                </template>
            </Card>
        </div>
        <template #footer>
            <Button class="kn-button--secondary" :label="$t('common.cancel')" @click="resetAndClose" />

            <Button class="kn-button--primary" v-t="'common.save'" :disabled="buttonDisabled" @click="savePreparedDataset()" />
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
    import InputSwitch from 'primevue/inputswitch'

    export default defineComponent({
        name: 'data-preparation-detail-save-dialog',
        props: {
            originalDataset: {} as any,
            config: {} as any,
            columns: [] as PropType<IDataPreparationColumn[]>,
            visibility: Boolean
        },
        components: { Dialog, KnScheduler, KnValidationMessages, InputSwitch, Textarea },
        data() {
            return {
                descriptor: DataPreparationDescriptor,
                preparedDataset: {} as IDataPreparationDataset,
                v$: useValidate() as any,
                validationDescriptor: DataPreparationValidationDescriptor,
                schedulerDescriptor: dataPreparationMonitoringDescriptor,
                currentCronExpression: '',
                enableSchedulation: false
            }
        },
        emits: ['update:visibility'],

        validations() {
            return {
                preparedDataset: createValidations('preparedDataset', this.validationDescriptor.validations.configuration)
            }
        },
        methods: {
            savePreparedDataset(): void {
                let processDefinition = this.createProcessDefinition()
                this.$http.post(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process', processDefinition).then(
                    (response: AxiosResponse<any>) => {
                        let processId = response.data.id
                        let datasetDefinition = this.createDatasetDefinition()
                        this.$http.patch(process.env.VUE_APP_DATA_PREPARATION_PATH + '1.0/process/' + processId + '/instance', datasetDefinition).then(
                            () => {
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
            createDatasetDefinition() {
                let toReturn = {}
                toReturn['config'] = this.enableSchedulation ? { cron: this.currentCronExpression } : {}
                toReturn['dataSetLabel'] = this.originalDataset.label
                toReturn['destinationDataSetLabel'] = this.preparedDataset.label
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
