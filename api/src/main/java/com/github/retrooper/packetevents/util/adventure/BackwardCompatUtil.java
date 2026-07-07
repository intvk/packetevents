/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2024 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.retrooper.packetevents.util.adventure;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;

// Silnestium fork: originally lived in the vendored :patch:adventure-text-serializer-gson module and
// probed the runtime Adventure version with try/catch feature checks so packetevents could run on any
// Adventure 4.x. The slim build pins Adventure 5, so the probes collapse to constants and the pre-4.x
// fallback branches (which no longer compile against 5) are gone. Public surface kept exactly as the
// api sources consume it. It lives in packetevents' own util.adventure package: shadow drops
// project-local classes under net/kyori/** from the jar, so the original patch-module package is
// not reusable for source kept in :api.
public final class BackwardCompatUtil {

    public static final boolean IS_4_10_0_OR_NEWER = true;
    public static final boolean IS_4_13_0_OR_NEWER = true;
    public static final boolean IS_4_15_0_OR_NEWER = true;
    public static final boolean IS_4_17_0_OR_NEWER = true;
    public static final boolean IS_4_18_0_OR_NEWER = true;
    public static final boolean IS_4_22_0_OR_NEWER = true;
    public static final boolean IS_4_23_0_OR_NEWER = true;
    public static final boolean IS_4_25_0_OR_NEWER = true;
    public static final boolean IS_4_26_0_OR_NEWER = true;

    private BackwardCompatUtil() {
    }

    public interface ShowAchievementToComponent {

        @NotNull
        Component convert(@NotNull String input);

    }

    public static HoverEvent.ShowItem createShowItem(final @NotNull Key item, final @Range(from = 0, to = Integer.MAX_VALUE) int count, final @Nullable BinaryTagHolder nbt) {
        return HoverEvent.ShowItem.showItem(item, count, nbt);
    }

    public static HoverEvent.ShowEntity createShowEntity(final @NotNull Key type, final @NotNull UUID id, final @Nullable Component name) {
        return HoverEvent.ShowEntity.showEntity(type, id, name);
    }

    public static <D, E, DX extends Throwable, EX extends Throwable> @NotNull Codec<D, E, DX, EX> createCodec(final @NotNull Codec.Decoder<D, E, DX> decoder, final @NotNull Codec.Encoder<D, E, EX> encoder) {
        return Codec.codec(decoder, encoder);
    }

    public static ComponentBuilder<?, ?> toBuilder(Component component) {
        return component.toBuilder();
    }

    public static Component build(ComponentBuilder<?, ?> builder) {
        return builder.build();
    }
}
