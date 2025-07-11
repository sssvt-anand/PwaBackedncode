package com.room.app.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;
	private static final List<String> EXCLUDED_PATHS = Arrays.asList("/api/auth/login", "/api/auth/refresh",
			"/api/auth/register");

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		if (EXCLUDED_PATHS.contains(path)) {
			filterChain.doFilter(request, response);
			return;
		}

		final String tokenHeader = request.getHeader("Authorization");

		if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
			try {
				String token = tokenHeader.substring(7);
				String username = jwtUtil.extractUsername(token);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);

					if (jwtUtil.validateToken(token, userDetails)) {
						UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);

						if (jwtUtil.shouldRefreshToken(token)) {
							String newToken = jwtUtil.generateToken(userDetails);
							response.setHeader("New-Token", newToken);
							response.setHeader("Access-Control-Expose-Headers", "New-Token");
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error processing JWT token: {}");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}