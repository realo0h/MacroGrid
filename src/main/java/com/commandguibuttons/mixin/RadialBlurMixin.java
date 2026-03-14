package com.commandguibuttons.mixin;

import com.commandguibuttons.gui.RadialOverlayScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class RadialBlurMixin {

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void noBlurForRadial(CallbackInfo ci) {
        if ((Object) this instanceof RadialOverlayScreen) {
            ci.cancel();
        }
    }
}