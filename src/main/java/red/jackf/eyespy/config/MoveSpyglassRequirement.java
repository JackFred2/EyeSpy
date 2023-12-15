package red.jackf.eyespy.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import net.fabricmc.loader.api.Version;
import red.jackf.jackfredlib.api.config.migration.Migration;
import red.jackf.jackfredlib.api.config.migration.MigrationResult;

import java.util.function.Supplier;

public class MoveSpyglassRequirement implements Migration<EyeSpyConfig> {
    @Override
    public MigrationResult migrate(
            JsonObject config,
            Supplier<EyeSpyConfig> defaultSupplier,
            Jankson jankson,
            Version oldVersion,
            Version newVersion) {
        var ping = config.recursiveGet(JsonObject.class, "ping");
        if (ping == null) return MigrationResult.NOT_APPLICABLE;

        var requireZoom = ping.remove("requiresZoomIn");
        if (requireZoom instanceof JsonPrimitive primitive && primitive.getValue() instanceof Boolean requireZoomValue) {
            if (requireZoomValue) {
                ping.put("pingRequirement", new JsonPrimitive("zoomed_with_spyglass"));
            } else {
                ping.put("pingRequirement", new JsonPrimitive("holding_spyglass"));
            }
            return MigrationResult.SUCCESS("Changed zoom requirement from bool to enum (%s -> %s)".formatted(requireZoomValue, ping.get("pingRequirement")));
        }

        return MigrationResult.NOT_APPLICABLE;
    }
}
