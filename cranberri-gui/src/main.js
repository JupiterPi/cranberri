const { app, BrowserWindow, ipcMain } = require("electron")
const path = require("path")

const api = require("./api")

const createWindow = () => {
    const win = new BrowserWindow({
        width: 1200,
        height: 800,
        webPreferences: {
            preload: path.join(__dirname, "preload.js")
        }
    })

    win.loadFile('src/render/index.html')
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