package com.collegeroom.allocationsystem.controller;

import com.collegeroom.allocationsystem.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SwitchRoleController {

    private final CustomUserDetailsService userDetailsService;

    public SwitchRoleController(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/switch-role")
    public String switchRole(
            @RequestParam(defaultValue = "STUDENT") String role,
            HttpServletRequest request) {

        String targetEmail;
        String redirectUrl;

        switch (role.toUpperCase()) {
            case "ADMIN":
                targetEmail = "admin@test.com";
                redirectUrl = "redirect:/admin/dashboard";
                break;
            case "HOD":
                targetEmail = "hod@test.com";
                redirectUrl = "redirect:/hod/dashboard";
                break;
            case "STUDENT":
            default:
                targetEmail = "student@test.com";
                redirectUrl = "redirect:/student/dashboard";
                break;
        }

        // Authenticate directly in memory
        UserDetails userDetails = userDetailsService.loadUserByUsername(targetEmail);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return redirectUrl;
    }
}
