<template>
    <div>
        <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
            <template #start>{{ timespan?.name }}</template>
            <template #end>
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="saveDisabled" @click="saveTimespan(null)" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTimespanDetails" />
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />

        <div class="p-m-2">
            <TimespanForm class="p-my-4" :propTimespan="timespan" :categories="categories"></TimespanForm>
            <TimespanIntervalForm class="p-my-4" :propTimespan="timespan"></TimespanIntervalForm>
            <TimespanIntervalTable class="p-mt-4" :propTimespan="timespan"></TimespanIntervalTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iCategory } from './Timespan'
import { AxiosResponse } from 'axios'
import TimespanForm from './TimespanForm.vue'
import TimespanIntervalForm from './TimespanIntervalForm.vue'
import TimespanIntervalTable from './TimespanIntervalTable.vue'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'timespan-detail',
    components: { TimespanForm, TimespanIntervalForm, TimespanIntervalTable },
    props: { id: { type: String }, clone: { type: String }, categories: { type: Array as PropType<iCategory[]> }, timespans: { type: Array as PropType<iTimespan[]>, required: true } },
    emits: ['timespanCreated'],
    data() {
        return {
            timespan: null as iTimespan | null,
            operation: 'create',
            loading: false
        }
    },
    computed: {
        saveDisabled(): any {
            return !this.timespan || !this.timespan.name || this.timespan.definition.length === 0
        }
    },
    watch: {
        id() {
            this.loadTimespan()
        },
        clone() {
            if (this.id == this.timespan?.id) {
                this.loadTimespan()
            }
        }
    },
    created() {
        this.loadTimespan()
    },
    methods: {
        async loadTimespan() {
            this.loading = true

            if (this.id) {
                await this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/loadTimespan?ID=${this.id}`)
                    .then((response: AxiosResponse<any>) => (this.timespan = response.data))
                    .catch(() => {})
                if (this.clone === 'true') await this.cloneTimespan()
            } else {
                this.timespan = this.getDefautTimespan()
            }
            this.loading = false
        },
        getDefautTimespan(): iTimespan {
            return {
                name: '',
                type: 'time',
                definition: [],
                category: '',
                isnew: true
            }
        },
        closeTimespanDetails() {
            this.timespan = null
            this.$router.push('/timespan')
        },
        async saveTimespan(tempTimespan: iTimespan | null) {
            const timespan = tempTimespan ?? this.timespan

            if (!timespan) return

            if (timespan.definition.length === 0) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.noIntervalError') })
                return
            }

            this.operation = timespan.id ? 'update' : 'create'

            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/timespan/saveTimespan`, timespan)
                .then((response: AxiosResponse<any>) => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.' + this.operation + 'Title'),
                        msg: this.$t('common.toast.success')
                    })
                    if (this.operation === 'create' && timespan.isnew) {
                        timespan.id = response.data?.id
                        delete timespan.isnew
                        this.$router.push(`/timespan/edit-timespan?id=${timespan.id}&clone=false`)
                    }
                    this.$emit('timespanCreated')
                })
                .catch(() => {})
            this.loading = false
        },
        async cloneTimespan() {
            const tempTimespan = deepcopy(this.timespan)
            if (!tempTimespan) {
                this.$router.push('/timespan')
                this.timespan = this.getDefautTimespan()
                return
            }
            delete tempTimespan.id
            tempTimespan.isnew = true

            const pattern = new RegExp(/.*#.*\d/gi)
            if (pattern.test(tempTimespan.name)) {
                tempTimespan.name = tempTimespan.name.substring(0, tempTimespan.name.length - 1) + '' + (parseInt(tempTimespan.name[tempTimespan.name.length - 1]) + 1)
            } else {
                tempTimespan.name = tempTimespan.name + ' #2'
            }

            for (let i = 0; i < this.timespans.length; i++) {
                if (this.timespans[i].name === tempTimespan.name) {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.cloneAlreadyDefined') })
                    this.$router.push('/timespan')
                    this.timespan = this.getDefautTimespan()
                    return
                }
            }

            const firstInterval = tempTimespan.definition[tempTimespan.definition.length - 1]

            const fromDate = new Date(firstInterval.from.replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3'))
            const toDate = new Date(firstInterval.to.replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3'))

            const millsDay = 86400000
            const tempInterval = { from: toDate, to: new Date() }
            tempInterval.from.setTime(toDate.getTime() + millsDay)
            tempInterval.to.setTime(tempInterval.from.getTime() + toDate.getTime() - fromDate.getTime() - millsDay)

            firstInterval.from = tempInterval.from.getDate() + '/' + (tempInterval.from.getMonth() + 1) + '/' + tempInterval.from.getFullYear()
            firstInterval.to = tempInterval.to.getDate() + '/' + (tempInterval.to.getMonth() + 1) + '/' + tempInterval.to.getFullYear()
            tempTimespan.definition = [firstInterval]
            await this.saveTimespan(tempTimespan)
        }
    }
})
</script>
