<template>
    <Dialog class="kn-dialog--toolbar--primary dataPreparationSaveDialog" v-bind:visible="visibility" footer="footer" :header="$t('managers.workspaceManagement.dataPreparation.savePreparedDataset')" :closable="false" modal>
        <div class="p-d-flex p-mt-5">
            <span class="p-float-label p-field p-ml-2 kn-flex">
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
            <span class="p-float-label p-field p-ml-2 kn-flex">
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
        </div>
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <InputText
                    class="kn-material-input"
                    type="text"
                    v-model.trim="v$.preparedDataset.description.$model"
                    :class="{
                        'p-invalid': v$.preparedDataset.description.$invalid
                    }"
                    maxLength="100"
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
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <InputText
                    class="kn-material-input"
                    type="text"
                    v-model.trim="v$.preparedDataset.dataSource.$model"
                    :class="{
                        'p-invalid': v$.preparedDataset.dataSource.$invalid
                    }"
                    maxLength="100"
                />
                <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.dataSource') }}</label>
                <KnValidationMessages
                    :vComp="v$.preparedDataset.dataSource"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.configurationManagement.headers.dataSource')
                    }"
                ></KnValidationMessages>
            </span>
        </div>
        <div class="p-d-flex">
            <span class="p-float-label p-field p-ml-2 kn-flex">
                <InputText class="kn-material-input" type="text" v-model="preparedDataset.visibility" maxLength="100" />
                <label class="kn-material-input-label" for="label">{{ $t('managers.workspaceManagement.dataPreparation.dataset.visibility') }}</label>
            </span>

            <span class="p-float-label p-field p-ml-2 kn-flex">
                <Dropdown
                    id="type"
                    class="kn-material-input"
                    v-model="preparedDataset.refreshRate"
                    dataKey="id"
                    optionLabel="name"
                    optionValue="code"
                    :options="descriptor.dataPreparation.refreshRate.options"
                    :placeholder="$t('managers.workspaceManagement.dataPreparation.dataset.refreshRate.label')"
                    maxLength="100"
                />
            </span>
        </div>

        <template #footer>
            <Button class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="resetAndClose" />

            <Button class="kn-button kn-button--primary" v-t="'common.save'" :disabled="buttonDisabled" @click="savePreparedDataset()" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'

import { createValidations } from '@/helpers/commons/validationHelper'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import DataPreparationDescriptor from './DataPreparationDescriptor.json'
import useValidate from '@vuelidate/core'
import DataPreparationValidationDescriptor from './DataPreparationValidationDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import { IDataPreparationDataset, IDataPreparationColumn } from '@/modules/workspace/dataPreparation/DataPreparation'

export default defineComponent({
    name: 'data-preparation-detail-save-dialog',
    props: {
        originalDataset: {} as any,
        config: {} as any,
        columns: [] as PropType<IDataPreparationColumn[]>,
        visibility: Boolean
    },
    components: { Dialog, Dropdown, KnValidationMessages },
    data() {
        return { descriptor: DataPreparationDescriptor, preparedDataset: {} as IDataPreparationDataset, v$: useValidate() as any, validationDescriptor: DataPreparationValidationDescriptor }
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
            toReturn['config'] = {}
            toReturn['dataSetLabel'] = this.originalDataset.label
            toReturn['destinationDataSetLabel'] = this.preparedDataset.label
            toReturn['destinationDataSetName'] = this.preparedDataset.name
            toReturn['destinationDataSetDescription'] = this.preparedDataset.description
            toReturn['destinationDataSource'] = this.preparedDataset.dataSource
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

<style lang="scss" scoped>
.p-multiselect,
.p-inputtext,
.p-dropdown {
    width: 100%;
}
.dataPreparationSaveDialog {
    min-width: 600px !important;
    width: 50vw;
    max-width: 1200px !important;
    &:deep(.p-dialog-content) {
        height: 30vw;
    }
}
</style>
