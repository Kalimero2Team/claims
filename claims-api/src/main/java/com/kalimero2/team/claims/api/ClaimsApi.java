package com.kalimero2.team.claims.api;


public interface ClaimsApi {

    static ClaimsApi getApi() {
        return ClaimsApiHolder.getApi();
    }



}
