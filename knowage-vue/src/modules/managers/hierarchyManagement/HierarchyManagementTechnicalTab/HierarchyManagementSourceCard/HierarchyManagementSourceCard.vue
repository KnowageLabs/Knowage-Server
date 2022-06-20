<template>
    <Card class="p-m-2 p-d-flex p-flex-column hierarchy-scrollable-card">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.hierarchiesSource') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-3">
                    <Calendar class="kn-material-input" v-model="validityDate" :manualInput="true" :showIcon="true" @dateSelect="onValidityDateSelected" />
                </div>
                <div class="p-field p-col-12 p-lg-3">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="selectedDimension" :options="dimensions" optionLabel="DIMENSION_NM" @change="onSelectedDimensionChange" />
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.dimensions') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-3">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="hierarchyType" :options="hierarchyManagementSourceCardDescriptor.hierarchyTypes" :disabled="!selectedDimension" @change="onHierarchyTypeSelected" />
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchyType') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-3">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="selectedHierarchy" :options="hierarchies" optionLabel="HIER_NM" :disabled="!hierarchyType" @change="onHierarchySelected" />
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
                treeMode="info"
                @loading="$emit('loading', $event)"
            ></HierarchyManagementHierarchiesTree>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iDimensionMetadata, iNodeMetadata } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import moment from 'moment'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import hierarchyManagementSourceCardDescriptor from './HierarchyManagementSourceCardDescriptor.json'
import HierarchyManagementHierarchiesTree from '../../HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesTree/HierarchyManagementHierarchiesTree.vue'
import HierarchyManagementHierarchiesFilterCard from '../../HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesFilterCard/HierarchyManagementHierarchiesFilterCard.vue'

export default defineComponent({
    name: 'hierarchy-management-source-card',
    components: { Card, Calendar, Dropdown, HierarchyManagementHierarchiesTree, HierarchyManagementHierarchiesFilterCard },
    props: { dimensions: { type: Array as PropType<iDimension[]> }, optionsDate: { type: Date } },
    emits: ['loading', 'validityDateSelected', 'dimensionSelected', 'nodeMetadataChanged', 'hierarchyTypeSelected', 'hierarchySelected'],
    data() {
        return {
            hierarchyManagementSourceCardDescriptor,
            validityDate: new Date(),
            selectedDimension: null as iDimension | null,
            dimensionMetadata: null as iDimensionMetadata | null,
            nodeMetadata: null as iNodeMetadata | null,
            hierarchyType: '' as string,
            hierarchies: [] as iHierarchy[],
            selectedHierarchy: null as iHierarchy | null,
            filterData: null as { showMissingElements: boolean; afterDate: Date | null } | null,
            tree: null as any
        }
    },
    async created() {},
    methods: {
        async onValidityDateSelected() {
            this.$emit('validityDateSelected', this.validityDate)
            await this.loadNodeMetadata()
        },
        async onSelectedDimensionChange() {
            this.$emit('dimensionSelected', this.selectedDimension)
            await this.loadNodeMetadata()
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
        async loadNodeMetadata() {
            this.$emit('loading', true)
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `hierarchies/nodeMetadata?dimension=${this.selectedDimension?.DIMENSION_NM}&excludeLeaf=false`).then((response: AxiosResponse<any>) => (this.nodeMetadata = response.data))
            this.$emit('nodeMetadataChanged', this.nodeMetadata)
            this.$emit('loading', false)
        },
        async loadHierarchies() {
            this.$emit('loading', true)
            const url = this.hierarchyType === 'MASTER' ? `hierarchiesMaster/getHierarchiesMaster?dimension=${this.selectedDimension?.DIMENSION_NM}` : `hierarchiesTechnical/getHierarchiesTechnical?dimension=${this.selectedDimension?.DIMENSION_NM}`
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.hierarchies = response.data))
            this.$emit('loading', false)
        },
        async loadHierarchyTree() {
            this.$emit('loading', true)
            const date = moment(this.validityDate).format('YYYY-MM-DD')
            let url = `hierarchies/getHierarchyTree?dimension=${this.selectedDimension?.DIMENSION_NM}&filterHierarchy=${this.selectedHierarchy?.HIER_NM}&filterType=${this.hierarchyType}&validityDate=${date}`
            if (this.filterData) {
                if (this.filterData.showMissingElements) {
                    url = url.concat('&optionDate=' + moment(this.optionsDate).format('YYYY-MM-DD'))
                }
                if (this.filterData.afterDate) url = url.concat('&filterDate=' + moment(this.filterData.afterDate).format('YYYY-MM-DD'))
            }
            await this.$http
                .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + url)
                .then((response: AxiosResponse<any>) => {
                    this.tree = response.status === 200 ? response.data : null
                })
                .catch(() => {})
            this.$emit('loading', false)
        },
        onApplyFilters(filterData: { showMissingElements: boolean; afterDate: Date | null }) {
            this.filterData = filterData
            this.loadHierarchyTree()
        }
    }
})
</script>
