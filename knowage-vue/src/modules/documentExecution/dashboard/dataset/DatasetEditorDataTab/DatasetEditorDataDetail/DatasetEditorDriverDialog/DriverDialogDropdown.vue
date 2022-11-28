<template>
    <div v-if="driver && driver.parameterValue" class="p-fluid p-formgrid p-grid p-jc-center p-ai-center p-p-5 p-m-0">
        <div class="p-field p-col-12">
            <span class="p-float-label">
                <MultiSelect v-if="driver.multivalue" v-model="driver.parameterValue" :options="driver.options" optionLabel="description" @change="onMultiselectChange" />
                <Dropdown v-else class="kn-material-input" v-model="driver.parameterValue[0].value" :options="driver.options" optionValue="value" optionLabel="description" @change="onDropdownChange" />
                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'driver-dialog-dropdown',
    components: { Dropdown, MultiSelect },
    props: { propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true } },
    computed: {},
    data() {
        return {
            driver: null as IDashboardDatasetDriver | null
        }
    },
    watch: {
        propDriver() {
            this.loadDriver()
        }
    },
    created() {
        this.loadDriver()
    },
    methods: {
        loadDriver() {
            this.driver = this.propDriver
        },
        onDropdownChange() {
            if (!this.driver) return
            this.updateDriverDescriptionOnDropdownChange()
        },
        updateDriverDescriptionOnDropdownChange() {
            if (!this.driver || !this.driver.parameterValue[0] || !this.driver.options) return
            const index = this.driver.options.findIndex((option: { value: string; description: string }) => option.value === this.driver?.parameterValue[0].value)
            if (index !== -1) this.driver.parameterValue[0].description = this.driver.options[index].description
        },
        onMultiselectChange() {
            console.log('>>>>>> MULTISELECT CHANGED: ', this.driver)
        }
    }
})
</script>
