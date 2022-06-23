<template>
    <Card class="p-m-2 p-d-flex p-flex-column hierarchy-scrollable-card">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.dimensions') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <Calendar class="kn-material-input" v-model="validityDate" :manualInput="true" :showIcon="true" @dateSelect="onValidityDateSelected" />
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="selectedDimension" :options="dimensions" optionLabel="DIMENSION_NM" @change="onSelectedDimensionChange" />
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.dimensions') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-6">
                    <Button class="kn-button kn-button--primary kn-truncated" :label="$t('managers.hierarchyManagement.createHierarchyMaster')" :disabled="!selectedDimension" @click="openHierarchyMasterDialog" />
                </div>
                <div class="p-field p-col-6">
                    <Button class="kn-button kn-button--primary" :label="$t('managers.hierarchyManagement.synchronize')" :disabled="synchronizeButtonDisabled" @click="synchronize" />
                </div>
            </form>

            <HierarchyManagementDimensionsFilterCard v-show="selectedDimension" :dimensionFilters="dimensionFilters" :selectedHierarchy="selectedHierarchy" @applyFilters="onApplyFilters" />
            <HierarchyManagementDimensionsTable v-show="dimensionData" :dimensionData="dimensionData" />
        </template>
    </Card>

    <HierarchyManagementHierarchyMasterDialog
        :visible="hierarchyMasterDialogVisible"
        :nodeMetadata="nodeMetadata"
        :dimensionMetadata="dimensionMetadata"
        :validityDate="validityDate"
        :selectedDimension="selectedDimension"
        :dimensionFilters="dimensionFilters"
        @close="hierarchyMasterDialogVisible = false"
        @masterHierarchyCreated="onMasterHierarchyCreated"
    />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iDimensionMetadata, iNodeMetadata, iDimensionFilter, iHierarchy } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import moment from 'moment'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import HierarchyManagementDimensionsTable from './HierarchyManagementDimensionsTable/HierarchyManagementDimensionsTable.vue'
import HierarchyManagementHierarchyMasterDialog from './HierarchyManagementHierarchyMasterDialog/HierarchyManagementHierarchyMasterDialog.vue'
import HierarchyManagementDimensionsFilterCard from './HierarchyManagementDimensionsFilterCard/HierarchyManagementDimensionsFilterCard.vue'

export default defineComponent({
    name: 'hierarchy-management-dimensions-card',
    components: { Card, Calendar, Dropdown, HierarchyManagementDimensionsTable, HierarchyManagementHierarchyMasterDialog, HierarchyManagementDimensionsFilterCard },
    props: { dimensions: { type: Array as PropType<iDimension[]> }, hierarchyType: { type: String }, selectedHierarchy: { type: Object as PropType<iHierarchy | null> }, validityTreeDate: { type: Object as PropType<Date | null> } },
    emits: ['loading', 'dimensionSelected', 'nodeMetadataChanged', 'validityDateSelected', 'dimensionMetadataChanged', 'synchronized', 'masterHierarchyCreated'],
    data() {
        return {
            validityDate: new Date(),
            selectedDimension: null as iDimension | null,
            hierarchyMasterDialogVisible: false,
            dimensionData: null as any,
            dimensionMetadata: null as iDimensionMetadata | null,
            nodeMetadata: null as iNodeMetadata | null,
            dimensionFilters: [] as iDimensionFilter[],
            filterData: null as { filters: iDimensionFilter[]; showMissingElements: boolean } | null
        }
    },
    computed: {
        synchronizeButtonDisabled(): boolean {
            return !this.dimensionData || this.dimensionData.root.length === 0 || this.hierarchyType?.toUpperCase() !== 'MASTER' || !this.selectedHierarchy
        }
    },
    async created() {},
    methods: {
        async onValidityDateSelected() {
            this.$emit('validityDateSelected', this.validityDate)
            await this.loadData()
        },
        async onSelectedDimensionChange() {
            this.$emit('dimensionSelected', this.selectedDimension)
            await this.loadData()
        },
        async loadData() {
            await this.loadDimensionData()
            await this.loadDimensionMetadata()
            await this.loadNodeMetadata()
            await this.loadDimensionFilters()
        },
        async loadDimensionData() {
            this.$emit('loading', true)
            this.dimensionData = null
            const date = moment(this.validityDate).format('YYYY-MM-DD')
            let url = `dimensions/dimensionData?dimension=${this.selectedDimension?.DIMENSION_NM}&validityDate=${date}`
            if (this.filterData) {
                if (this.filterData.showMissingElements) {
                    const filterDate = this.validityTreeDate ? moment(this.validityTreeDate).format('YYYY-MM-DD') : ''
                    url = url.concat('&filterDate=' + filterDate)
                    url = url.concat('&filterHierType=' + this.hierarchyType)
                    url = url.concat('&filterHierarchy=' + this.selectedHierarchy?.HIER_NM)
                }
                if (this.filterData.filters.length > 0) url = url.concat('&optionalFilters=' + encodeURI(JSON.stringify(this.filterData.filters)))
            }
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.dimensionData = response.data))
            this.$emit('loading', false)
        },
        async loadDimensionMetadata() {
            this.$emit('loading', true)
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `dimensions/dimensionMetadata?dimension=${this.selectedDimension?.DIMENSION_NM}`).then((response: AxiosResponse<any>) => (this.dimensionMetadata = response.data))
            this.$emit('dimensionMetadataChanged', this.dimensionMetadata)
            this.$emit('loading', false)
        },
        async loadNodeMetadata() {
            this.$emit('loading', true)
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `hierarchies/nodeMetadata?dimension=${this.selectedDimension?.DIMENSION_NM}&excludeLeaf=false`).then((response: AxiosResponse<any>) => (this.nodeMetadata = response.data))
            this.$emit('nodeMetadataChanged', this.nodeMetadata)
            this.$emit('loading', false)
        },
        async loadDimensionFilters() {
            this.$emit('loading', true)
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `dimensions/dimensionFilterMetadata?dimension=CDC`).then((response: AxiosResponse<any>) => (this.dimensionFilters = response.data?.DIM_FILTERS))
            this.$emit('loading', false)
        },
        onApplyFilters(filtersData: { filters: iDimensionFilter[]; showMissingElements: boolean }) {
            this.filterData = filtersData
            this.loadDimensionData()
        },
        openHierarchyMasterDialog() {
            this.hierarchyMasterDialogVisible = true
        },
        async synchronize() {
            this.$emit('loading', true)
            const postData = {
                dimension: this.selectedDimension?.DIMENSION_NM,
                validityDate: moment(this.validityDate).format('YYYY-MM-DD'),
                validityTreeDate: this.validityTreeDate ? moment(this.validityTreeDate).format('YYYY-MM-DD') : null,
                filterHierarchy: this.selectedHierarchy?.HIER_NM,
                filterHierType: this.hierarchyType,
                optionalFilters: this.filterData ? this.filterData.filters : []
            }
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `hierarchiesMaster/syncronizeHierarchyMaster`, postData)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.response === 'ok') {
                        this.store.commit('setInfo', {
                            title: this.$t('common.info.info'),
                            msg: this.$t('managers.hierarchyManagement.synchronizationSuccess')
                        })
                        this.$emit('synchronized')
                    }
                })
                .catch(() => {})
            this.$emit('loading', false)
        },
        onMasterHierarchyCreated() {
            this.hierarchyMasterDialogVisible = false
            this.$emit('masterHierarchyCreated')
        }
    }
})
</script>
