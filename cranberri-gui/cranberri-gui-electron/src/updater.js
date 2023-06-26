const fs = require("fs")
const https = require("https")
const {SERVER_ROOT, WORLDS_REGISTRY, PROJECTS_ROOT, ARCHIVED_WORLDS_DIR} = require("./paths")

function isInstalled() {
    return fs.existsSync(`${SERVER_ROOT}/paper-1.19.4-550.jar`)
}

async function install() {
    await installPaper()
    installPlugin()
    fs.writeFileSync(`${SERVER_ROOT}/eula.txt`, "eula=true\n")
    setup()
}

function setup() {
    if (!fs.existsSync(WORLDS_REGISTRY)) {
        fs.writeFileSync(WORLDS_REGISTRY, JSON.stringify({
            activeWorldId: null,
            worlds: []
        }, null, 2))
    }
    fs.mkdirSync(PROJECTS_ROOT, { recursive: true })
    fs.mkdirSync(ARCHIVED_WORLDS_DIR, { recursive: true })
}
if (isInstalled()) setup()

function installPaper() {
    return new Promise((resolve, _) => {
        fs.mkdirSync(SERVER_ROOT, { recursive: true })

        const file = fs.createWriteStream(`${SERVER_ROOT}/paper-1.19.4-550.jar`)
        https.get("https://api.papermc.io/v2/projects/paper/versions/1.19.4/builds/550/downloads/paper-1.19.4-550.jar", function(response) {
            response.pipe(file)
            file.on("finish", () => {
                file.close()
                resolve()
            })
        })
    })
}

function installPlugin() {
    //TODO implement
}

module.exports = {
    isInstalled,
    updateAvailable: () => false,
    installOrUpdate: install,
}