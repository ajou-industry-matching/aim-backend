package ajou.aim_be.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CrawlerFilter extends OncePerRequestFilter {

    @Value("${crawler.api-key}")
    private String expectedApiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(
                request.getMethod().equals("POST")
                        && request.getRequestURI().equals("/api/crawled-projects/crawler")
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String actualApiKey =
                request.getHeader("X-Crawler-Key");

        if (actualApiKey == null ||
                !MessageDigest.isEqual(
                        expectedApiKey.getBytes(StandardCharsets.UTF_8),
                        actualApiKey.getBytes(StandardCharsets.UTF_8)
                )) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}