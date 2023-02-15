<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ title }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable
                v-model:selection="selectedCategories"
                :value="dataList"
                class="p-datatable-sm kn-table"
                data-key="ID"
                responsive-layout="stack"
                breakpoint="960px"
                :scrollable="true"
                data-test="data-table"
                @rowSelect="setDirty"
                @rowUnselect="setDirty"
                @rowSelectAll="onSelectAll"
                @rowUnselectAll="onUnselectAll"
            >
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column selection-mode="multiple" :style="tabViewDescriptor.column.style" data-key="ID"></Column>
                <Column field="LABEL" :header="$t('common.name')"></Column>
                <Column v-if="dataList && dataList.length > 0 && dataList[0].DESCRIPTION" field="DESCRIPTION" :header="$t('common.description')"></Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import tabViewDescriptor from '../TenantManagementTabViewDescriptor.json'

export default defineComponent({
    name: 'selection-table',
    components: {
        Card,
        Column,
        DataTable
    },
    props: {
        title: String,
        dataList: Array,
        selectedData: Array
    },
    emits: ['changed'],
    data() {
        return {
            tabViewDescriptor,
            selectedCategories: [] as any
        }
    },
    watch: {
        selectedData() {
            this.selectedCategories = this.selectedData
        }
    },
    created() {
        this.selectedCategories = this.selectedData
    },
    methods: {
        setDirty() {
            this.$emit('changed', this.selectedCategories)
        },
        onSelectAll(event) {
            this.selectedCategories = event.data
            this.$emit('changed', this.selectedCategories)
        },
        onUnselectAll() {
            this.selectedCategories = []
            this.$emit('changed', this.selectedCategories)
        }
    }
})
</script>
