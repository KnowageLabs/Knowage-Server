<template>
    <!-- <div class="custom-cell-container p-d-flex kn-height-full">{{ params.value }}</div> -->
    <!-- <div class=""> -->
    <Checkbox v-if="params.colDef.editorType == 'TEXT' && params.colDef.columnInfo?.type === 'boolean'" :disabled="!params.colDef.isEditable" v-model="params.value" :binary="true" @change=""></Checkbox>
    <div v-else class="custom-cell-container p-d-flex kn-height-full">{{ params.value }}</div>
    <!-- <RegistryDatatableEditableField
            v-else-if="params.colDef.isEditable || params.colDef.columnInfo?.type === 'int' || params.colDef.columnInfo?.type === 'float'"
            :column="params.colDef"
            :propRow="params.data"
            :comboColumnOptions="comboColumnOptions"
            @rowChanged=""
            @dropdownChanged="onDropdownChange"
            @dropdownOpened="addColumnOptions"
        ></RegistryDatatableEditableField> -->
    <!-- </div> -->
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
    watch: {
        params: {
            async handler() {},
            deep: true
        }
    },
    data() {
        return {}
    },
    created() {},
    methods: {}
})
</script>
