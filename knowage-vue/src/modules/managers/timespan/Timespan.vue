<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.scheduler.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showTimespanDetails(null, false)" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
                <KnListBox :options="timespans" :settings="timespanDescriptor.knListSettings" @click="showTimespanDetails($event, false)" @clone.stop="showTimespanDetails($event, true)" @delete.stop="deleteTimespanConfirm" />
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <router-view />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iTimespan, iCategory } from './Timespan'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import timespanDescriptor from './TimespanDescriptor.json'

export default defineComponent({
    name: 'timespan',
    components: { KnListBox, FabButton },
    data() {
        return {
            timespanDescriptor,
            timespans: [] as iTimespan[],
            categories: [] as iCategory[],
            loading: false
        }
    },
    async created() {
        await this.loadTimespans()
        await this.loadCategories()
    },
    methods: {
        async loadTimespans() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/listDynTimespan`).then((response: AxiosResponse<any>) => (this.timespans = response.data))
            this.loading = false

            console.log('loadTimespans() - LOADED TIMESPANS: ', this.timespans)
        },
        async loadCategories() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=TIMESPAN_CATEGORY`).then((response: AxiosResponse<any>) => (this.categories = response.data))
            this.loading = false

            console.log('loadCategories() - LOADED CATEGORIES: ', this.categories)

            // TODO - Remove harcoded possible categories
            this.categories.push(
                {
                    VALUE_NM: 'Date',
                    VALUE_DS: 'Date',
                    VALUE_ID: 43,
                    VALUE_CD: 'DATE'
                },
                {
                    VALUE_NM: 'Regexp',
                    VALUE_DS: 'Regular Expression',
                    VALUE_ID: 44,
                    VALUE_CD: 'REGEXP'
                },
                {
                    VALUE_NM: 'Max Length',
                    VALUE_DS: 'Max Length',
                    VALUE_ID: 45,
                    VALUE_CD: 'MAXLENGTH'
                }
            )
        },
        showTimespanDetails(event: any, clone: boolean) {
            console.log('showTimespanDetails() - event: ', event, ', colne: ', clone)
        },
        deleteTimespanConfirm(event: any) {
            console.log('deleteTimespanConfirm() - event: ', event)
        }
    }
})
</script>
