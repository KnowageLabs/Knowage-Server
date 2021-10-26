<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.myRepository') }} - {{ selectedFolder.label }}
        </template>
        <template #right>
            <FabButton icon="fas fa-folder" data-test="new-folder-button" @click="displayCreateFolderDialog = true" />
        </template>
    </Toolbar>
    <div class="p-m-2 overflow">
        <DataTable
            class="p-datatable-sm kn-table"
            :value="documents"
            :loading="loading"
            :scrollable="true"
            scrollHeight="89vh"
            dataKey="biObjId"
            responsiveLayout="stack"
            breakpoint="600px"
            v-model:filters="filters"
            v-model:selection="selectedDocument"
            selectionMode="single"
            @rowSelect="showDetailSidebar = true"
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
            <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" />
            <Column class="icon-cell" @rowClick="false">
                <template #body="slotProps">
                    <Button icon="fas fa-edit" class="p-button-link" @click="logEvent(slotProps.data)" />
                    <Button icon="fas fa-play-circle" class="p-button-link" />
                </template>
            </Column>
        </DataTable>
    </div>
    <Dialog id="saveDialog" class="kn-dialog--toolbar--primary importExportDialog" v-bind:visible="displayCreateFolderDialog" footer="footer" :closable="false" modal>
        <template #header>
            <h4>{{ $t('workspace.myRepository.newFolderTitle') }}</h4>
        </template>
        <form class="p-fluid p-formgrid p-grid">
            <div class="p-field p-col-6  p-mt-5">
                <span class="p-float-label ">
                    <InputText
                        id="code"
                        class="kn-material-input"
                        type="text"
                        maxLength="25"
                        v-model.trim="v$.newFolder.code.$model"
                        :class="{
                            'p-invalid': v$.newFolder.code.$invalid && v$.newFolder.code.$dirty
                        }"
                        @blur="v$.newFolder.code.$touch()"
                    />
                    <label for="code" class="kn-material-input-label">{{ $t('managers.glossary.common.code') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.newFolder.code"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.glossary.common.code')
                    }"
                />
            </div>
            <div class="p-field p-col-6 p-mt-5">
                <span class="p-float-label ">
                    <InputText
                        id="name"
                        class="kn-material-input"
                        type="text"
                        maxLength="25"
                        v-model.trim="v$.newFolder.name.$model"
                        :class="{
                            'p-invalid': v$.newFolder.name.$invalid && v$.newFolder.name.$dirty
                        }"
                        @blur="v$.newFolder.name.$touch()"
                    />
                    <label for="name" class="kn-material-input-label">{{ $t('importExport.catalogFunction.column.name') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.newFolder.name"
                    :additionalTranslateParams="{
                        fieldName: $t('importExport.catalogFunction.column.name')
                    }"
                />
            </div>
            <div class="p-field p-col-12">
                <span class="p-float-label p-mb-2">
                    <InputText id="descr" class="kn-material-input" type="text" maxLength="254" v-model.trim="newFolder.descr" />
                    <label for="descr" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                </span>
            </div>
        </form>
        <template #footer>
            <div>
                <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="displayCreateFolderDialog = false" />
                <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="createNewFolder" :disabled="buttonDisabled" />
            </div>
        </template>
    </Dialog>

    <DetailSidebar :visible="showDetailSidebar" :viewType="'recent'" :document="selectedDocument" @close="showDetailSidebar = false" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { createValidations } from '@/helpers/commons/validationHelper'
import { IDocument, IFolder } from '@/modules/workspace/Workspace'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import repositoryDescriptor from './WorkspaceRepositoryViewDescriptor.json'
import useValidate from '@vuelidate/core'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import FabButton from '@/components/UI/KnFabButton.vue'
import Dialog from 'primevue/dialog'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    components: { DataTable, Column, FabButton, Dialog, KnValidationMessages, DetailSidebar },
    emits: ['showMenu', 'reloadRepositoryMenu'],
    props: { selectedFolder: { type: Object }, id: { type: String, required: false } },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    data() {
        return {
            v$: useValidate() as any,
            loading: false,
            showDetailSidebar: false,
            displayCreateFolderDialog: false,
            documents: [] as IDocument[],
            selectedDocument: {} as IDocument,
            newFolder: {} as IFolder,
            columns: repositoryDescriptor.columns,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    validations() {
        return {
            newFolder: createValidations('newFolder', repositoryDescriptor.validations.newFolder)
        }
    },
    watch: {
        id() {
            this.getFolderDocuments()
        }
    },
    created() {
        this.getFolderDocuments()
    },
    methods: {
        getFolderDocuments() {
            this.loading = true
            return this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/${this.id}`)
                .then((response) => {
                    this.documents = [...response.data]
                })
                .finally(() => (this.loading = false))
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        async createNewFolder() {
            this.newFolder.parentFunct = this.selectedFolder?.functId
            this.newFolder.path = this.selectedFolder?.path + `/` + encodeURIComponent(this.newFolder.code)
            this.newFolder.prog = this.selectedFolder?.prog
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/organizer/foldersee/', this.newFolder, { headers: { 'X-Disable-Errors': true } })
                .then(() => {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                    this.$emit('reloadRepositoryMenu')
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.displayCreateFolderDialog = false))
        },
        logEvent(event) {
            console.log(event)
        }
    }
})
</script>
