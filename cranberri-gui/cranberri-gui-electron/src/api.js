const fs = require("fs")
const path = require("path")
const child_process = require("child_process")

const updater = require("./updater")
const {SERVER_ROOT, WORLDS_REGISTRY, WORLDS_DIR, ACTIVE_WORLD_DIRS, ACTIVE_WORLD_ROOT, ARCHIVED_WORLDS_DIR, PROJECTS_ROOT} = require("./paths")

const SAMPLE_SCRIPT = `
// sample script

fun tick() {
    writePin(2, readPin(1))
}
`

function readWorlds() {
    return JSON.parse(fs.readFileSync(WORLDS_REGISTRY, "utf-8"))
}
function writeWorlds(worlds) {
    fs.writeFileSync(WORLDS_REGISTRY, JSON.stringify(worlds, null, 2))
}

module.exports = {
    test: () => "Hello there.",
    isInstalled: () => updater.isInstalled(),
    updateAvailable: () => updater.updateAvailable(),
    installOrUpdate: async () => await updater.installOrUpdate(),
    getWorlds: () => readWorlds()["worlds"],
    getActiveWorldId: () => readWorlds()["activeWorldId"],
    renameWorld: (id, name) => {
        let worlds = readWorlds()
        worlds["worlds"].forEach(world => {
            if (world.id === id) world.name = name
        })
        writeWorlds(worlds)
        return worlds["worlds"]
    },
    archiveWorld: (id) => {
        let worlds = readWorlds()
        const worldName = worlds["worlds"].filter(world => world.id === id)[0].name.replace(" ", "_")
        worlds["worlds"] = worlds["worlds"].filter(world => world.id !== id)
        fs.mkdirSync(ARCHIVED_WORLDS_DIR, { recursive: true })
        if (id === worlds["activeWorldId"]) {
            ACTIVE_WORLD_DIRS.forEach(dir => fs.renameSync(`${ACTIVE_WORLD_ROOT}/${dir}`, `${ARCHIVED_WORLDS_DIR}/${id}-${worldName}/${dir}`))
            worlds["activeWorld"] = null
        } else {
            fs.renameSync(`${WORLDS_DIR}/${id}`, `${ARCHIVED_WORLDS_DIR}/${id}-${worldName}`)
        }
        writeWorlds(worlds)
        return worlds["worlds"]
    },
    getProjects: () => {
        return fs.readdirSync(PROJECTS_ROOT).map(name => {
            return {name, language: "kotlin"}
        })
    },
    openProjectsFolder: () => {
        child_process.exec(`start "" "${path.resolve(PROJECTS_ROOT)}"`)
        // see https://stackoverflow.com/a/35076582/13164753
    },
    openProjectFolder: (projectName) => {
        child_process.exec(`start "" "${path.resolve(path.join(PROJECTS_ROOT, projectName))}"`)
    },
    createProject: (name, language) => {
        fs.mkdirSync(`${PROJECTS_ROOT}/${name}/scripts`, { recursive: true })
        fs.writeFileSync(`${PROJECTS_ROOT}/${name}/scripts/my_script.kt`, SAMPLE_SCRIPT)
        return { name, language: "kotlin" }
    },
    startServer: (worldId) => {
        let world

        const worlds = readWorlds()
        if (worldId !== worlds["activeWorldId"]) {
            if (worlds["activeWorldId"] != null) {
                fs.mkdirSync(`${WORLDS_DIR}/${worlds["activeWorldId"]}`, { recursive: true })
                ACTIVE_WORLD_DIRS.forEach(dir => fs.renameSync(`${ACTIVE_WORLD_ROOT}/${dir}`, `${WORLDS_DIR}/${worlds["activeWorldId"]}/${dir}`))
            }
            if (worldId != null) {
                ACTIVE_WORLD_DIRS.forEach(dir => {
                    fs.renameSync(`${WORLDS_DIR}/${worldId}/${dir}`, `${ACTIVE_WORLD_ROOT}/${dir}`);
                })
                worlds["activeWorldId"] = worldId
                world = worlds["worlds"].filter(world => world.id === worldId)[0]
            }
        }
        if (worldId == null) {
            const id = require("crypto").randomBytes(4).toString("hex")
            world = { id, name: "Unnamed World" }
            worlds["worlds"].push(world)
            worlds["activeWorldId"] = id
        }
        writeWorlds(worlds)

        child_process.exec(`start cmd.exe /c "cd ${SERVER_ROOT} && java -jar paper-1.19.4-550.jar nogui"`)

        return world
    },
}