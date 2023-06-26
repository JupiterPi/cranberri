const { contextBridge, ipcRenderer } = require("electron")

contextBridge.exposeInMainWorld("api", {
    test: () => ipcRenderer.invoke("api-test"),
    isInstalled: () => ipcRenderer.invoke("api-isInstalled"),
    updateAvailable: () => ipcRenderer.invoke("api-updateAvailable"),
    installOrUpdate: () => ipcRenderer.invoke("api-installOrUpdate"),
    getWorlds: () => ipcRenderer.invoke("api-getWorlds", "teste1", "teste2"),
    getActiveWorldId: () => ipcRenderer.invoke("api-getActiveWorldId"),
    renameWorld: (id, name) => ipcRenderer.invoke("api-renameWorld", id, name),
    archiveWorld: (id) => ipcRenderer.invoke("api-archiveWorld", id),
    getProjects: () => ipcRenderer.invoke("api-getProjects"),
    openProjectsFolder: () => ipcRenderer.invoke("api-openProjectsFolder"),
    openProjectFolder: (projectName) => ipcRenderer.invoke("api-openProjectFolder", projectName),
    createProject: (name, language) => ipcRenderer.invoke("api-createProject", name, language),
    startServer: (worldId) => ipcRenderer.invoke("api-startServer", worldId),

    close: () => ipcRenderer.invoke("close"),
})