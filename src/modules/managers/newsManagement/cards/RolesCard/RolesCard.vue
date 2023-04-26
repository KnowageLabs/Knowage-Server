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
                v-model:selection="selectedCategories"
                :value="categoryList"
                class="p-datatable-sm kn-table"
                data-key="id"
                :rows="20"
                :scrollable="true"
                :scroll-height="rolesCardDescriptor.table.scrollHeight"
                responsive-layout="stack"
                breakpoint="960px"
                data-test="data-table"
                @rowSelect="setDirty"
                @rowUnselect="setDirty"
                @rowSelectAll="onSelectAll"
                @rowUnselectAll="onUnselectAll"
            >
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column field="name" :header="$t('managers.newsManagement.role')" :style="rolesCardDescriptor.column.header.style"></Column>
                <Column selection-mode="multiple" :style="rolesCardDescriptor.column.style" data-key="id"></Column>
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
