<template>
    <iframe class="kn-width-full kn-height-full" :src="url"></iframe>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'document-execution-container',
    components: {},
    props: { id: { type: String } },
    data() {
        return {
            url:
                process.env.VUE_APP_HOST_URL +
                '/knowagecockpitengine/api/1.0/pages/edit?NEW_SESSION=TRUE&SBI_LANGUAGE=en&user_id=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZGVtb19hZG1pbiIsImV4cCI6MTY0NjQzNTAxNn0.3HhqSCMfJslCbXxMw3HPtYjC9rhSMIdLpyuZDOshNVA&SBI_COUNTRY=US&SBI_EXECUTION_ID=095e7ac39bc211ec90095bb127e68384&SBI_ENVIRONMENT=DOCBROWSER&IS_TECHNICAL_USER=true&documentMode=EDIT&FUNCTIONALITY_ID=601'
        }
    },
    created() {},
    methods: {
        createUrl() {
            const user = (this.$store.state as any).user
            const language = user.locale.split('_')[0]
            const uniqueID = user.userUniqueIdentifier
            const country = user.locale.split('_')[1]

            return (
                process.env.VUE_APP_HOST_URL +
                `/knowagecockpitengine/api/1.0/pages/edit?NEW_SESSION=TRUE&SBI_LANGUAGE=${language}&user_id=${uniqueID}&SBI_COUNTRY=${country}&SBI_EXECUTION_ID=095e7ac39bc211ec90095bb127e68384&SBI_ENVIRONMENT=DOCBROWSER&IS_TECHNICAL_USER=true&documentMode=EDIT&FUNCTIONALITY_ID=601`
            )
        }
    }
})
</script>
