<template>
    <Dialog id="hierarchy-management-node-detail-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false" :style="hierarchyManagementHierarchiesTreeDescriptor.dialog.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.details') }}
                </template>

                <template #end>
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="close" />
                </template>
            </Toolbar>
        </template>
        <div v-if="selectedItem" class="p-fluid p-formgrid p-grid p-m-4">
            <div class="p-col-4 p-p-3" v-for="(item, index) in selectedItem" :key="index">
                <span class="p-float-label">
                    <InputText class="kn-material-input" v-model.trim="item.value" :disabled="true" />
                    <label class="kn-material-input-label"> {{ item.label }}</label>
                </span>
            </div>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dialog from 'primevue/dialog'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'

export default defineComponent({
    name: 'hierarchy-management-node-detail-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, selectedItem: { type: Array as PropType<{ value: string; label: string }[]> } },
    emits: ['close'],
    data() {
        return {
            hierarchyManagementHierarchiesTreeDescriptor
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
        loadNodeData() {},
        save() {},
        close() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#hierarchy-management-node-detail-dialog .p-dialog-header,
#hierarchy-management-node-detail-dialog .p-dialog-content {
    padding: 0;
}
#hierarchy-management-node-detail-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
