<template>
    <div v-if="driver && driver.parameterValue" class="p-fluid p-formgrid p-grid p-p-5 p-m-0">
        <div class="p-field p-col-12">
            <span class="p-float-label">
                <Calendar v-model="driver.parameterValue[0].value as Date" :showButtonBar="true" :showIcon="true" :manualInput="true" class="kn-material-input custom-timepicker" />
                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import Calendar from 'primevue/calendar'

export default defineComponent({
    name: 'driver-dialog-date-input',
    components: { Calendar },
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
            if (this.driver && !this.driver.parameterValue[0]) {
                this.driver.parameterValue = [{ value: '', description: '' }]
            }

            // TODO - See if we need this after we remove the mocked drivers
            if (this.driver && !(this.driver.parameterValue[0].value instanceof Date)) {
                this.driver.parameterValue[0].value = new Date(this.driver.parameterValue[0].value)
            }
        }
    }
})
</script>
