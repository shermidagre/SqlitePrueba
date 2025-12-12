package com.dam.mysqlite

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dam.mysqlite.FeedReaderContract.FeedEntry
import com.dam.mysqlite.FeedReaderContract.FeedReaderDbHelper
import com.example.mysqlite.ui.theme.MysqliteTheme

/**
 * Actividad principal de la aplicación.
 *
 * Esta clase demuestra el ciclo completo de operaciones CRUD (Create, Read, Update, Delete)
 * utilizando SQLite en Android de forma nativa mediante [SQLiteOpenHelper].
 * Además, configura una interfaz de usuario básica utilizando Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    /**
     * Método llamado cuando se crea la actividad.
     * Aquí se inicializa la UI y se ejecutan las pruebas de base de datos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita el diseño de borde a borde (pantalla completa sin marcos negros de sistema)
        enableEdgeToEdge()

        // Configura el contenido de la vista utilizando Jetpack Compose
        setContent {
            MysqliteTheme {
                // Scaffold proporciona la estructura básica de material design (barra superior, fondo, etc.)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // --- INICIO DE LÓGICA DE BASE DE DATOS ---

        // Instanciamos el ayudante de base de datos pasando el contexto de la aplicación
        val dbHelper = FeedReaderDbHelper(application)
        val TAG = "PruebaSQLite"

        Log.d(TAG, "Conectando a la base de datos")

        // 1. ESCRITURA (INSERT)
        // Obtiene el repositorio de datos en modo escritura
        val db = dbHelper.writableDatabase

        // Crea un nuevo mapa de valores, donde las claves son los nombres de las columnas
        val values = ContentValues().apply {
            put(FeedEntry.COLUMN_NAME_TITLE, "Prueba") // Valor para columna Título
            put(FeedEntry.COLUMN_NAME_SUBTITLE, "prueba") // Valor para columna Subtítulo
        }
        Log.d(TAG, "Fila creada")

        // Inserta la nueva fila, devolviendo el valor de clave primaria de la nueva fila
        // El segundo argumento es 'nullColumnHack', que permite insertar una fila vacía si values está vacío (aquí no se usa)
        val newRowId = db?.insert(FeedEntry.TABLE_NAME, null, values)
        Log.d(TAG, "insertados datos $newRowId")


        // 2. LECTURA (SELECT)
        // Obtenemos la base de datos en modo lectura para realizar consultas
        val dbl = dbHelper.readableDatabase
        Log.d(TAG, "Conectando a la base de datos lectura")

        // Define una proyección que especifica qué columnas de la base de datos
        // usarás realmente después de esta consulta.
        val projection = arrayOf(BaseColumns._ID, FeedEntry.COLUMN_NAME_TITLE, FeedEntry.COLUMN_NAME_SUBTITLE)

        // Filtrar resultados DONDE "Prueba" = 'Prueba'
        Log.d(TAG, "Filtrando select")

        // La parte de la selección (WHERE clause)
        val selection = "${FeedEntry.COLUMN_NAME_TITLE} = ?"
        // Los argumentos para reemplazar el '?' (esto previene inyección SQL)
        val selectionArgs = arrayOf("Prueba")

        // Cómo quieres que se ordenen los resultados en el Cursor resultante
        Log.d(TAG, "Ordenando select")
        val sortOrder = "${FeedEntry.COLUMN_NAME_SUBTITLE} DESC"

        // Ejecuta la consulta
        val cursor = dbl.query(
            FeedEntry.TABLE_NAME,   // La tabla a consultar
            projection,             // El array de columnas a devolver (pasar null para obtener todas)
            selection,              // Las columnas para la cláusula WHERE
            selectionArgs,          // Los valores para la cláusula WHERE
            null,                   // No agrupar las filas
            null,                   // No filtrar por grupos de filas
            sortOrder               // El orden de clasificación
        )
        Log.d(TAG, "Valores recibidos")


        // Lista mutable para guardar los resultados leídos
        val itemIds = mutableListOf<String>()

        // Recorre el cursor para leer los datos
        with(cursor) {
            while (moveToNext()) {
                // Obtiene el valor de la columna TITLE de la fila actual
                val itemId = getString(getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE))
                Log.d(TAG, "Valor = $itemId")

                itemIds.add(itemId)
            }
        }
        Log.d(TAG, "Valores = $itemIds")

        // Es importante cerrar el cursor para liberar memoria
        cursor.close()


        // 3. ACTUALIZACIÓN (UPDATE)

        // Nuevo valor para una columna
        val title = "MyNewTitle"
        val valuesA = ContentValues().apply {
            put(FeedEntry.COLUMN_NAME_TITLE, title)
        }

        // Qué fila actualizar, basado en el título antiguo
        val selectionA = "${FeedEntry.COLUMN_NAME_TITLE} LIKE ?"
        val selectionArgsA = arrayOf("My Title")

        // Ejecuta la actualización y devuelve el número de filas afectadas
        val count = db.update(
            FeedEntry.TABLE_NAME,
            valuesA,
            selectionA,
            selectionArgsA
        )
        Log.d(TAG, "Actualizando base $count")

        // --- Verificación de la actualización (Lectura de control) ---
        // Realizamos una nueva consulta para verificar que el cambio se hizo efectivo.
        // Nota: Aquí se busca por el NUEVO título o se reusa la lógica de búsqueda.
        // En tu código original buscas donde title LIKE ? y pasas 'MyNewTitle' (variable title).

        val cursorA = dbl.query(
            FeedEntry.TABLE_NAME,   // La tabla a consultar
            projection,             // Las columnas a devolver
            selectionA,             // La cláusula WHERE (Title LIKE ?)
            arrayOf(title),         // El argumento (ahora buscamos "MyNewTitle")
            null,
            null,
            sortOrder
        )
        Log.d(TAG, "valores recibidos tras update")

        val itemIdsA = mutableListOf<String>()

        with(cursorA) {
            while (moveToNext()) {
                val itemId = getString(getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE))
                Log.d(TAG, "Valor = $itemId")
                itemIdsA.add(itemId)
            }
        }
        Log.d(TAG, "Valores encontrados = $itemIdsA")

        // Cerramos el segundo cursor
        cursorA.close()


        // 4. BORRADO (DELETE)

        // Define la parte 'where' de la consulta.
        val selectionD = "${FeedEntry.COLUMN_NAME_TITLE} LIKE ?"
        // Especifica los argumentos en orden de marcadores de posición.
        val selectionArgsD = arrayOf("MyNewTitle") // Borramos el que acabamos de renombrar

        // Ejecuta la sentencia SQL de borrado.
        val deletedRows = db.delete(FeedEntry.TABLE_NAME, selectionD, selectionArgsD)
        Log.d(TAG, "Borrando datos $deletedRows")

        Log.d(TAG, "Desconectando base")

        // Cierra la conexión a la base de datos al finalizar
        dbHelper.close()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

/**
 * Componente Composable que muestra un saludo.
 *
 * @param name El nombre a mostrar en el saludo.
 * @param modifier Modificadores para ajustar el diseño.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

/**
 * Función de previsualización para el editor de diseño de Android Studio.
 * Muestra cómo se ve el componente Greeting sin ejecutar la app.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MysqliteTheme {
        Greeting("Android")
    }
}
