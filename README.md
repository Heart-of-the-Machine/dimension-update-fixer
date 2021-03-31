# Dimension Update Fixer
A simple mod that unyeets the Nether and End when a world is loaded without a dimension or is upgraded between 
Minecraft versions with dimension mods installed.

This mod does now fix the issue where removing a dimension from a world deletes all other dimensions.

## Jank
The way this mod fixes the dimension-removal issue is by mixing in and overriding the `decode` method in 
`UnboundedMapCodec`. This is somewhat of a janky fix and may break other mods that expect a certain load order for 
DFU classes or that also attempt to fix the dimension-removal issue in the same way.

## Known to Work With
This mod is known to work with:

* [Heart of the Machine](https://heart-of-the-machine.github.io/) 
  [(Github)](https://github.com/Heart-of-the-Machine/heart-of-the-machine)
  [(CurseForge)](https://www.curseforge.com/minecraft/mc-mods/heart-of-the-machine)
* [Applied Energistics 2](https://ae-mod.info/) 
  [(Github)](https://github.com/AppliedEnergistics/Applied-Energistics-2)
  [(CurseForge)](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2)
  (Fabric Build)
