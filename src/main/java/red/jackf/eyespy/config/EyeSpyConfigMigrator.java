package red.jackf.eyespy.config;

import red.jackf.eyespy.EyeSpy;
import red.jackf.jackfredlib.api.config.migration.MigratorBuilder;

public interface EyeSpyConfigMigrator {
    static MigratorBuilder<EyeSpyConfig> get() {
        return MigratorBuilder.forMod(EyeSpy.MODID);
    }
}
