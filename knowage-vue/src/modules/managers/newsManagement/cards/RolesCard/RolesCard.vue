<template>
    <Card :style="rolesCardDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
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
                scrollHeight="200px"
                responsiveLayout="stack"
                breakpoint="960px"
                @rowSelect="setDirty"
                @rowUnselect="setDirty"
                @rowSelectAll="setDirty"
                @rowUnselectAll="setDirty"
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
            selectedCategories: [] as any
        }
    },
    watch: {
        selected() {
            this.selectedCategories = this.selected
        }
    },
    created() {
        this.selectedCategories = this.selected
    },
    methods: {
        setDirty() {
            this.$emit('changed', this.selectedCategories)
        }
    }
})
</script>
