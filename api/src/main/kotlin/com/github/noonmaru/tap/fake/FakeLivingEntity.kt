/*
 * Copyright (c) 2020 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap.fake

import com.github.noonmaru.tap.protocol.EntityPacket
import com.github.noonmaru.tap.protocol.sendServerPacket
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

open class FakeLivingEntity(private val livingEntity: LivingEntity) : FakeEntity(livingEntity) {
    override fun spawnTo(player: Player) {
        val spawnPacket = EntityPacket.spawnMob(livingEntity)
        val metaPacket = EntityPacket.metadata(livingEntity)

        player.sendServerPacket(spawnPacket)
        player.sendServerPacket(metaPacket)

    }
}