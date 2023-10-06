package com.kalimero2.team.claims.api;

public enum PermissionLevel {

    MEMBER(0),
    MODERATOR(1),
    ADMIN(9);

    final int level;

    PermissionLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
