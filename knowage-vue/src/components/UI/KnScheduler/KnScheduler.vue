<template>
    <div class="p-grid p-m-2 knScheduler">
        <span class="p-field p-float-label p-col-12" v-if="descriptor.startDateEnabled"
            ><Calendar id="icon" v-model="startDate" :showIcon="true" /><label for="startDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.startDate') }} </label>
        </span>

        <span class="p-field p-float-label p-col-12">
            <Dropdown id="type" class="kn-material-input" v-model="selectedRefreshRate" dataKey="id" optionLabel="name" optionValue="code" :options="refreshRates" maxLength="100" @change="resetFormula()" :disabled="readOnly" />
            <label for="refreshRate" class="kn-material-input-label"> {{ $t(descriptor.refreshRate.placeholder) }}</label>
        </span>

        <div class="p-d-flex p-flex-wrap p-col-12" v-if="selectedRefreshRate">
            <div v-if="selectedRefreshRate === 'daily'">
                <div class="p-d-flex p-ai-center">
                    <RadioButton class="p-mr-2" id="dayConf1" name="dayConf" value="everyDay" v-model="dayConf" :disabled="readOnly" />
                    <i18n-t keypath="knScheduler.everyDay" tag="div" class="p-d-flex p-ai-center p-m-2">
                        <template #day>
                            <Dropdown id="type" class="kn-material-input" v-model="selectedDay" optionLabel="name" optionValue="code" :options="getNumberOptions(5)" maxLength="100" @change="dayConf = 'everyDay'" :disabled="readOnly" />
                        </template>
                    </i18n-t>

                    {{ $t('knScheduler.startingIn') }}
                    <Dropdown id="type" class="kn-material-input" v-model="selectedDayExtended" dataKey="id" optionLabel="name" optionValue="id" :options="days" maxLength="100" @change="dayConf = 'everyDay'" :disabled="readOnly" />
                </div>

                <div class="field-radiobutton">
                    <RadioButton class="p-mr-2" id="dayConf2" name="dayConf" value="everyNotWorkingDays" v-model="dayConf" :disabled="readOnly" />
                    {{ $t('knScheduler.everyNotWorkingDays') }}
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'weekly'" class="p-d-flex p-flex-wrap p-jc-between">
                <!--                 <div class="p-d-flex p-ai-center">
                    <i18n-t keypath="knScheduler.everyWeek" tag="div" class="p-d-flex p-ai-center">
                        <template #week>
                            <span class="p-float-label">
                                <Dropdown id="type" class="kn-material-input" v-model="selectedWeek" optionLabel="name" optionValue="code" :options="getNumberOptions(5)" maxLength="100" @change="updateFormula()" :disabled="readOnly" />

                            </span>
                        </template>
                    </i18n-t>
                </div> -->

                <div class="p-d-flex field-checkbox p-mb-2 p-mr-2" v-for="(day, index) in descriptor.days" v-bind:key="index">
                    <Checkbox :id="day - `${index}`" :name="day.name" :value="day.code" v-model="selectedWeekdays[index]" /><label class="p-ml-2">{{ $t(day.name) }}</label>
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'monthly'">
                <div class="p-d-flex p-ai-center">
                    <i18n-t keypath="knScheduler.everyMonth" tag="div" class="p-d-flex p-ai-center">
                        <template #month>
                            <Dropdown id="type" class="kn-material-input" v-model="selectedMonth" optionLabel="name" optionValue="id" :options="getNumberOptions(5)" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                        </template>
                    </i18n-t>
                </div>
                <div class="p-d-flex p-ai-center">
                    {{ $t('knScheduler.startingIn') }}
                    <Dropdown id="type" class="kn-material-input" v-model="selectedMonthExtended" dataKey="id" optionLabel="name" optionValue="id" :options="months" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                </div>

                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf1" name="monthConf" v-model="monthConf" value="theDay" :disabled="readOnly" /> {{ $t('knScheduler.theDay') }}

                        <Dropdown id="type" class="kn-material-input" v-model="selectedDayNumber" dataKey="id" optionLabel="name" optionValue="code" :options="getNumberOptions(31)" maxLength="100" @change="monthConf = 'theDay'" :disabled="readOnly" />
                    </div>
                </div>

                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf2" name="monthConf" v-model="monthConf" value="theOrdinalDay" :disabled="readOnly" /> {{ $t('knScheduler.the') }}

                        <Dropdown id="type" class="kn-material-input" v-model="selectedDayOrdinal" dataKey="id" optionLabel="name" optionValue="id" :options="ordinal" maxLength="100" @change="monthConf = 'theOrdinalDay'" :disabled="readOnly" />

                        <Dropdown id="type" class="kn-material-input" v-model="selectedDayExtended" dataKey="id" optionLabel="name" optionValue="id" :options="days" maxLength="100" @change="monthConf = 'theOrdinalDay'" :disabled="readOnly" />
                    </div>
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'yearly'">
                <div class="p-d-flex p-ai-center">
                    <i18n-t keypath="knScheduler.everyYear" tag="div" class="p-d-flex p-ai-center">
                        <template #year>
                            <Dropdown id="type" class="kn-material-input" v-model="selectedYear" optionLabel="name" optionValue="code" :options="getNumberOptions(5)" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                        </template>
                    </i18n-t>
                </div>

                <div class="p-d-flex p-ai-center">
                    {{ $t('knScheduler.in') }}

                    <Dropdown id="type" class="kn-material-input" v-model="selectedMonth" dataKey="id" optionLabel="name" optionValue="code" :options="months" maxLength="100" @change="updateFormula()" :disabled="readOnly" />
                </div>

                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf1" name="yearConf" v-model="yearConf" value="theDay" :disabled="readOnly" /> {{ $t('knScheduler.theDay') }}

                        <Dropdown id="type" class="kn-material-input" v-model="selectedDayNumber" dataKey="id" optionLabel="name" optionValue="code" :options="getNumberOptions(31)" maxLength="100" @change="yearConf = 'theDay'" :disabled="readOnly" />
                    </div>
                </div>
                <div class="field-radiobutton">
                    <div class="p-d-flex p-ai-center">
                        <RadioButton class="p-mr-2" id="monthConf2" name="yearConf" v-model="yearConf" value="theOrdinalDay" :disabled="readOnly" /> {{ $t('knScheduler.the') }}

                        <Dropdown id="type" class="kn-material-input" v-model="selectedDayOrdinal" dataKey="id" optionLabel="name" optionValue="id" :options="ordinal" maxLength="100" @change="yearConf = 'theOrdinalDay'" :disabled="readOnly" />

                        <Dropdown id="type" class="kn-material-input" v-model="selectedDayExtended" dataKey="id" optionLabel="name" optionValue="id" :options="days" maxLength="100" @change="yearConf = 'theOrdinalDay'" :disabled="readOnly" />
                    </div>
                </div>
            </div>
            <div v-if="selectedRefreshRate === 'custom'">
                <span class="p-field p-float-label p-col-12"> <InputText :id="name" type="text" v-model="formula" v-bind="$attrs" :class="[cssClass ? cssClass + ' kn-truncated' : 'kn-material-input kn-truncated', required && !modelValue ? 'p-invalid' : '']"/></span>
            </div>
        </div>
        <span class="p-field p-float-label p-col-12" v-if="descriptor.endDateEnabled"
            ><Calendar id="icon" v-model="endDate" :showIcon="true" /><label for="endDate" class="kn-material-input-label"> {{ $t('kpi.targetDefinition.endDate') }} </label>
        </span>
        <span class="p-field p-float-label p-col-12">
            <Message severity="info" :closable="false"> {{ formula }} </Message></span
        >{{ getCronstrueFormula }}
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
            readOnly: Boolean || false
        },
        emits: ['touched', 'cronValid'],
        data() {
            return {
                //albnale
                startDate: null as Date | null,
                endDate: null as Date | null,
                selectedRefreshRate: null,
                selectedWeek: null,
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
                formula: {} as any,
                nextFlush: '',
                selectedWeekdays: {} as any,
                startDateEnabled: false,
                endDateEnabled: false
            }
        },
        computed: {
            getCronstrueFormula(): String {
                let locale = localStorage.getItem('locale')
                let cronLocale = ''
                if (locale) {
                    let splitted = locale.split('_')

                    if (locale.includes('#')) {
                        cronLocale = splitted[0] + '_' + splitted[2]
                    } else {
                        cronLocale = splitted[0]
                    }
                }
                return cronstrue.toString(this.formula, { locale: cronLocale })
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

            this.formula = '0 0 0 * * ? *'
            this.selectedRefreshRate = this.descriptor?.refreshRate.options[0].code
        },
        methods: {
            getNumberOptions(max: Number) {
                let tmp = [] as any
                for (var i = 1; i <= max; i++) tmp.push({ code: i.toString(), id: i, name: i.toString() })
                return tmp
            },
            resetFormula() {
                this.formula = '0 0 0 * * ? *'
                this.selectedWeek = null
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
                let formulaArr = this.formula.split(' ')
                formulaArr[0] = formulaArr[1] = formulaArr[2] = '0'
                if (this.selectedRefreshRate === 'daily') {
                    if (this.dayConf === 'everyDay') {
                        formulaArr[3] = '?'

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
                        t += '*'
                    }

                    formulaArr[5] = t

                    formulaArr[4] = this.selectedWeek ? this.selectedWeek : '*'
                } else if (this.selectedRefreshRate === 'monthly') {
                    if (this.selectedMonthExtended && this.selectedMonth) {
                        formulaArr[4] = this.selectedMonthExtended + '/' + this.selectedMonth
                    }

                    if (this.monthConf === 'theDay') {
                        formulaArr[3] = this.selectedDayNumber ? this.selectedDayNumber : '*'

                        formulaArr[5] = '?'
                    } else if (this.monthConf === 'theOrdinalDay') {
                        formulaArr[3] = '?'

                        if (this.selectedDayExtended && this.selectedDayOrdinal) {
                            formulaArr[5] = this.selectedDayExtended + '#' + this.selectedDayOrdinal
                        }
                    }
                    formulaArr[6] = '*'
                } else if (this.selectedRefreshRate === 'yearly') {
                    formulaArr[4] = this.selectedMonth ? this.selectedMonth : '*'

                    if (this.yearConf === 'theDay') {
                        formulaArr[3] = this.selectedDayNumber ? this.selectedDayNumber : '*'
                    } else if (this.yearConf === 'theOrdinalDay') {
                        formulaArr[3] = '?'

                        if (this.selectedDayExtended && this.selectedDayOrdinal) {
                            formulaArr[5] = this.selectedDayExtended + '#' + this.selectedDayOrdinal
                        }
                    }

                    if (this.selectedYear) {
                        formulaArr[6] = moment().year() + '/' + this.selectedYear
                    } else {
                        formulaArr[6] = '*'
                    }

                    /*  var interval = this.parser.parseExpression(this.formula)
                          this.nextFlush = 'Next: ' + interval.next().toString() + '\nFollowing: ' + interval.next().toString()*/
                }
                this.formula = formulaArr.join(' ')
            }
        },
        watch: {
            selectedRefreshRate() {
                this.updateFormula()
            },
            selectedWeek() {
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
            }
        }
    })
</script>

<style lang="css">
    .knScheduler {
        min-width: 200px;
        min-height: 300px;
        max-height: 500px;
        justify-content: space-between;
    }
</style>
