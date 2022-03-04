<template>
    <div class="p-m-2">
        <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
            <template #start>{{ timespan?.name }}</template>
            <template #end>
                <!-- <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="saveDisabled" @click="saveJob" data-test="save-button" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeJobDetail" /> -->
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />

        {{ timespan }}

        <TimespanForm class="p-my-4" :propTimespan="timespan" :categories="categories"></TimespanForm>
        <TimespanIntervalForm class="p-my-4" :propTimespan="timespan"></TimespanIntervalForm>
        <TimespanIntervalTable class="p-mt-4" :propTimespan="timespan"></TimespanIntervalTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iCategory } from './Timespan'
import { AxiosResponse } from 'axios'
import TimespanForm from './TimespanForm.vue'
import TimespanIntervalForm from './TimespanIntervalForm.vue'
import TimespanIntervalTable from './TimespanIntervalTable.vue'

export default defineComponent({
    name: 'timespan-detail',
    components: { TimespanForm, TimespanIntervalForm, TimespanIntervalTable },
    props: { id: { type: String }, clone: { type: String }, categories: { type: Array as PropType<iCategory[]> } },
    data() {
        return {
            timespan: null as iTimespan | null,
            loading: false
        }
    },
    watch: {
        id() {
            this.loadTimespan()
        }
    },
    created() {
        this.loadTimespan()
    },
    methods: {
        async loadTimespan() {
            this.loading = true
            if (this.id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/loadTimespan?ID=${this.id}`).then((response: AxiosResponse<any>) => (this.timespan = response.data))
            } else {
                this.timespan = {
                    name: '',
                    type: 'time',
                    definition: [],
                    category: '',
                    isnew: true
                }
            }
            this.loading = false

            console.log('loadTimespan() - LOADED TIMESPAN: ', this.timespan)
        }
    }
})
</script>
