<template>
    <Card class="p-mb-3">
        <template #content>
            <form class="p-fluid p-m-3">
                <div class="p-field p-mb-3">
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
                            data-test="name-input"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages :vComp="v$.schema.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field p-mb-3">
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
                            data-test="description-input"
                        />
                        <label for="description" class="kn-material-input-label">
                            {{ $t('common.description') }}
                        </label>
                    </span>
                    <KnValidationMessages :vComp="v$.schema.description" :additionalTranslateParams="{ fieldName: $t('common.description') }" />
                </div>
                <div class="p-field">
                    <span class="p-float-label">
                        <KnInputFile label="" :changeFunction="onVersionUpload" accept=".csv" :visibility="true" />
                    </span>
                </div>
            </form>
        </template>
    </Card>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('managers.mondrianSchemasManagement.detail.savedVersions') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <div>
                <div class="p-col">
                    <DataTable
                        v-if="!loading"
                        :value="versions"
                        :scrollable="true"
                        scrollHeight="40vh"
                        :loading="loading"
                        :rows="7"
                        class="p-datatable-sm kn-table"
                        dataKey="id"
                        responsiveLayout="stack"
                        breakpoint="960px"
                        v-model:selection="selectedVersion"
                        v-model:filters="filters"
                        @row-select="onActiveVersionChange"
                    >
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
                        <template #filter="{ filterModel }">
                            <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                        </template>
                        <Column selectionMode="single" :header="$t('managers.mondrianSchemasManagement.headers.active')" headerStyle="width: 3em"></Column>
                        <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" :style="detailDescriptor.table.column.style"> </Column>
                        <Column field="creationDate" :header="$t('managers.mondrianSchemasManagement.headers.creationDate')" dataType="date">
                            <template #body="{data}">
                                {{ formatDate(data.creationDate) }}
                            </template>
                        </Column>
                        <Column :style="detailDescriptor.table.iconColumn.style" @rowClick="false">
                            <template #body="slotProps">
                                <Button icon="pi pi-download" class="p-button-link" @click="downloadVersion(slotProps.data.id)" />
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
    import { downloadDirect } from '@/helpers/commons/fileHelper'
    import { AxiosResponse } from 'axios'
    import moment from 'moment'
    import useValidate from '@vuelidate/core'
    import tabViewDescriptor from '../MondrianSchemasTabViewDescriptor.json'
    import detailDescriptor from './MondrianSchemasDetailDescriptor.json'
    import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
    import KnInputFile from '@/components/UI/KnInputFile.vue'
    import Card from 'primevue/card'
    import DataTable from 'primevue/datatable'
    import Column from 'primevue/column'
    import { formatDateWithLocale } from '@/helpers/commons/localeHelper'

    export default defineComponent({
        name: 'detail-tab',
        components: {
            Card,
            KnValidationMessages,
            DataTable,
            Column,
            KnInputFile
        },
        props: {
            selectedSchema: {
                type: Object,
                requried: false
            },
            reloadTable: {
                type: Boolean,
                default: false
            }
        },
        emits: ['fieldChanged', 'activeVersionChanged', 'versionUploaded', 'versionsReloaded'],
        data() {
            return {
                loading: false,
                moment,
                tabViewDescriptor,
                detailDescriptor,
                v$: useValidate() as any,
                schema: {} as iSchema,
                versions: [] as any,
                selectedVersion: null as iVersion | null,
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
            },
            reloadTable() {
                if (this.reloadTable) {
                    this.loadVersions()
                }
            }
        },
        methods: {
            onFieldChange(fieldName: string, value: any) {
                this.$emit('fieldChanged', { fieldName, value })
            },
            onActiveVersionChange(event) {
                let versionId = event.data.id
                this.$emit('activeVersionChanged', versionId)
            },
            async onVersionUpload(event) {
                let uploadedVersion = event.target.files[0]
                this.$emit('versionUploaded', uploadedVersion)
            },
            async loadVersions() {
                if (!this.schema.id) {
                    this.versions = []
                    return
                }
                this.loading = true
                await this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.schema.id}` + '/versions')
                    .then((response: AxiosResponse<any>) => {
                        this.versions = response.data
                        setTimeout(() => (this.selectedVersion = this.versions ? this.versions.find((version) => version.active) : null), 200)
                        this.$emit('versionsReloaded')
                    })
                    .finally(() => (this.loading = false))
            },
            async downloadVersion(versionId) {
                await this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.schema.id}` + `/versions/${versionId}` + `/file`, {
                        headers: {
                            Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
                        }
                    })
                    .then(
                        (response: AxiosResponse<any>) => {
                            if (response.data.errors) {
                                this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t('common.error.errorCreatingPackage') })
                            } else {
                                this.$store.commit('setInfo', { title: this.$t('managers.mondrianSchemasManagement.toast.downloadFile.downloaded'), msg: this.$t('managers.mondrianSchemasManagement.toast.downloadFile.ok') })
                                var contentDisposition = response.headers['content-disposition']

                                var contentDispositionMatches = contentDisposition.match(/(?!([\b attachment;filename= \b])).*(?=)/g)
                                if (contentDispositionMatches && contentDispositionMatches.length > 0) {
                                    var fileAndExtension = contentDispositionMatches[0]
                                    var completeFileName = fileAndExtension.replaceAll('"', '')
                                    downloadDirect(response.data, completeFileName, 'application/zip; charset=utf-8')
                                }
                            }
                        },
                        (error) => this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t(error) })
                    )
            },
            showDeleteDialog(versionId: number) {
                this.$confirm.require({
                    message: this.$t('common.toast.deleteMessage'),
                    header: this.$t('common.toast.deleteConfirmTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.deleteVersion(versionId)
                })
            },
            async deleteVersion(versionId: number) {
                await this.$http.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/mondrianSchemasResource/${this.schema.id}` + '/versions/' + versionId).then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.loadVersions()
                })
            },
            formatDate(date) {
                return formatDateWithLocale(date, { dateStyle: 'short', timeStyle: 'short' })
            }
        }
    })
</script>
