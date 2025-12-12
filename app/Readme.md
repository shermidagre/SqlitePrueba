
---

## 📚 **Documentación del Código: Implementación de SQLite en Android (Kotlin)**
**Fecha de documentación:** 12 de diciembre de 2025  
**Aplicación:** `com.dam.mysqlite`  
**Objetivo:** Demostrar el ciclo completo de operaciones CRUD (Create, Read, Update, Delete) con SQLite usando `SQLiteOpenHelper`.

---

### ✅ **1. Contrato de la Base de Datos (`FeedReaderContract`)**
**Clase:** `FeedReaderContract` (objeto singleton)  
**Propósito:** Definir de forma segura y mantenible el *esquema* de la base de datos mediante constantes.  
**Patrón:** Contrato de tabla `FeedEntry` implementa `BaseColumns` para heredar `_ID` (clave primaria esperada por APIs de Android como `CursorAdapter`).

#### 🔧 Estructura:
```kotlin
object FeedReaderContract {
    const val TAG = "SQLite"

    // Clase interna que define la tabla y sus columnas
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "entry"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_SUBTITLE = "subtitle"
    }
}
```

#### 📌 Características clave:
- ✅ **Autodocumentación**: El esquema (tabla + columnas) se expone en código legible.
- ✅ **Mantenibilidad**: Cambiar un nombre de columna aquí se propaga automáticamente a todo el código.
- ✅ **Compatibilidad con Android**: Herencia de `BaseColumns._ID` mejora la integración con componentes del framework (e.g., `RecyclerView` con `CursorAdapter`).

---

### 🛠 **2. Asistente de Base de Datos (`FeedReaderDbHelper`)**
**Clase:** `FeedReaderDbHelper` (extiende `SQLiteOpenHelper`)  
**Propósito:** Gestionar la creación y actualización segura de la base de datos SQLite.

#### 🔧 Estructura:
```kotlin
class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(...) {
    override fun onCreate(db: SQLiteDatabase) { ... }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { ... }
    override fun onDowngrade(...) { ... }
    
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"
    }
}
```

#### 📌 Características clave:
| Método | Función | Nota |
|--------|---------|------|
| `onCreate()` | Ejecuta `SQL_CREATE_ENTRIES` para crear la tabla. | Se llama solo si la BD no existe. |
| `onUpgrade()` | Borra la tabla y vuelve a crearla (`DROP` + `CREATE`). | Estrategia *destructiva*: adecuada para cachés o apps sin datos críticos persistentes. |
| `onDowngrade()` | Redirige a `onUpgrade()` (rollback implícito). | Buen enfoque para prototipado/simple apps. |
| `DATABASE_VERSION` | Controla cuándo se dispara `onUpgrade()`. | **¡Incrementar al modificar el esquema!** |

#### 💡 Buenas prácticas aplicadas:
- ✅ **Logging** con `Log.d()` para seguimiento de creación/actualización.
- ✅ Uso de **constantes privadas** para SQL (`SQL_CREATE_ENTRIES`, `SQL_DELETE_ENTRIES`) → evita *SQL injection* y mejora legibilidad.

---

### 📦 **3. Operaciones CRUD en `MainActivity`**
**Objetivo:** Ejecutar **insertar → consultar → actualizar → eliminar**, mostrando el estado en los logs.

#### 🔧 Flujo implementado:
1. **Conexión a la BD en modo escritura**
2. **Inserción** de una fila (`title="My Title", subtitle="prueba2"`)
3. **Consulta** con filtro y ordenamiento
4. **Actualización** del título por coincidencia parcial (`LIKE ?`)
5. **Nueva consulta** para verificar la actualización
6. **Eliminación** de filas por título actualizado
7. **Cierre explícito** de la BD (⚠️ *Nota crítica abajo*)

#### ✅ Implementación detallada:

| Operación | Código clave | Observaciones |
|-----------|--------------|---------------|
| **💾 Insertar** | `db?.insert(TABLE_NAME, null, values)` | ✔️ Uso de `ContentValues.apply{}` (idiomático Kotlin)<br>✔️ `newRowId` captura el ID generado (o `-1` en error) |
| **🔍 Consultar** | `db.query()` con `projection`, `selection`, `selectionArgs`, `sortOrder` | ✔️ **Inyección SQL evitada**: parámetros separados (`?` + `selectionArgs`)<br>✔️ Uso de `getColumnIndexOrThrow()` → evita errores de índice |
| **✏️ Actualizar** | `db.update(TABLE_NAME, valuesA, selectionA, selectionArgsA)` | ✔️ Solo actualiza `title`, mantiene `subtitle`<br>✔️ `count` devuelve filas afectadas (0 = no hay coincidencias) |
| **🗑 Eliminar** | `db.delete(TABLE_NAME, selectionD, selectionArgsD)` | ✔️ Usa `LIKE ?` para borrar por coincidencia<br>✔️ `deletedRows` confirma éxito |

#### 📌 Logs generados (ejemplo):
```log
D/PruebaSQLite: Conectando base
D/PruebaSQLite: Creada fila
D/PruebaSQLite: insertados datos 1
D/PruebaSQLite: Conectando base lectura
D/PruebaSQLite: Valor = My Title
D/PruebaSQLite: Valores = [My Title]
D/PruebaSQLite: Actualizando base 1
D/PruebaSQLite: Valor = MyNewTitle
D/PruebaSQLite: Borrando datos 1
D/PruebaSQLite: Desconectando base
```

---

### ⚠️ **Advertencias y Mejoras Recomendadas**

#### 🔴 **Problema crítico en `MainActivity`:**
```kotlin
// ❌ MAL: Cerrar BD en onCreate() → rompe persistencia
dbHelper.close() // dentro de onCreate()
```
**Consecuencia:** Si intentas acceder a `dbHelper` después de `onCreate()` (e.g., en otro botón), lanzará excepción.  
**✅ Solución:**
```kotlin
override fun onDestroy() {
    dbHelper.close() // ✅ Cerrar SOLO al destruir la Activity
    super.onDestroy()
}
```

#### 🔧 Otras mejoras:
| Tema | Recomendación |
|------|----------------|
| **🧵 Subprocesos** | Usar `Dispatchers.IO` para `getWritableDatabase()` (operación bloqueante). |
| **♻️ Gestión de recursos** | Usar `use{}` con `Cursor` para garantizar `close()`:<br>`cursor.use { ... }` |
| **🧪 Pruebas** | Añadir `@Test` con `InstrumentationRegistry` para validar esquema/CRUD. |
| **🚀 Migración a Room** | La guía oficial lo **recomienda enfáticamente** (evita SQL raw, verifica en compilación, reduce boilerplate). |

---

### 📌 Conclusión
La implementación **cumple fielmente la guía de Android para SQLite**, demostrando:
- ✅ Definición clara de contrato (`FeedReaderContract`)
- ✅ Uso correcto de `SQLiteOpenHelper`
- ✅ Operaciones CRUD completas y seguras (con `?` y `selectionArgs`)
- ✅ Logging para trazabilidad

