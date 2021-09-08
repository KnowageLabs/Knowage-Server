<template>
    <Card>
        <template #content>
            <label>{{ filter.title }}</label>
            <div v-if="filter.static">
                <div v-if="filter.visible">
                    <InputText class="p-inputtext-sm" v-model="filter.filterValue" disabled />
                </div>
            </div>
            <div v-else>
                <InputText v-if="filter.presentation === 'MANUAL'" class="p-inputtext-sm" v-model="filter.filterValue" />
                <Dropdown v-else-if="filter.presentation === 'COMBO'" v-model="filter.filterValue" :options="options" optionValue="column_1" optionLabel="column_1" @change="test"> </Dropdown>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'registry-filter-card',
    components: { Card, Dropdown },
    props: { propFilter: { type: Object }, filterOptions: { type: Array } },
    data() {
        return {
            filter: {} as any,
            options: [] as any
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        }
    },
    async created() {
        this.loadFilter()
        if (this.filter.presentation === 'COMBO') {
            await this.loadFilterOptions('sales_city')
            console.log('OPTIONS: ', this.options)
        }
    },
    methods: {
        loadFilter() {
            this.filter = { ...this.propFilter }
        },
        async loadFilterOptions(column: string) {
            console.log('LOADING MOCKED OPTIONS FOR COLUMN: ', column)
            await axios
                // .get(`knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=c75a32e00fbf11ec8b65ed57c30e47f4`)
                .get('../data/demo_dropdown_store_type.json')
                .then((response) => (this.options = response.data.rows))
        }
    }
})
</script>
