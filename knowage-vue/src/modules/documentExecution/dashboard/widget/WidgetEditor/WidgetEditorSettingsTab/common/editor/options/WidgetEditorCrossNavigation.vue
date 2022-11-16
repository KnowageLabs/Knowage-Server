<template>
    <div class="p-fluid p-formgrid p-grid">
        <div class="p-field p-col-12 p-p-2">
            <span class="p-float-label">
                <Dropdown class="kn-material-input" v-model="selectedColumnName" :options="widgetModel.columns" optionValue="columnName" optionLabel="columnName" @change="onColumnChanged"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
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
    name: 'widget-editor-cross-navigation',
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
            const forInsert = this.widgetModel.type === 'html' ? `<div kn-cross[kn-column='${this.selectedColumnName}']</div>` : `<span class='crossNavigation' kn-cross="">[kn-column='${this.selectedColumnName}' row='0']</span>`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
