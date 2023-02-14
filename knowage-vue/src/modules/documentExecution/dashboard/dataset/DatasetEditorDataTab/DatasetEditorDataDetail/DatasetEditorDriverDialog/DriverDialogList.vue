<template>
    <div v-if="driver" class="p-fluid p-formgrid p-grid p-jc-center p-ai-center p-p-5 p-m-0">
        <div class="p-field-radiobutton p-col-12" v-for="(option, index) in driver.options" :key="index">
            <RadioButton v-if="!driver.multivalue && driver.parameterValue" :value="option.value" v-model="driver.parameterValue[0].value" @change="setRadioButtonValue" />
            <Checkbox v-if="driver.multivalue && driver.parameterValue" :value="option.value" v-model="checkedValues" @change="setCheckboxValue" />
            <label>{{ option.description }}</label>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import Checkbox from 'primevue/checkbox'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'driver-dialog-list',
    components: { Checkbox, RadioButton },
    props: { propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true } },
    computed: {},
    data() {
        return {
            driver: null as IDashboardDatasetDriver | null,
            checkedValues: [] as string[]
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
            if (this.driver?.multivalue) {
                this.checkedValues = this.driver.parameterValue.map((parameterValue: { value: string | number | Date; description: string }) => parameterValue.value as string)
            }
        },
        setRadioButtonValue() {
            if (!this.driver || !this.driver.options) return
            this.driver.parameterValue[0].description = this.getDescriptionForTheValue(this.driver?.parameterValue[0].value as string) as string
        },
        setCheckboxValue() {
            if (!this.driver) return
            this.driver.parameterValue = []
            this.checkedValues.forEach((checkedValue: string) => {
                this.driver?.parameterValue.push({ value: checkedValue, description: this.getDescriptionForTheValue(checkedValue) as string })
            })
        },
        getDescriptionForTheValue(value: string) {
            if (!this.driver || !this.driver.options) return
            const index = this.driver.options.findIndex((option: { value: string; description: string }) => option.value === value)
            return index !== -1 ? this.driver.options[index].description : ''
        }
    }
})
</script>
