/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.urbanbites;

/**
 *
 * @author erickvasquezgongora
 */
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Lazy;
import com.urbanbites.domain.Ruta;
import com.urbanbites.service.RutaService;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ProjectConfig implements WebMvcConfigurer {

    /* Los siguiente métodos son para implementar el tema de seguridad dentro del proyecto */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("landing/index");
        registry.addViewController("/ejemplo2").setViewName("ejemplo2");
        registry.addViewController("/multimedia").setViewName("multimedia");
        registry.addViewController("/iframes").setViewName("iframes");
        registry.addViewController("/login").setViewName("auth/login");
        registry.addViewController("/registro").setViewName("auth/registro");
        registry.addViewController("/registro/nuevo").setViewName("/registro/nuevo");
    }

    /* El siguiente método se utilizar para publicar en la nube, independientemente  */
    @Bean
    public SpringResourceTemplateResolver templateResolver_0() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setOrder(0);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Bean
    public LocaleResolver localeResolver() {
        var slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.getDefault());
        slr.setLocaleAttributeName("session.current.locale");
        slr.setTimeZoneAttributeName("session.current.timezone");
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        var lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(localeChangeInterceptor());
    }

    //Bean para poder acceder a los messages.properties en código...
    @Bean("messageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Lazy RutaService rutaService) throws Exception {
        var rutas = rutaService.getRutas();
        
        var rutasOrdenadas = rutas.stream()
            .sorted((r1, r2) -> {
                String ruta1 = r1.getRuta();
                String ruta2 = r2.getRuta();
                
                boolean tieneWildcard1 = ruta1.contains("*");
                boolean tieneWildcard2 = ruta2.contains("*");
                
                if (tieneWildcard1 && !tieneWildcard2) {
                    return 1;
                }
                if (!tieneWildcard1 && tieneWildcard2) {
                    return -1;
                }
                
                if (tieneWildcard1 && tieneWildcard2) {
                    long wildcards1 = ruta1.chars().filter(ch -> ch == '*').count();
                    long wildcards2 = ruta2.chars().filter(ch -> ch == '*').count();
                    if (wildcards1 != wildcards2) {
                        return Long.compare(wildcards1, wildcards2);
                    }
                }
                
                return Integer.compare(ruta2.length(), ruta1.length());
            })
            .collect(java.util.stream.Collectors.toList());
        
        http.authorizeHttpRequests(requests -> {
            // Agrupar rutas por patrón para manejar múltiples roles
            java.util.Map<String, java.util.Set<String>> rutasPorPatron = new java.util.HashMap<>();
            java.util.Set<String> rutasPublicas = new java.util.HashSet<>();
            
            for (Ruta ruta : rutasOrdenadas) {
                if (!ruta.getRequiereRol()) {
                    rutasPublicas.add(ruta.getRuta());
                } else if (ruta.getRol() != null) {
                    rutasPorPatron.computeIfAbsent(ruta.getRuta(), k -> new java.util.HashSet<>())
                        .add(ruta.getRol().getNombre());
                }
            }
            
            // Aplicar rutas públicas primero
            for (String rutaPublica : rutasPublicas) {
                requests.requestMatchers(rutaPublica).permitAll();
            }
            
            // Aplicar rutas con roles (agrupadas)
            for (java.util.Map.Entry<String, java.util.Set<String>> entry : rutasPorPatron.entrySet()) {
                String ruta = entry.getKey();
                java.util.Set<String> roles = entry.getValue();
                
                if (roles.size() == 1) {
                    // Un solo rol, usar hasRole
                    requests.requestMatchers(ruta).hasRole(roles.iterator().next());
                } else {
                    // Múltiples roles, usar hasAnyRole
                    requests.requestMatchers(ruta).hasAnyRole(roles.toArray(new String[0]));
                }
            }
            
            // Rutas que requieren autenticación pero sin rol específico
            for (Ruta ruta : rutasOrdenadas) {
                if (ruta.getRequiereRol() && ruta.getRol() == null) {
                    requests.requestMatchers(ruta.getRuta()).authenticated();
                }
            }
            
            requests.anyRequest().authenticated();
        });

        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/login/success", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/errores/403")
        );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configurerGlobal(AuthenticationManagerBuilder build, 
            @Lazy PasswordEncoder passwordEncoder, 
            @Lazy UserDetailsService userDetailsService) throws Exception {
        build.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

}
