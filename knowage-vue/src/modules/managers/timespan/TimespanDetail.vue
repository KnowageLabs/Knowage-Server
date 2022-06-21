<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #start>{{ timespan?.name }}</template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="saveDisabled" @click="saveTimespan(null)" data-test="save-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTimespanDetailsConfirm" />
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />

    <div class="p-d-flex p-flex-column kn-flex kn-overflow">
        <TimespanForm :propTimespan="timespan" :categories="categories" @touched="touched = true"></TimespanForm>
        <TimespanIntervalTable :propTimespan="timespan" @touched="touched = true"></TimespanIntervalTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iCategory, iInterval } from './Timespan'
import { AxiosResponse } from 'axios'
import { formatDate } from '@/helpers/commons/localeHelper'
import { createDateFromIntervalTime } from './timespanHelpers'
import TimespanForm from './TimespanForm.vue'
import TimespanIntervalTable from './TimespanIntervalTable.vue'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'timespan-detail',
    components: { TimespanForm, TimespanIntervalTable },
    props: { id: { type: String }, clone: { type: String }, categories: { type: Array as PropType<iCategory[]> }, timespans: { type: Array as PropType<iTimespan[]>, required: true } },
    emits: ['timespanCreated'],
    data() {
        return {
            timespan: null as iTimespan | null,
            operation: 'create',
            loading: false,
            touched: false
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
            this.touched = false

            if (this.id) {
                await this.$http
                    .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/timespan/loadTimespan?ID=${this.id}`)
                    .then((response: AxiosResponse<any>) => {
                        this.timespan = response.data
                        if (this.timespan?.type === 'temporal') this.formatIntervalDates()
                    })
                    .catch(() => {})
                if (this.clone === 'true') await this.cloneTimespan()
            } else {
                this.timespan = this.getDefautTimespan()
            }
            this.loading = false
        },
        formatIntervalDates() {
            if (this.timespan) {
                this.timespan.definition.forEach((interval: iInterval) => {
                    interval.fromLocalized = this.getFormattedDate(interval.from)
                    interval.toLocalized = this.getFormattedDate(interval.to)
                })
            }
        },
        getFormattedDate(date: string) {
            return formatDate(date, '', 'DD/MM/yyyy')
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
        closeTimespanDetailsConfirm() {
            if (this.touched) {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.closeTimespanDetails()
                    }
                })
            } else {
                this.closeTimespanDetails()
            }
        },
        closeTimespanDetails() {
            this.touched = false
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
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/timespan/saveTimespan`, timespan)
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

            this.genereateClonedTimespanName(tempTimespan)
            if (this.checkIfCloneAlreadyDefined(tempTimespan)) return

            this.createFirstIntervalForClonedTimespan(tempTimespan)
            await this.saveTimespan(tempTimespan)
        },
        genereateClonedTimespanName(tempTimespan: iTimespan) {
            const pattern = new RegExp(/.*#.*\d/gi)
            if (pattern.test(tempTimespan.name)) {
                tempTimespan.name = tempTimespan.name.substring(0, tempTimespan.name.length - 1) + '' + (parseInt(tempTimespan.name[tempTimespan.name.length - 1]) + 1)
            } else {
                tempTimespan.name = tempTimespan.name + ' #2'
            }
        },
        checkIfCloneAlreadyDefined(tempTimespan: iTimespan) {
            let alreadyDefined = false
            for (let i = 0; i < this.timespans.length; i++) {
                if (this.timespans[i].name === tempTimespan.name) {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.cloneAlreadyDefined') })
                    this.$router.push('/timespan')
                    this.timespan = this.getDefautTimespan()
                    alreadyDefined = true
                    break
                }
            }
            return alreadyDefined
        },
        createFirstIntervalForClonedTimespan(tempTimespan: iTimespan) {
            const firstInterval = tempTimespan.definition[tempTimespan.definition.length - 1]

            const fromDate = createDateFromIntervalTime(firstInterval.from)
            const toDate = createDateFromIntervalTime(firstInterval.to)

            const millsDay = 86400000
            const tempInterval = { from: toDate, to: new Date() }
            tempInterval.from.setTime(toDate.getTime() + millsDay)
            tempInterval.to.setTime(tempInterval.from.getTime() + toDate.getTime() - fromDate.getTime() - millsDay)

            firstInterval.from = this.getFormattedDateString(tempInterval.from)
            firstInterval.to = this.getFormattedDateString(tempInterval.to)
            tempTimespan.definition = [firstInterval]
        },
        getFormattedDateString(date: Date) {
            return date.getDate() + '/' + (date.getMonth() + 1) + '/' + date.getFullYear()
        }
    }
})
</script>
