package com.example.inventoryapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext // Aunque no se usa directamente, es útil para el contexto
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.KeyboardType // Importar KeyboardType

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
class InventoryViewModel : ViewModel() {
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryApp() {
    val viewModel: InventoryViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    MaterialTheme {
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
    var quantityInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
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
                    value = quantityInput,
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
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        showPriceError = false // Reset error on change
                    },
                    label = { Text("Precio") },
                    isError = showPriceError,
                    supportingText = { if (showPriceError) Text("Debe ser un número válido") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal) // CAMBIO AQUÍ
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
    var quantityInput by remember { mutableStateOf(product.quantity.toString()) }
    var priceInput by remember { mutableStateOf(product.price.toString()) }
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
                    value = quantityInput,
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
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        showPriceError = false
                    },
                    label = { Text("Precio") },
                    isError = showPriceError,
                    supportingText = { if (showPriceError) Text("Debe ser un número válido") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal) // CAMBIO AQUÍ
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
    InventoryApp()
}