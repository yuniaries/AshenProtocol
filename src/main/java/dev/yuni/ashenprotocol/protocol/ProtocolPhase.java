package dev.yuni.ashenprotocol.protocol;

public enum ProtocolPhase {

    DORMANT("休眠", "Dormant"),
    RECOVERY("恢复", "Recovery"),
    STABLE("稳定", "Stable"),
    FRACTURE("裂解", "Fracture"),
    CASCADE("级联失稳", "Cascade");

    private final String zh;
    private final String en;

    ProtocolPhase(String zh, String en) {
        this.zh = zh;
        this.en = en;
    }

    public String zh() {
        return zh;
    }

    public String en() {
        return en;
    }

    public static ProtocolPhase from(int integrity, int entropy) {
        if (entropy >= 8000) {
            return CASCADE;
        }

        if (entropy >= 4000) {
            return FRACTURE;
        }

        if (integrity >= 3000) {
            return STABLE;
        }

        if (integrity >= 500) {
            return RECOVERY;
        }

        return DORMANT;
    }
}
