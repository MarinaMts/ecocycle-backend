package com.ecocycle.backend.config;

import com.ecocycle.backend.logging.CorrelationIdFilter;
import com.ecocycle.backend.security.CustomUserDetailsService;
import com.ecocycle.backend.security.JwtAuthenticationEntryPoint;
import com.ecocycle.backend.security.JwtAuthenticationFilter;
import com.ecocycle.backend.security.LoginRateLimitFilter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final CorrelationIdFilter correlationIdFilter;

    // Origens permitidas para CORS, separadas por virgula. Relevante apenas para
    // clientes baseados em navegador (ex.: Flutter Web, um futuro painel admin,
    // Swagger UI) - o app Flutter nativo (Android) NAO passa por CORS, pois essa
    // e uma restricao imposta pelo navegador, nao pelo cliente HTTP do app.
    // Configuravel via variavel de ambiente CORS_ALLOWED_ORIGINS em producao.
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
    private String allowedOriginsProperty;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            LoginRateLimitFilter loginRateLimitFilter,
            CorrelationIdFilter correlationIdFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.correlationIdFilter = correlationIdFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt (ver documento de decisoes tecnicas, secao 1.1)
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Os filtros customizados sao @Component, entao o Spring Boot tambem os
    // registraria automaticamente como filtros de servlet, FORA da cadeia de
    // seguranca (e antes dela). Desabilitar esse registro garante que eles rodem
    // apenas dentro do SecurityFilterChain, na ordem definida abaixo.
    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(CorrelationIdFilter filter) {
        return desabilitarRegistroAutomatico(filter);
    }

    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilterRegistration(LoginRateLimitFilter filter) {
        return desabilitarRegistroAutomatico(filter);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        return desabilitarRegistroAutomatico(filter);
    }

    private static <T extends Filter> FilterRegistrationBean<T> desabilitarRegistroAutomatico(T filter) {
        FilterRegistrationBean<T> registro = new FilterRegistrationBean<>(filter);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rotas publicas
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // Documentacao interativa (Swagger UI + OpenAPI) - publica por
                        // design, para o front-end poder consultar o contrato sem precisar
                        // de token so para LER a documentacao.
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        // Todas as demais rotas exigem autenticacao (ex.: /api/v1/users/me)
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                // Ordem explicita: correlationId -> rateLimit -> jwt -> filtro padrao.
                // Garante requestId disponivel mesmo em requisicoes bloqueadas pelo
                // rate limiter, e que a duracao do access log reflita o tempo real.
                // IMPORTANTE: um filtro customizado so pode ser usado como referencia
                // em addFilterBefore/After depois de ele mesmo ja ter sido registrado -
                // por isso a ordem das chamadas abaixo e do mais interno (relativo a um
                // filtro padrao do Spring Security) para o mais externo.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimitFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(correlationIdFilter, LoginRateLimitFilter.class);

        // Necessario apenas para o console do H2 funcionar em iframe durante o desenvolvimento
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Origens explicitas (nunca "*"), lidas de cors.allowed-origins.
        // allowCredentials fica em false: a API usa Bearer token no header
        // Authorization, nunca cookie de sessao - nao ha necessidade real de
        // credentials aqui, e essa combinacao (wildcard + credentials=true) e
        // justamente o padrao inseguro mais comum em CORS.
        List<String> origensPermitidas = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isBlank())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origensPermitidas);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
