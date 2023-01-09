<template>
    <Checkbox v-if="params.colDef.editorType == 'TEXT' && params.colDef.columnInfo?.type === 'boolean'" :disabled="!params.colDef.isEditable" v-model="value" :binary="true" @change=""></Checkbox>
    <RegistryDatatableEditableField
        v-else-if="params.colDef.isEditable || params.colDef.columnInfo?.type === 'int' || params.colDef.columnInfo?.type === 'float'"
        :valueToChange="value"
        :column="params.colDef"
        :propRow="params.data"
        :comboColumnOptions="params.comboColumnOptions"
        @rowChanged="onRowChanged"
        @dropdownChanged="onDropdownChange"
        @dropdownOpened="addColumnOptions"
    ></RegistryDatatableEditableField>
</template>

<script lang="ts">
import Checkbox from 'primevue/checkbox'
import { defineComponent } from 'vue'
import RegistryDatatableEditableField from '../RegistryDatatableEditableField.vue'

export default defineComponent({
    components: { RegistryDatatableEditableField, Checkbox },
    props: {
        params: {
            required: true,
            type: Object as any
        }
    },
    data() {
        return {
            value: null as any
        }
    },
    created() {
        this.value = this.getInitialValue()
    },
    methods: {
        getValue() {
            return this.value
        },
        getInitialValue() {
            let startValue = this.params.value
            const isBackspaceOrDelete = this.params.eventKey === 'Backspace' || this.params.eventKey === 'Delete'
            if (isBackspaceOrDelete) startValue = null
            if (startValue !== null && startValue !== undefined) return startValue
            return null
        },
        onRowChanged(payload: any) {
            console.log('onRowChanged', payload)
            this.params.context.componentParent.setRowEdited(payload)
            // this.value = payload.row[this.params.colDef.field]
        },
        onDropdownChange(payload: any) {
            console.log('onDropdownChange', payload)
            this.params.context.componentParent.onDropdownChange(payload)
            // this.value = payload.row[this.params.colDef.field]
        },
        addColumnOptions(payload: any) {
            console.log('addColumnOptions', payload)
            this.params.context.componentParent.addColumnOptions(payload)
            // this.value = payload.row[this.params.colDef.field]
        }
    }
})
</script>
