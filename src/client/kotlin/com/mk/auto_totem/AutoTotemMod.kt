package com.mk.auto_totem

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class AutoTotemMod : ClientModInitializer {
    private var enabled = true
    private lateinit var toggleKey: KeyBinding

    override fun onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.auto_totem.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.auto_totem.main"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val player = client.player ?: return@register

            if (toggleKey.wasPressed()) {
                enabled = !enabled
                player.sendMessage(Text.literal("§6Simple Auto Totem §${if (enabled) "aON" else "cOFF"}"), true)
            }

            if (!enabled) return@register

            // Only try to equip if offhand is empty
            if (player.offHandStack.isEmpty) {
                tryEquipTotem(client, player)
            }
        }
    }

    private fun tryEquipTotem(client: net.minecraft.client.MinecraftClient, player: net.minecraft.client.network.ClientPlayerEntity) {
        // Search entire inventory for totem
        for (slot in 0..35) {
            if (player.inventory.getStack(slot).isOf(Items.TOTEM_OF_UNDYING)) {
                val containerSlot = if (slot < 9) slot + 36 else slot
                moveToOffhand(client, player, containerSlot)
                return
            }
        }
    }

    private fun moveToOffhand(client: net.minecraft.client.MinecraftClient, player: net.minecraft.client.network.ClientPlayerEntity, fromSlot: Int) {
        val screenHandler = player.currentScreenHandler
        val syncId = screenHandler.syncId

        client.interactionManager?.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, player)
        client.interactionManager?.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, player)

        // If swap happened (cursor not empty), put it back
        if (!screenHandler.cursorStack.isEmpty) {
            client.interactionManager?.clickSlot(syncId, fromSlot, 0, SlotActionType.PICKUP, player)
        }
    }
}