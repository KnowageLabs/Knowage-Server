<template>
    <Message v-if="!widgetModel.dataset" class="p-mb-2" severity="warn" :closable="false" :style="descriptor.hintStyle">
        {{ $t(`managers.functionsCatalog.noDatasetSelected`) }}
    </Message>
    <div v-else class="p-fluid p-formgrid p-grid">
        <div class="p-field p-col-8">
            <span class="p-float-label">
                <Dropdown class="kn-material-input" v-model="selectedColumnName" :options="widgetModel.columns" optionValue="columnName" optionLabel="columnName" @change="onColumnChanged"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
            </span>
        </div>
        <div class="p-field p-col-4">
            <span class="p-float-label">
                <InputText class="kn-material-input" v-model="row" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('common.row') }}</label>
            </span>
        </div>
        <div class="p-field p-col-3">
            <span class="p-float-label">
                <Dropdown class="kn-material-input" v-model="aggregation" :options="tableDescriptor.aggregationOptions" optionValue="value" optionLabel="label" @change="onColumnChanged"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.aggregation') }}</label>
            </span>
        </div>
        <div class="p-field p-col-3">
            <span class="p-float-label">
                <InputText type="number" class="kn-material-input" v-model="precision" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.precision') }}</label>
            </span>
        </div>
        <div class="p-field p-col-3">
            <span class="p-float-label">
                <InputText class="kn-material-input" v-model="prefix" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.prefix') }}</label>
            </span>
        </div>
        <div class="p-field p-col-3">
            <span class="p-float-label">
                <InputText class="kn-material-input" v-model="suffix" @change="onColumnChanged" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.suffix') }}</label>
            </span>
        </div>
        <div class="p-field p-d-flex p-col-12 p-mt-2">
            <InputSwitch class="" v-model="format" @change="onColumnChanged" />
            <label class="kn-material-input-label p-mx-2">{{ $t('dashboard.widgetEditor.editorTags.toLocale') }}</label>
            <i class="p-button-text p-button-rounded p-button-plain fas fa-circle-question" style="color: rgba(0, 0, 0, 0.6)" v-tooltip.right="$t('dashboard.widgetEditor.editorTags.hint.toLocale')" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Dropdown from 'primevue/dropdown'
import descriptor from '../WidgetTagsDialogDescriptor.json'
import tableDescriptor from '../../../TableWidget/TableWidgetSettingsDescriptor.json'
import Message from 'primevue/message'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'widget-editor-column-data',
    components: { Dropdown, Message, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            descriptor,
            tableDescriptor,
            selectedColumnName: '',
            row: '',
            aggregation: '',
            prefix: '',
            suffix: '',
            precision: 0 as any,
            format: false
        }
    },
    created() {},
    methods: {
        onColumnChanged() {
            // prettier-ignore
            const forInsert = `[kn-column='${this.selectedColumnName}'${this.row ? ` row='${this.row}'` : ''}${this.aggregation ? ` aggregation='${this.aggregation}'` : ''}${this.precision ? ` precision='${this.precision}'` : ''}${this.format ? ' format' : ''}${this.prefix ? ` prefix='${this.prefix}'` : ''}${this.suffix ? ` suffix='${this.suffix}'` : ''}]`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
