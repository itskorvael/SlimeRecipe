# SlimeRecipe

A small Paper plugin for servers where slimes no longer spawn. Slime balls are crafted instead.

Built for Paper 26.1.2 (`api-version: 26.1`, Java 25).

## The recipe: Alginate Gel

Shapeless craft:

| Ingredient | Why |
|---|---|
| 2x Dried Kelp | alginate, the seaweed gelling agent |
| 1x Honey Bottle | sticky base (returns the glass bottle) |
| 1x Bone Meal | calcium, which sets the gel into a ball |
| 1x Lime Dye | colour |

Result: **2 Slime Balls**.

Everything is renewable (bees, kelp, bone meal). A slime block works out to about 4.5 honey bottles, so slime stays scarce.

## Slime spawns

Slime mobs are cancelled when they spawn for any reason listed in `spawns.blocked-reasons`
(default: `NATURAL`, `CHUNK_GEN`, `SPAWNER`). Spawn eggs, `/summon` and slimes splitting when they die still work.

## Recipe book

With `recipe.unlock: ingredient` (default) the recipe appears the first time a player holds a honey bottle or dried
kelp. Set `recipe.unlock: join` to unlock it for everyone on login, which removes the discovery step.

## Config

`plugins/SlimeRecipe/config.yml`:

```yaml
recipe:
  enabled: true
  dried-kelp: 2     # 1-6
  yield: 2          # slime balls per craft, 1-64
  unlock: ingredient  # ingredient | join
spawns:
  blocked-reasons: [NATURAL, CHUNK_GEN, SPAWNER]
```

## Commands

- `/slimerecipe reload` (permission `slimerecipe.admin`, ops by default): re-reads the config and re-registers the recipe.
  Players who are already online do not get the recipe unlocked by a reload.

## Building

```bash
./gradlew build
```

The jar is written to `build/libs/SlimeRecipe-1.0.0.jar`. To target another Paper line:

```bash
./gradlew build -PpaperApi=26.2.build.135-stable -PapiVersion=26.2
```

Drop the jar into `plugins/` and restart. A new plugin jar only loads on a restart.
