<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.scorecards.title') }}
            </template>
            <template #end>
                <KnFabButton icon="fas fa-plus"></KnFabButton>
            </template>
        </Toolbar>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import scorecardsDescriptor from './ScorecardsDescriptor.json'

export default defineComponent({
    name: 'scorecards',
    components: {},
    data() {
        return {
            scorecardsDescriptor,

            loading: false
        }
    },
    computed: {},
    async created() {},
    methods: {}
})
</script>
