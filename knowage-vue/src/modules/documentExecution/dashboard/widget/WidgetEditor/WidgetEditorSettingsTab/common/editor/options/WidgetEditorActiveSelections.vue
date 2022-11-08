<template>
    <div v-if="widgetModel" class="p-field kn-flex">
        <span class="p-float-label">
            <Dropdown class="kn-material-input" v-model="selectedColumnName" :options="widgetModel.columns" optionValue="columnName" optionLabel="columnName" @change="onColumnChanged"> </Dropdown>
            <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dropdown from 'primevue/dropdown'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'

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
