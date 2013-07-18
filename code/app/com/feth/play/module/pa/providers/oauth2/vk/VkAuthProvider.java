package com.feth.play.module.pa.providers.oauth2.vk;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import org.codehaus.jackson.JsonNode;
import play.Application;
import play.Logger;
import play.libs.WS;

/**
 * @author Denis Borisenko
 */
public class VkAuthProvider extends OAuth2AuthProvider<VkAuthUser, VkAuthInfo> {

    static final String PROVIDER_KEY = "vk";

    private static final String USER_INFO_URL_SETTING_KEY = "userInfoUrl";
    private static final String USER_INFO_FIELDS_SETTING_KEY = "userInfoFields";

    private static final String FIELDS = "fields";
    private static final String ERROR_MESSAGE = "error_description";

    public VkAuthProvider(final Application app) {
        super(app);
    }

    @Override
    protected VkAuthUser transform(VkAuthInfo info, final String state)
            throws AuthException {

        final String url = getConfiguration().getString(
                USER_INFO_URL_SETTING_KEY);
        final String fields = getConfiguration().getString(
                USER_INFO_FIELDS_SETTING_KEY);
        final WS.Response r = WS
                .url(url)
                .setQueryParameter(FIELDS, fields)
                .get()
                .get(PlayAuthenticate.TIMEOUT);

        final JsonNode result = r.asJson();
        Logger.debug(result.toString());

        if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
            throw new AuthException(result.get(ERROR_MESSAGE).asText());
        } else {
            Logger.debug(result.toString());
            return new VkAuthUser(result, info, state);
        }
    }

    @Override
    public String getKey() {
        return PROVIDER_KEY;
    }

    @Override
    protected VkAuthInfo buildInfo(final WS.Response r)
            throws AccessTokenException {
        final JsonNode n = r.asJson();
        Logger.debug(n.toString());

        if (n.get(OAuth2AuthProvider.Constants.ERROR) != null) {
            throw new AccessTokenException(n.get(ERROR_MESSAGE).asText());
        } else {
            return new VkAuthInfo(n);
        }
    }
}
