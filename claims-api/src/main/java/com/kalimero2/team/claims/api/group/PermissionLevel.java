package com.kalimero2.team.claims.api.group;

public enum PermissionLevel {
    MEMBER(0),
    MODERATOR(1),
    ADMIN(2),
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
}
