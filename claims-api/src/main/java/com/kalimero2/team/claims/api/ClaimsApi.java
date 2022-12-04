package com.kalimero2.team.claims.api;

import java.util.UUID;

public interface ClaimsApi {

    static ClaimsApi getApi() {
        return ClaimsApiHolder.getApi();
    }

    ClaimsChunk getChunk(int x, int z, UUID world);

}
