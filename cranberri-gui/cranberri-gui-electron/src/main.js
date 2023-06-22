const { app, BrowserWindow, ipcMain } = require("electron")
const isDev = require('electron-is-dev')
const path = require("path")

const api = require("./api")

const createWindow = () => {
    const win = new BrowserWindow({
        title: "Cranberri GUI",
        width: 740,
        height: 360,
        webPreferences: {
            preload: path.join(__dirname, "preload.js")
        },
    })

    if (isDev) win.loadURL("http://localhost:4200")
    else win.loadFile("src/render/index.html")

    win.setTitle("Cranberri GUI")
    win.removeMenu()
    win.openDevTools()
}

app.whenReady().then(() => {
    ipcMain.handle("test", () => api.test())
    for (const [funName, fun] of Object.entries(api)) {
        ipcMain.handle(`api-${funName}`, () => fun())
    }

    createWindow()

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow()
    })
})

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit()
})