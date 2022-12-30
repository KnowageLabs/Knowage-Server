<template>
    <div v-if="widgetModel" class="dashboard-editor-list-card-container kn-flex p-m-3">
        <div class="gallery-inputs p-d-flex p-flex-row p-ai-center p-flex-wrap p-mt-4 p-ml-4">
            <InputText class="kn-material-input p-mr-2 model-search" :style="galleryDescriptor.style.filterInput" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" />
        </div>

        <MasonryWall class="p-mx-4 p-my-2 kn-flex kn-overflow dashboard-scrollbar" :items="filteredChartTypes" :column-width="200" :gap="6">
            <template #default="{ chart, index }">
                <div class="gallery-card kn-cursor-pointer" :style="(galleryDescriptor.style.galleryCard as any)" @click="onChange(filteredChartTypes[index].value)">
                    <label class="kn-material-input-label p-ml-2 p-mt-1">{{ $t(`${filteredChartTypes[index].label}`) }}</label>
                    <img :src="getImageSource(filteredChartTypes[index].value)" />
                </div>
            </template>
        </MasonryWall>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IChartType } from '@/modules/documentExecution/Dashboard/Dashboard'
import galleryDescriptor from './ChartWidgetGalleryDescriptor.json'
import commonDescriptor from '../../common/WidgetCommonDescriptor.json'
import Dropdown from 'primevue/dropdown'
import MasonryWall from '@yeger/vue-masonry-wall'

export default defineComponent({
    name: 'chart-widget-chart-type-dropdown',
    components: { Dropdown, MasonryWall },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    emits: ['selectedChartTypeChanged'],
    data() {
        return {
            galleryDescriptor,
            commonDescriptor,
            selectedType: '',
            searchWord: '',
            chartTypes: commonDescriptor.chartTypeOptions as IChartType[],
            filteredChartTypes: [] as IChartType[]
        }
    },
    async created() {
        this.loadSelectedType()
    },
    methods: {
        loadSelectedType() {
            const chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            this.filteredChartTypes = [...this.chartTypes] as IChartType[]
            if (chartModel?.chart.type) {
                this.selectedType = chartModel.chart.type
            }
        },
        onChange(selectedType: string) {
            this.selectedType = selectedType
            // TODO - remove hardcoded after implementing other chart types
            if (!['pie', 'gauge', 'activitygauge', 'solidgauge'].includes(this.selectedType)) this.selectedType = 'pie'
            this.$emit('selectedChartTypeChanged', this.selectedType)
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredChartTypes = [...this.chartTypes] as IChartType[]
                } else {
                    this.filteredChartTypes = this.filteredChartTypes.filter((icon: IChartType) => {
                        return icon.label?.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
        },
        getImageSource(chartValue: string) {
            return `${import.meta.env.VITE_PUBLIC_PATH}images/dashboard/chartTypes/${chartValue}.png`
        }
    }
})
</script>
<style lang="scss" scoped>
.gallery-card {
    height: 200px;
    width: 200px;
}
.gallery-card:hover {
    border-color: #43749e !important;
}
</style>
