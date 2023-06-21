module.exports = {
    test: () => "Hello there.",
    getWorlds: () => [],
    getActiveWorldId: () => "",
    renameWorld: (id, name) => {},
    archiveWorld: (id) => {},
    getProjects: () => [],
    openProjectsFolder: () => {},
    openProjectFolder: (projectName) => {},
    createProject: (name, language) => {},
    startServer: (worldId) => {},
}