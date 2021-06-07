<template>
    <div>
        <PickList v-model="availableUsersList" @move-to-target="onUserChange" @move-to-source="onUserChange" @reorder="onUserChange">
            <template #sourceHeader>
                {{ $t('managers.mondrianSchemasManagement.workFlow.availableUsers') }}
            </template>
            <template #targetHeader>
                {{ $t('managers.mondrianSchemasManagement.workFlow.userWf') }}
            </template>
            <template #item="slotProps">
                <div>
                    <h4 class="p-mb-2">{{ slotProps.item.userId }}</h4>
                    <span>{{ slotProps.item.fullName }}</span>
                </div>
            </template>
        </PickList>
        {{ availableUsersList[1] }}
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import tabViewDescriptor from '../MondrianSchemasTabViewDescriptor.json'
import PickList from 'primevue/picklist'
export default defineComponent({
    name: 'workflow-tab',
    components: {
        PickList
    },
    props: {
        usersList: Array
    },
    emits: ['changed', 'selectedUsersChanged'],
    data() {
        return {
            tabViewDescriptor,
            availableUsersList: [] as any[]
        }
    },

    mounted() {
        this.availableUsersList = this.usersList as any[]
    },
    watch: {
        usersList() {
            this.availableUsersList = this.usersList as any[]
        }
    },
    methods: {
        onUserChange() {
            let selectedUsers = this.availableUsersList[1]
            this.$emit('selectedUsersChanged', selectedUsers)
        }
    }
})
</script>
