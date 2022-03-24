<template>
    <div class="p-grid p-m-2 knScheduler p-jc-between">
        <div class="p-field p-float-label p-col-12 p-mb-1" v-if="descriptor.startDateEnabled">
            <Calendar id="startDate" v-model="startDate" :showIcon="true" /><label for="startDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.startDate') }} </label>
        </div>

        <span class="p-field p-float-label p-col-12 p-mb-1">
            <Dropdown id="selectedRefreshRate" class="kn-material-input" v-model="selectedRefreshRate" dataKey="id" optionLabel="name" optionValue="code" :options="refreshRates" maxLength="100" @change="resetFormula()" :disabled="readOnly" />
            <label for="refreshRate" class="kn-material-input-label"> {{ $t(descriptor.refreshRate.placeholder) }}</label>
        </span>

        <div class="p-d-flex p-flex-wrap p-col-12 p-mb-1" v-if="selectedRefreshRate">
            <div v-if="selectedRefreshRate === 'daily'">
                <div class="p-d-flex p-ai-center">
                    <RadioButton class="p-mr-2" id="dayConf1" name="dayConf" value="everyDay" v-model="dayConf" :disabled="readOnly" />
                    <i18n-t keypath="knScheduler.everyDay" tag="div" class="p-d-flex p-ai-center p-mr-2">
                        <template #day>
                            <Dropdown id="type" class="kn-material-input p-mr-2" v-model="selectedDay" optionLabel="name" optionValue="code" :options="getNumberOptions(5)" maxLength="100" @change="dayConf = 'everyDay'" :disabled="readOnly" />
                        </template>
                    </i18n-t>

                    <span class="p-mr-2" style="white-space:nowrap">
                        {{ $t('knScheduler.startingIn') }}
                    </span>
                    <Dropdown id="type" :class="descriptor.style.dropdown" v-model="selectedDayExtended" dataKey="id" optionLabel="name" optionValue="id" :options="days" maxLength="100" @change="dayConf = 'everyDay'" :disabled="readOnly" />
                </div>

                <div class="field-radiobutton">
                    <RadioButton class="p-mr-2" id="dayConf2" name="dayConf" value="everyNotWorkingDays" v-model="dayConf" :disabled="readOnly" />
                    {{ $t('knScheduler.everyNotWorkingDays') }}
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'weekly'" class="p-d-flex p-flex-wrap">
                <div class="p-d-flex field-checkbox p-mb-2 p-mr-2 dayCheckbox" v-for="(day, index) in descriptor.days" v-bind:key="index">
                    <Checkbox :id="day - `${index}`" :name="day.name" :value="day.code" v-model="selectedWeekdays[day.id]" /><label class="p-ml-2">{{ $t(day.name) }}</label>
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'monthly'">
                <i18n-t keypath="knScheduler.everyMonth" tag="div" class="p-d-flex p-ai-center p-mr-2">
                    <template #month>
                        <Dropdown id="selectedMonth" :class="descriptor.style.dropdown" v-model="selectedMonth" optionLabel="name" optionValue="id" :options="getNumberOptions(5)" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                    </template>
                </i18n-t>

                <div class="p-d-flex p-ai-center">
                    {{ $t('knScheduler.startingIn') }}
                    <Dropdown id="selectedMonthExtended" :class="descriptor.style.dropdown" v-model="selectedMonthExtended" dataKey="id" optionLabel="name" optionValue="id" :options="months" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                </div>

                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf1" name="monthConf" v-model="monthConf" value="theDay" :disabled="readOnly" /> {{ $t('knScheduler.theDay') }}

                        <Dropdown id="selectedDayNumber" :class="descriptor.style.dropdown" v-model="selectedDayNumber" dataKey="id" optionLabel="name" optionValue="code" :options="getNumberOptions(31)" maxLength="100" @change="monthConf = 'theDay'" :disabled="readOnly" />
                    </div>
                </div>

                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf2" name="monthConf" v-model="monthConf" value="theOrdinalDay" :disabled="readOnly" /> {{ $t('knScheduler.the') }}

                        <Dropdown id="selectedDayOrdinal" :class="descriptor.style.dropdown" v-model="selectedDayOrdinal" dataKey="id" optionLabel="name" optionValue="id" :options="ordinal" maxLength="100" @change="monthConf = 'theOrdinalDay'" :disabled="readOnly" />

                        <Dropdown id="selectedDayExtended" :class="descriptor.style.dropdown" v-model="selectedDayExtended" dataKey="id" optionLabel="name" optionValue="id" :options="days" maxLength="100" @change="monthConf = 'theOrdinalDay'" :disabled="readOnly" />
                    </div>
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'yearly'">
                <i18n-t keypath="knScheduler.everyYear" tag="div" class="p-d-flex p-ai-center p-mr-2">
                    <template #year>
                        <Dropdown id="selectedYear" :class="descriptor.style.dropdown" v-model="selectedYear" dataKey="id" optionLabel="name" optionValue="code" :options="getNumberOptions(5)" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                    </template>
                </i18n-t>

                <div class="p-d-flex p-ai-center">
                    {{ $t('knScheduler.in') }}

                    <Dropdown id="selectedMonth" :class="descriptor.style.dropdown" v-model="selectedMonth" dataKey="id" optionLabel="name" optionValue="code" :options="months" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                </div>

                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf1" name="yearConf" v-model="yearConf" value="theDay" :disabled="readOnly" /> {{ $t('knScheduler.theDay') }}

                        <Dropdown id="type" :class="descriptor.style.dropdown" v-model="selectedDayNumber" dataKey="id" optionLabel="name" optionValue="code" :options="getNumberOptions(31)" maxLength="100" @change="yearConf = 'theDay'" :disabled="readOnly" />
                    </div>
                </div>
                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf2" name="yearConf" v-model="yearConf" value="theOrdinalDay" :disabled="readOnly" /> {{ $t('knScheduler.the') }}

                        <Dropdown id="selectedDayOrdinal" :class="descriptor.style.dropdown" v-model="selectedDayOrdinal" dataKey="id" optionLabel="name" optionValue="id" :options="ordinal" maxLength="100" @change="yearConf = 'theOrdinalDay'" :disabled="readOnly" />

                        <Dropdown id="selectedDayExtended" :class="descriptor.style.dropdown" v-model="selectedDayExtended" dataKey="id" optionLabel="name" optionValue="id" :options="days" maxLength="100" @change="yearConf = 'theOrdinalDay'" :disabled="readOnly" />
                    </div>
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'custom'">
                <span class="p-field p-float-label p-col-12"> <InputText :id="name" type="text" v-model="localFormula" v-bind="$attrs" :class="[cssClass ? cssClass + ' kn-truncated' : 'kn-material-input kn-truncated', required && !modelValue ? 'p-invalid' : '']"/></span>
            </div>
        </div>
        <div class="p-field p-float-label p-col-12 p-mb-1" v-if="descriptor.endDateEnabled">
            <Calendar id="icon" v-model="endDate" :showIcon="true" />
            <label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} </label>
        </div>

        <Message class="p-col-12 messageClass" severity="info" :closable="false"> {{ getCronstrueFormula }} </Message>
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
            formula: String
        },
        emits: ['touched', 'validSchedulation'],
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
                localFormula: '',
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
                return cronstrue.toString(this.localFormula, { locale: cronLocale })
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

            this.localFormula = this.formula || '0 0 0 * * ? *'

            this.selectedRefreshRate = this.descriptor?.refreshRate.options[0].code
        },
        methods: {
            getNumberOptions(max: Number) {
                let tmp = [] as any
                for (var i = 1; i <= max; i++) tmp.push({ code: i.toString(), id: i, name: i.toString() })
                return tmp
            },
            isSet(formulaToken): Boolean {
                return formulaToken !== this.allValues && formulaToken !== this.noSpecificValue
            },
            parseFormula(formula) {
                if (formula === '0 0 0 ? * MON,TUE,WED,THU,FRI *') {
                    // @ts-ignore
                    this.selectedRefreshRate = 'daily'
                    // @ts-ignore
                    this.dayConf = 'everyNotWorkingDays'
                } else {
                    let formulaArr = formula.split(' ')
                    if (this.isSet(formulaArr[3])) {
                        this.selectedDayNumber = formulaArr[3]
                    }
                    if (this.isSet(formulaArr[4])) {
                        let splitted = formulaArr[4].split('/')
                        if (splitted.length > 1) {
                            // @ts-ignore
                            this.selectedMonthExtended = parseInt(splitted[0])
                            // @ts-ignore
                            this.selectedMonth = parseInt(splitted[1])
                        } else if (this.months.filter((x) => x.code === formulaArr[4]).length == 1) {
                            this.selectedMonth = formulaArr[4]
                        }
                    }
                    if (this.isSet(formulaArr[5])) {
                        let splitted = formulaArr[5].split('/')
                        if (splitted.length > 1) {
                            // @ts-ignore
                            this.selectedDayExtended = parseInt(splitted[0])
                            // @ts-ignore
                            this.selectedDay = splitted[1]
                        } else {
                            splitted = formulaArr[5].split('#')
                            if (splitted.length > 1) {
                                // @ts-ignore
                                this.selectedDayExtended = parseInt(splitted[0])
                                // @ts-ignore
                                this.selectedDayOrdinal = parseInt(splitted[1])
                            } else {
                                splitted = formulaArr[5].split(',')
                                if (splitted.length > 0) {
                                    for (var index in splitted) {
                                        let day = this.descriptor?.days.filter((x) => x.code === splitted[index].toLowerCase())[0]

                                        this.selectedWeekdays[day.id] = [day.code]
                                    }
                                }
                            }
                        }
                    }
                    if (this.isSet(formulaArr[6])) {
                        // @ts-ignore
                        this.selectedYear = formulaArr[6].split('/')[1]
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
                this.localFormula = '0 0 0 * * ? *'

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
                let formulaArr = this.localFormula.split(' ')
                formulaArr[0] = formulaArr[1] = formulaArr[2] = '0'
                if (this.selectedRefreshRate === 'daily') {
                    if (this.dayConf === 'everyDay') {
                        formulaArr[3] = this.noSpecificValue

                        if (this.selectedDay && this.selectedDayExtended) {
                            formulaArr[5] = this.selectedDayExtended + '/' + this.selectedDay
                        }
                    } else if (this.dayConf === 'everyNotWorkingDays') {
                        formulaArr = '0 0 0 ? * MON,TUE,WED,THU,FRI *'.split(' ')
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

                    formulaArr[5] = t

                    formulaArr[4] = this.allValues
                } else if (this.selectedRefreshRate === 'monthly') {
                    if (this.selectedMonthExtended && this.selectedMonth) {
                        formulaArr[4] = this.selectedMonthExtended + '/' + this.selectedMonth
                    }

                    if (this.monthConf === 'theDay') {
                        formulaArr[3] = this.selectedDayNumber ? this.selectedDayNumber! : this.allValues

                        formulaArr[5] = this.noSpecificValue
                    } else if (this.monthConf === 'theOrdinalDay') {
                        formulaArr[3] = this.noSpecificValue

                        if (this.selectedDayExtended && this.selectedDayOrdinal) {
                            formulaArr[5] = this.selectedDayExtended + '#' + this.selectedDayOrdinal
                        }
                    }
                    formulaArr[6] = this.allValues
                } else if (this.selectedRefreshRate === 'yearly') {
                    formulaArr[4] = this.selectedMonth ? this.selectedMonth! : this.allValues

                    if (this.yearConf === 'theDay') {
                        formulaArr[3] = this.selectedDayNumber ? this.selectedDayNumber! : this.allValues
                    } else if (this.yearConf === 'theOrdinalDay') {
                        formulaArr[3] = this.noSpecificValue

                        if (this.selectedDayExtended && this.selectedDayOrdinal) {
                            formulaArr[5] = this.selectedDayExtended + '#' + this.selectedDayOrdinal
                        }
                    }

                    if (this.selectedYear) {
                        formulaArr[6] = moment().year() + '/' + this.selectedYear
                    } else {
                        formulaArr[6] = this.allValues
                    }
                }
                this.localFormula = formulaArr.join(' ')
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
            formula(newFormula) {
                if (newFormula) {
                    this.localFormula = newFormula
                    this.parseFormula(this.localFormula)
                }
            }
        }
    })
</script>

<style lang="css">
    .knScheduler {
        min-width: 200px;
        min-height: 300px;
        height: 100%;
        font-size: 0.9rem;
    }
    .dayCheckbox {
        width: 100px;
    }

    .messageClass {
        height: 50px;
    }
</style>
