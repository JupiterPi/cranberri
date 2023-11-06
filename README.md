# Cranberri
Minecraft microcontrollers mod

See also [cranberri-gui](https://github.com/JupiterPi/cranberri-gui).
See also [cranberri-guides](https://github.com/JupiterPi/cranberri-guides) (in German).

![Cranberri logo](logo.png)

## Installation and Usage

1. Install the [Cranberri GUI](https://github.com/JupiterPi/cranberri-gui/releases/latest), which you can use to install all necessary components and start the server.
2. Launch Minecraft 1.19.4 and connect to the server at server address `localhost`.

## Dev environment setup

1. Download and install [PaperMC](https://papermc.io/downloads/paper) 1.19.4 inside ./dev-server
2. Build cranberri-server-plugin: `$ gradlew installServerPlugin` inside ./cranberri-server-plugin
3. Execute server: `$ java -jar paper-1.19.4-....jar -nogui` inside ./dev-server
