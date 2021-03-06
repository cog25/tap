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

package com.github.noonmaru.tap.event;

import org.jetbrains.annotations.NotNull;

/**
 * @author Nemo
 */
@SuppressWarnings("rawtypes")
public class EventEntityProvider {

    private final Class<?> eventClass;

    private final EntityProvider provider;

    EventEntityProvider(@NotNull final Class<?> eventClass, @NotNull final EntityProvider provider) {
        this.eventClass = eventClass;
        this.provider = provider;
    }

    EventEntityProvider(@NotNull final EntityProvider provider) {
        this(EventTools.getGenericEventType(provider.getClass()), provider);
    }

    @NotNull
    public Class<?> getEventClass() {
        return eventClass;
    }

    @NotNull
    public EntityProvider getProvider() {
        return provider;
    }
}
