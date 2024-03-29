package com.fjern.app.run.configs;

import com.fjern.app.security.MyPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Profile("oauth")
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyPasswordEncoder myPasswordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value(value = "${client.live-test-client-id}")
    private String liveTestClientId;

    @Value(value = "${client.live-test-client-secret}")
    private String liveTestClientSecret;

    @Value(value = "${client.open-api-id}")
    private String openApiClientId;

    @Value(value = "${client.open-api-secret}")
    private String openApiClientSecret;

    public AuthorizationServerConfiguration() {
        super();
    }

    @Bean
    public JwtAccessTokenConverter jwtTokenConverter() {
        final JwtAccessTokenConverter accessTokenConverter= new JwtAccessTokenConverter();
        accessTokenConverter.setSigningKey("erwerwefwefwevwevwerwetqvdfg");
        return accessTokenConverter;
    }

    @Bean
    public TokenStore tokenStore () {return new JwtTokenStore(jwtTokenConverter());
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        final DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        return tokenServices;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpointsConfigurer) {
        endpointsConfigurer.tokenStore(tokenStore())
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .accessTokenConverter(jwtTokenConverter());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception{
        clients.inMemory()
                .withClient(liveTestClientId)
                .secret(myPasswordEncoder.getPasswordEncoder().encode(liveTestClientSecret))
                .authorizedGrantTypes("password", "refresh_token")
                .refreshTokenValiditySeconds(3600 * 24)
                .scopes("2ch-web-app")
                .autoApprove("2ch-web-app")
                .accessTokenValiditySeconds(3600)
                .and()
                .withClient(openApiClientId)
                .secret(myPasswordEncoder.getPasswordEncoder().encode(openApiClientSecret))
                .authorizedGrantTypes("client_credentials")
                .refreshTokenValiditySeconds(3600 * 24)
                .scopes("2ch-web-app")
                .autoApprove("2ch-web-app")
                .accessTokenValiditySeconds(3600)
                ;

    }

    @Override
    public void configure(final AuthorizationServerSecurityConfigurer securityConfigurer) throws Exception {
        securityConfigurer.checkTokenAccess("permitAll()");
        super.configure(securityConfigurer);
    }
}
