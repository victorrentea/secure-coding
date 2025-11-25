package victor.training.spring.security.config.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class BackChannelLoggingConfig {

	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeAccessTokenResponseClient() {
    var accessTokenResponseClient = new RestClientAuthorizationCodeTokenResponseClient();
		accessTokenResponseClient.setRestClient(backChannelRestClient());
		return accessTokenResponseClient;
	}

	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenAccessTokenResponseClient() {
    var accessTokenResponseClient = new RestClientRefreshTokenTokenResponseClient();
		accessTokenResponseClient.setRestClient(backChannelRestClient());
		return accessTokenResponseClient;
	}

	@Bean
	public OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsAccessTokenResponseClient() {
    var accessTokenResponseClient = new RestClientClientCredentialsTokenResponseClient();
		accessTokenResponseClient.setRestClient(backChannelRestClient());
		return accessTokenResponseClient;
	}

	@Bean
	public OAuth2AccessTokenResponseClient<JwtBearerGrantRequest> jwtBearerAccessTokenResponseClient() {
    var accessTokenResponseClient = new RestClientJwtBearerTokenResponseClient();
		accessTokenResponseClient.setRestClient(backChannelRestClient());
		return accessTokenResponseClient;
	}

	@Bean
	public OAuth2AccessTokenResponseClient<TokenExchangeGrantRequest> tokenExchangeAccessTokenResponseClient() {
    var accessTokenResponseClient = new RestClientTokenExchangeTokenResponseClient();
		accessTokenResponseClient.setRestClient(backChannelRestClient());
		return accessTokenResponseClient;
	}

	@Bean
	public RestClient backChannelRestClient() {
    return RestClient.builder()
        .requestFactory(new HttpComponentsClientHttpRequestFactory()) // +added
        // = below, copied from org.springframework.security.oauth2.client.endpoint.AbstractRestClientOAuth2AccessTokenResponseClient.restClient
        .messageConverters((messageConverters) -> {
          messageConverters.clear();
          messageConverters.add(new FormHttpMessageConverter());
          messageConverters.add(new OAuth2AccessTokenResponseHttpMessageConverter());
        })
        .defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
        .build();
	}

}