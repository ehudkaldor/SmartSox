# One Akka cluster to rule them all

This project aims to be a smart-home like manager, with modules to handle different household IoT devices, such as doors, windows, light, AC and such. Both on an individual level (open a door, turn AC off) and coordinating level (sound the alarm if door opens, turn AC on if temperature is above XX degrees).

This project is written in Scala, with Akka being the infrastructure.
Akka modules to be used are:
* **Akka** - communication shall be actor-based
* **Akka-cluster** - the cluster shall tie all components together, and provide basis for component interaction
* **Akka PubSub** - event publishing and listening by components, to trigger action flows
* **Akka-persistence** - for persistency
* **Akka-http** - to provide management API

This is a learning project. That means I am writing it to get more hands on with the components used. So, it will probably be dirty most of the time. Please feel free to comment or criticize. I am here to learn.
