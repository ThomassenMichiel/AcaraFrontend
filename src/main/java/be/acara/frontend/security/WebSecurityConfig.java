package be.acara.frontend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService userDetailsService;
    
    private static final String ROLE_ADMIN = "ADMIN";
    
    @Autowired
    private TokenLogoutHandler tokenLogoutHandler;
    
    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
    
    
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login").not().authenticated()
                .antMatchers("/users/registration").not().authenticated()
                .antMatchers("/events/search/**").permitAll()
                .antMatchers("/events/delete/{\\d+}").hasRole(ROLE_ADMIN)
                .antMatchers("/events/new").hasRole(ROLE_ADMIN)
                .antMatchers("/events/{\\d+}").hasRole(ROLE_ADMIN)
                .antMatchers("/users/detail/{\\d+}").hasRole(ROLE_ADMIN)
                .antMatchers("/users/{\\d+}").hasRole(ROLE_ADMIN)
                .and()
                .formLogin()
                .defaultSuccessUrl("/events")
                .loginProcessingUrl("/login")
                .and()
                .logout()
                .logoutSuccessUrl("/events")
                .logoutSuccessHandler(tokenLogoutHandler)
                .deleteCookies("JSESSIONID");
    
        // H2 webconsole stuff, don't add stuff to this
        http.authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .and()
                .csrf().disable()
                .headers().frameOptions().disable();
    }
}
