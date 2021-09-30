<template>
    <Card>
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" maxLength="50" v-model="v$.dataset.label.$model" :class="{ 'p-invalid': v$.dataset.label.$invalid && v$.dataset.label.$dirty }" @blur="v$.dataset.label.$touch()" @change="$emit('touched')" data-test="label-input" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.label" :additionalTranslateParams="{ fieldName: $t('common.label') }" />
                </div>
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <InputText id="name" class="kn-material-input" type="text" maxLength="50" v-model="v$.dataset.name.$model" :class="{ 'p-invalid': v$.dataset.name.$invalid && v$.dataset.name.$dirty }" @blur="v$.dataset.name.$touch()" @change="$emit('touched')" data-test="name-input" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field p-mt-1 p-col-12">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            class="kn-material-input"
                            type="text"
                            maxLength="150"
                            v-model="v$.dataset.description.$model"
                            :class="{ 'p-invalid': v$.dataset.description.$invalid && v$.dataset.description.$dirty }"
                            @blur="v$.dataset.description.$touch()"
                            @change="$emit('touched')"
                            data-test="description-input"
                        />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.description" :additionalTranslateParams="{ fieldName: $t('common.description') }" />
                </div>
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="scope"
                            class="kn-material-input"
                            :options="scopeTypes"
                            optionLabel="VALUE_CD"
                            optionValue="VALUE_CD"
                            v-model="v$.dataset.scopeCd.$model"
                            :class="{
                                'p-invalid': v$.dataset.scopeCd.$invalid && v$.dataset.scopeCd.$dirty
                            }"
                            @before-show="v$.dataset.scopeCd.$touch()"
                            @change="updateIdFromCd(this.scopeTypes, 'scopeId', $event.value), $emit('touched')"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.scope') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.scopeCd"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.scope')
                        }"
                    />
                </div>
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <Dropdown id="category" class="kn-material-input" :options="categoryTypes" optionLabel="VALUE_CD" optionValue="VALUE_CD" v-model="dataset.catTypeVn" @change="updateIdFromCd(this.categoryTypes, 'catTypeId', $event.value), $emit('touched')" />
                        <label for="category" class="kn-material-input-label"> {{ $t('common.category') }} </label>
                    </span>
                </div>
                <div class="p-field p-mt-1 p-col-12">
                    <span class="p-float-label kn-material-input">
                        <Chips id="tags" v-model="dataset.tags" @add="buildTagObject" @remove="$emit('touched')" :allowDuplicate="false">
                            <template #chip="slotProps">
                                {{ slotProps.value.name }}
                            </template>
                        </Chips>
                        <label for="tags" class="kn-material-input-label">{{ $t('common.tags') }}</label>
                    </span>
                    <small id="username1-help">{{ $t('managers.widgetGallery.tags.availableCharacters') }}</small>
                </div>
            </form>
        </template>
    </Card>
    <Card class="p-mt-3">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('managers.datasetManagement.oldVersions') }}
                </template>
                <template #right>
                    <Button icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" :disabled="noDatasetVersions" @click="deleteConfirm('deleteAll')" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <DataTable v-if="!loading" class="p-datatable-sm kn-table" :value="selectedDatasetVersions" :scrollable="true" scrollHeight="400px" :loading="loading" dataKey="versNum" responsiveLayout="stack" breakpoint="960px">
                <Column field="userIn" :header="$t('managers.datasetManagement.creationUser')" :sortable="true" />
                <Column field="type" :header="$t('common.type')" :sortable="true" />
                <Column field="dateIn" :header="$t('managers.mondrianSchemasManagement.headers.creationDate')" dataType="date" :sortable="true">
                    <template #body="{data}">
                        {{ moment(data.dateIn).format('YYYY[/]MM[/]DD,  hh:mm:ss') }}
                    </template>
                </Column>
                <Column @rowClick="false">
                    <template #body="slotProps">
                        <Button icon="fas fa-retweet" class="p-button-link" @click="restoreVersion(slotProps.data)" />
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteConfirm('deleteOne', slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import axios from 'axios'
import moment from 'moment'
import useValidate from '@vuelidate/core'
import detailTabDescriptor from './DatasetManagementDetailCardDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Chips from 'primevue/chips'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, Chips, DataTable, Column },
    props: {
        scopeTypes: { type: Array as any, required: true },
        categoryTypes: { type: Array as any, required: true },
        selectedDataset: { type: Object as any },
        selectedDatasetVersions: { type: Array as any },
        loading: { type: Boolean }
    },
    computed: {
        noDatasetVersions(): any {
            if (this.selectedDatasetVersions.length > 0) {
                return false
            }
            return true
        }
    },
    emits: ['touched', 'scopeTypeChanged', 'reloadVersions', 'restoreDatasetVersion'],
    data() {
        return {
            moment,
            detailTabDescriptor,
            v$: useValidate() as any,
            dataset: {} as any,
            datasetVersions: [] as any
        }
    },
    created() {
        this.dataset = this.selectedDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    validations() {
        return {
            dataset: createValidations('dataset', detailTabDescriptor.validations.dataset)
        }
    },
    methods: {
        restoreVersion(event) {
            this.$emit('restoreDatasetVersion', event)
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        updateIdFromCd(optionsArray, fieldToUpdate, updatedField) {
            const selectedField = optionsArray.find((option) => option.VALUE_CD === updatedField)
            selectedField ? (this.dataset[fieldToUpdate] = selectedField.VALUE_ID) : ''
        },
        buildTagObject() {
            this.dataset.tags = this.dataset.tags.map((tag) => {
                if (typeof tag !== 'string') {
                    return tag
                } else {
                    return { name: tag }
                }
            })
        },

        //#region ===================== Delete Versions Functionality ====================================================
        deleteConfirm(deletetype, event) {
            let msgDesc = ''
            deletetype === 'deleteOne' ? (msgDesc = 'managers.datasetManagement.deleteOneVersionMsg') : (msgDesc = 'managers.datasetManagement.deleteAllVersionsMsg')
            this.$confirm.require({
                message: this.$t(msgDesc),
                header: this.$t('common.uppercaseDelete'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    deletetype === 'deleteOne' ? this.deleteSelectedVersion(event) : this.deleteAllVersions()
                }
            })
        },
        async deleteSelectedVersion(event) {
            return axios
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${event.dsId}/version/${event.versNum}`)
                .then(() => {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                    this.$emit('reloadVersions')
                })
                .catch((error) => this.$store.commit('setError', { title: this.$t('common.error.generic'), msg: error.message }))
        },
        async deleteAllVersions() {
            return axios
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${this.selectedDataset.id}/allversions/`)
                .then(() => {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('managers.datasetManagement.deleteAllVersionsSuccess') })
                    this.$emit('reloadVersions')
                })
                .catch((error) => this.$store.commit('setError', { title: this.$t('common.error.generic'), msg: error.message }))
        }
        //#endregion ================================================================================================
    }
})
</script>
