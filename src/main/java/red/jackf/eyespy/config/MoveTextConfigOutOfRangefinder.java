package red.jackf.eyespy.config;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import net.fabricmc.loader.api.Version;
import red.jackf.jackfredlib.api.config.migration.Migration;
import red.jackf.jackfredlib.api.config.migration.MigrationResult;

import java.util.function.Supplier;

public class MoveTextConfigOutOfRangefinder implements Migration<EyeSpyConfig> {
    @Override
    public MigrationResult migrate(
            JsonObject config,
            Supplier<EyeSpyConfig> defaultSupplier,
            Jankson jankson,
            Version oldVersion,
            Version newVersion) {

        var rangefinder = config.recursiveGet(JsonObject.class, "rangefinder");
        if (rangefinder == null) return MigrationResult.NOT_APPLICABLE;
        var useColours = rangefinder.remove("useColours");
        var textScale = rangefinder.remove("textScale");
        if (useColours != null || textScale != null) {
            var baseText = new JsonObject();
            if (useColours != null) baseText.put("useColours", useColours);
            if (textScale != null) baseText.put("textScale", textScale);
            config.put("text", baseText);
            return MigrationResult.SUCCESS("Moved text settings from rangefinder");
        }

        return MigrationResult.NOT_APPLICABLE;
    }
}
