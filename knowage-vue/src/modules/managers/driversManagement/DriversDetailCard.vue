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
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
//import Dropdown from 'primevue/dropdown'
//import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
export default defineComponent({
    name: 'name-card',
    components: {},
    props: {
        selectedDriver: {
            type: Object,
            required: false
        }
    },
    data() {
        return {
            driver: {} as any
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
