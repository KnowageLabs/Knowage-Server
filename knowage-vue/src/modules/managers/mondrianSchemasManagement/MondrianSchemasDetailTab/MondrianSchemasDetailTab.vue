<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-3">
                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.schema.name.$model"
                            :class="{
                                'p-invalid': v$.schema.name.$invalid && v$.schema.name.$dirty
                            }"
                            maxLength="100"
                            @blur="v$.schema.name.$touch()"
                            @input="onFieldChange('name', $event.target.value)"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.mondrianSchemasManagement.detail.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.schema.name"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.mondrianSchemasManagement.detail.name')
                        }"
                    />
                </div>
                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.schema.description.$model"
                            :class="{
                                'p-invalid': v$.schema.description.$invalid && v$.schema.description.$dirty
                            }"
                            maxLength="500"
                            @blur="v$.schema.description.$touch()"
                            @input="onFieldChange('description', $event.target.value)"
                        />
                        <label for="description" class="kn-material-input-label">
                            {{ $t('managers.mondrianSchemasManagement.detail.description') }}
                        </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.schema.description"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.mondrianSchemasManagement.detail.description')
                        }"
                    />
                </div>
                <div class="p-field">
                    <span class="p-float-label">
                        <FileUpload mode="basic" name="file" url="http://localhost:8080/knowage/restful-services/2.0/mondrianSchemasResource/26/versions" @upload="onUpload" />
                        <!-- <template #empty>
                                <p>Drag and drop files to here to upload.</p>
                            </template>
                        </FileUpload> -->
                    </span>
                </div>
                <!-- <InputText id="type" class="kn-material-input" type="text" v-model.trim="v$.schema.type.$model" @input="onFieldChange('type', $event.target.value)" /> -->
            </form>
        </template>
    </Card>
    <Card :style="tabViewDescriptor.tableCard.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    Saved versions
                </template>
            </Toolbar>
        </template>
        <template #content>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <div v-if="!loading">
                <div class="p-col">
                    <DataTable :value="versions" :scrollable="true" scrollHeight="45vh" :loading="loading" :rows="7" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" v-model:selection="selectedVersion" v-model:filters="filters">
                        <template #header>
                            <div class="table-header">
                                <span class="p-input-icon-left">
                                    <i class="pi pi-search" />
                                    <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
                                </span>
                            </div>
                        </template>
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #loading v-if="loading">
                            test
                            {{ $t('common.info.dataLoading') }}
                        </template>
                        <Column selectionMode="single" :header="$t('managers.mondrianSchemasManagement.headers.active')" headerStyle="width: 3em"></Column>
                        <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" :style="detailDescriptor.table.column.style">
                            <template #filter="{ filterModel }">
                                <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                            </template>
                        </Column>
                        <Column :style="detailDescriptor.table.iconColumn.style" @rowClick="false">
                            <template #body="slotProps">
                                <Button icon="pi pi-download" class="p-button-link" />
                                <Button icon="pi pi-trash" class="p-button-link" @click="showDeleteDialog(slotProps.data.id)" />
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { iSchema, iVersion } from '../MondrianSchemas'
import { filterDefault } from '@/helpers/commons/filterHelper'
import axios from 'axios'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../MondrianSchemasTabViewDescriptor.json'
import detailDescriptor from './MondrianSchemasDetailDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import FileUpload from 'primevue/fileupload'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'detail-tab',
    components: {
        Card,
        KnValidationMessages,
        FileUpload,
        DataTable,
        Column
    },
    props: {
        selectedSchema: {
            type: Object,
            requried: false
        }
    },
    emits: ['fieldChanged'],
    data() {
        return {
            loading: false,
            tabViewDescriptor,
            detailDescriptor,
            v$: useValidate() as any,
            schema: {} as iSchema,
            versions: {} as any,
            selectedVersion: {} as iVersion,
            columns: detailDescriptor.columns,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    validations() {
        return {
            schema: createValidations('schema', detailDescriptor.validations.schema)
        }
    },
    mounted() {
        if (this.selectedSchema) {
            this.schema = { ...this.selectedSchema } as iSchema
        }
    },
    watch: {
        selectedSchema() {
            this.schema = { ...this.selectedSchema } as iSchema
            this.loadVersions()
        }
    },
    methods: {
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
        },
        async loadVersions() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.schema.id}` + '/versions')
                .then((response) => {
                    this.versions = response.data
                })
                .finally(() => (this.loading = false))
        },
        showDeleteDialog(versionId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteVersion(versionId)
            })
        },
        onUpload() {
            this.$toast.add({ severity: 'info', summary: 'Success', detail: 'File Uploaded', life: 3000 })
        },
        async deleteVersion(versionId: number) {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.schema.id}` + '/versions/' + versionId).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.loadVersions()
            })
        }
    }
})
</script>
