<template>
    <div class="kn-height-full olap-page-container">
        <div class="p-d-flex p-flex-row">
            <div v-if="olapSidebarVisible" id="olap-backdrop" @click="olapSidebarVisible = false"></div>
            <OlapSidebar v-if="olapSidebarVisible" class="olap-sidebar kn-overflow-y"></OlapSidebar>

            <!-- {{ customViewVisible }} -->
            <div ref="olap-table" class="test-olap" v-if="olap && olap.table" v-html="olap.table" @click="test"></div>
        </div>
    </div>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent, h } from 'vue'
import olapDescriptor from './OlapDescriptor.json'
import OlapSidebar from './olapSidebar/OlapSidebar.vue'

export default defineComponent({
    name: 'olap',
    components: { OlapSidebar },
    props: { id: { type: String }, reloadTrigger: { type: Boolean }, olapCustomViewVisible: { type: Boolean } },
    data() {
        return {
            olapDescriptor,
            firstOlap: null as any,
            olap: null as any,
            olapSidebarVisible: true,
            customViewVisible: false,
            table: h('test', '<div v-if="customViewVisible"></div>') as any,
            loading: false
        }
    },
    async created() {
        await this.loadPage()
    },
    computed: {
        dynamicComponent() {
            return {
                template: '<h1 v-drilldown>TEST</h1>',

                // redirect every shared data here
                data: () => {
                    return {}
                },

                created() {
                    console.log(' >>> CREATED INSIDE DYNAMIC!!!')
                },
                // redirect every shared methods here
                methods: {}
            }
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        },
        async reloadTrigger() {
            await this.loadPage()
        },
        olapCustomViewVisible() {
            this.loadCustomViewVisbility()
        }
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadOlapModel()
            this.loadCustomViewVisbility()
            this.loading = false
        },
        loadCustomViewVisbility() {
            this.customViewVisible = this.olapCustomViewVisible
        },
        async loadOlapModel() {
            this.loading = true
            await this.$http
                .post(
                    process.env.VUE_APP_OLAP_PATH + `1.0/model/?SBI_EXECUTION_ID=${this.id}`,
                    {},
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*',
                            'Content-Type': 'application/x-www-form-urlencoded'
                        }
                    }
                )
                .then(async (response: AxiosResponse<any>) => {
                    this.firstOlap = response.data

                    console.log('LOADED FIRST OLAP: ', this.firstOlap)
                    console.log('MODEL CONFIG: ', this.firstOlap.modelConfig)

                    await this.loadModelConfig()
                })
                .catch(() => {})
            this.loading = false
        },
        async loadModelConfig() {
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/modelconfig?SBI_EXECUTION_ID=${this.id}&NOLOADING=undefined`, this.firstOlap.modelConfig, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()

            console.log('LOADED OLAP: ', this.olap)
        },
        formatOlapTable() {
            this.olap.table = this.olap.table.replaceAll('</drillup>', ' <div class="drill-up"></div></drillup>')
            this.olap.table = this.olap.table.replaceAll('</drilldown>', '<div class="drill-down"></div> </drilldown> ')
        },
        async drillDown(event: any) {
            this.loading = true
            console.log('EVENT INSIDE DRILL DOWN: ', event)
            const axis = event.target.parentNode.getAttribute('axis')
            const position = event.target.parentNode.getAttribute('position')
            const member = event.target.parentNode.getAttribute('memberordinal')

            const postData = JSON.stringify({
                memberUniqueName: event.target.parentNode.getAttribute('uniquename'),
                positionUniqueName: event.target.parentNode.getAttribute('positionuniquename')
            })
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/member/drilldown/${axis}/${position}/${member}/?SBI_EXECUTION_ID=${this.id}`, postData, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})

            this.formatOlapTable()

            this.loading = false
        },
        test(event: Event) {
            console.log('>>> COMPONENT: ', this.dynamicComponent)
            console.log('EVENT: ', event)

            const eventTarget = event.target as any

            if (eventTarget) {
                switch (eventTarget.className) {
                    case 'drill-up':
                        console.log('TODO - DRILL UP!')
                        break
                    case 'drill-down':
                        this.drillDown(event)
                        break
                }
            }
        }
    }
})
</script>

<style lang="scss">
.olap-page-container {
    display: flex;
    flex-direction: column;
}

#olap-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

.olap-sidebar {
    margin-left: auto;
}

.drill-up {
    background-image: url('C:\\Users\\bojan.sovtic\\Desktop\\Projekat\\Knowage-Server\\knowage-vue\\src\\modules\\documentExecution\\olap\\mdx.png');
    height: 20px;
    width: 20px;
}

.drill-down {
    background-image: url('C:\\Users\\bojan.sovtic\\Desktop\\Projekat\\Knowage-Server\\knowage-vue\\src\\modules\\documentExecution\\olap\\cc.png');
    height: 20px;
    width: 20px;
}
</style>
