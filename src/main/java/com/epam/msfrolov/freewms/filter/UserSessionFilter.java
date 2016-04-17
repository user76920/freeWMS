package com.epam.msfrolov.freewms.filter;

import com.epam.msfrolov.freewms.model.User;
import com.epam.msfrolov.freewms.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "1UserSessionFilter", urlPatterns = "/wms/*")
public class UserSessionFilter implements Filter {
    private static final String FORBIDDEN_PAGE = "/WEB-INF/jsp/forbidden.jsp";
    private static final Logger log = LoggerFactory.getLogger(UserSessionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) req, (HttpServletResponse) resp, chain);
    }

    @Override
    public void destroy() {
    }

    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        String pathInfo = req.getPathInfo();
        User user = (User) req.getSession(false).getAttribute("user");
        UserRole role;
        if (user == null)
            role = UserRole.GUEST;
        else
            role = user.getRole();
        log.debug("UserSessionFilter pathInfo: {}", pathInfo);
        log.debug("UserSessionFilter user: {}", user);
        log.debug("UserSessionFilter role: {}", role);
        if (checkPathInfo(pathInfo, role)) {
            log.debug("  Forbidden!");
            forbidden(req, resp, chain);
        }
        chain.doFilter(req, resp);
    }

    private boolean checkPathInfo(String pathInfo, UserRole role) {
        if ("/cabinet".equalsIgnoreCase(pathInfo)) {
            if (getAccessLevel(role) < 5)
                return true;
        }
        return false;
    }

    private int getAccessLevel(UserRole role) {
        if (role.equals(UserRole.ADMIN)) return 5;
        else if (role.equals(UserRole.ACCOUNTANT)) return 4;
        else if (role.equals(UserRole.STOCKMAN)) return 3;
        else if (role.equals(UserRole.USER)) return 2;
        else return 1;
    }

    private void forbidden(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
        int statusCode = 403;
        String requestUri = req.getMethod() + req.getPathInfo();
        req.setAttribute("statusCode", statusCode);
        req.setAttribute("reqUri", requestUri);
        String path = FORBIDDEN_PAGE;
        log.debug("RequestDispatcher");
        log.debug("Status code: {}", statusCode);
        log.debug("URI: {}", requestUri);
        req.getRequestDispatcher(path).forward(req, resp);
    }


}