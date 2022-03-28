<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.dimensions') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <div>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <div class="kn-flex">
                        <span class="p-float-label">
                            <Calendar v-model="validityDate" :manualInput="true"></Calendar>
                        </span>
                    </div>
                    <div id="hierarchy-management-dimension-dropdown-container" class="p-fluid">
                        <span class="p-float-label p-m-2">
                            <Dropdown class="kn-material-input" v-model="selectedDimension" :options="dimensions" optionLabel="DIMENSION_NM" @change="onSelectedDimensionChange"> </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.dimensions') }} </label>
                        </span>
                    </div>
                </div>

                <HierarchyManagementDimensionsTable :dimensionData="dimensionData"></HierarchyManagementDimensionsTable>

                <div class="p-d-flex p-flex-row p-jc-around p-mt-2">
                    <Button class="kn-button kn-button--primary hierarchy-management-dimension-card-button" :label="$t('managers.hierarchyManagement.createHierarchyMaster')" :disabled="!selectedDimension" @click="openHierarchyMasterDialog" />
                    <Button class="kn-button kn-button--primary hierarchy-management-dimension-card-button" :label="$t('managers.hierarchyManagement.synchronize')" @click="synchronize" />
                </div>
            </div>
            <HierarchyManagementHierarchyMasterDialog :visible="hierarchyMasterDialogVisible" :nodeMetadata="nodeMetadata" :dimensionMetadata="dimensionMetadata" @close="hierarchyMasterDialogVisible = false"></HierarchyManagementHierarchyMasterDialog>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iDimensionMetadata, iNodeMetadata } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import moment from 'moment'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import HierarchyManagementDimensionsTable from './HierarchyManagementDimensionsTable/HierarchyManagementDimensionsTable.vue'
import HierarchyManagementHierarchyMasterDialog from './HierarchyManagementHierarchyMasterDialog/HierarchyManagementHierarchyMasterDialog.vue'

export default defineComponent({
    name: 'hierarchy-management-dimensions-card',
    components: { Card, Calendar, Dropdown, HierarchyManagementDimensionsTable, HierarchyManagementHierarchyMasterDialog },
    props: { dimensions: { type: Array as PropType<iDimension[]> } },
    emits: ['loading'],
    data() {
        return {
            validityDate: new Date(),
            selectedDimension: null as iDimension | null,
            hierarchyMasterDialogVisible: false,
            dimensionData: null as any,
            dimensionMetadata: null as iDimensionMetadata | null,
            nodeMetadata: null as iNodeMetadata | null
        }
    },
    async created() {},
    methods: {
        async onSelectedDimensionChange() {
            await this.loadDimensionData()
            await this.loadDimensionMetadata()
            await this.loadNodeMetadata()
        },
        async loadDimensionData() {
            this.$emit('loading', true)
            this.dimensionData = []
            const date = moment(this.validityDate).format('YYYY-MM-DD')
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dimensions/dimensionData?dimension=${this.selectedDimension?.DIMENSION_NM}&validityDate=${date}`).then((response: AxiosResponse<any>) => (this.dimensionData = response.data))
            this.$emit('loading', false)
        },
        async loadDimensionMetadata() {
            this.$emit('loading', true)
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dimensions/dimensionMetadata?dimension=${this.selectedDimension?.DIMENSION_NM}`).then((response: AxiosResponse<any>) => (this.dimensionMetadata = response.data))
            this.$emit('loading', false)
        },
        async loadNodeMetadata() {
            this.$emit('loading', true)
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `hierarchies/nodeMetadata?dimension=${this.selectedDimension?.DIMENSION_NM}&excludeLeaf=false`).then((response: AxiosResponse<any>) => (this.nodeMetadata = response.data))
            this.$emit('loading', false)
        },
        openHierarchyMasterDialog() {
            this.hierarchyMasterDialogVisible = true
        },
        synchronize() {}
    }
})
</script>

<style lang="scss" scoped>
#hierarchy-management-dimension-dropdown-container {
    flex: 3;
}

.hierarchy-management-dimension-card-button {
    min-width: 250px;
    max-width: 250px;
}
</style>
