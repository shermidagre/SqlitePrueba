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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MysqliteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        val dbHelper = FeedReaderDbHelper(application)
        val TAG = "PruebaSQLite"

        Log.d(TAG,"Conectando base")
        // Gets the data repository in write mode
        val db = dbHelper.writableDatabase

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(FeedEntry.COLUMN_NAME_TITLE, "My Title")
            put(FeedEntry.COLUMN_NAME_SUBTITLE, "prueba2")
        }
        Log.d(TAG,"Creada fila")


        // Insert the new row, returning the primary key value of the new row
        val newRowId = db?.insert(FeedEntry.TABLE_NAME, null, values)
        Log.d(TAG,"insertados datos $newRowId")



        val dbl = dbHelper.readableDatabase
        Log.d(TAG,"Conectando base lectura")


// Define a projection that specifies which columns from the database
// you will actually use after this query.
        val projection = arrayOf(BaseColumns._ID, FeedEntry.COLUMN_NAME_TITLE, FeedEntry.COLUMN_NAME_SUBTITLE)

// Filter results WHERE "title" = 'My Title'
        Log.d(TAG,"Filtrando select")

        val selection = "${FeedEntry.COLUMN_NAME_TITLE} = ?"
        val selectionArgs = arrayOf("My Title")

// How you want the results sorted in the resulting Cursor
        Log.d(TAG,"Ordenando select")

        val sortOrder = "${FeedEntry.COLUMN_NAME_SUBTITLE} DESC"

        val cursor = dbl.query(
            FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )
        Log.d(TAG,"valores recibidos")


        val itemIds = mutableListOf<String>()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getString(getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE))
                Log.d(TAG,"Valor = $itemId")

                itemIds.add(itemId)
            }
        }
        Log.d(TAG,"Valores = $itemIds")

        cursor.close()




        // New value for one column
        val title = "MyNewTitle"
        val valuesA = ContentValues().apply {
            put(FeedEntry.COLUMN_NAME_TITLE, title)
        }

// Which row to update, based on the title
        val selectionA = "${FeedEntry.COLUMN_NAME_TITLE} LIKE ?"
        val selectionArgsA = arrayOf("My Title")
        val count = db.update(
            FeedEntry.TABLE_NAME,
            valuesA,
            selectionA,
            selectionArgsA)
        Log.d(TAG,"Actualizando base $count")

        val cursorA = dbl.query(
            FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selectionA,              // The columns for the WHERE clause
            arrayOf(title),          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )
        Log.d(TAG,"valores recibidos")


        val itemIdsA = mutableListOf<String>()

        with(cursorA) {
            while (moveToNext()) {
                val itemId = getString(getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE))
                Log.d(TAG,"Valor = $itemId")

                itemIdsA.add(itemId)
            }
        }
        Log.d(TAG,"Valores = $itemIdsA")

        cursor.close()

        // Define 'where' part of query.
        val selectionD = "${FeedEntry.COLUMN_NAME_TITLE} LIKE ?"
// Specify arguments in placeholder order.
        val selectionArgsD = arrayOf("MyNewTitle")
// Issue SQL statement.
        val deletedRows = db.delete(FeedEntry.TABLE_NAME, selectionD, selectionArgsD)
        Log.d(TAG,"Borrando datos $deletedRows")

        Log.d(TAG,"Desconectando base")

        dbHelper.close()

    }

    override fun onDestroy() {

        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MysqliteTheme {
        Greeting("Android")
    }
}