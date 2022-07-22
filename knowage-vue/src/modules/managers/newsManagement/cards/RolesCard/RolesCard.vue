<template>
    <Card :style="rolesCardDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('managers.newsManagement.roles') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable
                :value="categoryList"
                v-model:selection="selectedCategories"
                class="p-datatable-sm kn-table"
                dataKey="id"
                :rows="20"
                :scrollable="true"
                :scrollHeight="rolesCardDescriptor.table.scrollHeight"
                responsiveLayout="stack"
                breakpoint="960px"
                @rowSelect="setDirty"
                @rowUnselect="setDirty"
                @rowSelectAll="onSelectAll"
                @rowUnselectAll="onUnselectAll"
                data-test="data-table"
            >
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column field="name" :header="$t('managers.newsManagement.role')" :style="rolesCardDescriptor.column.header.style"></Column>
                <Column selectionMode="multiple" :style="rolesCardDescriptor.column.style" dataKey="id"></Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iRole } from '../../NewsManagement'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import rolesCardDescriptor from './RolesCardDescriptor.json'

export default defineComponent({
    name: 'roles-card',
    components: {
        Card,
        Column,
        DataTable
    },
    props: {
        categoryList: Array,
        selected: Array
    },
    emits: ['changed'],
    data() {
        return {
            rolesCardDescriptor,
            selectedCategories: [] as iRole[]
        }
    },
    watch: {
        selected() {
            this.loadSelectedCategories()
        }
    },
    created() {
        this.loadSelectedCategories()
    },
    methods: {
        setDirty() {
            this.$emit('changed', this.selectedCategories)
        },
        onSelectAll(event: any) {
            this.selectedCategories = event.data
            this.$emit('changed', this.selectedCategories)
        },
        onUnselectAll() {
            this.selectedCategories = []
            this.$emit('changed', this.selectedCategories)
        },
        loadSelectedCategories() {
            this.selectedCategories = this.selected as iRole[]
        }
    }
})
</script>
