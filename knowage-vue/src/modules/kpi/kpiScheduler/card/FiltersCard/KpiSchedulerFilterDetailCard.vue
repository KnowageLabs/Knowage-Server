<template>
    <Card v-if="currentFilter">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ filter.placeholderName }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-d-flex p-flex-row">
                <Dropdown id="valueCd" class="kn-material-input p-mr-2" :style="kpiSchedulerFilterDetailCardDescriptor.input.style" optionLabel="valueCd" optionValue="valueCd" v-model="currentFilter.type.valueCd" :options="placeholderType" @change="currentFilter.value = null" />

                <div v-if="currentFilter.type.valueCd === 'FIXED_VALUE'">
                    <span class="p-float-label">
                        <InputText class="kn-material-input" :style="kpiSchedulerFilterDetailCardDescriptor.input.style" v-model.trim="currentFilter.value" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.value') }} * </label>
                    </span>
                </div>

                <div v-else-if="currentFilter.type.valueCd === 'TEMPORAL_FUNCTIONS'">
                    <span class="p-float-label">
                        <Dropdown id="valueCd" class="kn-material-input p-mr-2" :style="kpiSchedulerFilterDetailCardDescriptor.input.style" optionLabel="valueCd" optionValue="valueCd" v-model="currentFilter.value" :options="temporalType" />
                    </span>
                </div>

                <div v-else-if="currentFilter.type.valueCd === 'LOV'">
                    <span class="p-float-label">
                        <AutoComplete class="p-mr-2" :style="kpiSchedulerFilterDetailCardDescriptor.input.style" v-model="currentFilter.value" :suggestions="filteredLovs" field="name" :forceSelection="true" @complete="searchCategories($event)" @item-select="setLovValue($event.value, filter)" />
                    </span>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import AutoComplete from 'primevue/autocomplete'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import kpiSchedulerFilterDetailCardDescriptor from './KpiSchedulerFilterDetailCardDescriptor.json'

export default defineComponent({
    name: 'filters-card',
    components: { AutoComplete, Card, Dropdown },
    props: { filter: { type: Object }, placeholderType: { type: Array }, temporalType: { type: Array }, lovs: { type: Array, required: true } },
    data() {
        return {
            kpiSchedulerFilterDetailCardDescriptor,
            currentFilter: {} as any,
            filteredLovs: [] as any[]
        }
    },
    watch: {
        filter() {
            this.loadFilter()
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.currentFilter = this.filter as any[]
            if (this.currentFilter.type.valueCd === 'LOV') {
                this.currentFilter.value = this.getLovValue(this.currentFilter.value)
            }
        },

        searchCategories(event) {
            setTimeout(() => {
                if (!event.query.trim().length) {
                    this.filteredLovs = [...this.lovs] as any[]
                } else {
                    this.filteredLovs = this.lovs.filter((lov: any) => {
                        return lov.name.toLowerCase().startsWith(event.query.toLowerCase())
                    })
                }
            }, 250)
        },
        setLovValue(value: any, filter: any) {
            // console.log('FIlter', filter)
            // console.log('value', value)
            // console.log('RETURNED VALUE', this.getLovValue(value.label))
            filter.value = this.getLovValue(value.label)
        },
        getLovValue(value: string) {
            // console.log('FC - Value ', value)
            const tempLov = this.lovs.find((lov: any) => lov.label === value) as any
            return tempLov ? tempLov.name : ''
        }
    }
})
</script>
