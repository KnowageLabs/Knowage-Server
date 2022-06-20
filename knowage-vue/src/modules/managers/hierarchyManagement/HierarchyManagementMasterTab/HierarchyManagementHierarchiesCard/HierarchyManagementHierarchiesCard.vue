<template>
    <Card class="p-m-2 p-d-flex p-flex-column hierarchy-scrollable-card">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.hierarchies') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <Calendar class="kn-material-input" v-model="date" :manualInput="true" :showIcon="true" @dateSelect="onTreeDateChanged" />
                </div>
                <div class="p-field p-col-8 p-lg-4">
                    <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="!treeModel" @click="handleSaveHiararchy" />
                </div>
                <div class="p-field-checkbox p-col-4 p-lg-2">
                    <Checkbox v-model="backup" :binary="true" :disabled="!treeModel" />
                    <label class="kn-material-input-label p-ml-2"> {{ $t('managers.hierarchyManagement.backup') }}</label>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="hierarchyType" :options="hierarchyManagementHierarchiesCardDescriptor.hierarchyTypes" :disabled="!dimension" @change="onHierarchyTypeSelected" />
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchyType') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="selectedHierarchy" :options="hierarchies" optionLabel="HIER_NM" :disabled="!dimension" @change="onHierarchySelected" />
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchies') }} </label>
                    </span>
                </div>
            </form>

            <HierarchyManagementHierarchiesFilterCard :selectedHierarchy="selectedHierarchy" @applyFilters="onApplyFilters" />
            <HierarchyManagementHierarchiesTree
                v-show="tree"
                :propTree="tree"
                :nodeMetadata="nodeMetadata"
                :selectedDimension="dimension"
                :selectedHierarchy="selectedHierarchy"
                :dimensionMetadata="dimensionMetadata"
                :propRelationsMasterTree="relationsMasterTree"
                @treeUpdated="updateTreeModel"
                @loading="$emit('loading', $event)"
            />
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iNodeMetadata, iNode, iDimensionMetadata } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import moment from 'moment'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import hierarchyManagementHierarchiesCardDescriptor from './HierarchyManagementHierarchiesCardDescriptor.json'
import HierarchyManagementHierarchiesTree from './HierarchyManagementHierarchiesTree/HierarchyManagementHierarchiesTree.vue'
import HierarchyManagementHierarchiesFilterCard from './HierarchyManagementHierarchiesFilterCard/HierarchyManagementHierarchiesFilterCard.vue'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-card',
    components: { Card, Calendar, Checkbox, Dropdown, HierarchyManagementHierarchiesTree, HierarchyManagementHierarchiesFilterCard },
    props: {
        selectedDimension: { type: Object as PropType<iDimension | null> },
        nodeMetadata: { type: Object as PropType<iNodeMetadata | null> },
        validityDate: { type: Object as PropType<Date | null> },
        dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> },
        synchronizationTrigger: { type: Boolean },
        reloadHierarchiesTrigger: { type: Boolean }
    },
    emits: ['loading', 'hierarchySelected', 'dateSelected', 'hierarchyTypeSelected'],
    data() {
        return {
            hierarchyManagementHierarchiesCardDescriptor,
            date: new Date(),
            backup: true,
            dimension: null as iDimension | null,
            hierarchyType: '' as string,
            hierarchies: [] as iHierarchy[],
            selectedHierarchy: null as iHierarchy | null,
            tree: null as any,
            treeModel: null as any,
            filterData: null as { showMissingElements: boolean; afterDate: Date | null } | null,
            relationsMasterTree: [] as any[]
        }
    },
    computed: {},
    watch: {
        selectedDimension() {
            this.loadDimension()
        },
        synchronizationTrigger() {
            this.loadHierarchyTree()
        },
        reloadHierarchiesTrigger() {
            if (this.hierarchyType === 'MASTER') this.loadHierarchies()
        }
    },
    created() {
        this.loadDimension()
    },
    methods: {
        loadDimension() {
            this.dimension = this.selectedDimension as iDimension
        },

        async loadHierarchies() {
            this.$emit('loading', true)
            const url = this.hierarchyType === 'MASTER' ? `hierarchiesMaster/getHierarchiesMaster?dimension=${this.selectedDimension?.DIMENSION_NM}` : `hierarchiesTechnical/getHierarchiesTechnical?dimension=${this.selectedDimension?.DIMENSION_NM}`
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.hierarchies = response.data))
            this.relationsMasterTree = []
            this.$emit('loading', false)
        },
        async loadHierarchyTree() {
            this.$emit('loading', true)
            const date = moment(this.date).format('YYYY-MM-DD')
            let url = `hierarchies/getHierarchyTree?dimension=${this.selectedDimension?.DIMENSION_NM}&filterHierarchy=${this.selectedHierarchy?.HIER_NM}&filterType=${this.hierarchyType}&validityDate=${date}`
            if (this.filterData) {
                if (this.filterData.showMissingElements) url = url.concat('&filterDimension=' + this.filterData.showMissingElements)
                if (this.validityDate) url = url.concat('&optionDate=' + moment(this.validityDate).format('YYYY-MM-DD'))
                if (this.filterData.afterDate) url = url.concat('&filterDate=' + moment(this.filterData.afterDate).format('YYYY-MM-DD'))
            }
            await this.$http
                .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + url)
                .then((response: AxiosResponse<any>) => (this.tree = response.data))
                .catch(() => {})
            this.relationsMasterTree = []
            this.$emit('loading', false)
        },
        async onHierarchyTypeSelected() {
            this.selectedHierarchy = null
            this.$emit('hierarchyTypeSelected', this.hierarchyType)
            this.$emit('hierarchySelected', this.selectedHierarchy)
            await this.loadHierarchies()
        },
        async onHierarchySelected() {
            this.$emit('hierarchySelected', this.selectedHierarchy)
            await this.loadHierarchyTree()
        },
        async onTreeDateChanged() {
            this.$emit('dateSelected', this.date)
            await this.loadHierarchyTree()
        },
        onApplyFilters(filterData: { showMissingElements: boolean; afterDate: Date | null }) {
            this.filterData = filterData
            this.loadHierarchyTree()
        },
        updateTreeModel(nodes: iNode[]) {
            this.treeModel = this.formatNodes(nodes)[0]
        },
        formatNodes(nodes: iNode[]) {
            return nodes?.map((node: any) => {
                node = {
                    ...node.data,
                    children: node.children
                }
                if (node.parent) delete node.parent
                if (node.leaf && node.children) delete node.children
                if (node.children && node.children.length > 0) {
                    node.children = this.formatNodes(node.children)
                }
                return node
            })
        },
        async handleSaveHiararchy() {
            this.$emit('loading', true)

            if (this.checkIfNodesWithoutChildren(this.treeModel)) {
                this.$confirm.require({
                    message: this.$t('managers.hierarchyManagement.parentWithoutChildrenConfirm'),
                    header: this.$t('managers.hierarchyManagement.saveChanges'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: async () => await this.saveHierarchy()
                })
            } else {
                await this.saveHierarchy()
            }
            this.$emit('loading', false)
        },
        async saveHierarchy() {
            if (!this.dimension || !this.selectedHierarchy) return

            this.updateLevelRecursive(this.treeModel, 0)
            const postData = {
                dimension: this.dimension.DIMENSION_NM,
                code: this.selectedHierarchy.HIER_CD,
                description: this.selectedHierarchy.HIER_DS,
                name: this.selectedHierarchy.HIER_NM,
                type: this.selectedHierarchy.HIER_TP,
                dateValidity: moment(this.date).format('YYYY-MM-DD'),
                isInsert: false,
                doBackup: this.backup,
                relationsMT: this.relationsMasterTree,
                root: this.treeModel
            }
            await this.$http
                .post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `hierarchies/saveHierarchy`, postData)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.response === 'ok') {
                        this.$store.commit('setInfo', { title: this.$t('common.toast.createTitle'), msg: this.$t('common.toast.success') })
                        this.loadHierarchyTree()
                    }
                })
                .catch(() => {})
        },
        updateLevelRecursive(node, level) {
            if (level !== 0) node.LEVEL = level
            if (node.children && node.children.length > 0) {
                for (let i = 0; i < node.children.length; i++) {
                    this.updateLevelRecursive(node.children[i], level + 1)
                }
            }
        },
        checkIfNodesWithoutChildren(node: any) {
            if (!node.root && !node.leaf && node.children?.length === 0) {
                return true
            } else if (node.children != null) {
                let result = false as any
                for (let i = 0; result === false && i < node.children.length; i++) {
                    result = this.checkIfNodesWithoutChildren(node.children[i])
                }
                return result
            }
            return false
        }
    }
})
</script>

<style lang="scss" scoped></style>
