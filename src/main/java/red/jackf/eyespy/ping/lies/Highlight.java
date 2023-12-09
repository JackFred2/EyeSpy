package red.jackf.eyespy.ping.lies;

import red.jackf.jackfredlib.api.lying.Lie;

public sealed interface Highlight permits BlockHighlight, EntityHighlight {
    Lie lie();

    void refreshLifetime();
}
