<template>
    <Dialog :style="lovsManagementParamsDialogDescriptor.dialog.style" :header="$t('managers.lovsManagement.dependenciesTitle')" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div v-for="(dependency, index) in dependenciesList" :key="index">
            <p>{{ $t(lovsManagementParamsDialogDescriptor.info[dependency.type]) }}</p>
            <div>
                <span class="p-float-label">
                    <InputText v-model.trim="dependency.value" class="kn-material-input" type="text" />
                    <label class="kn-material-input-label"> {{ dependency.name }}</label>
                </span>
            </div>
        </div>
        <template #footer>
            <Button class="kn-button kn-button--primary" @click="mode === 'preview' ? $emit('preview') : $emit('test')"> {{ mode === 'preview' ? $t('managers.lovsManagement.preview') : $t('managers.lovsManagement.test') }}</Button>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import lovsManagementParamsDialogDescriptor from './LovsManagementParamsDialogDescriptor.json'

export default defineComponent({
    name: 'lovs-management-params-dialog',
    components: { Dialog },
    props: {
        visible: { type: Boolean },
        dependenciesList: { type: Array },
        mode: { type: String }
    },
    emits: ['close', 'preview', 'test'],
    data() {
        return {
            lovsManagementParamsDialogDescriptor
        }
    },
    created() {},
    methods: {}
})
</script>
