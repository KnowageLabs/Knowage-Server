<template>
    <Card v-if="currentFilter">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ filter.placeholderName }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-d-flex p-flex-row">
                <Dropdown id="valueCd" v-model="currentFilter.type" class="kn-material-input p-mr-2" :style="kpiSchedulerFilterDetailCardDescriptor.input.style" :options="placeholderType" @change="resetValue">
                    <template #value="slotProps">
                        <div v-if="slotProps.value" class="p-dropdown-car-value">
                            <span>{{ slotProps.value['valueCd'] }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div class="p-dropdown-car-option">
                            <span>{{ slotProps.option.valueCd }}</span>
                        </div>
                    </template>
                </Dropdown>

                <div v-if="currentFilter.type.valueCd === 'FIXED_VALUE'">
                    <span class="p-float-label">
                        <InputText
                            v-model.trim="currentFilter.value"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': currentFilter.value === ''
                            }"
                            :style="kpiSchedulerFilterDetailCardDescriptor.input.style"
                            @input="$emit('touched')"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.value') }} * </label>
                    </span>
                </div>

                <div v-else-if="currentFilter.type.valueCd === 'TEMPORAL_FUNCTIONS'">
                    <span class="p-float-label">
                        <Dropdown id="valueCd" v-model="currentFilter.value" class="kn-material-input p-mr-2" :style="kpiSchedulerFilterDetailCardDescriptor.input.style" option-label="valueCd" option-value="valueCd" :options="temporalType" @change="$emit('touched')" />
                    </span>
                </div>

                <div v-else-if="currentFilter.type.valueCd === 'LOV'">
                    <span class="p-float-label">
                        <AutoComplete
                            v-model="currentFilter.value"
                            class="p-mr-2"
                            :style="kpiSchedulerFilterDetailCardDescriptor.input.style"
                            :suggestions="filteredLovs"
                            field="name"
                            :dropdown="true"
                            :force-selection="true"
                            @complete="searchCategories($event)"
                            @item-select="setLovValue($event.value, filter)"
                        />
                    </span>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { iFilter, iLov } from '../../KpiScheduler'
    import AutoComplete from 'primevue/autocomplete'
    import Card from 'primevue/card'
    import Dropdown from 'primevue/dropdown'
    import kpiSchedulerFilterDetailCardDescriptor from './KpiSchedulerFilterDetailCardDescriptor.json'

    export default defineComponent({
        name: 'filters-card',
        components: { AutoComplete, Card, Dropdown },
        props: { filter: { type: Object }, placeholderType: { type: Array }, temporalType: { type: Array }, lovs: { type: Array, required: true } },
        emits: ['touched'],
        data() {
            return {
                kpiSchedulerFilterDetailCardDescriptor,
                currentFilter: {} as iFilter,
                filteredLovs: [] as iLov[]
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
                this.currentFilter = this.filter as iFilter
                if (this.currentFilter.type.valueCd === 'LOV') {
                    this.currentFilter.value = this.getLovValue(this.currentFilter.value as string)
                }
            },

            searchCategories(event) {
                setTimeout(() => {
                    if (!event.query.trim().length) {
                        this.filteredLovs = [...this.lovs] as { id: number; name: string; label: string }[]
                    } else {
                        this.filteredLovs = this.lovs.filter((lov: any) => {
                            return lov.name.toLowerCase().startsWith(event.query.toLowerCase())
                        }) as iLov[]
                    }
                }, 250)
            },
            setLovValue(value: iLov, filter: iFilter) {
                filter.value = this.getLovValue(value.label)
                this.$emit('touched')
            },
            getLovValue(value: string) {
                const tempLov = this.lovs.find((lov: any) => lov.label === value) as iLov
                return tempLov ? tempLov.name : ''
            },
            resetValue() {
                this.currentFilter.value = null
                this.$emit('touched')
            }
        }
    })
</script>
