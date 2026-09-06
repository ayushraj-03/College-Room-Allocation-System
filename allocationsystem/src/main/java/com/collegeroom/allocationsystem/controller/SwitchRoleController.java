package com.collegeroom.allocationsystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SwitchRoleController {

    @GetMapping("/switch-role")
    public String switchRole(
            @RequestParam(defaultValue = "STUDENT") String role,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Invalidate current session and clear security context
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        String normalizedRole;
        switch (role.toUpperCase()) {
            case "ADMIN":
                normalizedRole = "ADMIN";
                break;
            case "HOD":
                normalizedRole = "HOD";
                break;
            case "STUDENT":
            default:
                normalizedRole = "STUDENT";
                break;
        }

        // Redirect to login page requiring password authentication for the chosen role
        return "redirect:/login?role=" + normalizedRole;
    }
}
