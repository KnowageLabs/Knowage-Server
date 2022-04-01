<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.hierarchies') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <div>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <div class="kn-flex">
                        <span class="p-float-label">
                            <Calendar v-model="date" :manualInput="true" @dateSelect="onTreeDateChanged"></Calendar>
                        </span>
                    </div>
                    <div class="kn-flex p-d-flex p-flex-row p-ai-center p-jc-around">
                        <Button class="kn-button kn-button--primary hierarchy-management-hierarchies-card-button" :label="$t('common.save')" :disabled="!treeModel" @click="saveHierarchy" />
                        <div>
                            <Checkbox v-model="backup" :binary="true" :disabled="!treeModel"></Checkbox>
                            <label class="kn-material-input-label p-ml-2"> {{ $t('managers.hierarchyManagement.backup') }}</label>
                        </div>
                    </div>
                </div>

                <div class="p-d-flex p-flex-row p-mt-3">
                    <div class="p-fluid kn-flex">
                        <span class="p-float-label p-m-2">
                            <Dropdown class="kn-material-input" v-model="hierarchyType" :options="hierarchyManagementHierarchiesCardDescriptor.hierarchyTypes" :disabled="!dimension" @change="onHierarchyTypeSelected"> </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchyType') }} </label>
                        </span>
                    </div>
                    <div class="p-fluid kn-flex">
                        <span class="p-float-label p-m-2">
                            <Dropdown class="kn-material-input" v-model="selectedHierarchy" :options="hierarchies" optionLabel="HIER_NM" :disabled="!dimension" @change="onHierarchySelected"> </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchies') }} </label>
                        </span>
                    </div>
                </div>

                <HierarchyManagementHierarchiesFilterCard :selectedHierarchy="selectedHierarchy" @applyFilters="onApplyFilters"></HierarchyManagementHierarchiesFilterCard>
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
                ></HierarchyManagementHierarchiesTree>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iNodeMetadata, iNode, iDimensionMetadata } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import moment from 'moment'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import hierarchyManagementHierarchiesCardDescriptor from './HierarchyManagementHierarchiesCardDescriptor.json'
import HierarchyManagementHierarchiesTree from './HierarchyManagementHierarchiesTree/HierarchyManagementHierarchiesTree.vue'
import HierarchyManagementHierarchiesFilterCard from './HierarchyManagementHierarchiesFilterCard/HierarchyManagementHierarchiesFilterCard.vue'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-card',
    components: { Calendar, Checkbox, Dropdown, HierarchyManagementHierarchiesTree, HierarchyManagementHierarchiesFilterCard },
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
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.hierarchies = response.data))
            this.relationsMasterTree = []
            this.$emit('loading', false)
        },
        async loadHierarchyTree() {
            console.log('FILTER DATA: ', this.filterData)
            console.log('VALIDITY DATE: ', this.validityDate)
            this.$emit('loading', true)
            const date = moment(this.date).format('YYYY-MM-DD')
            let url = `hierarchies/getHierarchyTree?dimension=${this.selectedDimension?.DIMENSION_NM}&filterHierarchy=${this.selectedHierarchy?.HIER_NM}&filterType=${this.hierarchyType}&validityDate=${date}`
            if (this.filterData) {
                if (this.filterData.showMissingElements) url = url.concat('&filterDimension=' + this.filterData.showMissingElements)
                if (this.validityDate) url = url.concat('&optionDate=' + moment(this.validityDate).format('YYYY-MM-DD'))
                if (this.filterData.afterDate) url = url.concat('&filterDate=' + moment(this.filterData.afterDate).format('YYYY-MM-DD'))
            }
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.tree = response.data))
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
            console.log('PAYLOAD: ', filterData)
            this.filterData = filterData
            this.loadHierarchyTree()
        },
        updateTreeModel(nodes: iNode[]) {
            this.treeModel = this.formatNodes(nodes)[0]
            console.log('UPDATED TREE MODEL: ', this.treeModel)
        },
        formatNodes(nodes: iNode[]) {
            return nodes.map((node: any) => {
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
        async saveHierarchy() {
            if (!this.dimension || !this.selectedHierarchy) return
            this.$emit('loading', true)
            // TODO see relations MT
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
            console.log(' >>> TREE MODEL TO SAVE: ', this.treeModel)
            console.log(' >>> NODES WITHOUT CHILDREN EXIST: ', this.checkIfNodesWithoutChildren(this.treeModel))
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `hierarchies/saveHierarchy`, postData).then((response: AxiosResponse<any>) => {
                if (response.data.response === 'ok') {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.createTitle'), msg: this.$t('common.toast.success') })
                }
            })
            this.$emit('loading', false)
        },
        updateLevelRecursive(node, level) {
            console.log(' !!! FORMAT TREE LEVELS: ', this.treeModel)
            if (level !== 0) node.LEVEL = level
            if (node.children && node.children.length > 0) {
                for (let i = 0; i < node.children.length; i++) {
                    this.updateLevelRecursive(node.children[i], level + 1)
                }
            }
        },
        checkIfNodesWithoutChildren(node: iNode) {
            if (!node.data.root && !node.data.leaf && node.children?.length === 0) {
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

<style lang="scss" scoped>
.hierarchy-management-hierarchies-card-button {
    min-width: 150px;
    max-width: 150px;
}
</style>
