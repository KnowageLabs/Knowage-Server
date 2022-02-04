<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.functionsCatalog.outputColumns') }}
                </template>
                <template #end>
                    <Button v-if="!readonly" class="kn-button p-button-text" :label="$t('managers.functionsCatalog.addColumn')" @click="addOutputColumn"></Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div v-if="outputColumns.length === 0" class="p-d-flex p-flex-row p-jc-center">
                {{ $t('managers.functionsCatalog.noOutputColumnsRequired') }}
            </div>
            <template v-else>
                <FunctionsCatalogOutputColumn v-for="(outputColumn, index) in outputColumns" :key="index" :column="outputColumn" :readonly="readonly" @deleted="deleteOutputColumn(index)"></FunctionsCatalogOutputColumn>
            </template>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iOutputColumn } from '../../FunctionsCatalog'
import Card from 'primevue/card'
import FunctionsCatalogOutputColumn from './FunctionsCatalogOutputColumn.vue'

export default defineComponent({
    name: 'function-catalog-output-columns-card',
    components: { Card, FunctionsCatalogOutputColumn },
    props: { columns: { type: Array }, readonly: { type: Boolean } },
    data() {
        return {
            outputColumns: [] as iOutputColumn[]
        }
    },
    created() {
        this.loadOutputColumns()
    },
    methods: {
        loadOutputColumns() {
            this.outputColumns = this.columns as iOutputColumn[]
        },
        addOutputColumn() {
            this.outputColumns.push({ name: '', fieldType: '', type: '' })
        },
        deleteOutputColumn(index: number) {
            this.outputColumns.splice(index, 1)
        }
    }
})
</script>
