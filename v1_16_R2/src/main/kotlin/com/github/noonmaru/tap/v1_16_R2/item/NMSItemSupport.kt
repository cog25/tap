/*
 * Copyright (c) 2020 Noonmaru
 *  
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.v1_16_R2.item

import com.github.noonmaru.tap.item.ItemSupport
import net.minecraft.server.v1_16_R2.DamageSource
import net.minecraft.server.v1_16_R2.ItemStack
import net.minecraft.server.v1_16_R2.NBTTagCompound
import net.minecraft.server.v1_16_R2.PlayerInventory
import org.bukkit.craftbukkit.v1_16_R2.CraftEquipmentSlot
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventoryPlayer
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack as BukkitItemStack
import org.bukkit.inventory.PlayerInventory as BukkitPlayerInventory

class NMSItemSupport : ItemSupport {
    override fun saveToJsonString(item: BukkitItemStack): String {
        val nmsItem = CraftItemStack.asNMSCopy(item)
        return nmsItem.save(NBTTagCompound()).toString()
    }

    override fun damageArmor(playerInventory: BukkitPlayerInventory, attackDamage: Double) {
        val nmsInventory = (playerInventory as CraftInventoryPlayer).inventory

        nmsInventory.a(DamageSource.LAVA, attackDamage.toFloat())
    }

    override fun damageSlot(playerInventory: BukkitPlayerInventory, slot: EquipmentSlot, damage: Int) {
        val nmsInventory = (playerInventory as CraftInventoryPlayer).inventory
        val nmsSlot = CraftEquipmentSlot.getNMS(slot)
        val nmsItem = nmsInventory.getItem(slot)

        if (!nmsItem.isEmpty) {
            nmsItem.damage(damage, (playerInventory.holder as CraftPlayer).handle) { player ->
                player.broadcastItemBreak(nmsSlot)
            }
        }
    }

    override fun getItem(armorStand: ArmorStand, slot: EquipmentSlot): BukkitItemStack {
        val handle = (armorStand as CraftArmorStand).handle

        return CraftItemStack.asBukkitCopy(handle.getEquipment(CraftEquipmentSlot.getNMS(slot)))
    }
}

internal fun PlayerInventory.getItem(slot: EquipmentSlot): ItemStack {
    return when (slot) {
        EquipmentSlot.HAND -> itemInHand
        EquipmentSlot.OFF_HAND -> extraSlots[0]
        EquipmentSlot.FEET -> armorContents[0]
        EquipmentSlot.LEGS -> armorContents[1]
        EquipmentSlot.CHEST -> armorContents[2]
        EquipmentSlot.HEAD -> armorContents[3]
    }
}