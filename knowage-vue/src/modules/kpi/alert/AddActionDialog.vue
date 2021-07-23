<template>
    <Dialog :header="$t('kpi.alert.addAction')" :breakpoints="alertDescriptor.dialog.breakpoints" :style="alertDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('kpi.alert.addAction') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" />
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <div class="p-field p-col-6">
            <span class="p-float-label">
                <Dropdown id="type" class="kn-material-input" v-model="type" :options="alertDescriptor.actionType" @change="setType" />
                <label for="type" class="kn-material-input-label"> {{ $t('kpi.alert.type') }} * </label>
            </span>
        </div>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import alertDescriptor from './AlertDescriptor.json'
export default defineComponent({
    name: 'add-action-dialog',
    components: {
        Dialog,
        Dropdown
    },
    props: {
        dialogVisible: {
            type: Boolean,
            default: false
        }
    },
    data() {
        return {
            alertDescriptor,
            type: ''
        }
    }
})
</script>
