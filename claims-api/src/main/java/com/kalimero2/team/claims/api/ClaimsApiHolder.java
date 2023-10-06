package com.kalimero2.team.claims.api;

public class ClaimsApiHolder {
    private static ClaimsApi api;

    public static void setApi(ClaimsApi api) {
        if (ClaimsApiHolder.api == null) {
            ClaimsApiHolder.api = api;
        }
    }


    public static ClaimsApi getApi() {
        return api;
    }
}
