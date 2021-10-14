<template>
    <KnHint class="kn-card-reset" :title="'managers.driversManagement.useModes.title'" :hint="'managers.driversManagement.useModes.hint'" v-if="!selectedMode.useID" data-test="mode-hint"></KnHint>
    <TabView class="tabview-custom kn-page-content" v-else data-test="modes-form">
        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.details') }}</span>
                <Badge :value="invalidModes" class="p-ml-2" severity="danger" v-if="invalidModes > 0"></Badge>
            </template>
            <DetailsCard :selectedMode="mode" :selectionTypes="selectionTypes" :layers="layers" :isDate="isDate" :lovs="lovs" :showMapDriver="showMapDriver"></DetailsCard>
        </TabPanel>

        <TabPanel>
            <template #header>
                <span>{{ $t('managers.driversManagement.useModes.roles') }}</span>
                <Badge value="1" class="p-ml-2" severity="danger" v-if="invalidRoles"></Badge>
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
import DetailsCard from './DriversManagementUseModeDetailsCard.vue'
import RolesCard from './DriversManagementRolesCard.vue'
import ConstraintsCard from './DriversManagementConstraintsCard.vue'
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
        },
        showMapDriver: {
            type: Boolean,
            requierd: true
        }
    },
    data() {
        return {
            mode: {} as any
        }
    },
    computed: {
        invalidModes(): number {
            return this.mode.numberOfErrors
        },
        invalidRoles(): boolean {
            return this.mode.associatedRoles.length === 0
        },
        availableRoles(): any {
            return this.roles?.filter((role: any) => this.disabledRoles.findIndex((disabledRole: any) => role.id === disabledRole?.id) < 0)
        }
    },
    watch: {
        selectedMode() {
            this.mode = this.selectedMode as any
        }
    },
    mounted() {
        if (this.selectedMode) {
            this.mode = this.selectedMode as any
        }
    },
    methods: {}
})
</script>
<style lang="scss">
.kn-card-layout {
    .p-card-body {
        padding: 0 !important;
        height: calc(100% - 35px) !important;
        .p-card-content {
            padding: 0 !important;
        }
    }
}

.kn-card-reset {
    .p-card-body {
        padding: 0.75rem !important;
        .p-card-content {
            display: block !important;
            padding: 0.75rem 0 !important;
        }
    }
}
</style>
