<template>
    <Card style="width:100%" class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" v-model="driver.label" />
                        <label for="label" class="kn-material-input-label">{{ $t('common.label') }} * </label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <InputText id="name" class="kn-material-input" type="text" v-model="driver.name" />
                        <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown id="type" class="kn-material-input" v-model="driver.typeId" :options="types" optionValue="VALUE_ID" optionLabel="VALUE_NM" />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }} * </label>
                    </span>
                    <!-- <KnValidationMessages class="p-mt-1" :vComp="vcomp.alertListener" :additionalTranslateParams="{ fieldName: $t('kpi.alert.kpiListener') }"></KnValidationMessages> -->
                </div>
                <div class="p-field p-col-8">
                    <span class="p-float-label">
                        <InputText id="description" class="kn-material-input" type="text" v-model="driver.description" />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <MultiSelect class="kn-material-input" v-model="selectedOptions" :options="driversManagemenDetailtDescriptor.options" />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import driversManagemenDetailtDescriptor from './DriversManagementDetailDescriptor.json'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
export default defineComponent({
    name: 'detail-card',
    components: { Dropdown, MultiSelect },
    props: {
        selectedDriver: {
            type: Object,
            required: false
        },
        types: {
            type: Array,
            required: false
        }
    },
    data() {
        return {
            driver: {} as any,
            driversManagemenDetailtDescriptor,
            selectedOptions: []
        }
    },
    watch: {
        selectedDriver() {
            //this.v$.$reset()
            this.driver = this.selectedDriver as any
        }
    },
    mounted() {
        if (this.driver) {
            this.driver = this.selectedDriver as any
        }
    }
})
</script>
