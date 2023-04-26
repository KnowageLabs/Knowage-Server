<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary widget-tags-dialog" :style="descriptor.driverDialogStyle" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ driver ? driver.label : '' }}
                </template>
            </Toolbar>
        </template>

        <div v-if="driver">
            <DriverDialogManualInput v-if="driver.typeCode === 'MAN_IN' && (driver.type === 'NUM' || driver.type === 'STRING')" :prop-driver="driver"></DriverDialogManualInput>
            <DriverDialogList v-else-if="driver.selectionType === 'LIST'" :prop-driver="driver"></DriverDialogList>
            <DriverDialogDropdown v-else-if="driver.selectionType === 'COMBOBOX'" :prop-driver="driver"></DriverDialogDropdown>
            <DriverDialogDateInput v-else-if="driver.type === 'DATE'" :prop-driver="driver"></DriverDialogDateInput>
            <DriverDialogPopup v-else-if="driver.selectionType === 'LOOKUP'" :prop-driver="driver" :dashboard-id="dashboardId" :selected-dataset-prop="selectedDatasetProp" :drivers="drivers"></DriverDialogPopup>
            <DriverDialogTree v-else-if="driver.selectionType === 'TREE'" :prop-driver="driver" :dashboard-id="dashboardId" :selected-dataset-prop="selectedDatasetProp" :drivers="drivers"></DriverDialogTree>
            <span v-else>
                {{ driver }}
            </span>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="updateDriver"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import Dialog from 'primevue/dialog'
import descriptor from '../DatasetEditorDataDetailDescriptor.json'
import deepcopy from 'deepcopy'
import DriverDialogManualInput from './DriverDialogManualInput.vue'
import DriverDialogList from './DriverDialogList.vue'
import DriverDialogDropdown from './DriverDialogDropdown.vue'
import DriverDialogPopup from './DriverDialogPopup.vue'
import DriverDialogDateInput from './DriverDialogDateInput.vue'
import DriverDialogTree from './DriverDialogTree.vue'

export default defineComponent({
    name: 'dataset-editor-driver-dialog',
    components: { Dialog, DriverDialogManualInput, DriverDialogList, DriverDialogDropdown, DriverDialogPopup, DriverDialogDateInput, DriverDialogTree },
    props: {
        visible: Boolean,
        propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true },
        dashboardId: { type: String, required: true },
        selectedDatasetProp: { required: true, type: Object },
        drivers: { type: Array as PropType<IDashboardDatasetDriver[]>, required: true }
    },
    emits: ['close', 'updateDriver'],
    data() {
        return {
            descriptor,
            driver: null as IDashboardDatasetDriver | null
        }
    },
    watch: {
        propDriver() {
            this.loadDriver()
        }
    },
    created() {
        this.loadDriver()
    },
    methods: {
        loadDriver() {
            this.driver = deepcopy(this.propDriver)
        },
        updateDriver() {
            this.$emit('updateDriver', deepcopy(this.driver))
            this.driver = null
        },
        closeDialog() {
            this.driver = null
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
.widget-tags-dialog .p-dialog-content {
    padding: 0;
}
</style>
