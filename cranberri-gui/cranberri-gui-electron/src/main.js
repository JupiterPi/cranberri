const { app, BrowserWindow, ipcMain } = require("electron")
const isDev = require('electron-is-dev')
const path = require("path")

const api = require("./api")

const createWindow = () => {
    const win = new BrowserWindow({
        title: "Cranberri",
        frame: false,
        width: 740,
        height: 370,
        resizable: false,
        webPreferences: {
            preload: path.join(__dirname, "preload.js")
        },
    })

    if (isDev) win.loadURL("http://localhost:4200")
    else win.loadFile("dist/cranberri-gui-angular/index.html")

    win.setTitle("Cranberri")
}

app.whenReady().then(() => {
    for (const [funName, fun] of Object.entries(api)) {
        ipcMain.handle(`api-${funName}`, (_, ...args) => fun(...args))
    }

    ipcMain.handle("close", () => {
        app.quit()
    })

    createWindow()

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow()
    })
})

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit()
})