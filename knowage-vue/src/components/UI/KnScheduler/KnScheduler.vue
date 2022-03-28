<template>
    <div class="p-grid knScheduler p-jc-between p-flex-column">
        <div class="p-float-label p-col-12 p-mb-1" v-if="descriptor.startDateEnabled">
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
                :disabled="readOnly"
            />
            <label for="refreshRate" class="kn-material-input-label"> {{ $t(descriptor.refreshRate.placeholder) }}</label>
        </div>

        <div class="p-d-flex p-flex-wrap p-col-12 p-mb-1" v-if="selectedRefreshRate">
            <div v-if="selectedRefreshRate === 'daily'" class="p-d-flex p-flex-wrap">
                <div class="p-d-flex p-ai-center p-flex-wrap">
                    <RadioButton :class="descriptor.style.radiobutton" id="dayConf1" name="dayConf" value="everyDay" v-model="dayConf" :disabled="readOnly" @change="$emit('touched')" />
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
                                :disabled="readOnly"
                            />
                        </template>
                    </i18n-t>

                    <span class="p-mr-2" style="white-space:nowrap">
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
                        :disabled="readOnly"
                    />
                </div>

                <div class="p-d-flex p-ai-center">
                    <RadioButton :class="descriptor.style.radiobutton" id="dayConf2" name="dayConf" value="everyNotWorkingDays" v-model="dayConf" :disabled="readOnly" @change="$emit('touched')" />
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
                            :disabled="readOnly"
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
                        :disabled="readOnly"
                    />
                </div>

                <div class="p-d-flex p-ai-center p-flex-wrap">
                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf1" name="monthConf" v-model="monthConf" value="theDay" :disabled="readOnly" @change="$emit('touched')" /> {{ $t('knScheduler.theDay') }}

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
                        :disabled="readOnly"
                    />
                </div>

                <div class="p-d-flex p-ai-center p-flex-wrap">
                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf2" name="monthConf" v-model="monthConf" value="theOrdinalDay" :disabled="readOnly" /> {{ $t('knScheduler.the') }}

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
                        :disabled="readOnly"
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
                        :disabled="readOnly"
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
                            :disabled="readOnly"
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
                        :disabled="readOnly"
                    />
                </div>

                <div class="p-d-flex p-ai-center p-flex-wrap">
                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf1" name="yearConf" v-model="yearConf" value="theDay" :disabled="readOnly" /> {{ $t('knScheduler.theDay') }}

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
                        :disabled="readOnly"
                    />
                </div>

                <div class="p-d-flex p-ai-center p-flex-wrap">
                    <RadioButton :class="descriptor.style.radiobutton" id="monthConf2" name="yearConf" v-model="yearConf" value="theOrdinalDay" :disabled="readOnly" /> {{ $t('knScheduler.the') }}

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
                        :disabled="readOnly"
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
                        :disabled="readOnly"
                    />
                </div>
            </div>
            <div v-else-if="selectedRefreshRate === 'custom'">
                <span class="p-float-label p-col-12"> <InputText :id="name" type="text" v-model="localCronExpression" v-bind="$attrs" :class="[cssClass ? cssClass + ' kn-truncated' : 'kn-material-input kn-truncated', required && !modelValue ? 'p-invalid' : '']"/></span>
            </div>
        </div>

        <div class="p-float-label p-col-12 p-mb-1" v-if="descriptor.endDateEnabled">
            <Calendar id="icon" v-model="endDate" :showIcon="true" />
            <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} </label>
        </div>
        <Message v-if="!readOnly" :class="['p-col-12 messageClass', readOnly ? 'p-message-disabled' : '']" severity="info" :closable="false"> {{ getCronstrueFormula }} </Message>
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

    import moment from 'moment'

    import cronstrue from 'cronstrue/i18n'

    export default defineComponent({
        name: 'kn-scheduler',
        components: {
            Calendar,
            Checkbox,
            Dropdown,
            Message,
            InputText,
            RadioButton
        },
        props: {
            descriptor: Object,
            readOnly: Boolean || false,
            cronExpression: String
        },
        emits: ['touched', 'validSchedulation', 'update:loading'],
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
                noSpecificValue: '?'
            }
        },
        computed: {
            getCronstrueFormula(): String {
                let locale = localStorage.getItem('locale')
                let cronLocale = ''
                if (locale) {
                    let splitted = locale.split('_')

                    cronLocale = locale.includes('#') ? (cronLocale = splitted[0] + '_' + splitted[2]) : (cronLocale = splitted[0])
                }
                return cronstrue.toString(this.localCronExpression, { locale: cronLocale })
            }
        },
        async created() {
            this.startDateEnabled = this.descriptor?.startDateEnabled
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
        },
        methods: {
            getNumberOptions(max: Number) {
                let tmp = [] as any
                for (var i = 1; i <= max; i++) tmp.push({ code: i.toString(), id: i, name: i.toString() })
                return tmp
            },
            isSet(cronExpressionToken): Boolean {
                return cronExpressionToken !== this.allValues && cronExpressionToken !== this.noSpecificValue
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
                    this.$emit('update:loading', true)
                    this.localCronExpression = newFormula
                    this.parseFormula(this.localCronExpression)
                    this.$emit('update:loading', false)
                }
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
</style>
