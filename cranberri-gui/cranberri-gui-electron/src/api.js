module.exports = {
    test: () => "Hello there.",
    getWorlds: () => [
        {
            id: "cNjUO7Cp4T",
            name: "My World"
        },
        {
            id: "CYqC5eeN0G",
            name: "My World"
        },
        {
            id: "XfwIjiFy8z",
            name: "My World"
        }
    ],
    getActiveWorldId: () => "CYqC5eeN0G",
    renameWorld: (id, name) => {},
    archiveWorld: (id) => {},
    getProjects: () => [
        {
            "name": "test1",
            "language": "kotlin"
        },
        {
            "name": "my_project",
            "language": "java"
        },
        {
            "name": "horse",
            "language": "kotlin"
        }
    ],
    openProjectsFolder: () => {},
    openProjectFolder: (projectName) => {},
    createProject: (name, language) => {},
    startServer: (worldId) => {
        console.log("Start server!")
    },
}