package com.kalimero2.team.claims.api;

public class ClaimsApiHolder {
    private static ClaimsApi api;

    public static boolean setApi(ClaimsApi api){
        if (ClaimsApiHolder.api == null){
            ClaimsApiHolder.api = api;
            return true;
        }
        return false;
    }


    public static ClaimsApi getApi() {
        return api;
    }
}
