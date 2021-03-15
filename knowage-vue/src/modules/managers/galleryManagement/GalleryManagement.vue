<template>
  <div class="knPage">
    <div class="knPageContent p-grid p-m-0">
      <div class="knListColumn p-col-3 p-p-0">
        <Toolbar class="knToolbar">
          <template #left>
            {{$t('managers.widgetGallery.title')}}
          </template>
          <template #right>
            <FabButton icon="fas fa-plus" />
          </template>
        </Toolbar>
        <Listbox class="knList" :options="galleryTemplates" :filter="true" :filterPlaceholder="$t('common.search')" optionLabel="label" filterMatchMode="contains" :filterFields="['label','type','tags']" :emptyFilterMessage="$t('managers.widgetGallery.noResults')">
          <template #option="slotProps">
            <router-link class="kn-decoration-none" :to="{ name: 'gallerydetail', params: { id: slotProps.option.id }}" exact>
              <div class="knListItem">
                <Avatar :icon="iconTypesMap[slotProps.option.type].className" shape="circle" size="medium" :style="iconTypesMap[slotProps.option.type].style" />
                <div class="knListItemText">
                  <span>{{slotProps.option.label}}</span>
                  <span class="smallerLine">{{slotProps.option.author}}</span>
                </div>
                <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain kn-gallery-slotProps.option.type" @click="deleteTemplate($event,slotProps.option.id)"/>
              </div>
            </router-link>
          </template>
        </Listbox>
      </div>
      <div class="p-col-9 p-p-0 p-m-0">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent } from 'vue'
  import Avatar from 'primevue/avatar'
  import FabButton from '@/components/UI/fabButton/FabButton.vue'
  import Listbox from 'primevue/listbox'

  export default defineComponent({
    name: 'gallery-management',
    components: {
      Avatar,
      FabButton,
      Listbox
    },
    data (){
      return{
        galleryTemplates: [
          {id:'908d9674-ff77-43bd-90e6-fa11eef06c99', label:'colored card', type:'html', author:'davide.vernassa@eng.it', tags:["html","card"]},
          {id:'b206bf60-5622-4432-9e6c-fd4a66bab811', label:'advanced line chart', type:'chart', author:'matteo.massarotto@eng.it', tags:["chart", "highchart"]},
          {id:'b160c219-801e-4030-afa9-b52583a9094f', label:'hierarchy', type:'chart', author:'davide.vernassa@eng.it', tags:["highchart", "MARE"]},
          {id:'27f46bee-442b-4c65-a6ff-4e55a1caa93f', label:'double cards', type:'html', author:'davide.vernassa@eng.it', tags:["html", "card"]},
          {id:'84d99a09-5d07-4fa5-85f3-e0c19c78d508', label:'progression chart', type:'python', author:'alberto.nale@eng.it', tags:["python", "function","ai"]},
          {id:'cba22aa7-444f-4dfb-9d16-ca7df7965360', label:'multicard', type:'html', author:'davide.vernassa@eng.it', tags:["html", "multiple", "card"]},
          {id:'0e5c80b8-8308-48fe-8943-274d7bfa5dfb', label:'header_light', type:'html', author:'alberto.nale@eng.it', tags:["html", "header"]},
          {id:'833c2694-7873-4308-956d-f0a4ccddae08', label:'header_dark', type:'html', author:'alberto.nale@eng.it', tags:["html", "header"]}
        ],
        iconTypesMap: {
          "chart": {"className":"fab fa-js-square", "style":{"background-color":"#ffc107","color":"white"}},
          "html": {"className":"fab fa-html5", "style":{"background-color":"#c2185b","color":"white"}},
          "python": {"className":"fab fa-python", "style":{"background-color":"#4caf50","color":"white"}},
        }
      }
    },
    methods:{
      deleteTemplate(e,templateId){
        e.preventDefault()
        /*this.axios.delete(`/knowage/restful-services/3.0/gallery/${this.id}`)
            .then(response => this.template = response.data)
            .catch(error => console.error(error))*/
        alert('delete template: '+templateId)
      }
    }
  })
</script>

<style lang="scss" scoped>

  .knPage {
    display: flex;
    flex-direction: column;
    height: 100%;

    .knPageContent {
      flex: 1;
    }
  }
  .noDecoration{
    text-decoration: none;
    color: inherit;
  }

  .knListColumn {
    border-right: 1px solid #ccc;
  }

  .knList{
    border: none;
    border-radius: 0;
    &:deep() .p-listbox-item {
      padding: 0;
      a {
        display: block;
        padding: 0.75rem 0.75rem;
        &.router-link-active{
          background-color: $color-secondary;
        }
      }
    }
    &:deep() .p-listbox-filter-container{
      input.p-listbox-filter{
        border-radius: 0;
      }
    }
    
    .knListItem{
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      align-items: center;

      .knListItemText{
        display: flex;
        flex: 1;
        flex-direction: column;
        justify-content: center;
        align-items: flex-start;
        margin-left: .8rem;

        .smallerLine{
          color: rgb(148, 148, 148);
          font-size: .8rem;
        }
      }
    }
  }

  .knToolbar{
    background-color: #3B678C;
    border-radius: 0;
    color: white;
  }

</style>
