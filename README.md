# ⚠️ Unofficial Minecraft 1.21.4 back-port

**This is not the official JustEnoughItems repository, and this is not an official JEI build.**

This branch back-ports the upstream `1.21.5` branch (commit [`0772287a`](https://github.com/mezz/JustEnoughItems/commit/0772287a157beb93f438ee10f88afe402e262856)) to **Minecraft 1.21.4 / Fabric**.

* Official JEI lives at <https://github.com/mezz/JustEnoughItems>. Please do not report problems with this back-port there.
* **Fabric only.** `:NeoForge:build` is knowingly broken on this branch — see commit `db1828af`.
* **Recipes work in single-player only.** See below.
* The mod id is still `jei`, so this jar cannot be installed alongside official JEI.
* Upstream shipped JEI 20.0.0 for Minecraft 1.21.4, but **that release contains no Fabric module** — it is NeoForge-only.

### Recipes: single-player works, multiplayer does not

Upstream's Fabric module has never had recipe sync from the server. It entered history as
commit `b120e37a`, titled *"WIP Fabric support but missing recipe sync from the server"*.
Since Minecraft 1.21.2 recipes live on the server and the client's own `RecipeManager` is
empty, so `VanillaPlugin.registerRecipes` bails out and JEI reports
*"JEI is missing recipes…"* in chat with no recipes listed at all.

Commit `88aeb581` works around this **in single-player** by reading recipes straight out of
the integrated server, which runs in the same process:
`Minecraft.getSingleplayerServer().getRecipeManager().getRecipes()`.

In multiplayer the recipe list is still empty. Upstream's real fix (`597a0f69d`, "Use
fabric recipe sync") relies on `ClientRecipeSynchronizedEvent` from `fabric-recipe-api-v1`,
which does not exist in Fabric API 0.119.4+1.21.4, so it cannot be back-ported as-is.

### What has actually been tested

Confirmed working in single-player on Minecraft 1.21.4 with Fabric Loader 0.19.5 and Fabric
API 0.119.4+1.21.4: 1409 recipes read, 1781 ingredients in the item list, JEI ready in
about 8 seconds. Beyond that only the build, `validateAccessWidener`, the packaged jar
contents and the unit tests have been verified. Multiplayer is known not to show recipes;
anything past a plain vanilla single-player world is untested.

Original code is © mezz and MIT licensed — see [LICENSE.txt](LICENSE.txt). The back-port changes are offered under the same license.

### Building

Requires JDK 21:

    ./gradlew :Fabric:build

The jar is written to `Fabric/build/libs/`. On this branch the shared modules
(`CommonApi`, `Common`, `Library`, `Gui`) use `fabric-loom` instead of ModDevGradle, because
the NeoForm decompile step needs about 4 GB of RAM. That change is isolated in commit
`db1828af` and can be reverted if you build on a machine with enough memory.

> Неофициальный бэкпорт JEI на Minecraft 1.21.4 (Fabric). К mezz отношения не имеет.
> Проверен в одиночной игре; **рецепты работают только в одиночной игре**, в мультиплеере
> их не будет. modid остался `jei` — вместе с официальным JEI не встанет.

---

[![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https://ci.blamejared.com/job/mezz/job/jei/job/1.20/&style=?style=plastic)](https://ci.blamejared.com/job/mezz/job/jei/job/1.20/) [![](http://cf.way2muchnoise.eu/full_jei_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/jei) [![Discord](https://img.shields.io/discord/358816755646332941.svg?colorB=7289DA&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHYAAABWAgMAAABnZYq0AAAACVBMVEUAAB38%2FPz%2F%2F%2F%2Bm8P%2F9AAAAAXRSTlMAQObYZgAAAAFiS0dEAIgFHUgAAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfhBxwQJhxy2iqrAAABoElEQVRIx7WWzdGEIAyGgcMeKMESrMJ6rILZCiiBg4eYKr%2Fd1ZAfgXFm98sJfAyGNwno3G9sLucgYGpQ4OGVRxQTREMDZjF7ILSWjoiHo1n%2BE03Aw8p7CNY5IhkYd%2F%2F6MtO3f8BNhR1QWnarCH4tr6myl0cWgUVNcfMcXACP1hKrGMt8wcAyxide7Ymcgqale7hN6846uJCkQxw6GG7h2MH4Czz3cLqD1zHu0VOXMfZjHLoYvsdd0Q7ZvsOkafJ1P4QXxrWFd14wMc60h8JKCbyQvImzlFjyGoZTKzohwWR2UzSONHhYXBQOaKKsySsahwGGDnb%2FiYPJw22sCqzirSULYy1qtHhXGbtgrM0oagBV4XiTJok3GoLoDNH8ooTmBm7ZMsbpFzi2bgPGoXWXME6XT%2BRJ4GLddxJ4PpQy7tmfoU2HPN6cKg%2BledKHBKlF8oNSt5w5g5o8eXhu1IOlpl5kGerDxIVT%2BztzKepulD8utXqpChamkzzuo7xYGk%2FkpSYuviLXun5bzdRf0Krejzqyz7Z3p0I1v2d6HmA07dofmS48njAiuMgAAAAASUVORK5CYII%3D)](https://discord.gg/sCQcWU2)

# JustEnoughItems (JEI)
[JustEnoughItems](https://www.curseforge.com/minecraft/mc-mods/jei) is an Item and Recipe viewing mod for Minecraft with a focus on stability, performance, and ease of use.

This means:
 * just items and recipes
 * clean API for developers
 * not a coremod – no dependencies other than Forge.

### [JEI Developer Wiki](https://github.com/mezz/JustEnoughItems/wiki)

# Latest Versions:

## 1.21.1
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.21.1-neoforge%2Fmaven-metadata.xml&label=NeoForge%201.21.1)](https://maven.blamejared.com/mezz/jei/jei-1.21.1-neoforge/maven-metadata.xml)
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.21.1-fabric%2Fmaven-metadata.xml&label=Fabric%201.21.1)](https://maven.blamejared.com/mezz/jei/jei-1.21.1-fabric/maven-metadata.xml)
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.21.1-forge%2Fmaven-metadata.xml&label=Forge%201.21.1)](https://maven.blamejared.com/mezz/jei/jei-1.21.1-forge/maven-metadata.xml)

## 1.21
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.21-neoforge%2Fmaven-metadata.xml&label=NeoForge%201.21)](https://maven.blamejared.com/mezz/jei/jei-1.21-neoforge/maven-metadata.xml)
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.21-fabric%2Fmaven-metadata.xml&label=Fabric%201.21)](https://maven.blamejared.com/mezz/jei/jei-1.21-fabric/maven-metadata.xml)
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.21-forge%2Fmaven-metadata.xml&label=Forge%201.21)](https://maven.blamejared.com/mezz/jei/jei-1.21-forge/maven-metadata.xml)

## 1.20.1
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.20.1-fabric%2Fmaven-metadata.xml&label=Fabric%201.20.1)](https://maven.blamejared.com/mezz/jei/jei-1.20.1-fabric/maven-metadata.xml)
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.20.1-forge%2Fmaven-metadata.xml&label=Forge%201.20.1)](https://maven.blamejared.com/mezz/jei/jei-1.20.1-forge/maven-metadata.xml)

## 1.19.2
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.19.2-fabric%2Fmaven-metadata.xml&label=Fabric%201.19.2)](https://maven.blamejared.com/mezz/jei/jei-1.19.2-fabric/maven-metadata.xml)
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.19.2-forge%2Fmaven-metadata.xml&label=Forge%201.19.2)](https://maven.blamejared.com/mezz/jei/jei-1.19.2-forge/maven-metadata.xml)

## 1.18.2
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.18.2-fabric%2Fmaven-metadata.xml&label=Fabric%201.18.2)](https://maven.blamejared.com/mezz/jei/jei-1.18.2-fabric/maven-metadata.xml)
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.18.2-forge%2Fmaven-metadata.xml&label=Forge%201.18.2)](https://maven.blamejared.com/mezz/jei/jei-1.18.2-forge/maven-metadata.xml)

## 1.16.5
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei-1.16.5%2Fmaven-metadata.xml&label=Forge%201.16.5)](https://maven.blamejared.com/mezz/jei/jei-1.16.5/maven-metadata.xml)

## 1.12.2
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei_1.12.2%2Fmaven-metadata.xml&label=Forge%201.12.2)](https://maven.blamejared.com/mezz/jei/jei_1.12.2/maven-metadata.xml)

## 1.10.2
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei_1.10.2%2Fmaven-metadata.xml&label=Forge%201.10.2)](https://maven.blamejared.com/mezz/jei/jei_1.10.2/maven-metadata.xml)

## 1.8.9
* [![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fmezz%2Fjei%2Fjei_1.8.9%2Fmaven-metadata.xml&label=Forge%201.8.9)](https://maven.blamejared.com/mezz/jei/jei_1.8.9/maven-metadata.xml)
