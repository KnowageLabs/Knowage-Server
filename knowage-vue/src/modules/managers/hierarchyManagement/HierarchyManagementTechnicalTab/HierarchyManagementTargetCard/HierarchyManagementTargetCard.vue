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
                    <Button class="kn-button kn-button--primary" :label="$t('common.create')" :disabled="true" @click="createHierarchy" />
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
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy, iNodeMetadata, iDimensionMetadata } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import moment from 'moment'
import HierarchyManagementHierarchiesFilterCard from '../../HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesFilterCard/HierarchyManagementHierarchiesFilterCard.vue'
import HierarchyManagementHierarchiesTree from '../../HierarchyManagementMasterTab/HierarchyManagementHierarchiesCard/HierarchyManagementHierarchiesTree/HierarchyManagementHierarchiesTree.vue'

export default defineComponent({
    name: 'hierarchy-management-target-card',
    components: { Card, Calendar, Checkbox, Dropdown, HierarchyManagementHierarchiesFilterCard, HierarchyManagementHierarchiesTree },
    props: { selectedDimension: { type: Object as PropType<iDimension | null> }, validityDate: { type: Date }, dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> }, nodeMetadata: { type: Object as PropType<iNodeMetadata | null> } },
    emits: ['loading', 'optionsDateSelected'],
    data() {
        return {
            optionsDate: new Date(),
            backup: true,
            hierarchies: [] as iHierarchy[],
            selectedHierarchy: null as iHierarchy | null,
            filterData: null as { showMissingElements: boolean; afterDate: Date | null } | null,
            tree: null as any
        }
    },
    watch: {
        async selectedDimension() {
            if (this.selectedDimension) await this.loadTechnicalHierarchies()
        }
    },
    async created() {},
    methods: {
        onOptionsDateSelected() {
            this.$emit('optionsDateSelected', this.optionsDate)
            // await this.loadNodeMetadata()
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
        createHierarchy() {},
        saveHierarchy() {}
    }
})
</script>
