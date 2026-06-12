package com.magm.controller;

import com.magm.entity.Producto;
import com.magm.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/digemid")
@RequiredArgsConstructor

public class DigemidController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigemidController.class);
    private final ProductoRepository productoRepository;

    @PutMapping("/{idProducto}/codigo")
    public ResponseEntity<?> actualizarCodigoDigemid(@PathVariable Integer idProducto, @RequestBody Map<String, String> body, @RequestParam Integer idZona) {
        String nuevoCodigo = body.get("codigoDigemid");
        var productoOpt = productoRepository.findByIdProductoAndZonaIdZona(idProducto, idZona);
        if (productoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var producto = productoOpt.get();
        producto.setCodigoDigemid(nuevoCodigo);
        productoRepository.save(producto);
        return ResponseEntity.ok().body(Map.of("mensaje", "Código Digemid actualizado correctamente"));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscar(@RequestParam String q, @RequestParam Integer idZona) {
        log.info("Buscando productos con query: '{}' en zona: {}", q, idZona);
        List<Map<String, Object>> data = productoRepository
                .findByNombreProductoContainingIgnoreCaseAndZonaIdZonaAndEstado(q, idZona, 1)
                .stream().map(this::toRow).collect(Collectors.toList());
        log.info("Resultados encontrados: {}", data.size());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<Map<String, Object>> sincronizar(@RequestParam Integer idZona) {
        log.info("Sincronizando productos para zona: {}", idZona);
        long total = productoRepository.findByZonaIdZonaAndEstadoOrderByNombreProductoAsc(idZona, 1).size();
        Map<String, Object> payload = new HashMap<>();
        payload.put("idZona", idZona);
        payload.put("totalRegistros", total);
        payload.put("sincronizadoEn", LocalDateTime.now().toString());
        payload.put("estado", "OK");
        payload.put("mensaje", "Sincronizacion local completada (modo desarrollo)");
        log.info("Sincronización completada para zona: {} con {} registros", idZona, total);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/export/csv-zip")
    public ResponseEntity<InputStreamResource> exportarCsvZip(@RequestParam Integer idZona) throws IOException {
        log.info("Exportando productos a CSV-ZIP para zona: {}", idZona);
        // Solo productos con código Digemid válido y stock > 0

        var productos = productoRepository.findByZonaIdZonaAndEstadoOrderByNombreProductoAsc(idZona, 1)
            .stream()
            .filter(p -> p.getCodigoDigemid() != null && !p.getCodigoDigemid().isEmpty() &&
                        ((p.getUnidades() != null && p.getUnidades() > 0) || (p.getFraccion() != null && p.getFraccion() > 0)))
            .toList();
        String codEstab = productos.isEmpty() ? "" : (productos.get(0).getZona() != null ? productos.get(0).getZona().getRuc() : "");
        File tempCsv = File.createTempFile("digemid_", ".csv");
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempCsv))) {
            writer.println("CodEstab,CodProd,Precio 1,Precio 2");
            for (var p : productos) {
                String codProd = p.getCodigoDigemid();
                String precio1 = p.getPrecio() != null ? p.getPrecio().toString() : "";
                String precio2 = (p.getPrecio() != null && p.getNroFraccion() != null && p.getNroFraccion() > 0) ?
                        p.getPrecio().divide(new java.math.BigDecimal(p.getNroFraccion()), 2, java.math.RoundingMode.HALF_UP).toString() : "";
                writer.printf("%s,%s,%s,%s\n", codEstab, codProd, precio1, precio2);
            }
        }
        File tempZip = File.createTempFile("digemid_", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip));
             FileInputStream fis = new FileInputStream(tempCsv)) {
            ZipEntry entry = new ZipEntry(tempCsv.getName());
            zos.putNextEntry(entry);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
        log.info("Archivo ZIP generado para zona: {} con {} productos", idZona, productos.size());
        InputStreamResource resource = new InputStreamResource(new FileInputStream(tempZip));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=digemid.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(tempZip.length())
                .body(resource);
    }

    private Map<String, Object> toRow(Producto p) {
        Map<String, Object> row = new HashMap<>();
        row.put("idProducto", p.getIdProducto());
        row.put("nombreProducto", p.getNombreProducto());
        row.put("presentacion", p.getPresentacion());
        row.put("laboratorio", p.getLaboratorio() != null ? p.getLaboratorio().getNombreLaboratorio() : null);
        row.put("codigoDigemid", p.getCodigoDigemid());
        return row;
    }
}
