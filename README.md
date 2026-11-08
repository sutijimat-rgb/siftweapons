# Sift Weapons (Server-Side) v2.1.0

Freshlol-style 14-level Spear/Mace progression + limited God items.

**Server-only** — clients need nothing.

## Features

- Auto-gives **Level 1 Mace + Level 1 Spear** on first join (fixed)
- Kill enemies while holding a levelled weapon → upgrades it (max Level 14)
- Special **Sift Mace** (limited to **2** total on the server)
- **God Mace** (limited to **1**)
- **God Spear** (limited to **1**)
- Right-click dash on Sift Mace / God Mace
- Passive Strength III + Speed III while holding special maces

## OP Commands (permission level 2+)

```
/sift give <player> levelled <mace|spear> <1-14>
/sift give <player> siftmace
/sift give <player> godmace
/sift give <player> godspear
/sift setlevel <player> <mace|spear> <1-14>
/sift info
/sift help
```

### Examples
```
/sift give Steve godmace
/sift give @p siftmace
/sift give Alex levelled mace 10
/sift setlevel Steve spear 14
/sift info
```

## Limits (server-wide, persisted)

| Item       | Max |
|------------|-----|
| Sift Mace  | 2   |
| God Mace   | 1   |
| God Spear  | 1   |

Once the limit is reached, the `/sift give` command will refuse to create more.

## Building

Targets Minecraft 26.3 + Fabric.

```bash
gradle build
```

Or use the GitHub Action which produces the JAR automatically.
