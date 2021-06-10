<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('managers.templatePruning.title') }}
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <div id="cards-container" class="kn-page-contentp-grid p-m-0">
            <div class="p-col-12">
                <Card>
                    <template #header>
                        <Toolbar class="kn-toolbar kn-toolbar--secondary">
                            <template #left>
                                {{ $t('managers.templatePruning.referenceDate') }}
                            </template>
                        </Toolbar>
                    </template>
                    <template #content>
                        <div class="p-d-flex">
                            <div class="kn-flex">
                                {{ $t('managers.templatePruning.enterDateMessage') }}
                            </div>
                            <div class="kn-flex">
                                <div class="p-d-flex">
                                    <span class="p-float-label">
                                        <Calendar
                                            id="expirationDate"
                                            class="kn-material-input"
                                            type="text"
                                            v-model="selectedDate"
                                            :class="{
                                                'p-invalid': !selectedDate
                                            }"
                                            :showIcon="true"
                                            :maxDate="maxDate"
                                            data-test="date-input"
                                        />
                                        <label for="expirationDate" class="kn-material-input-label"> {{ $t('managers.templatePruning.selectDate') }} * </label>
                                    </span>
                                    <Button icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" :disabled="filterDisabled" @click="loadDocumentSelection" aria-label="Filter" data-test="filter-button" />
                                </div>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
            <div class="p-col-12" v-if="documentSelectionVisible">
                <Card data-test="document-selection-card">
                    <template #header>
                        <Toolbar class="kn-toolbar kn-toolbar--secondary">
                            <template #left>
                                {{ $t('managers.templatePruning.documentSelection') }}
                            </template>
                        </Toolbar>
                    </template>
                    <template #content>
                        <div class="p-d-flex">
                            <div class="kn-flex">
                                <p>{{ documentSelectionMessage }}</p>
                                <Button class="kn-button kn-button--primary" v-if="documentsAvailable" :disabled="deleteDisabled" @click="deleteConfirm" aria-label="Delete Templates" data-test="delete-button">{{ $t('common.delete') }}</Button>
                            </div>
                            <div class="kn-flex" v-if="documentsAvailable">
                                <h5>Checkbox Selection</h5>
                                <Tree :value="nodes" selectionMode="checkbox" v-model:selectionKeys="selectedKeys"></Tree>
                            </div>
                        </div>
                    </template>
                </Card>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFolder, iDocument } from './TemplatePruning'
import axios from 'axios'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'template-pruning',
    components: {
        Calendar,
        Card,
        Tree
    },
    data() {
        return {
            selectedDate: new Date(),
            folderStructure: [] as (iFolder | iDocument)[],
            documents: [] as iDocument[],
            selectedDocuments: [] as iDocument[],
            selectedKeys: null,
            documentSelectionVisible: false,
            loading: false,
            nodes: null
        }
    },
    computed: {
        maxDate() {
            return new Date()
        },
        filterDisabled(): boolean {
            return this.selectedDate === null
        },
        documentSelectionMessage(): string {
            return this.documents.length != 0 ? this.$t('managers.templatePruning.documentSelectionMessage') : this.$t('managers.templatePruning.noDocuments')
        },
        documentsAvailable(): boolean {
            return this.documents.length > 0
        },
        deleteDisabled(): boolean {
            return this.selectedDocuments.length === 0
        }
    },
    methods: {
        formatDate(date: Date) {
            return date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate()
        },
        async loadDocumentSelection() {
            this.loading = true
            await this.loadFolderStructure()
            await this.loadDocuments(this.selectedDate)
            this.createTree()
            this.loading = false
            this.documentSelectionVisible = true
            console.log('FOLDERS: ', this.folderStructure)
            console.log('DOCUMENTS: ', this.documents)
        },
        async loadFolderStructure() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/folders?includeDocs=true').then((response) => {
                this.folderStructure = []
                response.data.map((file: iFolder | iDocument) => this.folderStructure.push({ ...file, exportable: false }))
            })
        },
        async loadDocuments(date: Date) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents?date=${this.formatDate(date)}`).then((response) => (this.documents = response.data))
        },
        createTree() {
            this.folderStructure.map((file: any) => {
                if (file.biObjects.length != 0) {
                    file.biObjects.map((el) => {
                        this.documents.map((document: iDocument) => {
                            if (el.name === document.name) {
                                console.log(file)
                            }
                        })
                    })
                }
            })
        },
        deleteConfirm() {
            console.log('CALLED!')
        },
        test() {
            console.log('SELECTED DOCUMENTS', this.selectedDocuments)
        }
    }
})
</script>

<style lang="scss" scoped>
#cards-container {
    flex: 0.5;
}
</style>
