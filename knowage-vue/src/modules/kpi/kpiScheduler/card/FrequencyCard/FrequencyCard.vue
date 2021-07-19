<template>
    <Card>
        <template #content>
            <div class="p-d-flex">
                <span class="p-float-label">
                    <Calendar id="startDate" class="kn-material-input" v-model="startDate" :showIcon="true" :manualInput="false" />
                    <label for="expirationDate" class="kn-material-input-label"> {{ $t('managers.templatePruning.selectDate') }} * </label>
                </span>
                <Button icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" :disabled="filterDisabled" @click="loadDocumentSelection" aria-label="Filter" data-test="filter-button" />
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

import Calendar from 'primevue/calendar'
import Card from 'primevue/card'

export default defineComponent({
    name: 'frequency-card',
    components: {
        Calendar,
        Card
    },
    props: {
        frequency: {
            type: Object,
            required: true
        }
    },
    data() {
        return {
            startDate: null,
            endDate: null
        }
    },
    async created() {
        this.loadFrequency()
    },
    methods: {
        loadFrequency() {
            console.log('FREQUENCY: ', this.frequency)
            this.startDate = this.frequency.startDate
            this.startDate = this.frequency.endDate
        }
    }
})
</script>
