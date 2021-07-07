import { flushPromises, mount } from "@vue/test-utils";
import Button from "primevue/button";
import MenuConfiguration from "./MenuConfiguration.vue";
import InputText from "primevue/inputtext";
import ProgressBar from "primevue/progressbar";
import Card from "primevue/card";
import Toolbar from "primevue/toolbar";
import KnHint from "@/components/UI/KnHint.vue";
import Tree from "primevue/tree";
import axios from "axios";

const mockedMenuNodes = [
  {
    menuId: 33,
    objId: null,
    objParameters: null,
    subObjName: null,
    snapshotName: null,
    snapshotHistory: null,
    functionality: "WorkspaceManagement",
    initialPath: "analysis",
    name: "test",
    descr: "description",
    parentId: null,
    level: 1,
    depth: null,
    prog: 1,
    hasChildren: false,
    lstChildren: [],
    roles: [],
    viewIcons: false,
    hideToolbar: false,
    hideSliders: false,
    staticPage: "",
    code: null,
    url: null,
    iconPath: null,
    icon: {
      label: "",
      className: "fas fa-business-time",
      unicode: null,
      visible: true,
      id: 90,
      category: "solid",
      src: null,
    },
    custIcon: null,
    iconCls: null,
    groupingMenu: null,
    linkType: null,
    adminsMenu: false,
    externalApplicationUrl: null,
    clickable: true,
  },
  {
    menuId: 35,
    objId: null,
    objParameters: null,
    subObjName: null,
    snapshotName: null,
    snapshotHistory: null,
    functionality: null,
    initialPath: null,
    name: "subsubnode",
    descr: "desc",
    parentId: 32,
    level: 2,
    depth: null,
    prog: 1,
    hasChildren: false,
    lstChildren: [],
    roles: [
      {
        id: 28,
        name: "kte_dev",
        description: "kte_dev",
        roleTypeCD: "DEV_ROLE",
        code: "kte_dev",
        roleTypeID: 29,
        organization: "DEFAULT_TENANT",
        isPublic: false,
        ableToCreateSelfServiceCockpit: false,
        ableToCreateSelfServiceGeoreport: false,
        ableToCreateSelfServiceKpi: false,
        defaultRole: false,
        roleMetaModelCategories: null,
        ableToManageInternationalization: false,
        ableToSendMail: false,
        ableToSeeNotes: false,
        ableToManageGlossaryTechnical: false,
        ableToManageKpiValue: false,
        ableToManageCalendar: false,
        ableToEnableCopyAndEmbed: false,
        ableToEnableDatasetPersistence: false,
        ableToManageGlossaryBusiness: false,
        ableToUseFunctionsCatalog: false,
        ableToEnableFederatedDataset: false,
        ableToEnablePrint: false,
        ableToSaveSubobjects: false,
        ableToEnableRate: false,
        ableToCreateCustomChart: false,
        ableToEditPythonScripts: false,
        ableToSeeSubobjects: false,
        ableToSaveIntoPersonalFolder: false,
        ableToDeleteKpiComm: false,
        ableToSeeToDoList: false,
        ableToHierarchiesManagement: false,
        ableToSeeFavourites: false,
        ableToSaveMetadata: false,
        ableToSeeMetadata: false,
        ableToDoMassiveExport: false,
        ableToCreateSocialAnalysis: false,
        ableToEditMyKpiComm: false,
        ableToSeeDocumentBrowser: false,
        ableToSeeViewpoints: false,
        ableToSeeSnapshots: false,
        ableToBuildQbeQuery: false,
        ableToEditAllKpiComm: false,
        ableToSaveRememberMe: false,
        ableToSeeMyData: false,
        ableToCreateDocuments: false,
        ableToSeeMyWorkspace: false,
        ableToSeeSubscriptions: false,
        ableToViewSocialAnalysis: false,
        ableToManageUsers: false,
        ableToRunSnapshots: false,
      },
    ],
    viewIcons: false,
    hideToolbar: false,
    hideSliders: false,
    staticPage: "homePageWithLinks.html",
    code: null,
    url: null,
    iconPath: null,
    icon: null,
    custIcon: null,
    iconCls: null,
    groupingMenu: null,
    linkType: null,
    adminsMenu: false,
    externalApplicationUrl: null,
    clickable: true,
  },
  {
    menuId: 32,
    objId: null,
    objParameters: null,
    subObjName: null,
    snapshotName: null,
    snapshotHistory: null,
    functionality: "WorkspaceManagement",
    initialPath: "datasets",
    name: "subnode",
    descr: "desc",
    parentId: 33,
    level: 2,
    depth: null,
    prog: 1,
    hasChildren: false,
    lstChildren: [],
    roles: [
      {
        id: 28,
        name: "kte_dev",
        description: "kte_dev",
        roleTypeCD: "DEV_ROLE",
        code: "kte_dev",
        roleTypeID: 29,
        organization: "DEFAULT_TENANT",
        isPublic: false,
        ableToCreateSelfServiceCockpit: false,
        ableToCreateSelfServiceGeoreport: false,
        ableToCreateSelfServiceKpi: false,
        defaultRole: false,
        roleMetaModelCategories: null,
        ableToManageInternationalization: false,
        ableToSendMail: false,
        ableToSeeNotes: false,
        ableToManageGlossaryTechnical: false,
        ableToManageKpiValue: false,
        ableToManageCalendar: false,
        ableToEnableCopyAndEmbed: false,
        ableToEnableDatasetPersistence: false,
        ableToManageGlossaryBusiness: false,
        ableToUseFunctionsCatalog: false,
        ableToEnableFederatedDataset: false,
        ableToEnablePrint: false,
        ableToSaveSubobjects: false,
        ableToEnableRate: false,
        ableToCreateCustomChart: false,
        ableToEditPythonScripts: false,
        ableToSeeSubobjects: false,
        ableToSaveIntoPersonalFolder: false,
        ableToDeleteKpiComm: false,
        ableToSeeToDoList: false,
        ableToHierarchiesManagement: false,
        ableToSeeFavourites: false,
        ableToSaveMetadata: false,
        ableToSeeMetadata: false,
        ableToDoMassiveExport: false,
        ableToCreateSocialAnalysis: false,
        ableToEditMyKpiComm: false,
        ableToSeeDocumentBrowser: false,
        ableToSeeViewpoints: false,
        ableToSeeSnapshots: false,
        ableToBuildQbeQuery: false,
        ableToEditAllKpiComm: false,
        ableToSaveRememberMe: false,
        ableToSeeMyData: false,
        ableToCreateDocuments: false,
        ableToSeeMyWorkspace: false,
        ableToSeeSubscriptions: false,
        ableToViewSocialAnalysis: false,
        ableToManageUsers: false,
        ableToRunSnapshots: false,
      },
      {
        id: 5,
        name: "modeladmin",
        description: "modeladmin",
        roleTypeCD: "MODEL_ADMIN",
        code: null,
        roleTypeID: 31,
        organization: "DEFAULT_TENANT",
        isPublic: null,
        ableToCreateSelfServiceCockpit: false,
        ableToCreateSelfServiceGeoreport: false,
        ableToCreateSelfServiceKpi: false,
        defaultRole: false,
        roleMetaModelCategories: null,
        ableToManageInternationalization: false,
        ableToSendMail: false,
        ableToSeeNotes: false,
        ableToManageGlossaryTechnical: false,
        ableToManageKpiValue: false,
        ableToManageCalendar: false,
        ableToEnableCopyAndEmbed: false,
        ableToEnableDatasetPersistence: false,
        ableToManageGlossaryBusiness: false,
        ableToUseFunctionsCatalog: false,
        ableToEnableFederatedDataset: false,
        ableToEnablePrint: false,
        ableToSaveSubobjects: false,
        ableToEnableRate: false,
        ableToCreateCustomChart: false,
        ableToEditPythonScripts: false,
        ableToSeeSubobjects: false,
        ableToSaveIntoPersonalFolder: false,
        ableToDeleteKpiComm: false,
        ableToSeeToDoList: false,
        ableToHierarchiesManagement: false,
        ableToSeeFavourites: false,
        ableToSaveMetadata: false,
        ableToSeeMetadata: false,
        ableToDoMassiveExport: false,
        ableToCreateSocialAnalysis: false,
        ableToEditMyKpiComm: false,
        ableToSeeDocumentBrowser: false,
        ableToSeeViewpoints: false,
        ableToSeeSnapshots: false,
        ableToBuildQbeQuery: false,
        ableToEditAllKpiComm: false,
        ableToSaveRememberMe: false,
        ableToSeeMyData: false,
        ableToCreateDocuments: false,
        ableToSeeMyWorkspace: false,
        ableToSeeSubscriptions: false,
        ableToViewSocialAnalysis: false,
        ableToManageUsers: false,
        ableToRunSnapshots: false,
      },
    ],
    viewIcons: false,
    hideToolbar: false,
    hideSliders: false,
    staticPage: "",
    code: null,
    url: null,
    iconPath: null,
    icon: null,
    custIcon: null,
    iconCls: null,
    groupingMenu: null,
    linkType: null,
    adminsMenu: false,
    externalApplicationUrl: null,
    clickable: true,
  },
];

jest.mock("axios", () => ({
  get: jest.fn(() =>
    Promise.resolve({
      data: mockedMenuNodes,
    })
  ),
  delete: jest.fn(() => Promise.resolve()),
  post: jest.fn(() => Promise.resolve()),
}));

const $confirm = {
  require: jest.fn(),
};
const $store = {
  commit: jest.fn(),
};

const factory = () => {
  return mount(MenuConfiguration, {
    attachToDocument: true,
    global: {
      plugins: [],
      stubs: { Button, InputText, ProgressBar, Toolbar, Card, KnHint, Tree },
      mocks: {
        $t: (msg) => msg,
        $store,
        $confirm,
      },
    },
  });
};

afterEach(() => {
  jest.clearAllMocks();
});

describe("menu configuration management loading", () => {
  it("show progress bar when loading", async () => {
    const wrapper = factory();

    expect(wrapper.vm.loading).toBe(true);
    expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true);
  });
});

it("when loaded a tree with just the root is shown if no child are present", async () => {
  axios.get.mockReturnValueOnce(
    Promise.resolve({
      data: [
        {
          menuId: 33,
          objId: null,
          objParameters: null,
          subObjName: null,
          snapshotName: null,
          snapshotHistory: null,
          functionality: "WorkspaceManagement",
          initialPath: "analysis",
          name: "test",
          descr: "description",
          parentId: null,
          level: 1,
          depth: null,
          prog: 1,
          hasChildren: false,
          lstChildren: [],
          roles: [],
          viewIcons: false,
          hideToolbar: false,
          hideSliders: false,
          staticPage: "",
          code: null,
          url: null,
          iconPath: null,
          icon: {
            label: "",
            className: "fas fa-business-time",
            unicode: "null",
            visible: "true",
            id: 90,
            category: "solid",
            src: null,
          },
          custIcon: null,
          iconCls: null,
          groupingMenu: null,
          linkType: null,
          adminsMenu: false,
          externalApplicationUrl: null,
          clickable: true,
        },
      ],
    })
  );
  const wrapper = factory();
  const tree = wrapper.find('[data-test="menu-nodes-tree"]');

  await flushPromises();

  expect(wrapper.vm.menuNodes.length).toBe(1);
  expect(wrapper.vm.menuNodes.length).toBe(1);
  expect(tree.html()).toContain("test");
  expect(tree.html()).not.toContain("subnode");
  expect(tree.html()).not.toContain("subsubnode");
});

describe("menu configuration management", () => {
  it("opens empty form when the '+' button is clicked", async () => {
    const wrapper = factory();
    const openButton = wrapper.find('[data-test="open-form-button"]');
    await openButton.trigger("click");
    expect(wrapper.vm.hideForm).toBe(false);
  });
});
