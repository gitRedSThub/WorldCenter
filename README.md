# WorldCenter

Mobs drift toward a point you pick. By default that's X 0, Z 0.

Paper 1.21.11, Java 21.

## How it works

Every eligible mob gets one extra AI goal, slotted in underneath the goals that
matter. Minecraft's own priority system handles the rest like combat, panic, fleeing,
avoiding danger and following all sit above it and take movement back the moment they
want it. No vanilla goal is deleted or swapped out. Navigation is never held hostage.

`force-ai` puts that same goal at the top of the list instead. That's the entire
difference between the two modes. It's still walking either way.

## Install

Drop the jar in `plugins/` and restart. The config shows up at
`plugins/WorldCenter/config.yml`.

## Commands

| Command | |
| --- | --- |
| `/worldcenter set <setting> <value>` | Change a setting |
| `/worldcenter toggle traveling` | Turn the system on or off |
| `/worldcenter blacklist add <entity>` | Stop an entity type from travelling |
| `/worldcenter blacklist remove <entity>` | Let it travel again |
| `/worldcenter blacklist get` | Show the blacklist |
| `/worldcenter info` | Settings plus what's happening right now |
| `/worldcenter reload` | Re-read config.yml |
| `/worldcenter help` | The list above |

Per world:

```
/worldcenter set worlds <world> enabled <true|false>
/worldcenter set worlds <world> destination x <value>
/worldcenter set worlds <world> destination z <value>
```

Everything tab completes, and only values that'd actually be accepted get suggested.

`traveling.enabled` is a main switch, so it lives on `toggle` and you can't reach it
through `set`.

## Settings

| Setting | Default | Range |
| --- | --- | --- |
| `traveling.enabled` | true | |
| `traveling.force-ai.enabled` | false | |
| `traveling.stop-distance-chunks` | 2.00 | 0.25 – 32.00 |
| `traveling.minimum-y` | 63.00 | -2048.00 – 2048.00 |
| `traveling.ignore-teamed` | true | |
| `traveling.ignore-owned` | true | |
| `traveling.processing.interval` | 1.00 | 0.25 – 60.00 |
| `traveling.processing.random-delay` | true | |
| `traveling.pathfinding.update-interval` | 2.00 | 0.50 – 60.00 |
| `traveling.pathfinding.retry-delay` | 5.00 | 0.50 – 300.00 |
| `worlds.<world>.destination.x` / `.z` | 0.00 | ±30000000.00 |

## What gets skipped

- Anything on the blacklist. Ships with Villager, Bee, Iron Golem, Shulker, Cat.
- Anything that isn't an entity like players, armor stands, items, arrows.
- Mobs below `minimum-y`.
- Mobs already inside `stop-distance-chunks` of the destination.
- Pets, while `ignore-owned` is on.
- Mobs on a scoreboard team, while `ignore-teamed` is on.
- Mobs in a world that isn't in `worlds`. New worlds stay untouched until you add
  them.
- Bats, ghasts, happy ghasts, phantoms, the ender dragon. They fly with their own
  movement code and throw away any path you hand them, so there's no point.