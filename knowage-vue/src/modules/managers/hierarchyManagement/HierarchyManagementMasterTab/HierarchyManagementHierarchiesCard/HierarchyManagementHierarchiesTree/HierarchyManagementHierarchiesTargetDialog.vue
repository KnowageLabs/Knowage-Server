<template>
    <Dialog class="kn-dialog--toolbar--primary hierarchies-target-dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.hierarchyManagement.hierarchiesTarget') }}
                </template>
            </Toolbar>
        </template>

        <DataTable class="p-datatable-sm kn-table" :value="targets" v-model:selection="selectedTargets" dataKey="MT_ID">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column selectionMode="multiple" :style="hierarchyManagementHierarchiesTreeDescriptor.selectColumnStyle" />
            <Column field="label" :header="$t('common.label')" :sortable="true">
                <template #body="slotProps">
                    <div>
                        <span
                            ><b> {{ getLabel(slotProps.data) }}</b></span
                        ><br />
                        <span>{{ slotProps.data.PATH_NM_T }}</span>
                    </div>
                </template>
            </Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="close"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.ok') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iHierarchyTarget } from '../../../HierarchyManagement'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'

export default defineComponent({
    name: 'hierarchy-management-node-detail-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, hierarchiesTargets: { type: Array as PropType<iHierarchyTarget[]> } },
    emits: ['save', 'close'],
    data() {
        return {
            hierarchyManagementHierarchiesTreeDescriptor,
            targets: [] as iHierarchyTarget[],
            selectedTargets: [] as iHierarchyTarget[]
        }
    },
    watch: {
        hierarchiesTargets() {
            if (this.visible) this.loadTargets()
        }
    },
    created() {
        this.loadTargets()
    },
    methods: {
        loadTargets() {
            this.targets = this.hierarchiesTargets as iHierarchyTarget[]
        },
        getLabel(target: iHierarchyTarget) {
            return this.$t('managers.hierarchyManagement.hierarchy').toUpperCase() + ': ' + target.HIER_CD_T + ' - ' + target.HIER_NM_T + ' - ' + this.$t('managers.hierarchyManagement.levelNode') + ': ' + target.NODE_LEV_T
        },
        close() {
            this.selectedTargets = []
            this.$emit('close')
        },
        save() {
            this.$emit('save', [...this.selectedTargets])
            this.selectedTargets = []
        }
    }
})
</script>
<style lang="scss">
.hierarchies-target-dialog {
    width: 40%;
}
</style>
