package cn.starlight.fabricproxy;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class ModConfig {
    private Boolean BungeeCord = true;
    private Boolean alwaysOfficialUUID = false;
    private Boolean allowBypassProxy = false;

    private String secret = "";

    public Boolean getBungeeCord() {
        String env = System.getenv("FABRIC_PROXY_BUNGEECORD");
        if (env == null) {
            return BungeeCord;
        } else {
            return env.equalsIgnoreCase("true");
        }
    }

    public Boolean getAlwaysOfficialUUID(){
        String env = System.getenv("FABRIC_PROXY_ALWAYS_OFFICIAL_UUID");
        if(env == null){
            return alwaysOfficialUUID;
        } else {
          return env.equalsIgnoreCase("true");
        }
    }

    public String getSecret() {
        String env = System.getenv("FABRIC_PROXY_SECRET");
        if (env == null) {
            return secret;
        } else {
            return env;
        }
    }

    public Boolean getAllowBypassProxy() {
        String env = System.getenv("FABRIC_PROXY_ALLOW_BYPASS_PROXY");
        if (env == null) {
            return allowBypassProxy;
        } else {
            return Boolean.valueOf(env);
        }
    }
}
