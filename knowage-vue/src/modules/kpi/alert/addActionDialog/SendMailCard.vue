<template>
    <Card>
        <template #content>
            <AutoComplete class="p-inputtext-sm" :multiple="true" v-model="selectedUsers" :suggestions="filteredUsers" field="name" @complete="searchUsers($event)" @item-select="setUser($event.value)" />
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import AutoComplete from 'primevue/autocomplete'

export default defineComponent({
    name: 'send-mail-card',
    components: { AutoComplete },
    props: {
        action: {
            type: Object
        },
        users: {
            type: Array
        }
    },
    data() {
        return {
            selectedAction: {} as any,
            userList: [] as any[],
            selectedUsers: [],
            filteredUsers: [] as any[]
        }
    },
    watch: {
        users() {
            this.loadUsers()
        }
    },
    created() {
        this.loadAction()
        this.loadUsers()
    },
    methods: {
        loadAction() {
            this.selectedAction = this.action
            // MOCKED ACTION
            this.selectedAction = {
                idAction: '62',
                jsonActionParameters: {
                    body: 'Body',
                    mailTo: [{ name: 'demo_admin', userId: '', email: 'demo_admin' }],
                    subject: 'Subject'
                },
                thresholdValues: [147]
            }

            this.selectedUsers = this.selectedAction.jsonActionParameters.mailTo
            console.log('SELECTED ACTION', this.selectedAction)
            console.log('SELECTED USERS', this.selectedUsers)
        },
        loadUsers() {
            this.userList = this.users as any[]
            console.log('USERS', this.users)
        },
        searchUsers(event) {
            setTimeout(() => {
                if (!event.query.trim().length) {
                    this.filteredUsers = [...this.userList] as any[]
                } else {
                    this.filteredUsers = this.userList.filter((user: any) => {
                        return user.name.toLowerCase().startsWith(event.query.toLowerCase())
                    })
                }
            }, 250)
        },
        setUser(user: any) {
            console.log('SELECTED USER', user)
            console.log('SELECTED USERS AFTER CLICK', this.selectedUsers)
        }
    }
})
</script>
