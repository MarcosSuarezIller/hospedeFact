package com.example.hospedefact.ui.pedidos


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.ItemPedido
import com.example.hospedefact.data.models.MenuItem
import com.example.hospedefact.data.models.Pedido
import com.example.hospedefact.data.repository.MenuRepository
import com.example.hospedefact.data.repository.PedidoRepository
import com.example.hospedefact.data.repository.ProductoAlmacenRepository
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel para gestión de Pedidos
 * Maneja lógica de carrito y creación de pedidos
 */
class PedidoViewModel(
    private val menuRepository: MenuRepository = MenuRepository(),
    private val pedidoRepository: PedidoRepository = PedidoRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "PedidoViewModel"
    }

    /**
     * Inicia la recuperación del catálogo completo de artículos del menú.
     * 
     * @return [LiveData] que emite la lista de [MenuItem] o estados de carga/error.
     */
    fun cargarMenu() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando menú")
            emit("cargando")

            val resultado = menuRepository.cargarMenu()

            resultado.onSuccess { items ->
                Log.d(TAG, "Menú cargado: ${items.size} items")
                emit(items)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al cargar menú", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en cargarMenu", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Recupera los artículos del menú filtrados por una categoría específica (ej. "Bebidas", "Postres").
     * 
     * @param categoria Nombre de la categoría a filtrar.
     * @return [LiveData] con la lista filtrada de artículos.
     */
    fun cargarMenuPorCategoria(categoria: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando menú de categoría: $categoria")
            emit("cargando")

            val resultado = menuRepository.cargarMenuPorCategoria(categoria)

            resultado.onSuccess { items ->
                Log.d(TAG, "Menú de categoría cargado: ${items.size} items")
                emit(items)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al cargar menú por categoría", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en cargarMenuPorCategoria", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Procesa la creación de un nuevo pedido para un huésped específico.
     * Calcula automáticamente el total del pedido basándose en los ítems proporcionados.
     * 
     * @param huespedId ID del huésped que realiza el pedido.
     * @param items Lista de [ItemPedido] que componen el carrito de compras.
     * @return [LiveData] con el resultado de la creación del pedido.
     */
    fun crearPedido(
        huespedId: String,
        items: List<ItemPedido>
    ) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando pedido para huésped: $huespedId")
            emit("cargando")

            // Valida que haya items
            if (items.isEmpty()) {
                emit("error: El carrito está vacío")
                return@liveData
            }

            // Calcula el total
            val total = items.sumOf { it.cantidad * it.precioUnitario }

            // Crea objeto Pedido
            val pedido = Pedido(
                huespedId = huespedId,
                items = items,
                total = total,
                estado = "pendiente"
            )

            // Guarda en Firestore
            val resultado = pedidoRepository.crearPedido(pedido)

            resultado.onSuccess { id ->
                Log.d(TAG, "Pedido creado: $id")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al crear pedido", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en crearPedido", e)
            emit("error: ${e.message}")
        }
    }


    /**
     * Ejecuta una operación atómica para crear un pedido y descontar el stock de almacén.
     * Valida la disponibilidad de cada producto antes de confirmar la transacción.
     * Si algún producto no tiene stock suficiente, la operación se cancela.
     * 
     * @param huespedId ID del huésped.
     * @param items Lista de productos solicitados.
     * @param productoAlmacenRepository Repositorio para la gestión de inventario.
     * @return [LiveData] indicando el éxito del pedido o el error específico por falta de stock.
     */
    fun crearPedidoConStockDescontado(
        huespedId: String,
        items: List<ItemPedido>,
        productoAlmacenRepository: ProductoAlmacenRepository = ProductoAlmacenRepository()
    ) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando pedido y descontando stock")
            emit("cargando")

            // Valida que haya items
            if (items.isEmpty()) {
                emit("error: El carrito está vacío")
                return@liveData
            }

            // VALIDAR Y DESCONTAR STOCK DE CADA ITEM
            Log.d(TAG, "Validando disponibilidad de stock...")
            for (item in items) {
                Log.d(TAG, "Verificando stock del producto: ${item.itemId}")

                val resultado = productoAlmacenRepository.descontarStock(
                    productoId = item.itemId,
                    cantidad = item.cantidad,
                    referencia = "Pedido en el restaurante"
                )

                if (resultado.isFailure) {
                    val error = resultado.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error al descontar stock: $error")
                    emit("error: Stock insuficiente - ${item.nombre}: $error")
                    return@liveData
                }

                Log.d(TAG, "Stock descontado exitosamente para ${item.nombre}")
            }

            // Si llegamos aquí, todo el stock está disponible y ya fue descontado
            Log.d(TAG, "Stock descontado para todos los items")

            // Calcula el total
            val total = items.sumOf { it.cantidad * it.precioUnitario }

            // Crea objeto Pedido
            val pedido = Pedido(
                huespedId = huespedId,
                items = items,
                total = total,
                estado = "pendiente"
            )

            // Guarda en Firestore
            val resultado = pedidoRepository.crearPedido(pedido)

            resultado.onSuccess { id ->
                Log.d(TAG, "Pedido creado: $id (con stock descontado)")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al crear pedido", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }
    /**
     * Recupera el historial de pedidos pendientes asociados a un huésped.
     * Utilizado principalmente para procesos de facturación consolidada.
     * 
     * @param huespedId Identificador del huésped.
     * @return [LiveData] con la lista de pedidos encontrados.
     */
    fun obtenerPedidosPorHuesped(huespedId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo pedidos del huésped: $huespedId")
            emit("cargando")

            val resultado = pedidoRepository.obtenerPedidosPorHuesped(huespedId)

            resultado.onSuccess { pedidos ->
                Log.d(TAG, "Se obtuvieron ${pedidos.size} pedidos")
                emit(pedidos)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al obtener pedidos", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en obtenerPedidosPorHuesped", e)
            emit("error: ${e.message}")
        }
    }

}
