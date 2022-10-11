<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <Button icon="fas fa-square-check" class="p-button-rounded p-button-text p-button-plain" @click="logWidget" />
        <TableWidget class="p-m-2" v-if="propWidget.settings && propWidget.type == 'table'" :propWidget="propWidget" :datasets="datasets" :editorMode="true" style="height: 30%" />
        <SelectorWidget class="p-m-2" v-if="propWidget.settings && propWidget.type == 'selector'" :propWidget="propWidget" :datasets="datasets" :editorMode="true" style="height: 30%" />
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '../../Dashboard'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import TableWidget from '../TableWidget/TableWidget.vue'
import SelectorWidget from '../SelectorWidget/SelectorWidget.vue'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { TableWidget, SelectorWidget },
    props: {
        propWidget: {
            required: true,
            type: Object as PropType<IWidget>
        },
        datasets: { type: Array as PropType<IDataset[]> }
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
        logWidget() {
            console.log('widget ----------------- \n', this.propWidget)
        }
    }
})
</script>
