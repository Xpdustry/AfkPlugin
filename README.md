# AfkPlugin 

[![Jitpack latest version](https://jitpack.io/v/fr.xpdustry/AfkPlugin.svg)](https://jitpack.io/#fr.xpdustry/AfkPlugin)
[![Build status](https://github.com/Xpdustry/AfkPlugin/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/Xpdustry/AfkPlugin/actions/workflows/build.yml)
[![Mindustry 6.0 | 7.0](https://img.shields.io/badge/Mindustry-6.0%20%7C%207.0-ffd37f)](https://github.com/Anuken/Mindustry/releases)

## Description

Kick your useless teammates...

## Usage

This plugin is very simple:
```
afk <time/status/message> [arg...]
```
- `time` to set the max afk time in minutes.
- `status` to enable/disable the plugin.
- `message` to customize the kick message, you can include the afk time with a `@`, such as `You have been afk for @ minute(s).`

Not using the 2nd arg such as `afk status`, will just return the value.
All of these options are saved in `settings.bin`.
