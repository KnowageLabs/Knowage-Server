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
        <div v-if="!loading" class="p-d-flex p-flex-row kn-height-full">
            <div v-show="showEntitiesLists" class="entities-lists">
                <div class="p-d-flex p-flex-column kn-flex kn-overflow-hidden">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary kn-flex-0">
                        <template #left>
                            <span>Entities</span>
                        </template>
                        <template #right>
                            <Chip style="background-color:white"> {{ entities.entities.length }} </Chip>
                        </template>
                    </Toolbar>
                    <div class="kn-flex kn-overflow-hidden">
                        <ScrollPanel class="kn-height-full olap-scroll-panel">
                            <ExpandableEntity :availableEntities="entities.entities" />
                        </ScrollPanel>
                    </div>
                </div>
                <div class="p-d-flex p-flex-column kn-overflow-hidden" :class="{ 'derived-entities-toggle': showDerivedList }">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary kn-flex-0" @click="collapseDerivedList">
                        <template #left>
                            <span>Derived Entities</span>
                        </template>
                        <template #right>
                            <Chip style="background-color:white"> {{ qbe.qbeJSONQuery?.catalogue?.queries[0].subqueries.length }} </Chip>
                        </template>
                    </Toolbar>
                    <div v-show="showDerivedList" class="kn-flex kn-overflow-hidden">
                        <ScrollPanel class="kn-height-full olap-scroll-panel">
                            <SubqueryEntity :availableEntities="qbe.qbeJSONQuery?.catalogue?.queries[0].subqueries" />
                        </ScrollPanel>
                    </div>
                </div>
            </div>
            <div class="detail-view p-m-1" v-if="qbe">
                <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                    <template #left>
                        <Button v-if="showEntitiesLists" icon="pi pi-chevron-left" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.hideList')" @click="toggleEntitiesLists" />
                        <Button v-else icon="pi pi-chevron-right" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.showList')" @click="toggleEntitiesLists" />
                    </template>
                    <template #right>
                        <i v-if="qbe.qbeJSONQuery.catalogue.queries[0].fields.length > 0" class="fas fa-eraser kn-cursor-pointer p-mx-2" v-tooltip.top="$t('qbe.viewToolbar.deleteAllSelectedFields')" @click="deleteAllSelectedFields"></i>
                        <i v-if="hiddenColumnsExist" class="pi pi-eye kn-cursor-pointer p-mx-2" v-tooltip.top="$t('qbe.viewToolbar.showHiddenColumns')" @click="showHiddenColumns"></i>
                        <InputSwitch class="p-mr-2" v-model="smartView" />
                        <span>{{ $t('qbe.viewToolbar.smartView') }}</span>
                        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" />
                    </template>
                </Toolbar>
                <div class="kn-flex kn-overflow-y">
                    {{ hiddenColumnsExist }}
                    {{ qbe.qbeJSONQuery?.catalogue?.queries[0] }}
                    <QBESimpleTable v-if="!smartView" :query="qbe.qbeJSONQuery?.catalogue?.queries[0]" @columnVisibilityChanged="checkIfHiddenColumnsExist" @openFilterDialog="openFilterDialog"></QBESimpleTable>
                </div>
            </div>
        </div>

        <QBEFilterDialog :visible="filterDialogVisible" :filterDialogData="filterDialogData" :id="id" :entities="entities?.entities" @close="filterDialogVisible = false" @save="onFiltersSave"></QBEFilterDialog>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { iQBE, iQuery, iField, iQueryResult, iFilter } from './QBE'
import Dialog from 'primevue/dialog'
import Chip from 'primevue/chip'
import InputSwitch from 'primevue/inputswitch'
import QBEFilterDialog from './qbeDialogs/qbeFilterDialog/QBEFilterDialog.vue'
import QBESimpleTable from './qbeTables/qbeSimpleTable/QBESimpleTable.vue'
import ExpandableEntity from '@/modules/qbe/qbeComponents/expandableEntity.vue'
import SubqueryEntity from '@/modules/qbe/qbeComponents/subqueryEntity.vue'
import ScrollPanel from 'primevue/scrollpanel'

export default defineComponent({
    name: 'qbe',
    components: { Dialog, Chip, InputSwitch, ScrollPanel, QBEFilterDialog, QBESimpleTable, ExpandableEntity, SubqueryEntity },
    props: { id: { type: String }, visible: { type: Boolean } },
    emits: ['close'],
    data() {
        return {
            qbe: null as iQBE | null,
            customizedDatasetFunctions: {} as any,
            exportLimit: null as number | null,
            entities: {} as any,
            queryResult: {} as iQueryResult,
            loading: false,
            showEntitiesLists: true,
            smartView: false,
            hiddenColumnsExist: false,
            filterDialogVisible: false,
            filterDialogData: {} as { field: iField; query: iQuery },
            showDerivedList: true
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async created() {
        await this.loadPage()
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
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/Bojan`).then((response: AxiosResponse<any>) => {
                // await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/Darko%20QBE%20Test`).then((response: AxiosResponse<any>) => {
                this.qbe = response.data[0]
                if (this.qbe) this.qbe.qbeJSONQuery = JSON.parse(this.qbe.qbeJSONQuery)
            })
            console.log('LOADED QBE: ', this.qbe)
            console.log('SUBQUERY : ', this.qbe?.qbeJSONQuery?.catalogue?.queries[0].subqueries)
        },
        async loadCustomizedDatasetFunctions() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS/${this.qbe?.qbeDataSourceId}`).then((response: AxiosResponse<any>) => (this.customizedDatasetFunctions = response.data))
            // console.log('LOADED CUSTOMIZED DATASET FUNCTIONS: ', this.customizedDatasetFunctions)
        },
        async loadExportLimit() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/EXPORT.LIMITATION`).then((response: AxiosResponse<any>) => (this.exportLimit = response.data))
            // console.log('LOADED EXPORT LIMIT: ', this.exportLimit)
        },
        async loadEntities() {
            // HARDCODED SBI_EXECUTION_ID
            await this.$http.get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_TREE_ACTION&SBI_EXECUTION_ID=${this.id}&datamartName=null`).then((response: AxiosResponse<any>) => (this.entities = response.data))
            console.log('LOADED ENTITIES: ', this.entities)
        },
        async executeQBEQuery() {
            // HARDCODED a lot
            if (!this.qbe) return

            const postData = { catalogue: this.qbe?.qbeJSONQuery.catalogue.queries, meta: this.formatQbeMeta(), pars: [], qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
            await this.$http
                .post(process.env.VUE_APP_QBE_PATH + `qbequery/executeQuery/?SBI_EXECUTION_ID=${this.id}&currentQueryId=q1&start=0&limit=25`, postData)
                .then((response: AxiosResponse<any>) => (this.queryResult = response.data))
                .catch(() => {})
            // console.log('QUERY RESULT : ', this.queryResult)
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
        },
        collapseDerivedList() {
            this.showDerivedList = !this.showDerivedList
        },
        deleteAllSelectedFields() {
            if (this.qbe) this.qbe.qbeJSONQuery.catalogue.queries[0].fields = []
        },
        checkIfHiddenColumnsExist() {
            if (this.qbe) {
                this.hiddenColumnsExist = false
                for (let i = 0; i < this.qbe.qbeJSONQuery.catalogue.queries[0].fields.length; i++) {
                    console.log(' >>> FIELD: ', this.qbe.qbeJSONQuery.catalogue.queries[0].fields[i])
                    if (!this.qbe.qbeJSONQuery.catalogue.queries[0].fields[i].visible) {
                        this.hiddenColumnsExist = true
                        break
                    }
                }
            }
        },
        showHiddenColumns() {
            if (this.qbe) {
                this.qbe.qbeJSONQuery.catalogue.queries[0].fields.forEach((field: iField) => (field.visible = true))
                this.hiddenColumnsExist = false
            }
        },
        openFilterDialog(payload: { field: iField; query: iQuery }) {
            console.log('PAYLOAD FOR OPEN FILTER: ', payload)
            this.filterDialogData = payload
            this.filterDialogVisible = true
        },
        onFiltersSave(filters: iFilter[]) {
            console.log('QBE QUERY BEFORE FILTERS SAVED: ', this.qbe?.qbeJSONQuery.catalogue.queries[0])
            if (!this.qbe) return

            for (let i = 0; i < filters.length; i++) {
                const tempFilter = filters[i]
                const index = this.qbe.qbeJSONQuery.catalogue.queries[0].filters.findIndex((el: iFilter) => el.filterId === tempFilter.filterId)
                console.log('INDEX: ', index)
                if (index !== -1) {
                    this.qbe.qbeJSONQuery.catalogue.queries[0].filters[index] = tempFilter
                } else {
                    this.qbe.qbeJSONQuery.catalogue.queries[0].filters.push(tempFilter)
                }
            }

            console.log('QBE QUERY AFTER FILTERS SAVED: ', this.qbe?.qbeJSONQuery.catalogue.queries[0])

            // this.qbe.qbeJSONQuery.catalogue.queries[0].expression = this.createExpression(filters)
            console.log('ON FILTERS SAVE: ', filters)
        },
        createExpression(filters: iFilter[]) {
            const filtersLength = filters.length

            let expression = {}
            if (filtersLength === 0) {
                return expression
            } else if (filters.length === 1) {
                const tempFilter = filters[0]
                expression = {
                    type: 'NODE_CONST',
                    childNodes: [],
                    value: '$F{' + tempFilter.filterId + '}',
                    details: {
                        leftOperandAlias: tempFilter.leftOperandAlias,
                        operator: tempFilter.operator,
                        entity: tempFilter.entity,
                        rightOperandValue: tempFilter.rightOperandValue[0]
                    }
                }
            }

            return expression
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

.derived-entities-toggle {
    height: 25%;
}

.olap-scroll-panel .p-scrollpanel-content {
    padding: 0 !important;
}
.olap-scroll-panel .p-scrollpanel-bar {
    background-color: #43749eb6;
    width: 5px;
}
</style>
