<template>
    <Dialog class="full-screen-dialog" :visible="true" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #left>
                    <span>ds name goes here</span>
                </template>
                <template #right>
                    <Button icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.filter')" />
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />
        <div class="p-d-flex p-flex-row kn-height-full">
            <div v-show="showEntitiesLists" class="entities-lists">
                <div class="main-entities kn-flex">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            <span>Entities</span>
                        </template>
                        <template #right>
                            <Chip> .no </Chip>
                        </template>
                    </Toolbar>
                    <div>
                        Main List Test
                    </div>
                </div>
                <div class="derived-entities">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            <span>Derived Entities</span>
                        </template>
                        <template #right>
                            <Chip> .no </Chip>
                        </template>
                    </Toolbar>
                    <div>
                        Derived List Test
                    </div>
                </div>
            </div>
            <div class="detail-view p-m-1">
                <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                    <template #left>
                        <!-- todo: translation -->
                        <Button v-if="showEntitiesLists" icon="pi pi-chevron-left" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.hideList')" @click="toggleEntitiesLists" />
                        <Button v-else icon="pi pi-chevron-right" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.showList')" @click="toggleEntitiesLists" />
                    </template>
                    <template #right>
                        <InputSwitch class="p-mr-2" v-model="simpleTable" />
                        <span>Smart View</span>
                        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" />
                    </template>
                </Toolbar>
                <div>
                    Detail Container
                </div>
            </div>
        </div>

        <QBESimpleTable v-if="qbe" :query="qbe.qbeJSONQuery?.catalogue?.queries[0]"></QBESimpleTable>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { iQBE, iQuery, iField, iQueryResult } from './QBE'
import Dialog from 'primevue/dialog'
import Chip from 'primevue/chip'
import InputSwitch from 'primevue/inputswitch'
import QBESimpleTable from './qbeTables/qbeSimpleTable/QBESimpleTable.vue'

export default defineComponent({
    name: 'qbe',
    components: { Dialog, Chip, InputSwitch, QBESimpleTable },
    props: { id: { type: String }, visible: { type: Boolean } },
    emits: ['close'],
    data() {
        return {
            qbe: null as iQBE | null,
            customizedDatasetFunctions: {} as any,
            exportLimit: null as number | null,
            entities: [] as any[],
            queryResult: {} as iQueryResult,
            loading: false,
            showEntitiesLists: true,
            simpleTable: false
        }
    },
    watch: {
        async id() {
            // await this.loadPage()
        }
    },
    async created() {
        // await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadDataset()
            await this.loadCustomizedDatasetFunctions()
            await this.loadExportLimit()
            await this.loadEntities()
            await this.executeQBEQuery()
            this.loading = false
        },
        async loadDataset() {
            // HARDCODED Dataset label/name
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/Bojan%20QBE%20TEST`).then((response: AxiosResponse<any>) => {
                this.qbe = response.data[0]
                if (this.qbe) this.qbe.qbeJSONQuery = JSON.parse(this.qbe.qbeJSONQuery)
            })
            console.log('LOADED QBE: ', this.qbe)
        },
        async loadCustomizedDatasetFunctions() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS/${this.qbe?.qbeDataSourceId}`).then((response: AxiosResponse<any>) => (this.customizedDatasetFunctions = response.data))
            console.log('LOADED CUSTOMIZED DATASET FUNCTIONS: ', this.customizedDatasetFunctions)
        },
        async loadExportLimit() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/EXPORT.LIMITATION`).then((response: AxiosResponse<any>) => (this.exportLimit = response.data))
            console.log('LOADED EXPORT LIMIT: ', this.exportLimit)
        },
        async loadEntities() {
            // HARDCODED SBI_EXECUTION_ID
            await this.$http.get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_TREE_ACTION&SBI_EXECUTION_ID=bbb69a67777811ecb185855116b7fb4d&datamartName=null`).then((response: AxiosResponse<any>) => (this.entities = response.data))
            console.log('LOADED ENTITIES: ', this.entities)
        },
        async executeQBEQuery() {
            // HARDCODED a lot
            if (!this.qbe) return

            const postData = { catalogue: this.qbe?.qbeJSONQuery.catalogue.queries, meta: this.formatQbeMeta(), pars: [], qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
            await this.$http
                .post(process.env.VUE_APP_QBE_PATH + `qbequery/executeQuery/?SBI_EXECUTION_ID=bbb69a67777811ecb185855116b7fb4d&currentQueryId=q1&start=0&limit=25`, postData)
                .then((response: AxiosResponse<any>) => (this.queryResult = response.data))
                .catch(() => {})
            console.log('QUERY RESULT : ', this.queryResult)
        },
        formatQbeMeta() {
            const meta = [] as any[]
            this.qbe?.qbeJSONQuery.catalogue.queries?.forEach((query: iQuery) => {
                query.fields?.forEach((field: iField) => {
                    meta.push({ dataType: field.dataType, displayedName: field.alias, fieldType: field.fieldType.toUpperCase(), format: field.format, name: field.alias, type: field.type })
                })
            })
            return meta
        },
        toggleEntitiesLists() {
            this.showEntitiesLists = !this.showEntitiesLists
        }
    }
})
</script>
<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}

.full-screen-dialog.p-dialog .p-dialog-header,
.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}

.full-screen-dialog.p-dialog .p-dialog-content {
    flex: 1;
}

.entities-lists {
    display: flex;
    flex-direction: column;
    flex: 1;
    border-right: 1px solid #ccc;
    height: 100%;
}

.detail-view {
    display: flex;
    flex-direction: column;
    flex: 3;
}

.derived-entities {
    min-height: 25%;
}
</style>
