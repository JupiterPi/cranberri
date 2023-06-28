# Cranberri
Minecraft microcontrollers mod

See also [cranberri-gui](https://github.com/JupiterPi/cranberri-gui)

![Cranberri logo](logo.png)

## Dev environment setup

1. Download and install [PaperMC](https://papermc.io/downloads/paper) 1.19.4 inside ./dev-server
2. Build cranberri-server-plugin: `$ gradlew installServerPlugin` inside ./cranberri-server-plugin
3. Execute server: `$ java -jar paper-1.19.4-....jar -nogui` inside ./dev-server
