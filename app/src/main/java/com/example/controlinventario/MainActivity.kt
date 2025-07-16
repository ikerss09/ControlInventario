package com.example.controlinventario

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.KeyboardType // Importar KeyboardType para los teclados numéricos

// --- Nuevas importaciones para el tema dinámico ---
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color // Para definir colores personalizados si los necesitaras
// --- Fin de nuevas importaciones ---

// Modelo de datos
data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val quantity: Int,
    val price: Double,
    val category: String
)

// ViewModel para manejar el estado
class InventoryViewModel : androidx.lifecycle.ViewModel() {
    private val _products = mutableStateOf<List<Product>>(emptyList())
    val products: State<List<Product>> = _products

    private var nextId = 1

    init {
        // Datos de ejemplo
        addProduct(Product(0, "Laptop Dell", "Laptop para oficina", 5, 1200.0, "Electrónicos"))
        addProduct(Product(0, "Mouse Logitech", "Mouse inalámbrico", 15, 25.0, "Accesorios"))
        addProduct(Product(0, "Teclado Mecánico", "Teclado gaming", 8, 80.0, "Accesorios"))
    }

    // Aseguramos que el ID se asigne aquí
    fun addProduct(product: Product) {
        _products.value = _products.value + product.copy(id = nextId++)
    }

    fun updateProduct(product: Product) {
        _products.value = _products.value.map {
            if (it.id == product.id) product else it
        }
    }

    fun deleteProduct(productId: Int) {
        _products.value = _products.value.filter { it.id != productId }
    }

    fun getProductById(id: Int): Product? {
        return _products.value.find { it.id == id }
    }
}

// --- Definición de los esquemas de color para el tema claro y oscuro ---
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC), // Ejemplo de un color primario para modo oscuro
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFF121212), // Fondo oscuro
    surface = Color(0xFF121212),    // Superficie oscura
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,    // Texto claro en fondo oscuro
    onSurface = Color.White        // Texto claro en superficie oscura
    // Puedes personalizar más colores aquí si lo deseas
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE), // Ejemplo de un color primario para modo claro
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = Color.White,    // Fondo claro
    surface = Color.White,       // Superficie clara
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,  // Texto oscuro en fondo claro
    onSurface = Color.Black      // Texto oscuro en superficie clara
    // Puedes personalizar más colores aquí si lo deseas
)
// --- Fin de la definición de los esquemas de color ---


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Envuelve tu InventoryApp con el tema personalizado
            InventoryAppTheme {
                InventoryApp()
            }
        }
    }
}

// --- Composable para envolver tu aplicación con el tema ---
@Composable
fun InventoryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta si el sistema está en modo oscuro
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography, // Puedes definir una tipografía personalizada aquí
        content = content
    )
}
// --- Fin del Composable del tema ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryApp() {
    val viewModel: InventoryViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Ya no necesitas envolverlo en MaterialTheme aquí, ya lo hace InventoryAppTheme
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Inventarios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Estadísticas
            StatsSection(viewModel.products.value)

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de productos
            Text(
                text = "Productos en Inventario",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(viewModel.products.value) { product ->
                    ProductCard(
                        product = product,
                        onEdit = {
                            selectedProduct = product
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteProduct(product.id) }
                    )
                }
            }
        }
    }

    // Diálogo para agregar producto
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onAddProduct = { product ->
                viewModel.addProduct(product)
                showAddDialog = false
            }
        )
    }

    // Diálogo para editar producto
    if (showEditDialog && selectedProduct != null) {
        EditProductDialog(
            product = selectedProduct!!,
            onDismiss = {
                showEditDialog = false
                selectedProduct = null // Limpiar el producto seleccionado
            },
            onUpdateProduct = { product ->
                viewModel.updateProduct(product)
                showEditDialog = false
                selectedProduct = null // Limpiar el producto seleccionado
            }
        )
    }
}

@Composable
fun StatsSection(products: List<Product>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estadísticas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Total Productos", products.size.toString())
                StatItem("Total Cantidad", products.sumOf { it.quantity }.toString())
                StatItem("Valor Total", "$${String.format("%.2f", products.sumOf { it.price * it.quantity })}") // Formateo para dos decimales
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Categoría: ${product.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cantidad: ${product.quantity}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Precio: $${String.format("%.2f", product.price)}", // Formateo para dos decimales
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Total: $${String.format("%.2f", product.price * product.quantity)}", // Formateo para dos decimales
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAddProduct: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("") } // Cambiado a quantityInput
    var priceInput by remember { mutableStateOf("") }     // Cambiado a priceInput
    var category by remember { mutableStateOf("") }

    // Estado para errores de validación
    var showQuantityError by remember { mutableStateOf(false) }
    var showPriceError by remember { mutableStateOf(false) }
    var showNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showNameError = false // Reset error on change
                    },
                    label = { Text("Nombre") },
                    isError = showNameError,
                    supportingText = { if (showNameError) Text("El nombre no puede estar vacío") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityInput, // Usar quantityInput
                    onValueChange = {
                        quantityInput = it
                        showQuantityError = false // Reset error on change
                    },
                    label = { Text("Cantidad") },
                    isError = showQuantityError,
                    supportingText = { if (showQuantityError) Text("Debe ser un número entero válido") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceInput, // Usar priceInput
                    onValueChange = {
                        priceInput = it
                        showPriceError = false // Reset error on change
                    },
                    label = { Text("Precio") },
                    isError = showPriceError,
                    supportingText = { if (showPriceError) Text("Debe ser un número válido") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityInput.toIntOrNull()
                    val price = priceInput.toDoubleOrNull()

                    showNameError = name.isBlank()
                    showQuantityError = quantity == null || quantity <= 0
                    showPriceError = price == null || price <= 0.0

                    if (!showNameError && !showQuantityError && !showPriceError) {
                        onAddProduct(
                            Product(
                                id = 0, // Se asignará automáticamente en el ViewModel
                                name = name,
                                description = description,
                                quantity = quantity!!,
                                price = price!!,
                                category = category
                            )
                        )
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onUpdateProduct: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var description by remember { mutableStateOf(product.description) }
    var quantityInput by remember { mutableStateOf(product.quantity.toString()) } // Cambiado a quantityInput
    var priceInput by remember { mutableStateOf(product.price.toString()) }     // Cambiado a priceInput
    var category by remember { mutableStateOf(product.category) }

    // Estado para errores de validación
    var showQuantityError by remember { mutableStateOf(false) }
    var showPriceError by remember { mutableStateOf(false) }
    var showNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showNameError = false
                    },
                    label = { Text("Nombre") },
                    isError = showNameError,
                    supportingText = { if (showNameError) Text("El nombre no puede estar vacío") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityInput, // Usar quantityInput
                    onValueChange = {
                        quantityInput = it
                        showQuantityError = false
                    },
                    label = { Text("Cantidad") },
                    isError = showQuantityError,
                    supportingText = { if (showQuantityError) Text("Debe ser un número entero válido") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceInput, // Usar priceInput
                    onValueChange = {
                        priceInput = it
                        showPriceError = false
                    },
                    label = { Text("Precio") },
                    isError = showPriceError,
                    supportingText = { if (showPriceError) Text("Debe ser un número válido") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityInput.toIntOrNull()
                    val price = priceInput.toDoubleOrNull()

                    showNameError = name.isBlank()
                    showQuantityError = quantity == null || quantity <= 0
                    showPriceError = price == null || price <= 0.0

                    if (!showNameError && !showQuantityError && !showPriceError) {
                        onUpdateProduct(
                            product.copy(
                                name = name,
                                description = description,
                                quantity = quantity!!,
                                price = price!!,
                                category = category
                            )
                        )
                    }
                }
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun InventoryAppPreview() {
    // Asegúrate de que el Preview también use el tema
    InventoryAppTheme {
        InventoryApp()
    }
}