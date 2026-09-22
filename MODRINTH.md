# Camerao

Camera utilities for Fabric: a detached perspective camera, scroll zoom, an FOV zoom and a freecam. Client side only, so it works on any server.

## Perspective — `Left Alt`

Look around while your body keeps walking, mining or fighting. Hold the key by default, or switch it to toggle in the config. Yaw and pitch limits, camera view, inverted look and cinematic smoothing are there if you want them.

Scrolling to move the camera in and out is on by default: 2 blocks in, 32 blocks out.

![Perspective](https://raw.githubusercontent.com/plinthlol/camerao/26.3/assets/perspective.gif)

## Zoom — `C`

FOV zoom, 300% by default. It shows `Zoom 300%` above the hotbar and hides it as soon as you let go of the key.

![Zoom](https://raw.githubusercontent.com/plinthlol/camerao/26.3/assets/zoom.gif)

## Freecam — `V`

Fly around with WASD and space/shift, scroll to change speed. Your body stays where it was. Collision, keep sneak and exit on damage are in the config.

![Freecam](https://raw.githubusercontent.com/plinthlol/camerao/26.3/assets/freecam.gif)

## Detach camera — `G`

Leaves the camera where you are standing and gives your body back to you, so you can keep playing while the camera stays put.

![Detach camera](https://raw.githubusercontent.com/plinthlol/camerao/26.3/assets/detachcamera.gif)

## Keybinds

Changeable in Options → Controls → Key Binds → Camerao.

| Action | Key |
| --- | --- |
| Perspective | `Left Alt` |
| Zoom | `C` |
| Freecam | `V` |
| Detach camera | `G` |
| Open settings | unbound |

## Settings

Everything is in the config screen, from Mod Menu or the settings keybind. Saved to `config/camerao.json`.

**Perspective**

| Option | Default |
| --- | --- |
| Activation mode | Hold |
| Camera view | Third person back |
| Yaw limit | 360 |
| Pitch limit | 90 |
| Invert look up/down | Off |
| Cinematic camera | Off |
| Scroll camera zoom | On |
| Camera zoom in limit | 2 |
| Camera zoom out limit | 32 |

**Zoom**

| Option | Default |
| --- | --- |
| Default zoom % | 300 |
| Zoom % | On |
| Invert scroll | Off |

**Freecam**

| Option | Default |
| --- | --- |
| Activation mode | Toggle |
| Collision | Off |
| Keep sneak | On |
| Exit on damage | On |

## Install

Needs Fabric Loader 0.19.5+, Fabric API and Cloth Config API (Mod Menu optional). Java 21 for 1.21.11, Java 25 for 26.x. Drop the jar matching your game version into `mods`.

## Screenshots

![Perspective settings](https://raw.githubusercontent.com/plinthlol/camerao/26.3/assets/perspectivesettingspage.png)

![Zoom settings](https://raw.githubusercontent.com/plinthlol/camerao/26.3/assets/zoomsettings.png)

![Freecam settings](https://raw.githubusercontent.com/plinthlol/camerao/26.3/assets/freecamsettingspage.png)

## Notes

- Perspective, freecam and detach stop each other.
- While in freecam your body ignores knockback. With exit on damage on, getting hit puts you back in your body.
- In freecam your body stands still, so if you were sneaking when you started it, turn on keep sneak to stay crouched.

GPL-3.0
