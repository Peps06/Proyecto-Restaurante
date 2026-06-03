package com.mycompany.restaurante.Util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mycompany.restaurante.Modelo.OrdenItem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generador de tickets de pago en formato PDF para Saveurs Paris.
 * Permite al cajero guardar el comprobante en la ruta que elija.
 *
 * @author Citlaly
 * @version 1.0
 */
public class TicketPDF {
    // Colores del tema Saveurs Paris
    private static final BaseColor COLOR_AZUL_OSCURO = new BaseColor(26, 30, 46);
    private static final BaseColor COLOR_DORADO = new BaseColor(201, 168, 76);
    private static final BaseColor COLOR_CREMA = new BaseColor(245, 239, 230);
    private static final BaseColor COLOR_GRIS_LINEA = new BaseColor(212, 197, 176);

    // Fuentes
    private static final Font FUENTE_TITULO = new Font(
            Font.FontFamily.HELVETICA, 18, Font.BOLD, COLOR_CREMA);
    private static final Font FUENTE_SUBTITULO = new Font(
            Font.FontFamily.HELVETICA, 11, Font.BOLD, COLOR_DORADO);
    private static final Font FUENTE_NORMAL = new Font(
            Font.FontFamily.HELVETICA, 10, Font.NORMAL, COLOR_AZUL_OSCURO);
    private static final Font FUENTE_NEGRITA = new Font(
            Font.FontFamily.HELVETICA, 10, Font.BOLD, COLOR_AZUL_OSCURO);
    private static final Font FUENTE_TOTAL = new Font(
            Font.FontFamily.HELVETICA, 13, Font.BOLD, COLOR_AZUL_OSCURO);
    private static final Font FUENTE_FOLIO = new Font(
            Font.FontFamily.HELVETICA, 9, Font.ITALIC, COLOR_DORADO);
    private static final Font FUENTE_HEADER_TABLA = new Font(
            Font.FontFamily.HELVETICA, 10, Font.BOLD, COLOR_CREMA);

    /**
     * Genera el PDF del ticket y lo guarda en la ruta indicada.
     *
     * @param rutaArchivo Ruta completa donde se guardará el PDF
     * @param idPago Número de folio del pago
     * @param numMesa Número de mesa cobrada
     * @param idOrden ID de la orden cerrada
     * @param items Lista de productos de la orden
     * @param subtotal Subtotal antes de IVA
     * @param iva Monto del IVA
     * @param total Total final
     * @param metodoPago "EFECTIVO" o "TARJETA"
     * @param efectivo Monto recibido en efectivo (null si fue con tarjeta)
     * @param cambio Cambio devuelto (null si fue con tarjeta)
     * @throws DocumentException Si hay error al construir el PDF
     * @throws IOException Si hay error al escribir el archivo
     */
    public static void generar(
            String rutaArchivo,
            int idPago,
            int numMesa,
            int idOrden,
            List<OrdenItem> items,
            double subtotal,
            double iva,
            double total,
            String metodoPago,
            Double efectivo,
            Double cambio
    ) throws DocumentException, IOException {

        // Tamaño tipo ticket (80mm de ancho ≈ 227 puntos)
        Rectangle tamañoTicket = new Rectangle(227, 600);
        Document document = new Document(tamañoTicket, 10, 10, 10, 10);

        PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
        document.open();

        //  ENCABEZADO 
        agregarEncabezado(document, idPago, numMesa, idOrden);

        //  SEPARADOR 
        agregarSeparador(document);

        //  TABLA DE PRODUCTOS 
        agregarTablaProductos(document, items);

        //  SEPARADOR 
        agregarSeparador(document);

        //  TOTALES 
        agregarTotales(document, subtotal, iva, total,
                       metodoPago, efectivo, cambio);

        //  SEPARADOR 
        agregarSeparador(document);

        //  PIE DE PÁGINA 
        agregarPie(document);

        document.close();
    }

    //  SECCIONES DEL TICKET

    private static void agregarEncabezado(Document doc, int idPago,
            int numMesa, int idOrden) throws DocumentException {

        // Fondo azul oscuro para el encabezado
        PdfPTable headerBg = new PdfPTable(1);
        headerBg.setWidthPercentage(100);

        PdfPCell celdaRestaurante = new PdfPCell();
        celdaRestaurante.setBackgroundColor(COLOR_AZUL_OSCURO);
        celdaRestaurante.setBorder(Rectangle.NO_BORDER);
        celdaRestaurante.setPadding(8);

        // Nombre del restaurante
        Paragraph nombre = new Paragraph("SAVEURS PARIS", FUENTE_TITULO);
        nombre.setAlignment(Element.ALIGN_CENTER);
        celdaRestaurante.addElement(nombre);

        // Subtítulo
        Paragraph subtitulo = new Paragraph("Comprobante de Pago", FUENTE_SUBTITULO);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        celdaRestaurante.addElement(subtitulo);

        headerBg.addCell(celdaRestaurante);
        doc.add(headerBg);

        // Espacio
        doc.add(new Paragraph(" "));

        // Datos del ticket
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss"));

        agregarLineaDato(doc, "Fecha:",    fechaHora);
        agregarLineaDato(doc, "Mesa:",     "Mesa " + numMesa);
        agregarLineaDato(doc, "Orden #:",  String.valueOf(idOrden));
        agregarLineaDato(doc, "Folio:",    "PAY-" + String.format("%04d", idPago));

        doc.add(new Paragraph(" "));
    }

    private static void agregarTablaProductos(Document doc,
            List<OrdenItem> items) throws DocumentException {

        // Encabezado de la tabla
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4f, 1.5f, 2.5f});

        // Cabecera
        agregarCeldaHeader(tabla, "Producto");
        agregarCeldaHeader(tabla, "Cant.");
        agregarCeldaHeader(tabla, "Subtotal");

        // Filas de productos
        for (OrdenItem item : items) {
            agregarCeldaProducto(tabla, item.getProducto());
            agregarCeldaProducto(tabla, String.valueOf(item.getCantidad()));
            agregarCeldaProducto(tabla,
                    "$" + String.format("%.2f", item.getSubtotalItem()));
        }

        doc.add(tabla);
        doc.add(new Paragraph(" "));
    }

    private static void agregarTotales(Document doc,
            double subtotal, double iva, double total,
            String metodoPago, Double efectivo, Double cambio)
            throws DocumentException {

        agregarLineaTotales(doc, "Subtotal:", "$" + String.format("%.2f", subtotal));
        agregarLineaTotales(doc, "IVA (16%):", "$" + String.format("%.2f", iva));

        // Línea total más grande
        doc.add(new Paragraph(" "));
        PdfPTable tablaTot = new PdfPTable(2);
        tablaTot.setWidthPercentage(100);

        PdfPCell celdaLbl = new PdfPCell(new Phrase("TOTAL:", FUENTE_TOTAL));
        celdaLbl.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        celdaLbl.setBorderColor(COLOR_DORADO);
        celdaLbl.setPadding(4);

        PdfPCell celdaVal = new PdfPCell(
                new Phrase("$" + String.format("%.2f", total), FUENTE_TOTAL));
        celdaVal.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        celdaVal.setBorderColor(COLOR_DORADO);
        celdaVal.setPadding(4);
        celdaVal.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tablaTot.addCell(celdaLbl);
        tablaTot.addCell(celdaVal);
        doc.add(tablaTot);
        doc.add(new Paragraph(" "));

        // Método de pago
        agregarLineaTotales(doc, "Pago con:", metodoPago);

        if ("EFECTIVO".equals(metodoPago) && efectivo != null && cambio != null) {
            agregarLineaTotales(doc, "Recibido:", "$" + String.format("%.2f", efectivo));
            agregarLineaTotales(doc, "Cambio:", "$" + String.format("%.2f", cambio));
        }

        doc.add(new Paragraph(" "));
    }

    private static void agregarPie(Document doc) throws DocumentException {
        Paragraph gracias = new Paragraph(
                "¡Gracias por su visita!", FUENTE_SUBTITULO);
        gracias.setAlignment(Element.ALIGN_CENTER);
        doc.add(gracias);

        Paragraph web = new Paragraph(
                "Saveurs Paris · Restaurant", FUENTE_FOLIO);
        web.setAlignment(Element.ALIGN_CENTER);
        doc.add(web);

        String generado = "Generado: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph timestamp = new Paragraph(generado, FUENTE_FOLIO);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        doc.add(timestamp);
    }

    //  HELPERS DE FORMATO

    private static void agregarSeparador(Document doc)
            throws DocumentException {
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(COLOR_GRIS_LINEA);
        ls.setLineWidth(0.5f);
        doc.add(new Chunk(ls));
        doc.add(new Paragraph(" "));
    }

    private static void agregarLineaDato(Document doc,
            String etiqueta, String valor) throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);

        PdfPCell lbl = new PdfPCell(new Phrase(etiqueta, FUENTE_NEGRITA));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setPaddingBottom(2);

        PdfPCell val = new PdfPCell(new Phrase(valor, FUENTE_NORMAL));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        val.setPaddingBottom(2);

        tabla.addCell(lbl);
        tabla.addCell(val);
        doc.add(tabla);
    }

    private static void agregarLineaTotales(Document doc,
            String etiqueta, String valor) throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);

        PdfPCell lbl = new PdfPCell(new Phrase(etiqueta, FUENTE_NEGRITA));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setPaddingBottom(3);

        PdfPCell val = new PdfPCell(new Phrase(valor, FUENTE_NORMAL));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        val.setPaddingBottom(3);

        tabla.addCell(lbl);
        tabla.addCell(val);
        doc.add(tabla);
    }

    private static void agregarCeldaHeader(PdfPTable tabla,
            String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_HEADER_TABLA));
        celda.setBackgroundColor(COLOR_AZUL_OSCURO);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(4);
        tabla.addCell(celda);
    }

    private static void agregarCeldaProducto(PdfPTable tabla,
            String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_NORMAL));
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(COLOR_GRIS_LINEA);
        celda.setBorderWidth(0.3f);
        celda.setPadding(4);
        tabla.addCell(celda);
    }
}
