package com.github.hotm.dimensionupdatefixer;

import com.github.hotm.dimensionupdatefixer.mixin.CompoundListAccessor;
import com.github.hotm.dimensionupdatefixer.mixin.ProductAccessor;
import com.github.hotm.dimensionupdatefixer.mixin.TagAccessor;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.types.templates.Tag;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Function;

public class DimensionUpdateFixer {
    private static boolean schemaInjected = false;
    private static final Logger log = LogManager.getLogger("dimensionupdatefixer");

    private static boolean replaceTypeTemplate(TypeTemplate base, String name,
                                               Function<TypeTemplate, TypeTemplate> replacer) {
        if (base instanceof ProductAccessor) {
            if (replaceTypeTemplate(((ProductAccessor) base).getF(), name, replacer)) {
                return true;
            }
            return replaceTypeTemplate(((ProductAccessor) base).getG(), name, replacer);
        } else if (base instanceof TagAccessor) {
            if (Objects.equals(((TagAccessor) base).getName(), name)) {
                ((TagAccessor) base).setElement(replacer.apply(((TagAccessor) base).getElement()));
                return true;
            }
            return replaceTypeTemplate(((TagAccessor) base).getElement(), name, replacer);
        } else if (base instanceof CompoundListAccessor) {
            return replaceTypeTemplate(((CompoundListAccessor) base).getElement(), name, replacer);
        }

        return false;
    }

    public static void injectChunkGeneratorSchema(TypeTemplate tt, Schema schema) {
        if (!replaceTypeTemplate(tt, "generator", generator -> {
            if (generator instanceof TaggedChoice) {
                schemaInjected = true;

                log.info("Installing chunk generator schema fixes");

                return DSL.or(generator, DSL.remainder());
            } else {
                log.error("Unable to find \"generator\" TypeTemplate of correct type (type: " +
                        generator.getClass() +
                        "). This means that worlds will not migrate between minecraft versions correctly!");
            }
            return generator;
        })) {
            log.error(
                    "Unable to find \"generator\" TypeTemplate. This means that worlds will not migrate between minecraft versions correctly!");
        }
    }

    public static CompoundList.CompoundListType<String, ?> injectChunkGeneratorTypes(
            CompoundList.CompoundListType<String, ?> original, Schema schema) {
        if (!schemaInjected) {
            log.error("HotM schema was not injected, skipping data-fixer type injection...");
            return original;
        }

        log.info("Installing MissingDimensionFix fixes");

        TaggedChoice.TaggedChoiceType<String> field =
                (TaggedChoice.TaggedChoiceType<String>) original.getElement().findFieldType("generator");
        Tag.TagType<Either<Pair<String, ?>, Dynamic<?>>> newField =
                DSL.field("generator", DSL.or(field, DSL.remainderType()));
        CompoundList.CompoundListType<String, ?> adjusted =
                DSL.compoundList(original.getKey(), DSL.and(newField, DSL.remainderType()));

        return adjusted;
    }
}
