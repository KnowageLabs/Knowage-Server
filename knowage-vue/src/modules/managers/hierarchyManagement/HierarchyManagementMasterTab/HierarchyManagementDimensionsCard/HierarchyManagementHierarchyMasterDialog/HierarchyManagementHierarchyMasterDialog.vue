<template>
    <Dialog id="hierarchy-management-hierarchy-master-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false" :style="hierarchyManagementHierarchyMasterDialogDescriptor.dialog.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.hierarchyManagement.createHierarchyMaster') }}
                </template>

                <template #end>
                    <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="save" />
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="close" />
                </template>
            </Toolbar>
        </template>

        <div>
            <HierarchyManagementHierarchyMasterForm :nodeGeneralFields="nodeGeneralFields"></HierarchyManagementHierarchyMasterForm>
            <HierarchyManagementHierarchyMasterSelectList class="" :dimensionMetadata="dimensionMetadata"></HierarchyManagementHierarchyMasterSelectList>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iNodeMetadata, iNodeMetadataField, iDimensionMetadata } from '../../../HierarchyManagement'
import Dialog from 'primevue/dialog'
import hierarchyManagementHierarchyMasterDialogDescriptor from './HierarchyManagementHierarchyDescriptor.json'
import HierarchyManagementHierarchyMasterForm from './HierarchyManagementHierarchyMasterForm.vue'
import HierarchyManagementHierarchyMasterSelectList from './HierarchyManagementHierarchyMasterSelectList.vue'

export default defineComponent({
    name: 'hierarchy-management-hierarchy-master-dialog',
    components: { Dialog, HierarchyManagementHierarchyMasterForm, HierarchyManagementHierarchyMasterSelectList },
    props: { visible: { type: Boolean }, nodeMetadata: { type: Object as PropType<iNodeMetadata | null> }, dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> } },
    emits: ['close'],
    data() {
        return {
            hierarchyManagementHierarchyMasterDialogDescriptor,
            nodeGeneralFields: [] as iNodeMetadataField[]
        }
    },
    watch: {
        nodeMetadata() {
            this.loadNodeData()
        }
    },
    async created() {
        this.loadNodeData()
    },
    methods: {
        loadNodeData() {
            this.nodeGeneralFields = this.nodeMetadata
                ? this.nodeMetadata.GENERAL_FIELDS.map((field: iNodeMetadataField) => {
                      return { ...field, value: '' }
                  })
                : []
        },
        save() {},
        close() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#hierarchy-management-hierarchy-master-dialog .p-dialog-header,
#hierarchy-management-hierarchy-master-dialog .p-dialog-content {
    padding: 0;
}
#hierarchy-management-hierarchy-master-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
