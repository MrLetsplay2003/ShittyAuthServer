# ShittyAuthServer
A shitty implementation of the Yggdrasil authentication scheme

This project implements Mojang's Yggdrasil authentication scheme (which was used before the transition to Microsoft accounts).

It is intended to be used in conjunction with the [ShittyAuthLauncher](https://github.com/MrLetsplay2003/ShittyAuthLauncher)

# Compiling the server
The server uses Maven for building.

To compile the server, use
```
$ mvn package
```
which will generate a `ShittyAuthServer-VERSION.jar` in the `target` folder

# Running the server
First, compile the server or download a prebuilt JAR file from [here](https://ci.graphite-official.com/job/ShittyAuthServer/), then, run it using any Java 11+ VM.

Afterwards, navigate to `http://your.server.ip:8880` in your web browser. You will be prompted to set up the WebinterfaceAPI server the authentication server uses.

Once you're done, your players will be able to create accounts (using password authentication) and use their credentials to login in the launcher (after having changing the auth server URL to the correct domain/IP).

Optional: If you have a setup using an HTTP(S) proxy server (e.g. Apache), make sure to change the Skin base URL in the `Minecraft > Settings` tab to your public domain.
