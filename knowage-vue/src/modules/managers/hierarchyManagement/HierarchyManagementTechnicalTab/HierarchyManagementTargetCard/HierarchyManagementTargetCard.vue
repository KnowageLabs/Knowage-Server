<template>
    <Card class="p-m-2 p-d-flex p-flex-column">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.hierarchiesTarget') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6 p-lg-4">
                    <Calendar class="kn-material-input" v-model="optionsDate" :manualInput="true" :showIcon="true" @dateSelect="onOptionsDateSelected" />
                </div>
                <div class="p-field p-col-6 p-lg-3">
                    <Button class="kn-button kn-button--primary" :label="$t('common.create')" :disabled="!selectedDimension" @click="createHierarchy" />
                </div>
                <div class="p-field p-col-6 p-lg-3">
                    <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="true" @click="saveHierarchy" />
                </div>
                <div class="p-field-checkbox p-col-6 p-lg-2">
                    <Checkbox v-model="backup" :binary="true" :disabled="true" />
                    <label class="kn-material-input-label p-ml-2"> {{ $t('managers.hierarchyManagement.backup') }}</label>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="selectedHierarchy" :options="hierarchies" optionLabel="HIER_NM" :disabled="!selectedDimension" @change="onHierarchySelected" />
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchies') }} </label>
                    </span>
                </div>
            </form>

            <HierarchyManagementHierarchiesFilterCard :selectedHierarchy="selectedHierarchy" @applyFilters="onApplyFilters"></HierarchyManagementHierarchiesFilterCard>
            <HierarchyManagementHierarchiesTree
                v-if="tree"
                :propTree="tree"
                :nodeMetadata="nodeMetadata"
                :selectedDimension="selectedDimension"
                :selectedHierarchy="selectedHierarchy"
                :dimensionMetadata="dimensionMetadata"
                :propRelationsMasterTree="[]"
                @loading="$emit('loading', $event)"
            ></HierarchyManagementHierarchiesTree>
            <HierarchyManagementNodeDetailDialog :visible="detailDialogVisible" :selectedNode="selectedNode" :metadata="metadata" :mode="mode" @save="onNodeSave" @close="closeNodeDialog"></HierarchyManagementNodeDetailDialog>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iNodeMetadata, iDimensionMetadata, iNodeMetadataField } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import moment from 'moment'
import HierarchyManagementHierarchiesFilterCard from '../../HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesFilterCard/HierarchyManagementHierarchiesFilterCard.vue'
import HierarchyManagementHierarchiesTree from '../../HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesTree/HierarchyManagementHierarchiesTree.vue'
import HierarchyManagementNodeDetailDialog from '../../HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesTree/HierarchyManagementNodeDetailDialog.vue'
import hierarchyManagementTargetCardDescriptor from './HierarchyManagementTargetCardDescriptor.json'

const crypto = require('crypto')

export default defineComponent({
    name: 'hierarchy-management-target-card',
    components: { Card, Calendar, Checkbox, Dropdown, HierarchyManagementHierarchiesFilterCard, HierarchyManagementHierarchiesTree, HierarchyManagementNodeDetailDialog },
    props: { selectedDimension: { type: Object as PropType<iDimension | null> }, validityDate: { type: Date }, dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> }, nodeMetadata: { type: Object as PropType<iNodeMetadata | null> } },
    emits: ['loading', 'optionsDateSelected'],
    data() {
        return {
            hierarchyManagementTargetCardDescriptor,
            optionsDate: new Date(),
            backup: true,
            hierarchies: [] as iHierarchy[],
            selectedHierarchy: null as iHierarchy | null,
            filterData: null as { showMissingElements: boolean; afterDate: Date | null } | null,
            tree: null as any,
            detailDialogVisible: false,
            selectedNode: null as any,
            mode: 'createRoot',
            metadata: [] as iNodeMetadataField[]
        }
    },
    watch: {
        async selectedDimension() {
            if (this.selectedDimension) await this.loadTechnicalHierarchies()
        }
    },
    async created() {},
    methods: {
        async onOptionsDateSelected() {
            this.$emit('optionsDateSelected', this.optionsDate)
            await this.loadHierarchyTree()
        },
        async loadTechnicalHierarchies() {
            this.$emit('loading', true)
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `hierarchiesTechnical/getHierarchiesTechnical?dimension=${this.selectedDimension?.DIMENSION_NM}`).then((response: AxiosResponse<any>) => (this.hierarchies = response.data))
            this.$emit('loading', false)
        },
        async onHierarchySelected() {
            await this.loadHierarchyTree()
        },
        async loadHierarchyTree() {
            this.$emit('loading', true)
            const date = moment(this.optionsDate).format('YYYY-MM-DD')
            let url = `hierarchies/getHierarchyTree?dimension=${this.selectedDimension?.DIMENSION_NM}&filterHierarchy=${this.selectedHierarchy?.HIER_NM}&filterType=TECHNICAL&validityDate=${date}`
            if (this.filterData) {
                if (this.filterData.showMissingElements) {
                    url = url.concat('&filterDimension=' + this.filterData.showMissingElements)
                    url = url.concat('&optionDate=' + moment(this.validityDate).format('YYYY-MM-DD'))
                }
                if (this.filterData.afterDate) url = url.concat('&filterDate=' + moment(this.filterData.afterDate).format('YYYY-MM-DD'))
            }
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => {
                this.tree = response.status === 200 ? response.data : null
            })
            this.$emit('loading', false)
        },
        onApplyFilters(filterData: { showMissingElements: boolean; afterDate: Date | null }) {
            this.filterData = filterData
            this.loadHierarchyTree()
        },
        createHierarchy() {
            this.selectedNode = { HIER_TP: 'TECHNICAL', aliasId: 'HIER_CD', aliasName: 'HIER_NM', leaf: false, root: true, children: [] }
            this.metadata = this.nodeMetadata ? this.nodeMetadata.GENERAL_FIELDS : []
            for (let i = 0; i < this.metadata.length; i++) {
                console.log('METADATA FIELD: ', this.metadata[i])
                const temp = this.metadata[i] as any
                this.selectedNode[temp.ID] = ''
                if (temp.TYPE === 'Number') {
                    this.selectedNode[temp.ID] = -1
                } else if (temp.TYPE === 'Date') {
                    this.selectedNode[temp.ID] = new Date()
                } else if (temp.FIX_VALUE) {
                    this.selectedNode[temp.ID] = temp.FIX_VALUE
                }
            }

            this.detailDialogVisible = true
        },
        closeNodeDialog() {
            this.selectedNode = null
            this.metadata = []
            this.mode = ''
            this.detailDialogVisible = false
        },
        onNodeSave(payload: any) {
            console.log('PAYLOAD ON SAVE ROOT: ', payload)
            const node = payload.node
            if (payload.mode === 'createRoot') {
                this.selectedHierarchy = null
                this.tree = { ...payload.node, key: crypto.randomBytes(16).toString('hex'), id: node.name, label: node.name, children: node.children, data: node, style: this.hierarchyManagementTargetCardDescriptor.node.style, leaf: false, parent: null }
            }
            this.detailDialogVisible = false
        },
        saveHierarchy() {}
    }
})
</script>
