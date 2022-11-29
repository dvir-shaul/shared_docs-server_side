package docSharing.config;

import docSharing.filter.AuthorizationFilter;
//import docSharing.filter.PermissionFilter;
//import docSharing.filter.PermissionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class CustomWebSecurityConfigurerAdapter {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("in security filter chain");
        http.authorizeRequests().antMatchers("*").authenticated().and().httpBasic().and().csrf().disable();
//        http.addFilterAfter(new AuthorizationFilter(), BasicAuthenticationFilter.class);
//        http.addFilterAfter(new PermissionFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }
}