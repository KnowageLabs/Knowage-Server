<template>
    <Listbox
        v-if="!load"
        class="kn-list--column"
        :options="users"
        :filter="true"
        :filterPlaceholder="$t('common.search')"
        optionLabel="userId"
        filterMatchMode="contains"
        :filterFields="usersManagementDescriptor.globalFilterFields"
        :emptyFilterMessage="$t('managers.widgetGallery.noResults')"
        @change="onUserSelect"
        data-test="users-list"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #option="slotProps">
            <div class="kn-list-item" data-test="list-item">
                <div class="kn-list-item-text">
                    <span>{{ slotProps.option.userId }}</span>
                    <span class="kn-list-item-text-secondary">{{ slotProps.option.fullName }}</span>
                </div>
                <Button v-if="slotProps.option.failedLoginAttempts >=3" icon="pi pi-lock" class="p-button-danger p-button-text"/>
                <Button icon="pi pi-trash" class="p-button-link p-button-sm" @click="onUserDelete(slotProps.option.id)" :data-test="'deleteBtn'" />
            </div>
        </template>
    </Listbox>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iUser } from './UsersManagement'

import Listbox from 'primevue/listbox'

import usersManagementDescriptor from './UsersManagementDescriptor.json'

export default defineComponent({
    name: 'users-list-box',
    components: {
        Listbox
    },
    emits: ['selectedUser', 'deleteUser'],
    props: {
        users: Array,
        loading: Boolean
    },
    watch: {
        users: {
            handler: function(u) {
                this.listUsers = u
            }
        },
        loading: {
            handler: function(l) {
                this.load = l
            }
        }
    },
    data() {
        return {
            listUsers: [] as iUser[],
            load: false as Boolean,
            selectedUser: null as iUser | null,
            usersManagementDescriptor: usersManagementDescriptor
        }
    },
    methods: {
        onUserSelect(event: any) {
            this.$emit('selectedUser', event.value)
        },
        async onUserDelete(id: number) {
            this.$confirm.require({
                message: this.$t('managers.usersManagement.confirmDeleteMessage', {
                    item: 'user'
                }),
                header: this.$t('common.confirmation'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => {
                    this.$emit('deleteUser', id)
                }
            })
        }
    }
})
</script>
