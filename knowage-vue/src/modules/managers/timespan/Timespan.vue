<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.timespan.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showTimespanDetails(null, false)" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
                <KnListBox :options="timespans" :settings="timespanDescriptor.knListSettings" @click="showTimespanDetails($event, false)" @clone.stop="showTimespanDetails($event, true)" @delete.stop="deleteTimespanConfirm" />
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-router-view">
                <router-view :categories="categories" :timespans="timespans" @timespanCreated="onTimespanCreated" />
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
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/listDynTimespan`).then(
                (response: AxiosResponse<any>) =>
                    (this.timespans = response.data?.map((timespan: iTimespan) => {
                        return { ...timespan, isCloneable: timespan.type === 'temporal' }
                    }))
            )
            this.loading = false
        },
        async loadCategories() {
            this.loading = true
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=TIMESPAN_CATEGORY`).then((response: AxiosResponse<any>) => (this.categories = response.data))
            this.loading = false
        },
        showTimespanDetails(event: any, clone: boolean) {
            const url = event ? `/timespan/edit-timespan?id=${event.item?.id}&clone=${clone}` : '/timespan/new-timespan'
            this.$router.push(url)
        },
        deleteTimespanConfirm(event: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => {
                    await this.deleteTimespan(event.item.id)
                }
            })
        },
        async deleteTimespan(id: number) {
            this.loading = true
            await this.$http
                .post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/deleteTimespan?ID=${id}`)
                .then((response: AxiosResponse<any>) => {
                    if (response.data?.Status === 'OK') {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                        this.removeTimespan(id)
                        this.$router.push('/timespan')
                    }
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: error?.message
                    })
                })
            this.loading = false
        },
        removeTimespan(id: number) {
            const index = this.timespans.findIndex((timespan: iTimespan) => timespan.id === id)
            if (index !== -1) this.timespans.splice(index, 1)
        },
        onTimespanCreated() {
            this.loadTimespans()
        }
    }
})
</script>
