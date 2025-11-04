package com.example.myapplication.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.db.models.CartItem
import com.example.myapplication.db.models.Product
import com.example.myapplication.db.models.User
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [User::class, Product::class, CartItem::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "comicverse_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Crear usuario por defecto al crear la BD
                            db.execSQL("""
                                INSERT INTO users (id, name, email, password) 
                                VALUES (1, 'Usuario Invitado', 'guest@comicverse.com', 'guest123')
                            """)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ========== USER DAO ==========
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Delete
    suspend fun deleteUser(user: User)
}

// ========== PRODUCT DAO ==========
@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product?
}

// ========== CART DAO ==========
@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItemsByUser(userId: Int): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(userId: Int, productId: Int): CartItem?

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Int)

    @Query("""
        SELECT p.id, p.name, p.price, p.description, p.imageUrl, c.quantity 
        FROM products p
        INNER JOIN cart_items c ON p.id = c.productId
        WHERE c.userId = :userId
    """)
    fun getCartWithProducts(userId: Int): Flow<List<CartItemWithProduct>>
}

// The duplicate UserDao interface that was here has been removed.

data class CartItemWithProduct(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val quantity: Int
)
