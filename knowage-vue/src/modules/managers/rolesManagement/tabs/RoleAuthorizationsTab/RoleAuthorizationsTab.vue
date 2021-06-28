<template>
    <Card>
        <template #content>
            <div v-for="(category, index) of rolesManagementTabViewDescriptor.categories" :key="index">
                <template v-if="authorizationCBs[category.categoryName] && authorizationCBs[category.categoryName].length">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t(category.name) }}
                        </template>
                    </Toolbar>
                    <div v-for="(authCBInfo, index) of authorizationCBs[category.categoryName]" :key="index">
                        <div class="p-field-checkbox p-m-3">
                            <Checkbox id="binary" v-model="role[authCBInfo.fieldName]" :binary="true" :disabled="authCBInfo.enableForRole && !authCBInfo.enableForRole.includes(role.roleTypeID)" @change="authChanged(authCBInfo.fieldName, role[authCBInfo.fieldName])" />
                            <label for="binary">{{ $t(authCBInfo.label) }}</label>
                        </div>
                    </div>
                </template>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import rolesManagementTabViewDescriptor from '../../RolesManagementTabViewDescriptor.json'

export default defineComponent({
    name: 'authorizations-tab',
    components: {
        Card,
        Checkbox
    },
    props: {
        selectedRole: {
            type: Object,
            requried: false
        },
        authList: Array,
        authCBs: Object as any
    },
    emits: ['authChanged'],
    data() {
        return {
            rolesManagementTabViewDescriptor,
            role: {} as any,
            authorizationList: [] as any,
            authorizationCBs: {} as any
        }
    },
    created() {
        this.authorizationList = this.authList as any[]
        this.authorizationCBs = this.authCBs as any[]
        this.role = { ...this.selectedRole } as any
    },
    watch: {
        selectedRole: {
            handler: function(value) {
                this.role = { ...value } as any
            },
            deep: true
        }
    },
    methods: {
        authChanged(fieldName: string, value: any) {
            this.$emit('authChanged', { fieldName, value })
        }
    }
})
</script>
