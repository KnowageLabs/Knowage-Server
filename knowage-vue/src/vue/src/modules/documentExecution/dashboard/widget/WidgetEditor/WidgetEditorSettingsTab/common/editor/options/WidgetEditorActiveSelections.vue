<template>
    <div v-if="widgetModel" class="p-field">
        <span class="p-float-label">
            <Dropdown v-model="selectedColumnName" class="kn-material-input" :options="widgetModel.columns" option-value="columnName" option-label="columnName" @change="onColumnChanged"> </Dropdown>
            <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-active-selections',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            selectedColumnName: ''
        }
    },
    created() {},
    methods: {
        onColumnChanged() {
            const forInsert = `[kn-active-selection='${this.selectedColumnName}']`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
