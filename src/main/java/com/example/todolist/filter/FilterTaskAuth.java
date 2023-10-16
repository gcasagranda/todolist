package com.example.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.todolist.User.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FilterTaskAuth extends OncePerRequestFilter{
    
    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                var servletPath = request.getServletPath();
                if (servletPath.startsWith("/api/task/")){
                    var authorization = request.getHeader("Authorization");
                    var authEnconded = authorization.substring("Basic".length()).trim();
                    byte[] authDecoded = Base64.getDecoder().decode(authEnconded);      
                    var authString = new String(authDecoded);
                    String[] credentials = authString.split(":");
                    String username = credentials[0];
                    String password = credentials[1];
                    filterChain.doFilter(request, response);

                    var user = this.userRepository.findByUsername(username);
                    if (user == null){
                        response.sendError(401);
                    }
                    else{
                        var passVerified = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                        if (!passVerified.verified){
                            response.sendError(401);
                        }
                        else{
                            request.setAttribute("idUser", user.getId());
                            filterChain.doFilter(request, response);
                        }
                    };
                }else{
                    filterChain.doFilter(request, response);
                }

            
    }
    
}
