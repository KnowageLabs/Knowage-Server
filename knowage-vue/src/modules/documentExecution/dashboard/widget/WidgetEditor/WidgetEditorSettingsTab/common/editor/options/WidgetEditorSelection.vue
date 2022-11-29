<template>
    <Message v-if="!widgetModel.dataset" class="p-mb-2" severity="warn" :closable="false" :style="descriptor.hintStyle">
        {{ $t(`managers.functionsCatalog.noDatasetSelected`) }}
    </Message>
    <div v-else class="p-fluid p-formgrid p-grid">
        <div class="p-field p-col-6">
            <span class="p-float-label">
                <Dropdown class="kn-material-input" v-model="selectedColumnName" :options="widgetModel.columns" optionValue="columnName" optionLabel="columnName" @change="onColumnChanged"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
            </span>
        </div>
        <div class="p-field p-col-6">
            <span class="p-float-label">
                <InputText class="kn-material-input" v-model="selectionValue" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.editorTags.selectionVal') }}</label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Dropdown from 'primevue/dropdown'
import descriptor from '../WidgetTagsDialogDescriptor.json'
import Message from 'primevue/message'

export default defineComponent({
    name: 'widget-editor-selections',
    components: { Dropdown, Message },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            descriptor,
            selectedColumnName: '',
            selectionValue: ''
        }
    },
    created() {},
    methods: {
        onColumnChanged() {
            const forInsert =
                this.widgetModel.type === 'html'
                    ? `<div kn-selection-column="${this.selectedColumnName}" kn-selection-value="${this.selectionValue}"></div>`
                    : `<span class="selection" kn-selection-column="${this.selectedColumnName}" kn-selection-value="${this.selectionValue}">[kn-column='${this.selectedColumnName}']</span>`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
