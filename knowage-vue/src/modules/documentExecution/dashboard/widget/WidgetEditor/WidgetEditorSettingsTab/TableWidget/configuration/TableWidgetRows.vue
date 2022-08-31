<template>
    <div v-if="rowsModel" class="widget-editor-card p-p-2">
        <div class="p-d-flex p-flex-row p-ai-center">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.pagination') }}</label>
                <InputSwitch v-model="rowsModel.enabled" @change="rowsModelUpdated"></InputSwitch>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from '../TableWidgetSettingsDescriptor.json'

export default defineComponent({
    name: 'table-widget-configuration-container',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            rowsModel: null as any
        }
    },
    created() {
        this.loadRowsModel()
    },
    methods: {
        loadRowsModel() {
            console.log(' >>>> WIDGET MODEL: ', this.widgetModel)
        },
        rowsModelUpdated() {
            console.log(' >>>> ROWS MODEL UPDATED: ', this.rowsModel)
        }
    }
})
</script>
