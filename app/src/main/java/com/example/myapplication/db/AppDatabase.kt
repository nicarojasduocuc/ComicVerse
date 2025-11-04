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
    version = 6,  // Incrementar versión para forzar recreación
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
                            
                            // Crear usuario admin por defecto
                            db.execSQL("""
                                INSERT INTO users (id, name, email, password, isAdmin) 
                                VALUES (1, 'Administrador', 'comicverse@gmail.com', 'comicverse', 1)
                            """)
                            
                            // Insertar productos iniciales
                            db.execSQL("""
                                INSERT INTO products (name, type, year, stock, price, salePrice, description, imageUrl) VALUES
                                ('One Piece Vol. 1', 'Manga', 1997, 25, 12990, 9990, 'La aventura pirata de Luffy en busca del tesoro legendario One Piece.', 'https://i.pinimg.com/736x/b2/de/8c/b2de8c3896220fa0531ead303cfe6668.jpg'),
                                ('Batman: Year One', 'Comic', 1987, 10, 16990, NULL, 'El renacimiento del Caballero Oscuro en una Gotham corrupta.', 'https://m.media-amazon.com/images/M/MV5BNDk0M2YzZTEtZGU1YS00MGVmLThmNjEtODE3MGRiNTVjN2FiXkEyXkFqcGc@._V1_.jpg'),
                                ('Solo Leveling Vol. 1', 'Manhwa', 2018, 30, 14990, 11990, 'Un cazador débil obtiene el poder de crecer sin límites tras un misterioso evento.', 'https://i.pinimg.com/474x/3d/c1/54/3dc1547eaab88358af5ebfbef3521a35.jpg'),
                                ('Chainsaw Man Vol. 1', 'Manga', 2018, 20, 12990, 10990, 'Denji, un joven cazador de demonios, se fusiona con su demonio motosierra.', 'https://m.media-amazon.com/images/I/81s8xJUzWGL.jpg'),
                                ('Saga Vol. 1', 'Novela Gráfica', 2012, 12, 18990, NULL, 'Fantasía épica espacial que mezcla amor, guerra y política intergaláctica.', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRXDuOBM_04onn4dcLYK-c98TkDy4dSoD9c6Q&s'),
                                ('The Walking Dead Vol. 1', 'Comic', 2003, 15, 14990, NULL, 'Un grupo de sobrevivientes enfrenta el apocalipsis zombie.', 'https://m.media-amazon.com/images/I/81E1DCPTh3L._AC_UF1000,1000_QL80_.jpg'),
                                ('Jujutsu Kaisen Vol. 1', 'Manga', 2018, 18, 12990, 9990, 'Un estudiante absorbe un talismán maldito y se convierte en recipiente de un demonio.', 'https://m.media-amazon.com/images/I/81TmHlRleJL._SL1500_.jpg'),
                                ('Tower of God Vol. 1', 'Manhwa', 2010, 20, 13990, NULL, 'Un chico entra en una misteriosa torre llena de pruebas mortales.', 'https://images.penguinrandomhouse.com/cover/9781990259760'),
                                ('Berserk Vol. 1', 'Manga', 1989, 8, 15990, 13490, 'Guts, un guerrero maldito, lucha en un oscuro mundo medieval de demonios y sangre.', 'https://m.media-amazon.com/images/I/91Gs6wk1TdL._UF1000,1000_QL80_.jpg'),
                                ('Death Note Vol. 1', 'Manga', 2003, 22, 11990, 9990, 'Un estudiante encuentra un cuaderno que puede matar a cualquiera cuyo nombre escriba.', 'https://m.media-amazon.com/images/I/81iDNjn-r3L._AC_UF1000,1000_QL80_.jpg'),
                                ('Invincible Vol. 1', 'Comic', 2003, 14, 13990, NULL, 'Un adolescente descubre que su padre es el superhéroe (y villano) más poderoso del planeta.', 'https://images.cdn1.buscalibre.com/fit-in/360x360/ea/a7/eaa7caf31cadf76857a2489bbe48e69f.jpg'),
                                ('Demon Slayer Vol. 1', 'Manga', 2016, 25, 12490, 10990, 'Tanjiro busca vengar a su familia y curar a su hermana demonio.', 'https://images-cdn.ubuy.com.ar/634f22d146f0f87243748855-demon-slayer-kimetsu-no-yaiba-poster.jpg'),
                                ('Tokyo Ghoul Vol. 1', 'Manga', 2011, 16, 11990, NULL, 'Kaneki se convierte en mitad ghoul tras un encuentro fatal.', 'https://prodimage.images-bn.com/pimages/9781421580364_p0_v4_s1200x630.jpg'),
                                ('Chainsaw Man Vol. 2', 'Manga', 2019, 15, 12990, NULL, 'Segunda parte de la sangrienta historia de Denji y los cazadores de demonios.', 'https://m.media-amazon.com/images/I/81HC2HywjbL.jpg')
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
        SELECT p.id, p.name, p.price, p.description, p.imageUrl, p.year, p.type, p.stock, p.salePrice, c.quantity 
        FROM products p
        INNER JOIN cart_items c ON p.id = c.productId
        WHERE c.userId = :userId
    """)
    fun getCartWithProducts(userId: Int): Flow<List<CartItemWithProduct>>
}

data class CartItemWithProduct(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val year: Int,
    val type: String,
    val stock: Int,
    val salePrice: Double?,
    val quantity: Int
)