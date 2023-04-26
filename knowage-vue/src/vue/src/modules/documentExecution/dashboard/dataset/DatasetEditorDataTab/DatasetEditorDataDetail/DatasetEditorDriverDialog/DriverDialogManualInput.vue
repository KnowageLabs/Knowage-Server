<!-- eslint-disable vue/valid-v-model -->
<template>
    <div v-if="driver && driver.parameterValue" class="p-fluid p-formgrid p-grid p-p-5 p-m-0">
        <div class="p-field p-col-12">
            <span class="p-float-label">
                <InputText v-model="driver.parameterValue[0].value as string" class="kn-material-input" :type="driver.type === 'NUM' ? 'number' : 'text'" />
                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
            </span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'

export default defineComponent({
    name: 'driver-dialog-manual-input',
    components: {},
    props: { propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true } },
    data() {
        return {
            driver: null as IDashboardDatasetDriver | null
        }
    },
    computed: {},
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
        }
    }
})
</script>
