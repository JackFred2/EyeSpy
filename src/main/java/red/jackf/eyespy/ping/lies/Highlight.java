package red.jackf.eyespy.ping.lies;

public sealed interface Highlight permits BlockHighlight, EntityHighlight {
    void fade();

    void refreshLifetime();
}
