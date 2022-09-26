<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="logModel" />

        <TableWidget v-if="propWidget.settings" :propWidget="propWidget" :editorMode="true" style="flex: 0.5; min-width: 500px; min-height: 250px" />
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent, PropType, reactive } from 'vue'
import { emitter } from '../../DashboardHelpers'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css'
import 'ag-grid-community/styles/ag-theme-alpine.css'
import TableWidget from '../TableWidget/TableWidget.vue'
import { IWidget } from '../../Dashboard'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { TableWidget, AgGridVue },
    props: {
        propWidget: {
            required: true,
            type: Object as PropType<IWidget>
        }
    },
    data() {
        return {
            descriptor,
            mock
        }
    },
    created() {},
    mounted() {},

    methods: {
        logModel() {
            console.log('MODEL ------------- \n', this.propWidget)
        }
    }
})
</script>
