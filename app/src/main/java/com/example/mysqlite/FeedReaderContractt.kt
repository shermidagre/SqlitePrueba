package com.dam.mysqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

/**
 * Clase de contrato que especifica explícitamente el diseño del esquema para la base de datos "FeedReader".
 *
 * Este objeto sirve como un contenedor de constantes que definen nombres para URIs, tablas y columnas.
 * El uso de una clase de contrato ayuda a garantizar que se utilicen las mismas constantes en todas las clases
 * que interactúan con la base de datos, reduciendo el riesgo de errores tipográficos e inconsistencias.
 *
 * Incluye:
 * - [FeedEntry]: Objeto interno que define el contenido de la tabla.
 * - [FeedReaderDbHelper]: Una clase auxiliar para gestionar la creación de la base de datos y la gestión de versiones.
 * - Constantes de cadena SQL para crear y eliminar entradas.
 */
object FeedReaderContract {
    /** Etiqueta utilizada para el filtrado de logs en Logcat. */
    const val TAG = "SQLite"

    /**
     * Define el esquema de la tabla "entry".
     * Hereda de [BaseColumns] para obtener automáticamente la constante `_ID`.
     */
    object FeedEntry : BaseColumns {
        /** Nombre de la tabla en la base de datos. */
        const val TABLE_NAME = "entry"
        /** Nombre de la columna para el título de la entrada. */
        const val COLUMN_NAME_TITLE = "title"
        /** Nombre de la columna para el subtítulo de la entrada. */
        const val COLUMN_NAME_SUBTITLE = "subtitle"
    }

    /**
     * Sentencia SQL para crear la tabla.
     * Construye la cadena utilizando las constantes definidas en [FeedEntry].
     */
    private const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${FeedEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${FeedEntry.COLUMN_NAME_TITLE} TEXT," +
                "${FeedEntry.COLUMN_NAME_SUBTITLE} TEXT)"

    /**
     * Sentencia SQL para eliminar la tabla si existe.
     * Útil durante las actualizaciones de la base de datos.
     */
    private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedEntry.TABLE_NAME}"


    /**
     * Clase auxiliar para gestionar la creación y gestión de versiones de la base de datos.
     * Extiende [SQLiteOpenHelper] para manejar el ciclo de vida de la base de datos.
     *
     * @param context El contexto de la aplicación, necesario para crear la base de datos.
     */
    class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        /**
         * Se llama cuando la base de datos se crea por primera vez.
         * Aquí es donde debe ocurrir la creación de tablas y la población inicial de datos.
         *
         * @param db La base de datos.
         */
        override fun onCreate(db: SQLiteDatabase) {
            Log.d(TAG,"Creando base")
            db.execSQL(SQL_CREATE_ENTRIES)
            Log.d(TAG,"Creada base")
        }

        /**
         * Se llama cuando la base de datos necesita ser actualizada.
         * Ocurre cuando [DATABASE_VERSION] cambia.
         *
         * @param db La base de datos.
         * @param oldVersion La versión antigua de la base de datos.
         * @param newVersion La nueva versión de la base de datos.
         */
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.d(TAG,"Actualizando base")
            // Esta base de datos es solo una caché para datos en línea, por lo que su política de actualización
            // es simplemente descartar los datos y comenzar de nuevo.
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }

        /**
         * Se llama cuando la base de datos necesita ser degradada a una versión anterior.
         * Ocurre cuando la versión actual es mayor que la solicitada.
         *
         * @param db La base de datos.
         * @param oldVersion La versión actual de la base de datos (que es más alta).
         * @param newVersion La versión antigua a la que se quiere volver.
         */
        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.d(TAG,"desactualizando base")
            onUpgrade(db, oldVersion, newVersion)
        }

        /**
         * Constantes específicas para la configuración de la base de datos.
         */
        companion object {
            // Si cambias el esquema de la base de datos, debes incrementar la versión de la base de datos.
            /** Versión actual de la base de datos. Incrementar al cambiar el esquema. */
            const val DATABASE_VERSION = 1
            /** Nombre del archivo de la base de datos. */
            const val DATABASE_NAME = "FeedReader.db"
        }
    }
}
