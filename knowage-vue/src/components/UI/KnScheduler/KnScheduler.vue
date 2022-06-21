<template>
    <Message v-if="showHint" severity="info" :closable="false"> {{ $t('managers.workspaceManagement.dataPreparation.monitoring.hint') }} </Message>
    <div class="p-grid p-d-flex p-m-1 p-fluid">
        <div :class="getSchedulerClass" v-if="schedulerVisible">
            <Card class="kn-card full-height">
                <template #content>
                    <div class="p-grid knScheduler p-jc-between p-flex-column">
                        <div class="p-col-12 p-mb-1 p-d-flex p-ai-center p-jc-between">
                            <div>
                                <span> {{ $t('knScheduler.enableSchedulation') }}</span> <InputSwitch v-model="enableSchedulation" @change="toggleSchedulationEnabled" />
                            </div>

                            <div v-if="enableSchedulation" class="p-d-flex p-ai-center">
                                <template v-if="paused">
                                    {{ $t('knScheduler.resumeSchedulation') }}
                                    <Button icon="pi pi-play" class="p-button-text p-button-rounded p-button-plain" @click="togglePause" /> </template
                                ><template v-else>
                                    {{ $t('knScheduler.pauseSchedulation') }}
                                    <Button icon="pi pi-pause" class="p-button-text p-button-rounded p-button-plain" @click="togglePause" />
                                </template>
                            </div>
                        </div>
                        <div class="p-float-label p-col-12 p-mb-1" v-if="descriptor.config.startDateEnabled">
                            <Calendar id="startDate" v-model="startDate" :showIcon="true" @change="$emit('touched')" /><label for="startDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.startDate') }} </label>
                        </div>

                        <div class="p-float-label p-col-12 p-mb-1">
                            <Dropdown
                                id="selectedRefreshRate"
                                class="kn-material-input"
                                v-model="selectedRefreshRate"
                                dataKey="id"
                                optionLabel="name"
                                optionValue="code"
                                :options="refreshRates"
                                maxLength="100"
                                @change="
                                    () => {
                                        resetFormula()
                                        $emit('touched')
                                    }
                                "
                                :disabled="isDisabled"
                            />
                            <label for="refreshRate" class="kn-material-input-label"> {{ $t(descriptor.refreshRate.placeholder) }}</label>
                        </div>

                        <div class="p-d-flex p-flex-wrap p-col-12 p-mb-1" v-if="selectedRefreshRate">
                            <div v-if="selectedRefreshRate === 'daily'" class="p-d-flex p-flex-wrap">
                                <div class="p-d-flex p-ai-center p-flex-wrap itemClass">
                                    <RadioButton :class="descriptor.style.radiobutton" id="dayConf1" name="dayConf" value="everyDay" v-model="dayConf" :disabled="isDisabled" @change="$emit('touched')" />
                                    <i18n-t keypath="knScheduler.everyDay" tag="div" class="p-d-flex p-ai-center p-mr-2">
                                        <template #day>
                                            <Dropdown
                                                id="type"
                                                :class="descriptor.style.dropdown"
                                                v-model="selectedDay"
                                                optionLabel="name"
                                                optionValue="code"
                                                :options="getNumberOptions(5)"
                                                maxLength="100"
                                                @change="
                                                    () => {
                                                        dayConf = 'everyDay'
                                                        $emit('touched')
                                                    }
                                                "
                                                :disabled="isDisabled"
                                            />
                                        </template>
                                    </i18n-t>

                                    <span class="p-mr-2" style="white-space: nowrap">
                                        {{ $t('knScheduler.startingIn') }}
                                    </span>
                                    <Dropdown
                                        id="type"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedDayExtended"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="id"
                                        :options="days"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                dayConf = 'everyDay'
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />
                                </div>

                                <div class="p-d-flex p-ai-center">
                                    <RadioButton :class="descriptor.style.radiobutton" id="dayConf2" name="dayConf" value="everyNotWorkingDays" v-model="dayConf" :disabled="isDisabled" @change="$emit('touched')" />
                                    {{ $t('knScheduler.everyNotWorkingDays') }}
                                </div>
                            </div>
                            <div v-if="selectedRefreshRate === 'weekly'" class="p-d-flex p-flex-wrap">
                                <div class="p-d-flex field-checkbox p-mb-1 p-mr-2 dayCheckbox" v-for="(day, index) in descriptor.days" v-bind:key="index">
                                    <Checkbox :id="day - `${index}`" :name="day.name" :value="day.code" v-model="selectedWeekdays[day.id]" @change="$emit('touched')" /><label class="p-ml-2">{{ $t(day.name) }}</label>
                                </div>
                            </div>
                            <div v-else-if="selectedRefreshRate === 'monthly'">
                                <i18n-t keypath="knScheduler.everyMonth" tag="div" class="p-d-flex p-ai-center p-mr-2">
                                    <template #month>
                                        <Dropdown
                                            id="selectedMonth"
                                            :class="descriptor.style.dropdown"
                                            v-model="selectedMonth"
                                            optionLabel="name"
                                            optionValue="id"
                                            :options="getNumberOptions(5)"
                                            maxLength="100"
                                            @change="
                                                () => {
                                                    updateFormula()
                                                    $emit('touched')
                                                }
                                            "
                                            :disabled="isDisabled"
                                        />
                                    </template>
                                </i18n-t>

                                <div class="p-d-flex p-ai-center">
                                    {{ $t('knScheduler.startingIn') }}
                                    <Dropdown
                                        id="selectedMonthExtended"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedMonthExtended"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="id"
                                        :options="months"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                updateFormula()
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />
                                </div>

                                <div class="p-d-flex p-ai-center p-flex-wrap itemClass">
                                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf1" name="monthConf" v-model="monthConf" value="theDay" :disabled="isDisabled" @change="$emit('touched')" /> {{ $t('knScheduler.theDay') }}

                                    <Dropdown
                                        id="selectedDayNumber"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedDayNumber"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="code"
                                        :options="getNumberOptions(31)"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                monthConf = 'theDay'
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />
                                </div>

                                <div class="p-d-flex p-ai-center p-flex-wrap itemClass">
                                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf2" name="monthConf" v-model="monthConf" value="theOrdinalDay" :disabled="isDisabled" /> {{ $t('knScheduler.the') }}

                                    <Dropdown
                                        id="selectedDayOrdinal"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedDayOrdinal"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="id"
                                        :options="ordinal"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                monthConf = 'theOrdinalDay'
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />

                                    <Dropdown
                                        id="selectedDayExtended"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedDayExtended"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="id"
                                        :options="days"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                monthConf = 'theOrdinalDay'
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />
                                </div>
                            </div>
                            <div v-else-if="selectedRefreshRate === 'yearly'">
                                <i18n-t keypath="knScheduler.everyYear" tag="div" class="p-d-flex p-ai-center p-mr-2">
                                    <template #year>
                                        <Dropdown
                                            id="selectedYear"
                                            :class="descriptor.style.dropdown"
                                            v-model="selectedYear"
                                            dataKey="id"
                                            optionLabel="name"
                                            optionValue="code"
                                            :options="getNumberOptions(5)"
                                            maxLength="100"
                                            @change="
                                                () => {
                                                    updateFormula()
                                                    $emit('touched')
                                                }
                                            "
                                            :disabled="isDisabled"
                                        />
                                    </template>
                                </i18n-t>

                                <div class="p-d-flex p-ai-center">
                                    {{ $t('knScheduler.in') }}

                                    <Dropdown
                                        id="selectedMonth"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedMonth"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="code"
                                        :options="months"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                updateFormula()
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />
                                </div>

                                <div class="p-d-flex p-ai-center p-flex-wrap itemClass">
                                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf1" name="yearConf" v-model="yearConf" value="theDay" :disabled="isDisabled" /> {{ $t('knScheduler.theDay') }}

                                    <Dropdown
                                        id="type"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedDayNumber"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="code"
                                        :options="getNumberOptions(31)"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                yearConf = 'theDay'
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />
                                </div>

                                <div class="p-d-flex p-ai-center p-flex-wrap itemClass">
                                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf2" name="yearConf" v-model="yearConf" value="theOrdinalDay" :disabled="isDisabled" /> {{ $t('knScheduler.the') }}

                                    <Dropdown
                                        id="selectedDayOrdinal"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedDayOrdinal"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="id"
                                        :options="ordinal"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                yearConf = 'theOrdinalDay'
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />

                                    <Dropdown
                                        id="selectedDayExtended"
                                        :class="descriptor.style.dropdown"
                                        v-model="selectedDayExtended"
                                        dataKey="id"
                                        optionLabel="name"
                                        optionValue="id"
                                        :options="days"
                                        maxLength="100"
                                        @change="
                                            () => {
                                                yearConf = 'theOrdinalDay'
                                                $emit('touched')
                                            }
                                        "
                                        :disabled="isDisabled"
                                    />
                                </div>
                            </div>
                            <div v-else-if="selectedRefreshRate === 'custom'">
                                <span class="p-float-label p-col-12"> <InputText :id="name" type="text" v-model="localCronExpression" v-bind="$attrs" :class="[cssClass ? cssClass + ' kn-truncated' : 'kn-material-input kn-truncated', required && !modelValue ? 'p-invalid' : '']" /></span>
                                <small id="custom-cron-hint" v-html="$t('knScheduler.customCronHint')"></small>
                            </div>
                        </div>

                        <div class="p-float-label p-col-12 p-mb-1" v-if="descriptor.config.endDateEnabled">
                            <Calendar id="icon" v-model="endDate" :showIcon="true" />
                            <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} </label>
                        </div>

                        <Message v-if="!readOnly" :class="['p-col-12 messageClass', readOnly ? 'p-message-disabled' : '']" severity="info" :closable="false">
                            <template v-if="paused">{{ $t('knScheduler.schedulationPaused') }} </template><template v-else>{{ getCronstrueFormula }} </template></Message
                        >
                    </div></template
                ></Card
            >
        </div>
        <div :class="getLogsTableClass" v-if="logsVisible">
            <Card class="kn-card full-height--with-toolbar">
                <template #header
                    ><Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start>
                            {{ $t('managers.workspaceManagement.dataPreparation.monitoring.executionLogs') }}
                        </template>
                    </Toolbar></template
                ><template #content>
                    <DataTable
                        :value="logs"
                        v-model:filters="filters"
                        class="p-datatable-sm kn-table"
                        columnResizeMode="fit | expand"
                        dataKey="id"
                        :paginator="true"
                        :rows="10"
                        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        responsiveLayout="stack"
                        breakpoint="960px"
                        :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                        :globalFilterFields="descriptor.globalFilterFields"
                        :loading="loadingLogs"
                    >
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #loading>
                            {{ $t('common.info.dataLoading') }}
                        </template>

                        <Column
                            class="kn-truncated"
                            v-for="col of descriptor.columns"
                            :field="col.field"
                            :header="col.field !== 'errorFile' ? $t(col.header) : ''"
                            :key="col.field"
                            :sortable="col.field !== 'errorFile'"
                            :selectionMode="col.field == 'selectionMode' ? 'multiple' : ''"
                            :exportable="col.field == 'selectionMode' ? false : ''"
                            ><template #body="slotProps">
                                <span v-if="col.field === 'start' || col.field === 'stop'"> {{ getFormattedDate(slotProps.data[col.field]) }}</span>
                                <span v-else>{{ slotProps.data[col.field] }}</span>
                            </template></Column
                        >

                        <Column class="kn-truncated" field="errorFile" :header="''" :sortable="false" :selectionMode="false" :exportable="false">
                            <template #body="slotProps">
                                <span><Button icon="pi pi-download" class="p-button-link" v-if="slotProps.data['status'] === 'KO'" @click="downloadLog(slotProps.data)" /></span> </template
                        ></Column> </DataTable
                ></template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'
import RadioButton from 'primevue/radiobutton'
import InputText from 'primevue/inputtext'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputSwitch from 'primevue/inputswitch'

import moment from 'moment'
import { luxonFormatDate } from '@/helpers/commons/localeHelper'
import cronstrue from 'cronstrue/i18n'
import { downloadDirectFromResponse } from '@/helpers/commons/fileHelper'
import { IDataPrepLog } from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoring'
import { AxiosResponse } from 'axios'
import { mapState } from 'vuex'

export default defineComponent({
    name: 'kn-scheduler',
    components: {
        Calendar,
        Checkbox,
        Column,
        DataTable,
        Dropdown,
        InputSwitch,
        Message,
        InputText,
        RadioButton
    },
    props: {
        descriptor: Object,
        readOnly: { type: Boolean, default: false },
        cronExpression: String,
        logs: [] as any,
        schedulerVisible: { type: Boolean, default: true },
        logsVisible: { type: Boolean, default: true },
        schedulationEnabled: Boolean,
        schedulationPaused: Boolean,
        loadingLogs: { type: Boolean, default: false }
    },
    emits: ['touched', 'update:schedulationPaused', 'update:schedulationEnabled', 'update:currentCronExpression'],
    data() {
        return {
            startDate: null as Date | null,
            endDate: null as Date | null,
            selectedRefreshRate: null,
            selectedMonth: null,
            selectedYear: null,
            selectedDay: null,
            selectedDayExtended: null,
            selectedDayOrdinal: null,
            selectedDayNumber: null,
            selectedMonthExtended: null,
            dateFormat: '' as string,
            dayConf: null,
            monthConf: null,
            yearConf: null,
            days: [] as any,
            months: [] as any,
            ordinal: [] as any,
            refreshRates: [] as any,
            nextFlush: '',
            selectedWeekdays: {} as any,
            startDateEnabled: false,
            endDateEnabled: false,
            localCronExpression: '0 0 0 * * ? *',
            // CONST
            allValues: '*',
            noSpecificValue: '?',
            enableSchedulation: true,
            paused: false
        }
    },
    computed: {
        ...mapState({
            configuration: 'configuration'
        }),
        getCronstrueFormula(): String {
            let locale = localStorage.getItem('locale')
            let cronLocale = ''
            if (locale) {
                let splitted = locale.split('_')

                cronLocale = locale.includes('#') ? (cronLocale = splitted[0] + '_' + splitted[2]) : (cronLocale = splitted[0])
            }
            return cronstrue.toString(this.localCronExpression, { locale: cronLocale })
        },
        getSchedulerClass(): String {
            if (this.logsVisible) {
                if (this.schedulerVisible) {
                    return 'p-col-5'
                } else {
                    return 'p-col-0'
                }
            } else {
                return 'p-col-12'
            }
        },
        getLogsTableClass(): String {
            if (this.schedulerVisible) {
                if (this.logsVisible) {
                    return 'p-col-7'
                } else {
                    return 'p-col-0'
                }
            } else {
                return 'p-col-12'
            }
        },
        isDisabled(): Boolean {
            return this.readOnly || !this.enableSchedulation || this.loadingLogs
        }
    },
    async created() {
        if (!this.configuration || (!this.configuration && !this.configuration['SPAGOBI.TIMESTAMP-FORMAT.format'])) await this.loadUserConfig()

        this.startDateEnabled = this.descriptor?.config.startDateEnabled
        if (this.startDateEnabled) {
            this.startDate = new Date()
        }

        this.descriptor?.refreshRate.options.forEach((x) => {
            this.refreshRates.push({ code: x.code, id: x.id, name: this.$t(x.name) })
        })

        this.descriptor?.days.forEach((x) => {
            this.days.push({ code: x.code, id: x.id, name: this.$t(x.name) })
        })

        this.descriptor?.months.forEach((x) => {
            this.months.push({ code: x.code, id: x.id, name: this.$t(x.name) })
        })

        this.descriptor?.monthly.ordinal.options.forEach((x) => {
            this.ordinal.push({ code: x.code, id: x.id, name: this.$t(x.name) })
        })

        this.localCronExpression = this.cronExpression || '0 0 0 * * ? *'

        this.selectedRefreshRate = this.descriptor?.refreshRate.options[0].code

        if (this.schedulationEnabled) this.enableSchedulation = this.schedulationEnabled
    },
    updated() {
        this.enableSchedulation = this.schedulationEnabled

        this.paused = this.schedulationPaused
    },
    methods: {
        async downloadLog(item: IDataPrepLog) {
            await this.$http.post(import.meta.env.VITE_DATA_PREPARATION_PATH + '1.0/process/' + item.id + '/log/download').then((response: AxiosResponse<any>) => {
                downloadDirectFromResponse(response)
            })
        },
        getFormattedDate(date: any): String {
            return luxonFormatDate(new Date(date), undefined, this.dateFormat)
        },
        getNumberOptions(max: Number) {
            let tmp = [] as any
            for (var i = 1; i <= max; i++) tmp.push({ code: i.toString(), id: i, name: i.toString() })
            return tmp
        },
        isSet(cronExpressionToken): Boolean {
            return cronExpressionToken !== this.allValues && cronExpressionToken !== this.noSpecificValue
        },
        async loadUserConfig() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/user-configs`).then((response: AxiosResponse<any>) => {
                if (response.data) {
                    this.dateFormat = response.data['SPAGOBI.TIMESTAMP-FORMAT.format'] ? response.data['SPAGOBI.TIMESTAMP-FORMAT.format'] : response.data['SPAGOBI.DATE-FORMAT-SERVER.format'] === '%Y-%m-%d' ? 'dd/MM/yyyy' : response.data['SPAGOBI.DATE-FORMAT-SERVER.format']
                }
            })
        },
        parseFormula(cronExpression) {
            if (cronExpression === '0 0 0 ? * MON,TUE,WED,THU,FRI *') {
                // @ts-ignore
                this.selectedRefreshRate = 'daily'
                // @ts-ignore
                this.dayConf = 'everyNotWorkingDays'
            } else {
                let cronExpressionArr = cronExpression.split(' ')
                if (this.isSet(cronExpressionArr[3])) {
                    this.selectedDayNumber = cronExpressionArr[3]
                }
                if (this.isSet(cronExpressionArr[4])) {
                    let splitted = cronExpressionArr[4].split('/')
                    if (splitted.length > 1) {
                        // @ts-ignore
                        this.selectedMonthExtended = parseInt(splitted[0])
                        // @ts-ignore
                        this.selectedMonth = parseInt(splitted[1])
                    } else if (this.months.filter((x) => x.code === cronExpressionArr[4]).length == 1) {
                        this.selectedMonth = cronExpressionArr[4]
                    }
                }
                if (this.isSet(cronExpressionArr[5])) {
                    let splitted = cronExpressionArr[5].split('/')
                    if (splitted.length > 1) {
                        // @ts-ignore
                        this.selectedDayExtended = parseInt(splitted[0])
                        // @ts-ignore
                        this.selectedDay = splitted[1]
                    } else {
                        splitted = cronExpressionArr[5].split('#')
                        if (splitted.length > 1) {
                            // @ts-ignore
                            this.selectedDayExtended = parseInt(splitted[0])
                            // @ts-ignore
                            this.selectedDayOrdinal = parseInt(splitted[1])
                        } else {
                            splitted = cronExpressionArr[5].split(',')
                            if (splitted.length > 0) {
                                for (var index in splitted) {
                                    let day = this.descriptor?.days.filter((x) => x.code === splitted[index].toLowerCase())[0]

                                    this.selectedWeekdays[day.id] = [day.code]
                                }
                            }
                        }
                    }
                }
                if (this.isSet(cronExpressionArr[6])) {
                    // @ts-ignore
                    this.selectedYear = cronExpressionArr[6].split('/')[1]
                }

                if (this.selectedYear) {
                    // @ts-ignore
                    this.selectedRefreshRate = 'yearly'

                    if (this.selectedDayExtended)
                        // @ts-ignore
                        this.yearConf = 'theOrdinalDay'
                    // @ts-ignore
                    else this.yearConf = 'theDay'
                } else if (Object.keys(this.selectedWeekdays).length > 0) {
                    // @ts-ignore
                    this.selectedRefreshRate = 'weekly'
                } else {
                    if (this.selectedDay) {
                        // @ts-ignore
                        this.selectedRefreshRate = 'daily'
                        // @ts-ignore
                        this.dayConf = 'everyDay'
                    } else if (this.selectedMonth) {
                        // @ts-ignore
                        this.selectedRefreshRate = 'monthly'
                        if (this.selectedDayNumber) {
                            // @ts-ignore
                            this.monthConf = 'theDay'
                        } else {
                            // @ts-ignore
                            this.monthConf = 'theOrdinalDay'
                        }
                    }
                }
            }
        },
        resetFormula() {
            this.localCronExpression = '0 0 0 * * ? *'

            this.selectedMonth = null
            this.selectedYear = null
            this.selectedDay = null
            this.selectedDayExtended = null
            this.selectedDayOrdinal = null
            this.selectedDayNumber = null
            this.dayConf = null
            this.monthConf = null
            this.yearConf = null

            this.$emit('update:currentCronExpression', this.localCronExpression)
        },
        updateFormula() {
            let cronExpressionArr = this.localCronExpression.split(' ')
            cronExpressionArr[0] = cronExpressionArr[1] = cronExpressionArr[2] = '0'
            if (this.selectedRefreshRate === 'daily') {
                if (this.dayConf === 'everyDay') {
                    cronExpressionArr[3] = this.noSpecificValue

                    if (this.selectedDay && this.selectedDayExtended) {
                        cronExpressionArr[5] = this.selectedDayExtended + '/' + this.selectedDay
                    }
                } else if (this.dayConf === 'everyNotWorkingDays') {
                    cronExpressionArr = '0 0 0 ? * MON,TUE,WED,THU,FRI *'.split(' ')
                }
            } else if (this.selectedRefreshRate === 'weekly') {
                let t = ''
                let weekdayKeys = Object.keys(this.selectedWeekdays)
                if (weekdayKeys.length > 0) {
                    let set = new Set()

                    for (var day in this.selectedWeekdays) {
                        if (this.selectedWeekdays[day][0]) {
                            set.add(this.selectedWeekdays[day][0].toUpperCase())
                        }
                    }
                    t = Array.from(set).join(',')
                } else {
                    t += this.allValues
                }

                cronExpressionArr[5] = t

                cronExpressionArr[3] = '?'

                cronExpressionArr[4] = this.allValues
            } else if (this.selectedRefreshRate === 'monthly') {
                if (this.selectedMonthExtended && this.selectedMonth) {
                    cronExpressionArr[4] = this.selectedMonthExtended + '/' + this.selectedMonth
                }

                if (this.monthConf === 'theDay') {
                    cronExpressionArr[3] = this.selectedDayNumber ? this.selectedDayNumber! : this.allValues

                    cronExpressionArr[5] = this.noSpecificValue
                } else if (this.monthConf === 'theOrdinalDay') {
                    cronExpressionArr[3] = this.noSpecificValue

                    if (this.selectedDayExtended && this.selectedDayOrdinal) {
                        cronExpressionArr[5] = this.selectedDayExtended + '#' + this.selectedDayOrdinal
                    }
                }
                cronExpressionArr[6] = this.allValues
            } else if (this.selectedRefreshRate === 'yearly') {
                cronExpressionArr[4] = this.selectedMonth ? this.selectedMonth! : this.allValues

                if (this.yearConf === 'theDay') {
                    cronExpressionArr[3] = this.selectedDayNumber ? this.selectedDayNumber! : this.allValues
                } else if (this.yearConf === 'theOrdinalDay') {
                    cronExpressionArr[3] = this.noSpecificValue

                    if (this.selectedDayExtended && this.selectedDayOrdinal) {
                        cronExpressionArr[5] = this.selectedDayExtended + '#' + this.selectedDayOrdinal
                    }
                }

                if (this.selectedYear) {
                    cronExpressionArr[6] = moment().year() + '/' + this.selectedYear
                } else {
                    cronExpressionArr[6] = this.allValues
                }
            }
            this.localCronExpression = cronExpressionArr.join(' ')

            this.$emit('update:currentCronExpression', this.localCronExpression)
        },
        togglePause() {
            this.paused = !this.paused
            this.$emit('update:schedulationPaused', this.paused)
            this.$emit('touched')
        },
        toggleSchedulationEnabled() {
            this.$emit('update:schedulationEnabled', this.enableSchedulation)
            this.$emit('touched')
        }
    },
    watch: {
        selectedRefreshRate() {
            this.updateFormula()
        },

        selectedMonth() {
            this.updateFormula()
        },
        selectedYear() {
            this.updateFormula()
        },
        selectedDay() {
            this.updateFormula()
        },
        selectedDayExtended() {
            this.updateFormula()
        },
        selectedDayOrdinal() {
            this.updateFormula()
        },
        selectedDayNumber() {
            this.updateFormula()
        },
        dayConf() {
            this.updateFormula()
        },
        monthConf() {
            this.updateFormula()
        },
        selectedWeekdays: {
            handler() {
                this.updateFormula()
            },
            deep: true
        },
        cronExpression(newFormula) {
            if (newFormula) {
                this.localCronExpression = newFormula
                this.parseFormula(this.localCronExpression)
            }
        },
        localCronExpression() {
            this.parseFormula(this.localCronExpression)
        },
        schedulationPaused(newSchedulationPaused) {
            this.paused = newSchedulationPaused
        },
        schedulationEnabled(newSchedulationEnabled) {
            this.enableSchedulation = newSchedulationEnabled
        }
    }
})
</script>

<style lang="css">
.knScheduler {
    min-width: 200px;
    min-height: 100px;
    max-height: 350px;
    font-size: 0.9rem;
}
.dayCheckbox {
    width: 100px;
}

.messageClass {
    height: 50px;
}

.itemClass {
    width: 100%;
}
</style>
