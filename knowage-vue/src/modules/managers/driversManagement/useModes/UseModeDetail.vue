<template>
    <KnHint :title="'managers.driversManagement.useModes.title'" :hint="'managers.driversManagement.useModes.hint'" v-if="!selectedMode.useID" data-test="mode-hint"></KnHint>
    <TabView class="tabview-custom kn-page-content" v-else>
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.details') }}</span>
                <Badge :value="invalidModes" class="p-ml-2" severity="danger" v-if="invalidModes > 0"></Badge>
            </template>
            <DetailsCard :selectedMode="mode" :selectionTypes="selectionTypes" :layers="layers" :isDate="isDate" :lovs="lovs" :selectedLov="selectedLov" @apply="changeLov"></DetailsCard>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.roles') }}</span>
                <Badge value="1" class="p-ml-2" severity="danger" v-if="!mode.associatedRoles || mode.associatedRoles.lenght < 1"></Badge>
            </template>
            <RolesCard :roles="availableRoles" :selectedModeProp="mode"></RolesCard>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.constraints') }}</span>
            </template>
            <ConstraintsCard :constraints="constraints" :selectedModeProp="mode"></ConstraintsCard>
        </TabPanel>
    </TabView>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Badge from 'primevue/badge'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DetailsCard from './DetailsCard.vue'
import RolesCard from './RolesCard.vue'
import ConstraintsCard from './ConstraintsCard.vue'
import KnHint from '@/components/UI/KnHint.vue'

export default defineComponent({
    name: 'business-model-catalogue-detail',
    components: {
        Badge,
        TabView,
        TabPanel,
        DetailsCard,
        RolesCard,
        ConstraintsCard,
        KnHint
    },
    props: {
        selectedMode: {
            type: Object,
            required: false
        },
        roles: {
            type: Array,
            requierd: true
        },
        constraints: {
            type: Array,
            requierd: true
        },
        selectionTypes: {
            type: Array,
            requierd: true
        },
        layers: {
            type: Array,
            requierd: true
        },
        isDate: {
            type: Boolean,
            requierd: true
        },
        disabledRoles: {
            type: Array,
            required: true
        },
        lovs: {
            type: Array,
            required: true
        }
    },
    data() {
        return {
            mode: {} as any,
            selectedLov: {} as any
        }
    },
    computed: {
        invalidModes(): number {
            return this.mode.numberOfErrors
        },
        availableRoles(): any {
            return this.roles?.filter((role: any) => this.disabledRoles.findIndex((disabledRole: any) => role.id === disabledRole.id) < 0)
        }
    },
    watch: {
        selectedMode() {
            this.mode = this.selectedMode as any
            this.setLov()
        }
    },
    mounted() {
        if (this.selectedMode) {
            this.mode = this.selectedMode as any
            this.setLov()
        }
    },
    methods: {
        setLov() {
            if (this.mode.idLov) {
                this.selectedLov = this.lovs.filter((lov: any) => lov.id == this.mode.idLov)[0]
            }
        },
        changeLov(lov: any) {
            this.mode.idLov = lov.id
            this.selectedLov = lov
            console.log(this.selectedLov)
        }
    }
})
</script>
