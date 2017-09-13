package it.eng.knowage.slimerjs.wrapper.beans;

/**
 * Used to detect the hosting operating system which is used to determine which binaries should be used
 */
public class OperatingSystem {
    public enum OS {
        WINDOWS,
        MAC,
        UNIX
    }

    private static final OS OS = OperatingSystem.get(System.getProperty("os.name", "").toLowerCase());

    public static OS get() {
        return OS;
    }

    private static OS get(String osName) {
        if (isWindows(osName)) {
            return OS.WINDOWS;
        } else if (isMac(osName)) {
            return OS.MAC;
        } else if (isUnix(osName)) {
            return OS.UNIX;
        }
        return null;
    }

    public static boolean isWindows(String osName) {
        return (osName.contains("win"));
    }

    public static boolean isMac(String osName) {
        return (osName.contains("mac"));
    }

    public static boolean isUnix(String osName) {
        return (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"));
    }
}
