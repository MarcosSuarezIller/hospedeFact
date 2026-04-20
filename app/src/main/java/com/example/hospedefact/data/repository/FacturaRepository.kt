package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.Factura
import com.example.hospedefact.data.models.LineaFactura
import com.example.hospedefact.data.models.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para manejar Facturas en Firestore
 * El CORE del proyecto: consolidación de pedidos en facturas
 */
class FacturaRepository(
    private val pedidoRepository: PedidoRepository = PedidoRepository()
) {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("facturas")

    companion object {
        private const val TAG = "FacturaRepository"
        private const val IVA = 0.21  // 21% de IVA
    }

    /**
     * Algoritmo principal para la generación de facturas.
     * Consolida todos los pedidos pendientes de un huésped en una única factura,
     * calculando automáticamente el subtotal, el IVA y el importe total.
     * Al finalizar, marca todos los pedidos procesados como "facturados".
     *
     * @param huespedId ID del huésped para el cual se genera la factura.
     * @param usuarioId ID del usuario (empleado/admin) que emite la factura.
     * @return [Result] con el objeto [Factura] generado y guardado en Firestore.
     */
    suspend fun generarFactura(huespedId: String, usuarioId: String): Result<Factura> = try {
        Log.d(TAG, "Generando factura para huésped: $huespedId")

        // PASO 1: Obtener todos los pedidos pendientes del huésped
        Log.d(TAG, "PASO 1: Obteniendo pedidos pendientes...")
        val pedidosResult = pedidoRepository.obtenerPedidosPorHuesped(huespedId)

        if (pedidosResult.isFailure) {
            Log.e(TAG, "Error al obtener pedidos")
            throw Exception("No se pudieron obtener los pedidos")
        }

        val pedidos = pedidosResult.getOrNull() ?: emptyList()
        Log.d(TAG, "Se encontraron ${pedidos.size} pedidos pendientes")

        // Validar que hay pedidos
        if (pedidos.isEmpty()) {
            throw Exception("No hay pedidos pendientes para este huésped")
        }

        // PASO 2: Crear líneas de factura consolidadas
        Log.d(TAG, "PASO 2: Consolidando items en líneas de factura...")
        val lineas = mutableListOf<LineaFactura>()
        var subtotal = 0.0

        // Iterar sobre cada pedido
        for (pedido in pedidos) {
            Log.d(TAG, "Procesando pedido: ${pedido.id}")

            // Iterar sobre cada item del pedido
            for (item in pedido.items) {
                val subtotalItem = item.cantidad * item.precioUnitario

                Log.d(TAG, "  Item: ${item.nombre}, Cantidad: ${item.cantidad}, Subtotal: €$subtotalItem")

                // Crear línea de factura
                lineas.add(
                    LineaFactura(
                        descripcion = item.nombre,
                        cantidad = item.cantidad,
                        precioUnitario = item.precioUnitario,
                        subtotal = subtotalItem
                    )
                )

                subtotal += subtotalItem
            }
        }

        Log.d(TAG, "Total de líneas creadas: ${lineas.size}")
        Log.d(TAG, "Subtotal: €$subtotal")

        // PASO 3: Calcular IVA
        Log.d(TAG, "PASO 3: Calculando IVA (21%)...")
        val iva = subtotal * IVA
        Log.d(TAG, "IVA: €$iva")

        // PASO 4: Calcular total final
        Log.d(TAG, "PASO 4: Calculando total final...")
        val total = subtotal + iva
        Log.d(TAG, "TOTAL: €$total")

        // PASO 5: Crear objeto Factura
        Log.d(TAG, "PASO 5: Creando objeto Factura...")
        val factura = Factura(
            huespedId = huespedId,
            items = lineas,
            subtotal = subtotal,
            iva = iva,
            total = total,
            generadaPor = usuarioId,
            estado = "emitida"
        )

        // PASO 6: Guardar factura en Firestore
        Log.d(TAG, "PASO 6: Guardando factura en Firestore...")
        val doc = coleccion.document()
        val facturaConId = factura.copy(id = doc.id)
        doc.set(facturaConId).await()

        Log.d(TAG, "Factura guardada con ID: ${doc.id}")

        // PASO 7: Marcar todos los pedidos como "facturados"
        Log.d(TAG, "PASO 7: Marcando pedidos como facturados...")
        for (pedido in pedidos) {
            val pedidoActualizado = pedido.copy(estado = "facturado")
            pedidoRepository.actualizarPedido(pedidoActualizado).getOrThrow()
            Log.d(TAG, "Pedido marcado como facturado: ${pedido.id}")
        }

        Log.d(TAG, "FACTURA GENERADA EXITOSAMENTE")
        Result.success(facturaConId)

    } catch (e: Exception) {
        Log.e(TAG, "Error al generar factura", e)
        Result.failure(e)
    }

    /**
     * Recupera una factura específica de la base de datos utilizando su identificador único.
     * 
     * @param facturaId El ID de la factura a buscar.
     * @return [Result] que contiene el objeto [Factura] si se encuentra, o null.
     */
    suspend fun obtenerFacturaPorId(facturaId: String): Result<Factura?> = try {
        Log.d(TAG, "Obteniendo factura: $facturaId")

        val doc = coleccion.document(facturaId).get().await()
        val factura = doc.toObject(Factura::class.java)

        Log.d(TAG, "Factura encontrada: $facturaId")
        Result.success(factura)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener factura", e)
        Result.failure(e)
    }

    /**
     * Recupera el historial completo de facturas emitidas para un huésped específico.
     * 
     * @param huespedId ID único del huésped.
     * @return [Result] con la lista de facturas asociadas a dicho huésped.
     */
    suspend fun obtenerFacturasPorHuesped(huespedId: String): Result<List<Factura>> = try {
        Log.d(TAG, "Obteniendo facturas del huésped: $huespedId")

        val snapshot = coleccion
            .whereEqualTo("huespedId", huespedId)
            .get()
            .await()

        val facturas = snapshot.toObjects(Factura::class.java)
        Log.d(TAG, "Se obtuvieron ${facturas.size} facturas")
        Result.success(facturas)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener facturas", e)
        Result.failure(e)
    }

    /**
     * Obtiene una lista global con todas las facturas registradas en el sistema.
     * 
     * @return [Result] con la colección completa de objetos [Factura].
     */
    suspend fun obtenerTodasLasFacturas(): Result<List<Factura>> = try {
        Log.d(TAG, "Obteniendo todas las facturas")

        val snapshot = coleccion.get().await()
        val facturas = snapshot.toObjects(Factura::class.java)

        Log.d(TAG, "Total de facturas: ${facturas.size}")
        Result.success(facturas)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener todas las facturas", e)
        Result.failure(e)
    }

    /**
     * Actualiza la información de una factura existente.
     * 
     * @param factura Objeto [Factura] con los datos modificados.
     * @return [Result] indicando el resultado de la operación en Firestore.
     */
    suspend fun actualizarFactura(factura: Factura): Result<Unit> = try {
        Log.d(TAG, "Actualizando factura: ${factura.id}")

        coleccion.document(factura.id).set(factura).await()

        Log.d(TAG, "Factura actualizada")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar factura", e)
        Result.failure(e)
    }

    /**
     * Cambia el estado de una factura a "pagada" de forma directa.
     * 
     * @param facturaId ID único de la factura a actualizar.
     * @return [Result] indicando éxito o el error ocurrido durante el proceso.
     */
    suspend fun marcarComoPagada(facturaId: String): Result<Unit> = try {
        Log.d(TAG, "Marcando factura como pagada: $facturaId")

        val facturaResult = obtenerFacturaPorId(facturaId)

        if (facturaResult.isSuccess) {
            val factura = facturaResult.getOrNull()
            if (factura != null) {
                val facturaActualizada = factura.copy(estado = "pagada")
                coleccion.document(facturaId).set(facturaActualizada).await()

                Log.d(TAG, "Factura marcada como pagada")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Factura no encontrada"))
            }
        } else {
            Result.failure(facturaResult.exceptionOrNull() ?: Exception("Error desconocido"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al marcar como pagada", e)
        Result.failure(e)
    }

    /**
     * Genera una factura integral que incluye tanto los cargos por consumo de restaurante 
     * como el importe por los días de estancia en la habitación.
     *
     * @param huespedId ID del huésped.
     * @param nombreHuesped Nombre completo del huésped para el encabezado de factura.
     * @param precioNoche Precio diario de la habitación ocupada.
     * @param diasEstancia Número de noches de estancia.
     * @param pedidos Lista de pedidos del restaurante a incluir en la factura.
     * @return [Result] con el ID de la nueva factura generada.
     */
    suspend fun generarFacturaConEstancia(
        huespedId: String,
        nombreHuesped: String,
        precioNoche: Double,
        diasEstancia: Int,
        pedidos: List<Pedido>
    ): Result<String> = try {
        Log.d(TAG, "Generando factura con estancia")

        val costoEstancia = diasEstancia * precioNoche

        val itemsEstancia = listOf(
            LineaFactura(
                descripcion = "Estancia ${diasEstancia} noche(s)",
                cantidad = diasEstancia,
                precioUnitario = precioNoche,
                subtotal = costoEstancia
            )
        )

        val subtotalPedidos = pedidos.sumOf { it.total } / 1.21
        val subtotal = subtotalPedidos + costoEstancia
        val iva = subtotal * IVA
        val total = subtotal + iva

        val lineas = mutableListOf<LineaFactura>()

        for (pedido in pedidos) {
            lineas.addAll(pedido.items.map { item ->
                LineaFactura(
                    descripcion = item.nombre,
                    cantidad = item.cantidad,
                    precioUnitario = item.precioUnitario,
                    subtotal = item.cantidad * item.precioUnitario
                )
            })
        }

        lineas.addAll(itemsEstancia)

        val factura = Factura(
            huespedId = huespedId,
            nombreHuesped = nombreHuesped,
            items = lineas,
            subtotal = subtotal,
            iva = iva,
            total = total,
            estado = "emitida",
            incluyelEstancia = true,
            diasEstancia = diasEstancia,
            costoEstancia = costoEstancia
        )

        val doc = coleccion.document()
        coleccion.document(doc.id).set(factura.copy(id = doc.id)).await()

        Log.d(TAG, "Factura con estancia generada")

        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error", e)
        Result.failure(e)
    }
}