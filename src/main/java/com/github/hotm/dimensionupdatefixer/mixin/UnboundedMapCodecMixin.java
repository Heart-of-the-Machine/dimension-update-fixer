package com.github.hotm.dimensionupdatefixer.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(UnboundedMapCodec.class)
public abstract class UnboundedMapCodecMixin<K, V> implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
    @Override
    public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
        final ImmutableMap.Builder<K, V> read = ImmutableMap.builder();
        final ImmutableList.Builder<Pair<T, T>> failed = ImmutableList.builder();

        final DataResult<Unit> result = input.entries().reduce(
                DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                (r, pair) -> {
                    final DataResult<K> k = keyCodec().parse(ops, pair.getFirst());
                    final DataResult<V> v = elementCodec().parse(ops, pair.getSecond());

                    final DataResult<Pair<K, V>> entry = k.apply2stable(Pair::of, v);
                    entry.error().ifPresent(e -> failed.add(pair));
                    entry.result().ifPresent(e -> read.put(e.getFirst(), e.getSecond()));
                    return r.apply2stable((u, p) -> u, entry);
                },
                (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
        );

        final Map<K, V> elements = read.build();
        final T errors = ops.createMap(failed.build().stream());

        return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
    }
}
