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
                            data-test="name-input"
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
                            data-test="description-input"
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
                        <FileUpload mode="basic" name="file" url="./upload.php" @upload="onUpload" />
                        <!-- <template #empty>
                                <p>Drag and drop files to here to upload.</p>
                            </template>
                        </FileUpload> -->
                    </span>
                </div>
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
            <div v-if="!loading">
                <div class="p-col">
                    <DataTable
                        :value="versions"
                        :paginator="true"
                        :loading="loading"
                        :rows="10"
                        class="p-datatable-sm kn-table"
                        dataKey="id"
                        :rowsPerPageOptions="[5, 10, 15]"
                        responsiveLayout="stack"
                        breakpoint="960px"
                        :currentPageReportTemplate="
                            $t('common.table.footer.paginated', {
                                first: '{first}',
                                last: '{last}',
                                totalRecords: '{totalRecords}'
                            })
                        "
                        data-test="configurations-table"
                        v-model:selection="selectedVersion"
                    >
                        <template #header>
                            <div class="table-header">
                                <span class="p-input-icon-left">
                                    <i class="pi pi-search" />
                                    <InputText class="kn-material-input" type="text" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
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
                            <template>
                                <Button icon="pi pi-trash" class="p-button-link" />
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
import { iSchema } from '../MondrianSchemas'
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
            selectedVersion: null,
            columns: detailDescriptor.columns
        }
    },
    filters: {
        global: [filterDefault]
    } as Object,
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
        }
    }
})
</script>
