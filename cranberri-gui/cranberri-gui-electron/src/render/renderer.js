const {React} = require("react")

(async () => {

    const information = document.getElementById('info')
    information.innerText = await api.test()

})()