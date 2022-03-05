package dev.gegy.mdchat;// Created 2022-04-03T02:46:38

import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

/**
 * Bootstraps a {@link TextStyler} by building parser extensions and {@link NodeStyler node stylers} into a
 * {@link TextStyler.Builder}.
 * <p>
 * Instances of this exposed as a Fabric entrypoint through the {@code markdown-chat} key will be called and run for
 * the global {@link TextStyler#INSTANCE} styler, or any styler created through {@link TextStyler#builderWithGlobal()}.
 *
 * @author KJP12
 * @since 1.3.0
 **/
@FunctionalInterface
public interface StylerBootstrap {
    static StylerBootstrap global() {
        // We're using FabricLoader's entrypoint system here as it isn't possible to
        // determine when we'll be bootstrapped as any mod can load this class at any time.
        List<StylerBootstrap> bootstraps = FabricLoader.getInstance().getEntrypoints("markdown-chat", StylerBootstrap.class);

        return styler -> {
            BuiltinStylers.addTo(styler);
            for (StylerBootstrap bootstrap : bootstraps) {
                bootstrap.bootstrap(styler);
            }
        };
    }

    /**
     * Bootstraps a {@link TextStyler}.
     *
     * @param styler The {@link TextStyler.Builder} to attach styling functionality to
     * @implSpec This method is only called once per each instance of {@link TextStyler.Builder}.
     *         It must not assume that the first invocation is the {@link TextStyler#INSTANCE} text styler,
     *         and that each {@link TextStyler.Builder} given must be extended in the exact same way each
     *         time when being implemented as the {@code markdown-chat} entrypoint in {@code fabric.mod.json}.
     * @implNote It is possible for this method to be called off-thread. Ensure that the
     *         implementation is thread-safe (e.g. concurrent or read-only maps &amp; lists),
     *         and won't cause issues due to 2 calls occurring at once.
     * @see TextStyler#builderWithGlobal()
     */
    void bootstrap(TextStyler.Builder styler);
}
