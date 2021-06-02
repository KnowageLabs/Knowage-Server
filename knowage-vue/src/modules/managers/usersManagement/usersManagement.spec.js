import { mount } from "@vue/test-utils";
import axios from "axios";
import Button from "primevue/button";
import flushPromises from "flush-promises";
import UsersManagement from "./UsersManagement.vue";
import InputText from "primevue/inputtext";
import ProgressBar from "primevue/progressbar";
import Card from "primevue/card";
import Toolbar from "primevue/toolbar";

const mockedUsers = [
  {
    id: 1,
    userId: "biadmin",
    fullName: "Knowage Administrator",
    dtPwdBegin: null,
    dtPwdEnd: null,
    flgPwdBlocked: true,
    dtLastAccess: 1621249789000,
    isSuperadmin: true,
    defaultRoleId: null,
    failedLoginAttempts: 3,
    blockedByFailedLoginAttempts: false,
    sbiExtUserRoleses: [4],
    sbiUserAttributeses: {
      "1": {
        name: "Knowage Administrator",
      },
      "5": {
        email: "admin@eng.it",
      },
      "7": {
        pr_ruolo: "%",
      },
      "8": {
        pr_userid: "%",
      },
    },
  },
  {
    id: 3,
    userId: "bidemo",
    fullName: "Knowage Demo User",
    dtPwdBegin: null,
    dtPwdEnd: null,
    flgPwdBlocked: null,
    dtLastAccess: null,
    isSuperadmin: false,
    defaultRoleId: null,
    failedLoginAttempts: 0,
    blockedByFailedLoginAttempts: false,
    sbiExtUserRoleses: [6],
    sbiUserAttributeses: {
      "1": {
        name: "Knowage Demo User",
      },
      "7": {
        pr_ruolo: "%",
      },
      "8": {
        pr_userid: "%",
      },
    },
  },
  {
    id: 5,
    userId: "bidev",
    fullName: "Knowage Developer",
    dtPwdBegin: null,
    dtPwdEnd: null,
    flgPwdBlocked: null,
    dtLastAccess: null,
    isSuperadmin: false,
    defaultRoleId: 2,
    failedLoginAttempts: 0,
    blockedByFailedLoginAttempts: false,
    sbiExtUserRoleses: [1, 2, 3, 4, 20, 5, 6, 25, 26, 28, 13, 14],
    sbiUserAttributeses: {
      "1": {
        name: "Knowage Developer",
      },
      "7": {
        pr_ruolo: "%",
      },
      "8": {
        pr_userid: "%",
      },
    },
  },
];

jest.mock("axios", () => ({
  get: jest.fn(() =>
    Promise.resolve({
      data: mockedUsers,
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
  return mount(UsersManagement, {
    attachToDocument: true,
    global: {
      plugins: [],
      stubs: { Button, InputText, ProgressBar, Toolbar, Card },
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

describe("Users Management loading", () => {
  it("show progress bar when loading", async () => {
    const wrapper = factory();

    expect(wrapper.vm.loading).toBe(true);
    expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true);
  });
  it("shows error toast if service returns error", async () => {
    // not in this component
  });
  it('shows "no data" label when loaded empty', async () => {
    axios.get.mockReturnValueOnce(
      Promise.resolve({
        data: [],
      })
    );
    const wrapper = factory();
    await flushPromises();
    expect(wrapper.vm.users.length).toBe(0);
    expect(wrapper.find('[data-test="users-table"]').html()).toContain(
      "common.info.noDataFound"
    );
  });
});

describe("Users Management", () => {
  it("deletes user clicking on delete icon", async () => {
    const wrapper = factory();
    await flushPromises();
    const deleteButton = wrapper.find('[data-test="delete-button"]');
    await deleteButton.trigger("click");
    expect($confirm.require).toHaveBeenCalledTimes(1);

    await wrapper.vm.deleteUser(1);

    await wrapper.vm.loadAllUsers();
    expect(wrapper.vm.loading).toBe(false);
  });

  it("opens empty form when the '+' button is clicked", async () => {
    const wrapper = factory();
    const openButton = wrapper.find('[data-test="open-form-button"]');
    await openButton.trigger("click");
    expect(wrapper.vm.hiddenForm).toBe(false);
  });

  it("shows form when a row is clicked", async () => {
    const wrapper = factory();
    await flushPromises();
    const dataTable = wrapper.find('[data-test="users-table"]');
    await dataTable.find("tr td").trigger("click");

    expect(wrapper.vm.hiddenForm).toBe(false);
    expect(wrapper.vm.userDetailsForm).toStrictEqual({
      id: 1,
      userId: "biadmin",
      fullName: "Knowage Administrator",
      dtPwdBegin: null,
      dtPwdEnd: null,
      flgPwdBlocked: true,
      dtLastAccess: 1621249789000,
      isSuperadmin: true,
      defaultRoleId: null,
      failedLoginAttempts: 3,
      blockedByFailedLoginAttempts: false,
      sbiExtUserRoleses: [4],
      sbiUserAttributeses: {
        "1": {
          name: "Knowage Administrator",
        },
        "5": {
          email: "admin@eng.it",
        },
        "7": {
          pr_ruolo: "%",
        },
        "8": {
          pr_userid: "%",
        },
      },
    });
  });
});

describe("Users Management Search", () => {
  it("filters the list if a label (or other column) is provided", async () => {
    const wrapper = factory();
    await flushPromises();
    const dataTable = wrapper.find('[data-test="users-table"]');
    const inputSearch = wrapper.find('[data-test="search-input"]');

    expect(dataTable.html()).toContain("biadmin");
    expect(dataTable.html()).toContain("bidemo");
    expect(dataTable.html()).toContain("bidev");
    
    expect(dataTable.html()).toContain("Knowage Administrator");
    expect(dataTable.html()).toContain("Knowage Demo User");
    expect(dataTable.html()).toContain("Knowage Developer");

    // userID
    await inputSearch.setValue("biadmin");
    expect(dataTable.html()).not.toContain("bidev");
    expect(dataTable.html()).toContain("Knowage Administrator");

    // Full Name
    await inputSearch.setValue("Knowage Administrator");
    expect(dataTable.html()).not.toContain("bidemo");
    expect(dataTable.html()).toContain("Knowage Administrator");

  });

  it("returns no data if the label is not present", async () => {
    const wrapper = factory();
    await flushPromises();
    const dataTable = wrapper.find('[data-test="users-table"]');
    const inputSearch = wrapper.find('[data-test="search-input"]');

    expect(dataTable.html()).toContain("biadmin");
    expect(dataTable.html()).toContain("bidemo");
    expect(dataTable.html()).toContain("bidev");

    await inputSearch.setValue("not present value");
    expect(dataTable.html()).not.toContain("biadmin");
    expect(dataTable.html()).not.toContain("bidemo");
    expect(dataTable.html()).not.toContain("bidev");
    expect(wrapper.find('[data-test="users-table"]').html()).toContain(
      "common.info.noDataFound"
    );
  });
});
