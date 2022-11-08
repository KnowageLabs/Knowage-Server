<template>
    <Message v-if="selectedDatasets.length == 0" class="p-mb-2" severity="warn" :closable="false" :style="descriptor.hintStyle">
        {{ $t(`managers.functionsCatalog.noDatasetSelected`) }}
    </Message>
    <div v-else class="p-field">
        <span class="p-float-label">
            <Dropdown class="kn-material-input" v-model="selectedDatasetName" :options="selectedDatasets" optionValue="name" optionLabel="name" @change="onColumnChanged"> </Dropdown>
            <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-active-selections',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDatasets: { type: Array as PropType<IDataset[]> } },
    emits: ['insertChanged'],
    data() {
        return {
            selectedDatasetName: ''
        }
    },
    methods: {
        onColumnChanged() {
            const forInsert = `<div kn-preview="${this.selectedDatasetName}"></div>`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
