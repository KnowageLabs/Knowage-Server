<template>
    <div class="pivot-widget-container p-d-flex p-d-row kn-flex">
        <DxButton text="Apply" type="default" @click="doStuff()" />
        <DxPivotGrid
            id="pivotgrid"
            ref="grid"
            :data-source="dataSource"
            :allow-sorting="true"
            :allow-sorting-by-summary="true"
            :allow-filtering="true"
            :show-borders="true"
            :show-column-grand-totals="true"
            :show-row-grand-totals="true"
            :show-row-totals="true"
            :show-column-totals="true"
            @contentReady="onContentReady"
        >
            <DxFieldChooser :enabled="true" :height="400" />
        </DxPivotGrid>
    </div>
</template>

<script lang="ts">
import { DxPivotGrid, DxFieldChooser } from 'devextreme-vue/pivot-grid'
import { DxButton } from 'devextreme-vue/button'
import PivotGridDataSource from 'devextreme/ui/pivot_grid/data_source'

import { IDashboardDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'

// import { sales } from './MockData.js'

export default defineComponent({
    name: 'table-widget',
    components: { DxPivotGrid, DxFieldChooser, DxButton },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        editorMode: { type: Boolean, required: false },
        datasets: { type: Array as PropType<IDashboardDataset[]>, required: true },
        dataToShow: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['pageChanged', 'sortingChanged', 'launchSelection'],
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    data() {
        const dataSource = new PivotGridDataSource({
            fields: this.getFormattedFieldsFromModel(),
            store: this.getPivotData()
        })
        return {
            dataSource,
            tableData: [] as any
        }
    },
    watch: {
        propWidget: {
            handler() {},
            deep: true
        },
        dataToShow: {
            handler() {
                this.tableData = this.dataToShow
            },
            deep: true
        },
        propActiveSelections() {
            // this.loadActiveSelections()
        }
    },
    beforeMount() {},
    created() {},
    unmounted() {},
    mounted() {},

    methods: {
        onContentReady() {
            // console.log('CONTENT READY \n', this.dataSource.state())
        },
        doStuff() {
            console.groupCollapsed('DO STUFF ------------')
            console.log('propWidget', this.propWidget)
            console.groupEnd()
            // console.log('dataToShow', this.dataToShow)
            // console.log('this.dataSource.fields()', this.dataSource.fields())
        },
        getFields() {
            return [
                {
                    caption: 'Region',
                    width: 120,
                    dataField: 'region',
                    area: 'row',
                    headerFilter: {
                        allowSearch: true
                    }
                },
                {
                    caption: 'City',
                    dataField: 'city',
                    width: 150,
                    area: 'row',
                    headerFilter: {
                        allowSearch: true
                    },
                    selector(data) {
                        return `${data.city} (${data.country})`
                    }
                },
                {
                    dataField: 'date',
                    dataType: 'date',
                    area: 'column'
                },
                {
                    caption: 'Sales',
                    dataField: 'amount',
                    dataType: 'number',
                    summaryType: 'sum',
                    format: 'currency',
                    area: 'data'
                }
            ]
        },
        getFormattedFieldsFromModel() {
            const formattedFields = [] as any
            const responseMetadataFields = this.dataToShow?.metaData?.fields

            if (this.getPivotData().length > 0) {
                for (const fieldsName in this.propWidget.fields) {
                    const modelFields = this.propWidget.fields[fieldsName]
                    modelFields.forEach((modelField) => {
                        const tempField = {} as any
                        // console.log('FIELD', modelField)

                        const index = responseMetadataFields.findIndex((metaField: any) => {
                            if (typeof metaField == 'object') return metaField.header.toLowerCase() === modelField.alias.toLowerCase()
                        })

                        //TODO: split tempField props to methods
                        tempField.caption = modelField.alias
                        tempField.dataField = `column_${index}`
                        tempField.area = this.getDataField(fieldsName)
                        if (modelField.sort) tempField.sortOrder = modelField.sort.toLowerCase()

                        // console.log('INDEX FOUND', index, tempField)

                        formattedFields.push(tempField)
                    })
                }
            }

            // console.log('formattedFields', formattedFields)
            return formattedFields
        },
        getDataField(fieldsName) {
            switch (fieldsName) {
                case 'columns':
                    return 'column'
                case 'rows':
                    return 'row'
                default:
                    return fieldsName
            }
        },
        getPivotData() {
            if (this.dataToShow && this.dataToShow.rows) return this.dataToShow.rows
            else return []
        }
    }
})
</script>
<style lang="scss">
.pivot-widget-container {
    overflow: auto;
}
</style>
