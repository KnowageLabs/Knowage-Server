<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-8 p-md-6 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.scorecards.scorecardDesigner') }}
                    </template>
                </Toolbar>
            </div>

            <div class="p-col-4 p-sm-4 p-md-6 p-p-0 p-m-0">
                PERSPECTIVES LIST
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'scorecards-designer',
    components: {},
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
