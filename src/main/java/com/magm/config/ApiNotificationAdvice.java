package com.magm.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.Locale;

@ControllerAdvice
@Component
public class ApiNotificationAdvice implements ResponseBodyAdvice<Object> {

    private static final String HEADER_NOTIFICATION = "X-Notification";
    private static final String HEADER_EXPOSE = "Access-Control-Expose-Headers";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        HttpMethod method = request.getMethod();
        if (method == null || !isMutatingMethod(method)) {
            return body;
        }

        if (!(response instanceof ServletServerHttpResponse servletResponse)) {
            return body;
        }

        int status = servletResponse.getServletResponse().getStatus();
        if (status < 200 || status >= 300) {
            return body;
        }

        String path = request.getURI().getPath();
        String notification = resolveNotification(path, method);

        response.getHeaders().set(HEADER_NOTIFICATION, notification);
        exposeNotificationHeader(response);
        return body;
    }

    private boolean isMutatingMethod(HttpMethod method) {
        return HttpMethod.POST.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.DELETE.equals(method);
    }

    private void exposeNotificationHeader(ServerHttpResponse response) {
        List<String> current = response.getHeaders().get(HEADER_EXPOSE);
        if (current == null || current.isEmpty()) {
            response.getHeaders().set(HEADER_EXPOSE, HEADER_NOTIFICATION);
            return;
        }

        String merged = String.join(",", current);
        String lowered = merged.toLowerCase(Locale.ROOT);
        if (!lowered.contains(HEADER_NOTIFICATION.toLowerCase(Locale.ROOT))) {
            response.getHeaders().set(HEADER_EXPOSE, merged + "," + HEADER_NOTIFICATION);
        }
    }

    private String resolveNotification(String path, HttpMethod method) {
        if (path.startsWith("/api/productos")) {
            if (HttpMethod.POST.equals(method)) return "Producto guardado correctamente";
            if (HttpMethod.PUT.equals(method)) return "Producto actualizado correctamente";
            if (HttpMethod.DELETE.equals(method)) return "Producto eliminado correctamente";
        }

        if (path.startsWith("/api/ventas") && HttpMethod.POST.equals(method)) {
            return "Venta registrada correctamente";
        }

        if (path.startsWith("/api/compras") && HttpMethod.POST.equals(method)) {
            return "Compra registrada correctamente";
        }

        if (path.startsWith("/api/pedidos")) {
            if (HttpMethod.POST.equals(method)) return "Pedido guardado correctamente";
            if (HttpMethod.PUT.equals(method)) return "Pedido actualizado correctamente";
        }

        if (path.startsWith("/api/proveedores")) {
            if (HttpMethod.POST.equals(method)) return "Proveedor guardado correctamente";
            if (HttpMethod.PUT.equals(method)) return "Proveedor actualizado correctamente";
            if (HttpMethod.DELETE.equals(method)) return "Proveedor eliminado correctamente";
        }

        if (path.startsWith("/api/laboratorios")) {
            if (HttpMethod.POST.equals(method)) return "Laboratorio guardado correctamente";
            if (HttpMethod.PUT.equals(method)) return "Laboratorio actualizado correctamente";
            if (HttpMethod.DELETE.equals(method)) return "Laboratorio eliminado correctamente";
        }

        if (path.startsWith("/api/clinica/examenes-laboratorio")) {
            if (HttpMethod.POST.equals(method)) return "Examen de laboratorio guardado correctamente";
            if (HttpMethod.PUT.equals(method)) return "Examen de laboratorio actualizado correctamente";
        }

        if (path.startsWith("/api/genericos")) {
            if (HttpMethod.POST.equals(method)) return "Generico guardado correctamente";
            if (HttpMethod.PUT.equals(method)) return "Generico actualizado correctamente";
            if (HttpMethod.DELETE.equals(method)) return "Generico eliminado correctamente";
        }

        if (path.startsWith("/api/cargos") && HttpMethod.POST.equals(method)) {
            return "Cargo guardado correctamente";
        }

        if (path.startsWith("/api/incentivos") && HttpMethod.POST.equals(method)) {
            return "Incentivo guardado correctamente";
        }

        if (path.startsWith("/api/caja")) {
            if (HttpMethod.POST.equals(method)) return "Apertura de caja registrada";
            if (HttpMethod.PUT.equals(method)) return "Cierre de caja registrado";
        }

        if (HttpMethod.POST.equals(method)) return "Registro guardado correctamente";
        if (HttpMethod.PUT.equals(method)) return "Registro actualizado correctamente";
        if (HttpMethod.DELETE.equals(method)) return "Registro eliminado correctamente";

        return "Operacion exitosa";
    }
}
