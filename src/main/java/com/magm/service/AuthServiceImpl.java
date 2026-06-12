package com.magm.service;

import com.magm.dto.LoginRequest;
import com.magm.dto.LoginResponse;
import com.magm.entity.Usuario;
import com.magm.repository.UsuarioRepository;
import com.magm.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Autenticando usuario: {} en zona {}", request.getNombre(), request.getIdZona());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getNombre(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByNombreAndZona(request.getNombre(), request.getIdZona())
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado para la sucursal: {}", request.getIdZona());
                    return new RuntimeException("Usuario no encontrado para esta sucursal");
                });

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol());
        claims.put("idZona", usuario.getCliente().getZona().getIdZona());
        claims.put("id", usuario.getId());

        String token = jwtUtil.generateToken(usuario, claims);
        log.info("Token generado para usuario: {}", usuario.getNombre());

        return new LoginResponse(
                token,
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getCliente().getZona().getIdZona());
    }
}
