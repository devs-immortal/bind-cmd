package net.immortaldevs.bindcmd.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/world/ServerWorld$ServerEntityHandler")
public abstract class ServerWorldServerEntityHandlerMixin {
    @Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
    private void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
    }
}
