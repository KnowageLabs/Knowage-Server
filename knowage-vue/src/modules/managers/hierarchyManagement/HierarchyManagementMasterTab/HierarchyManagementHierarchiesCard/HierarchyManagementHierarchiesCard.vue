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
                            <Calendar v-model="date" :manualInput="true" @dateSelect="loadHierarchyTree"></Calendar>
                        </span>
                    </div>
                    <div class="kn-flex p-d-flex p-flex-row p-ai-center p-jc-around">
                        <Button class="kn-button kn-button--primary hierarchy-management-hierarchies-card-button" :label="$t('common.save')" :disabled="saveButtonDisabled" @click="save" />
                        <div>
                            <Checkbox v-model="backup" :binary="true" :disabled="!treeModel"></Checkbox>
                            <label class="kn-material-input-label p-ml-2"> {{ $t('managers.hierarchyManagement.backup') }}</label>
                        </div>
                    </div>
                </div>

                <div class="p-d-flex p-flex-row p-mt-3">
                    <div class="p-fluid kn-flex">
                        <span class="p-float-label p-m-2">
                            <Dropdown class="kn-material-input" v-model="hierarchyType" :options="hierarchyManagementHierarchiesCardDescriptor.hierarchyTypes" :disabled="!dimension" @change="loadHierarchies"> </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchyType') }} </label>
                        </span>
                    </div>
                    <div class="p-fluid kn-flex">
                        <span class="p-float-label p-m-2">
                            <Dropdown class="kn-material-input" v-model="selectedHierarchy" :options="hierarchies" optionLabel="HIER_NM" :disabled="!dimension" @change="loadHierarchyTree"> </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchies') }} </label>
                        </span>
                    </div>
                </div>

                <HierarchyManagementHierarchiesFilterCard :selectedHierarchy="selectedHierarchy" @applyFilters="onApplyFilters"></HierarchyManagementHierarchiesFilterCard>
                <HierarchyManagementHierarchiesTree v-show="tree" :propTree="tree" :nodeMetadata="nodeMetadata" :selectedDimension="dimension" @treeUpdated="updateTreeModel"></HierarchyManagementHierarchiesTree>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iNodeMetadata, iNode } from '../../HierarchyManagement'
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
    props: { selectedDimension: { type: Object as PropType<iDimension | null> }, nodeMetadata: { type: Object as PropType<iNodeMetadata | null> }, validityDate: { type: Object as PropType<Date | null> } },
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
            treeModel: null as any
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return false
        }
    },
    watch: {
        selectedDimension() {
            this.loadDimension()
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
            this.$emit('loading', false)
        },
        async loadHierarchyTree(filterData: { showMissingElements: boolean; afterDate: Date | null }) {
            console.log('FILTER DATA: ', filterData)
            console.log('VALIDITY DATE: ', this.validityDate)
            this.$emit('loading', true)
            const date = moment(this.date).format('YYYY-MM-DD')
            let url = `hierarchies/getHierarchyTree?dimension=${this.selectedDimension?.DIMENSION_NM}&filterHierarchy=${this.selectedHierarchy?.HIER_NM}&filterType=${this.hierarchyType}&validityDate=${date}`
            if (filterData) {
                if (filterData.showMissingElements) url = url.concat('&filterDimension=' + filterData.showMissingElements)
                if (this.validityDate) url = url.concat('&optionDate=' + moment(this.validityDate).format('YYYY-MM-DD'))
                if (filterData.afterDate) url = url.concat('&filterDate=' + moment(filterData.afterDate).format('YYYY-MM-DD'))
            }
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.tree = response.data))
            this.$emit('loading', false)
        },
        onApplyFilters(filterData: { showMissingElements: boolean; afterDate: Date | null }) {
            console.log('PAYLOAD: ', filterData)
            this.loadHierarchyTree(filterData)
        },
        updateTreeModel(nodes: iNode[]) {
            this.treeModel = [this.formatNodes(nodes)]
        },
        formatNodes(nodes: iNode[]) {
            return nodes.map((node: any) => {
                node = {
                    ...node.data,
                    children: node.children
                }
                if (node.children && node.children.length > 0) {
                    node.children = this.formatNodes(node.children)
                }
                return node
            })
        },
        save() {}
    }
})
</script>

<style lang="scss" scoped>
.hierarchy-management-hierarchies-card-button {
    min-width: 150px;
    max-width: 150px;
}
</style>
