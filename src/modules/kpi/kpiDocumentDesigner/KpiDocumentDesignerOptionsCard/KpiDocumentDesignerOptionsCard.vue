<template>
    <Card v-if="options" class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('common.options') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-grid p-m-1">
                <div class="p-col-6">
                    <Checkbox v-model="options.showvalue" class="p-mr-2" :binary="true" />
                    <label>{{ $t('kpi.kpiDocumentDesigner.showValue') }}</label>
                </div>
                <div class="p-col-6">
                    <Checkbox v-model="options.showtarget" class="p-mr-2" :binary="true" />
                    <label>{{ $t('kpi.kpiDocumentDesigner.showTarget') }}</label>
                </div>
                <div class="p-col-6">
                    <Checkbox v-model="options.showtargetpercentage" class="p-mr-2" :binary="true" />
                    <label>{{ $t('kpi.kpiDocumentDesigner.showPercentage') }}</label>
                </div>
                <div class="p-col-6">
                    <Checkbox v-model="options.showthreshold" class="p-mr-2" :binary="true" />
                    <label>{{ $t('kpi.kpiDocumentDesigner.showThreshold') }}</label>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label p-m-2">
                        <InputText
                            v-model="options.history.size"
                            class="kn-material-input"
                            type="number"
                            min="0"
                            :class="{
                                'p-invalid': +options.history.size < 0
                            }"
                        />
                        <label class="kn-material-input-label"> {{ $t('kpi.kpiDocumentDesigner.precision') }}</label>
                    </span>
                </div>
                <div class="p-col-6 p-fluid">
                    <span class="p-float-label p-m-2">
                        <Dropdown v-model="options.history.units" class="kn-material-input" :options="KpiDocumentDesignerOptionsCardDescriptor.unitsOptions" option-value="value">
                            <template #value="slotProps">
                                <div v-if="slotProps.value">
                                    <span>{{ slotProps.value }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </template>
                        </Dropdown>
                        <label class="kn-material-input-label"> {{ $t('kpi.kpiDocumentDesigner.units') }} </label>
                    </span>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iOptions } from '../KpiDocumentDesigner'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import KpiDocumentDesignerOptionsCardDescriptor from './KpiDocumentDesignerOptionsCardDescriptor.json'

export default defineComponent({
    name: 'kpi-edit-options-card',
    components: { Card, Checkbox, Dropdown },
    props: { propOptions: { type: Object as PropType<iOptions>, required: true } },
    data() {
        return {
            KpiDocumentDesignerOptionsCardDescriptor,
            options: null as iOptions | null
        }
    },
    wacth: {
        propOptions() {
            this.loadOptions()
        }
    },
    created() {
        this.loadOptions()
    },
    methods: {
        loadOptions() {
            this.options = this.propOptions
            this.options.showtarget = this.options.showtarget === 'true'
            this.options.showtargetpercentage = this.options.showtargetpercentage === 'true'
            this.options.showthreshold = this.options.showthreshold === 'true'
            this.options.showvalue = this.options.showvalue === 'true'
        }
    }
})
</script>
