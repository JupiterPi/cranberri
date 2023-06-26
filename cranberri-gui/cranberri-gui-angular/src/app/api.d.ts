export type World = {
  id: string;
  name: string;
}

export type ProjectLanguage = "kotlin" | "java"

export type Project = {
  name: string;
  language: ProjectLanguage;
}


declare global {
  const api: {
    test: () => Promise<string>,
    isInstalled: () => Promise<boolean>,
    updateAvailable: () => Promise<boolean>,
    installOrUpdate: () => Promise<void>,
    getWorlds: () => Promise<World[]>,
    getActiveWorldId: () => Promise<string>,
    renameWorld: (id: string, name: string) => Promise<World[]>,
    archiveWorld: (id: string) => Promise<World[]>,
    getProjects: () => Promise<Project[]>,
    openProjectsFolder: () => Promise<void>,
    openProjectFolder: (projectName: String) => Promise<void>,
    createProject: (name: string, language: string) => Promise<Project>,
    startServer: (worldId: string | null) => Promise<World>,

    close: () => Promise<void>,
  }
}
