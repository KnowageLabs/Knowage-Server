<template>
    <Card :style="tabViewDescriptor.style.cardinalityCard">
        <template #content>
            <div class="CodeMirrorMathematica">
                <div class="CodeMirror-code formula"></div>
            </div>
        </template>
    </Card>

    <Card v-if="!loading" :style="tabViewDescriptor.style.card">
        <template #content>
            <DataTable :value="attributesList" responsive-layout="scroll" class="cardinalityTable">
                <Column>
                    <template #body="slotProps">
                        {{ slotProps.data }}
                    </template>
                </Column>
                <Column v-for="measure of kpi.cardinality.measureList" :key="measure">
                    <template #header>
                        <div :style="tabViewDescriptor.style.cardinalityColumn">{{ measure.measureName }}</div>
                    </template>
                    <template #body="slotProps">
                        <div v-if="measureHaveAttribute(slotProps.data, slotProps.column.key)" class="measureCell" @click="toggleCell(slotProps.data, slotProps.column.key)">
                            <i v-if="!isEnabled(slotProps.data, slotProps.column.key)" class="fa fa-ban invalidCell"></i>
                            <i v-if="measure.attributes[slotProps.data]" class="fa fa-check selectedCell"></i>
                            <i v-if="measure.attributes[slotProps.data] && !canDisable(slotProps.data, slotProps.column.key)" class="fa fa-lock selectedCell"></i>
                            <i v-if="!measure.attributes[slotProps.data] && isEnabled(slotProps.data, slotProps.column.key)" class="fa fa-check selectableCell"></i>
                        </div>
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import tabViewDescriptor from '../KpiDefinitionDetailDescriptor.json'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { Card, DataTable, Column },
    props: {
        selectedKpi: {
            type: Object as any
        },
        updateMeasureList: Boolean,
        loading: Boolean
    },
    emits: ['touched', 'measureListUpdated'],
    data() {
        return {
            tabViewDescriptor,
            kpi: {} as any,
            attributesList: [] as any,
            currentCell: {} as any,
            formulaChanged: false,
            indexOfMeasure: -1,
            oldFormula: ''
        }
    },
    computed: {},
    watch: {
        selectedKpi() {
            this.kpi = this.selectedKpi as any
            this.oldFormula = this.kpi.definition.formulaSimple
        },
        updateMeasureList() {
            if (this.updateMeasureList === true) {
                this.createFormulaToShow()
                this.getAllMeasure()
            }
        }
    },
    methods: {
        createFormulaToShow() {
            if (this.kpi && this.kpi.definition && this.kpi.definition.formulaSimple) {
                if (this.oldFormula != this.kpi.definition.formulaSimple) {
                    this.formulaChanged = true
                    this.oldFormula = this.kpi.definition.formulaSimple
                }

                const string = this.kpi.definition.formulaSimple.split(' ')
                let count = 0
                let formuloaHTML = ''
                for (let i = 0; i < string.length; i++) {
                    let span = ''
                    if (string[i].trim() == '+' || string[i].trim() == '-' || string[i].trim() == '/' || string[i].trim() == '*' || string[i].trim() == '(' || string[i].trim() == ')' || string[i].trim() == '' || !isNaN(string[i])) {
                        span = "<span class='showFormula'>" + ' ' + string[i] + ' ' + '</span>'
                    } else {
                        span = "<span ng-class='{classBold:currentCell.row==" + i + "}' class='showFormula " + this.kpi.definition.functions[count] + "' id=M" + count + '>' + ' ' + string[i] + ' ' + '</span>'
                        count++
                    }
                    formuloaHTML += span
                }
                const formulas = document.getElementsByClassName('formula')
                if (formulas.length > 0) {
                    const arrFormulas = [...formulas]
                    arrFormulas.forEach((element) => (element.innerHTML = formuloaHTML))
                }
            }
        },

        getAllMeasure() {
            this.attributesList = []
            if (this.kpi.cardinality != undefined && this.kpi.cardinality != null) {
                if (typeof this.kpi.cardinality !== 'object') {
                    this.kpi.cardinality = JSON.parse(this.kpi.cardinality)
                }

                for (let i = 0; i < this.kpi.cardinality.measureList.length; i++) {
                    for (const tmpAttr in this.kpi.cardinality.measureList[i].attributes) {
                        if (this.attributesList.indexOf(tmpAttr) == -1) {
                            this.attributesList.push(tmpAttr)
                        }
                    }
                }
                this.retryNewAttributes()
            }
        },

        async retryNewAttributes() {
            const definition = {}
            for (let i = 0; i < this.kpi.definition.measures.length; i++) {
                const meas = this.kpi.definition.measures[i]
                definition[i] = meas
            }

            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpi/buildCardinalityMatrix', definition).then((response: AxiosResponse<any>) => {
                if (this.formulaChanged) {
                    this.kpi.cardinality.measureList = [...response.data]
                    this.formulaChanged = false
                    this.attributesList = []
                    for (let i = 0; i < response.data.length; i++) {
                        for (const key of Object.keys(response.data[i]['attributes'])) {
                            if (this.attributesList.indexOf(key) == -1) {
                                this.attributesList.push(key)
                            }
                        }
                    }
                }
                this.$emit('measureListUpdated')
            })
        },

        isEnabled(attr, measure) {
            const checkMs = this.checkMeasure(measure)
            return checkMs.status || this.isContainedByUpperSet(attr, measure, checkMs.itemNumber)
        },

        canDisable(attr, measure) {
            return !this.isContainedByUnderSet(attr, measure)
        },

        measureHaveAttribute(attr, measure) {
            // eslint-disable-next-line no-prototype-builtins
            return measure.attributes.hasOwnProperty(attr)
        },

        checkMeasure(measure) {
            let tot = 0
            for (const attr in measure.attributes) {
                if (measure.attributes[attr]) {
                    tot++
                }
            }
            const resp = {
                status: Object.keys(this.kpi.cardinality.checkedAttribute.attributeUnion).length == tot,
                itemNumber: tot
            }
            return resp
        },

        isContainedByUpperSet(attr, measure, measureItemNumber) {
            let upperSetAttributeNumber = 99999999
            let upperSet
            for (let i = 0; i < this.kpi.cardinality.measureList.length; i++) {
                const tmpMeas = this.kpi.cardinality.measureList[i]
                if (tmpMeas == measure) {
                    continue
                }
                let tmpTot = 0
                for (const tmpAttr in tmpMeas.attributes) {
                    if (tmpMeas.attributes[tmpAttr]) {
                        tmpTot++
                    }
                }
                if (tmpTot - 1 == measureItemNumber) {
                    upperSet = tmpMeas
                    break
                }
                if (tmpTot < upperSetAttributeNumber && tmpTot > measureItemNumber) {
                    upperSetAttributeNumber = tmpTot
                    upperSet = tmpMeas
                }
            }
            if (upperSet == undefined || upperSet.attributes[attr]) {
                return true
            } else {
                return false
            }
        },

        isContainedByUnderSet(attr, measure) {
            let measureItemNumber = 0
            for (const tmpattr in measure.attributes) {
                if (measure.attributes[tmpattr]) {
                    measureItemNumber++
                }
            }

            let underSetAttributeNumber = 0
            let underSet
            for (let i = 0; i < this.kpi.cardinality.measureList.length; i++) {
                const tmpMeas = this.kpi.cardinality.measureList[i]
                if (tmpMeas == measure) {
                    continue
                }
                let tmpTot = 0
                for (const tmpAttr in tmpMeas.attributes) {
                    if (tmpMeas.attributes[tmpAttr]) {
                        tmpTot++
                    }
                }
                if (tmpTot + 1 == measureItemNumber) {
                    underSet = tmpMeas
                    break
                }
                if (tmpTot > underSetAttributeNumber && tmpTot < measureItemNumber) {
                    underSetAttributeNumber = tmpTot
                    underSet = tmpMeas
                }
            }

            if (underSet == undefined || !underSet.attributes[attr]) {
                return false
            } else {
                return true
            }
        },

        getMaxAttributeNumber(data) {
            let max = 0
            for (const key in data) {
                if (data[key] >= max) {
                    max = data[key]
                }
            }
            return max
        },

        blinkMeasure(event, attr, index) {
            //blink measure in formula
            this.currentCell.row = attr
            this.currentCell.column = index
            this.indexOfMeasure = index
            const string = 'M' + this.indexOfMeasure
            const test = document.getElementById(string) as any
            test.css('background', '#eceff1')
        },

        removeblinkMeasure() {
            const string = 'M' + this.indexOfMeasure
            const test = document.getElementById(string) as any
            test.css('background', 'transparent')
        },

        toggleCell(attr, measure) {
            if (!measure.attributes[attr] && !this.isEnabled(attr, measure)) {
                return
            }
            if (measure.attributes[attr] && !this.canDisable(attr, measure)) {
                return
            }

            //toggle the value
            measure.attributes[attr] = !measure.attributes[attr]

            if (measure.attributes[attr]) {
                //update union
                if (this.kpi.cardinality.checkedAttribute.attributeUnion[attr]) {
                    this.kpi.cardinality.checkedAttribute.attributeUnion[attr]++
                } else {
                    this.kpi.cardinality.checkedAttribute.attributeUnion[attr] = 1
                }
            } else {
                if (this.kpi.cardinality.checkedAttribute.attributeUnion[attr] == 1) {
                    delete this.kpi.cardinality.checkedAttribute.attributeUnion[attr]
                } else {
                    this.kpi.cardinality.checkedAttribute.attributeUnion[attr]--
                }
            }
            //update intersection
            this.kpi.cardinality.checkedAttribute.attributeIntersection = {}
            const maxAttrNum = this.getMaxAttributeNumber(this.kpi.cardinality.checkedAttribute.attributeUnion)
            for (const key in this.kpi.cardinality.checkedAttribute.attributeUnion) {
                if (this.kpi.cardinality.checkedAttribute.attributeUnion[key] == maxAttrNum) {
                    this.kpi.cardinality.checkedAttribute.attributeIntersection[key] = true
                }
            }
        }
    }
})
</script>
<style lang="scss" scoped>
.cardinalityTable .measureCell {
    text-align: center;
    height: 30px;
}

.cardinalityTable .invalidCell {
    color: lightgray;
    line-height: 30px;
}

.cardinalityTable .selectedCell {
    color: green;
    line-height: 30px;
}

.cardinalityTable .selectableCell {
    color: lightgray;
    line-height: 30px;
}

.cardinalityTable {
    width: 100%;
    table-layout: fixed;
    border-collapse: collapse;
}

.cardinalityTable .attributeRow {
    border-bottom: 1px solid #eceff1;
}

.disabledCell {
    background-color: gray;
}
</style>
