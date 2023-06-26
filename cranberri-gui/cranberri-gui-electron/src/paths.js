const SERVER_ROOT = "server"

module.exports = {
    SERVER_ROOT,

    WORLDS_REGISTRY: `${SERVER_ROOT}/worlds.json`,
    WORLDS_DIR: `${SERVER_ROOT}/worlds`,
    ACTIVE_WORLD_DIRS: ["world", "world_nether", "world_the_end"],
    ACTIVE_WORLD_ROOT: SERVER_ROOT,
    ARCHIVED_WORLDS_DIR: `${SERVER_ROOT}/worlds_archive`,

    PROJECTS_ROOT: `${SERVER_ROOT}/cranberri_projects`,
}
