package com.kalimero2.team.claims.squaremap.data;

import java.util.ArrayList;
import java.util.List;

public final class Group {
    private final List<Claim> claims = new ArrayList<>();
    private final String owner;
    private final boolean is_player;
    private final boolean has_team_flag;

    public Group(Claim claim, String owner) {
        add(claim);
        this.owner = owner;
        this.is_player = claim.is_player();
        this.has_team_flag = claim.has_team_flag();
    }

    public boolean isTouching(Claim claim) {
        for (Claim toChk : claims) {
            if (toChk.isTouching(claim)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTouching(Group group) {
        for (Claim claim : group.claims()) {
            if (isTouching(claim)) {
                return true;
            }
        }
        return false;
    }

    public void add(Claim claim) {
        claims.add(claim);
    }

    public void add(Group group) {
        claims.addAll(group.claims());
    }

    public List<Claim> claims() {
        return claims;
    }

    public String owner() {
        return owner;
    }

    public boolean hasTeamFlag() {
        return has_team_flag;
    }

    public boolean isPlayer() {
        return is_player;
    }

    public String id() {
        if (claims.size() > 0) {
            Claim claim = claims.get(0);
            return claim.x() + "_" + claim.z();
        } else {
            return "NaN_NaN";
        }
    }
}
