# Minezone-Core

Minezone-Core is the main Java plugin behind **Minezone**, a custom Minecraft PvP server that I began developing in July 2020.

The project is built primarily around **Super Craft Bros**, a class-based PvP gamemode inspired by Minecade’s original Super Craft Bros. It also includes supporting Parkour and Fishing systems that give players additional progression and activities outside of matches.

## Main Features

### Super Craft Bros

Super Craft Bros is the main focus of Minezone-Core. Players choose from custom Minecraft-inspired classes, each with its own abilities, strengths, weaknesses, and playstyle.

The gamemode is designed around fast-paced combat, movement, knockback, map positioning, and learning how different classes interact with one another.

The project includes systems for:

* Custom classes, kits, and abilities
* Match creation and game flow
* Player lives and elimination
* Combat and damage handling
* Ability cooldowns
* Class selection
* Scoreboards and player status
* Spectating
* Statistics and leaderboards
* Rewards and progression
* Private games and game modifiers
* Party and friend integration

A large part of development also involves balancing classes, improving older abilities, resolving gameplay bugs, and making sure features work reliably during live matches.

### Parkour

The Parkour system provides an additional challenge for players outside of the main PvP gamemode.

It includes:

* Parkour start and finish detection
* Player timing
* Personal best times
* Leaderboard tracking
* Checkpoint and attempt management
* Protection against invalid completions

Parkour times are stored so players can continue competing for better personal and server-wide records.

### Fishing

Fishing is a secondary progression activity within the server.

The system includes:

* Fishing statistics
* Player progression
* Rewards
* Persistent fishing data
* Leaderboard support
* Custom handling for fishing-related events

Fishing gives players something more relaxed to work toward while still contributing to their overall server progression.

## Technology

Minezone-Core is developed using:

* Java
* Spigot/Paper 1.8.8
* Bukkit API
* Maven
* MySQL
* Git and GitHub

The project uses event-driven programming throughout its gameplay systems and contains persistent player data that is stored through MySQL.

## Development Areas

The codebase includes work involving:

* Event listeners
* Commands and permissions
* Game and player state management
* Custom combat mechanics
* Entity and projectile handling
* Inventory-based interfaces
* Scoreboards and holograms
* Cooldown systems
* Asynchronous database operations
* Player statistics and leaderboards
* Configuration files
* Packet-based features
* Error investigation and debugging

## Project History

I started Minezone-Core in 2020 while I was still learning Java and Minecraft plugin development. The project has grown significantly since then and has become my main long-term development project.

Because of its age, the codebase contains systems written at different stages of my development experience. A major part of my current work involves refactoring older areas, improving organization, removing outdated implementations, and making the project easier to maintain and expand.

Working on Minezone-Core has given me experience with the full development process, including:

* Planning features
* Implementing gameplay systems
* Testing updates
* Deploying changes
* Maintaining a live server
* Investigating stack traces and console errors
* Responding to player-reported bugs
* Improving features based on feedback

## Current Focus

Development is currently focused on:

* Improving Super Craft Bros gameplay
* Reworking and balancing classes
* Refining abilities and combat mechanics
* Refactoring older code
* Improving performance and reliability
* Expanding Parkour and Fishing progression
* Fixing issues found during live gameplay

## Inspiration

Super Craft Bros is inspired by **Minecade’s Super Craft Bros** and the style of classic Minecraft minigame servers.

Minezone is not intended to be a direct copy. The goal is to preserve the fast, creative, and class-based gameplay that made the original mode memorable while building new systems, classes, progression, and gameplay improvements around it.

## Status

Minezone-Core is currently in active development.

The main priority is Super Craft Bros, with Parkour and Fishing maintained as supporting server activities.
