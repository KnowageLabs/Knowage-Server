<template>
    <Dialog id="hierarchy-management-dimension-info-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false" :style="hierarchyManagementDimensionsTableDescriptor.dialog.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('common.details') }}
                </template>
            </Toolbar>
        </template>
        <form v-if="selectedItem" class="marginated-form p-fluid p-formgrid p-grid kn-flex p-m-2">
            <div v-for="(item, index) in selectedItem" :key="index" class="p-field p-col-12 p-md-6 p-lg-4">
                <span class="p-float-label">
                    <InputText v-model.trim="item.value" class="kn-material-input" :disabled="true" />
                    <label class="kn-material-input-label"> {{ item.label }}</label>
                </span>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--primary" :label="$t('common.ok')" @click="close" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dialog from 'primevue/dialog'
import hierarchyManagementDimensionsTableDescriptor from './HierarchyManagementDimensionsTableDescriptor.json'

export default defineComponent({
    name: 'hierarchy-management-dimension-info-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, selectedItem: { type: Array as PropType<{ value: string; label: string }[]> } },
    emits: ['close'],
    data() {
        return {
            hierarchyManagementDimensionsTableDescriptor
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
#hierarchy-management-dimension-info-dialog .p-dialog-header,
#hierarchy-management-dimension-info-dialog .p-dialog-content {
    padding: 0;
}
#hierarchy-management-dimension-info-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
.marginated-form {
    margin-top: 15px !important;
}
</style>
