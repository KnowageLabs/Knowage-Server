<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.hierarchies') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <div>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <div class="kn-flex">
                        <span class="p-float-label">
                            <Calendar v-model="date" :manualInput="true"></Calendar>
                        </span>
                    </div>
                    <div class="kn-flex p-d-flex p-flex-row p-ai-center p-jc-around">
                        <Button class="kn-button kn-button--primary hierarchy-management-hierarchies-card-button" :label="$t('common.save')" :disabled="saveButtonDisabled" @click="save" />
                        <div>
                            <Checkbox v-model="backup" :binary="true" :disabled="true"></Checkbox>
                            <label class="kn-material-input-label p-ml-2"> {{ $t('managers.hierarchyManagement.backup') }}</label>
                        </div>
                    </div>
                </div>

                <div class="p-d-flex p-flex-row">
                    <div class="p-fluid kn-flex">
                        <span class="p-float-label p-m-2">
                            <Dropdown class="kn-material-input" v-model="hierarchyType" :options="hierarchyManagementHierarchiesCardDescriptor.hierarchyTypes" :disabled="!dimension"> </Dropdown>
                            <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.hierarchyType') }} </label>
                        </span>
                    </div>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimension, iHierarchy } from '../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import hierarchyManagementHierarchiesCardDescriptor from './HierarchyManagementHierarchiesCardDescriptor.json'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-card',
    components: { Calendar, Checkbox, Dropdown },
    props: { selectedDimension: { type: Object as PropType<iDimension | null> } },
    data() {
        return {
            hierarchyManagementHierarchiesCardDescriptor,
            date: new Date(),
            backup: true,
            dimension: null as iDimension | null,
            hierarchyType: '' as string,
            hierarchies: [] as iHierarchy[]
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return false
        }
    },
    watch: {
        selectedDimension() {
            this.loadDimension()
        }
    },
    created() {
        this.loadDimension()
    },
    methods: {
        loadDimension() {
            this.dimension = this.selectedDimension as iDimension
        },
        async loadHieararchies() {
            this.$emit('loading', true)
            const url = this.hierarchyType === 'MASTER' ? `hierarchiesMaster/getHierarchiesMaster?dimension=${this.selectedDimension?.DIMENSION_NM}` : `hierarchiesTechnical/getHierarchiesTechnical?dimension=${this.selectedDimension?.DIMENSION_NM}`
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => (this.hierarchies = response.data))
            this.$emit('loading', false)
            console.log('LOADED HIERARCHIES: ', this.hierarchies)
        },
        save() {}
    }
})
</script>

<style lang="scss" scoped>
.hierarchy-management-hierarchies-card-button {
    min-width: 150px;
    max-width: 150px;
}
</style>
