package com.github.hotm.dimensionupdatefixer;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.lang.reflect.InvocationTargetException;

public class DimensionUpdateFixerPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        try {
            PreLaunchHacks.hackilyLoadForMixin("com.mojang.datafixers.kinds.App");
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
