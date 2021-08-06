<template>
    <Card style="width:100%" class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" v-model="mode.label" />
                        <label for="label" class="kn-material-input-label">{{ $t('common.label') }} * </label>
                    </span>
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="name" class="kn-material-input" type="text" v-model="mode.name" />
                        <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText id="description" class="kn-material-input" type="text" v-model="mode.description" />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
// import Dropdown from 'primevue/dropdown'
// import MultiSelect from 'primevue/multiselect'
//import driversManagemenDetailtDescriptor from './DriversManagementDetailDescriptor.json'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
export default defineComponent({
    name: 'detail-card',
    components: {},
    props: {
        selectedMode: {
            type: Object,
            required: false
        }
    },
    data() {
        return {
            mode: {} as any
        }
    },
    watch: {
        selectedMode() {
            //this.v$.$reset()
            this.mode = { ...this.selectedMode } as any
        }
    },
    mounted() {
        if (this.selectedMode) {
            this.mode = { ...this.selectedMode } as any
        }
    }
})
</script>
