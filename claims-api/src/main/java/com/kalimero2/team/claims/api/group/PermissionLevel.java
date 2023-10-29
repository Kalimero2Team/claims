package com.kalimero2.team.claims.api.group;

public enum PermissionLevel {
    MEMBER(0),
    MODERATOR(1),
    ADMIN(8),
    OWNER(9);


    final int level;

    PermissionLevel(int level) {
        this.level = level;
    }

    public static PermissionLevel fromLevel(int level) {
        for (PermissionLevel permissionLevel : values()) {
            if (permissionLevel.getLevel() == level) {
                return permissionLevel;
            }
        }
        return null;
    }

    public boolean isHigherOrEqual(PermissionLevel permissionLevel) {
        return this.level >= permissionLevel.getLevel();
    }

    public int getLevel() {
        return level;
    }

    public PermissionLevel next() {
        int level = getLevel();

        level++;
        if (level > 9) {
            return this;
        }
        while (fromLevel(level) == null) {
            level++;
        }

        return fromLevel(level);
    }

    public PermissionLevel previous() {
        int level = getLevel();
        level--;
        if (level < 0) {
            return this;
        }
        while (fromLevel(level) == null) {
            level--;
        }
        return fromLevel(level);
    }
}
