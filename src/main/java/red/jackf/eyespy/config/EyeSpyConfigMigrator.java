package red.jackf.eyespy.config;

import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.config.migration.MigratorBuilder;

public interface EyeSpyConfigMigrator {
    static MigratorBuilder<EyeSpyConfig> get() {
        return MigratorBuilder.<EyeSpyConfig>forMod(EyeSpy.MODID)
                .addMigration("1.3.0", new MoveTextConfigOutOfRangefinder())
                .addMigration("1.3.4", new MoveSpyglassRequirement());
    }
}
