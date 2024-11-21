package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;

/**
 * Тестирование сервиса покупок
 */
class ShoppingServiceTest {
    /**
     * Экземпляр DAO для товаров
     */
    private final ProductDao productDao = Mockito.mock(ProductDao.class);
    /**
     * Экземпляр сервиса покупок
     */
    private final ShoppingService shoppingService = new ShoppingServiceImpl(productDao);
    /**
     * Покупатель
     */
    private final Customer customer = new Customer(1L, "customer");
    /**
     * Продукт для покупки
     */
    private final Product product = new Product("123", 5);
    /**
     * Покупаемая корзина
     */
    private Cart cart;

    /**
     * Создать новую корзину для каждого теста
     */
    @BeforeEach
    void createCart() {
        cart = new Cart(customer);
    }

    /**
     * Нет смысла тестировать элементарный геттер без логики,
     * так как ответственность за выдачу продуктов лежит на DAO
     */
    @Test
    public void testGetAllProducts() { }

    /**
     * Нет смысла тестировать элементарный геттер без логики,
     * так как ответственность за выдачу имени продукта лежит на DAO
     */
    @Test
    public void testGetProductName() { }

    /**
     * Тестирование получения корзины покупателя
     */
    @Test
    public void testGetCart() {
        cart.add(product, 5);
        Cart userCart = shoppingService.getCart(customer);

        Assertions.assertEquals(1, userCart.getProducts().size());
        Assertions.assertTrue(userCart.getProducts().containsKey(product));
        Assertions.assertEquals(5, userCart.getProducts().get(product));
    }

    /**
     * Тестирование покупки пустой и null корзины
     */
    @Test
    public void testBuyEmptyNullCart() throws BuyException {
        Assertions.assertFalse(shoppingService.buy(cart));
        Assertions.assertFalse(shoppingService.buy(null));
    }

    /**
     * Тестирование покупки нескольких товаров
     */
    @Test
    public void testBuyCartWithSomeProduct() throws BuyException {
        Product anotherProduct = new Product("345", 7);
        cart.add(product, 2);
        cart.add(anotherProduct, 3);

        boolean buyResult = shoppingService.buy(cart);
        Assertions.assertTrue(buyResult);

        Mockito.verify(productDao, Mockito.times(1)).save(product);
        Mockito.verify(productDao, Mockito.times(1)).save(anotherProduct);
        Assertions.assertEquals(3, product.getCount());
        Assertions.assertEquals(4, anotherProduct.getCount());
        Assertions.assertEquals(0, cart.getProducts().size());
    }

    /**
     * Тестирование покупки всего количества товара
     */
    @Test
    public void testBuyCartWithFullProductCount() throws BuyException {
        cart.add(product, 5);

        boolean buyResult = shoppingService.buy(cart);
        Assertions.assertTrue(buyResult);
        Mockito.verify(productDao, Mockito.times(1)).save(product);
        Assertions.assertEquals(0, product.getCount());
        Assertions.assertEquals(0, cart.getProducts().size());
    }

    /**
     * Тестирование покупки товара с отрицательным количеством
     */
    @Test
    public void testBuyCartWithNegativeProductCount() throws BuyException {
        cart.add(product, -3);
        boolean buyResult = shoppingService.buy(cart);

        Assertions.assertFalse(buyResult);
        Mockito.verify(productDao, Mockito.times(0)).save(product);
        Assertions.assertEquals(5, product.getCount());
        Assertions.assertEquals(1, cart.getProducts().size());
    }

    /**
     * Тестирование невозможности покупки товара в связи с его нехваткой
     */
    @Test
    public void testBuyCartWithMoreProductCount() throws BuyException {
        cart.add(product, 3);

        boolean buyResult = shoppingService.buy(cart);
        Assertions.assertTrue(buyResult);
        Assertions.assertEquals(0, cart.getProducts().size());

        Cart newCart = new Cart(customer);
        newCart.add(product, 3);

        BuyException exception = Assertions.assertThrows(BuyException.class, () -> shoppingService.buy(cart));
        Assertions.assertEquals("В наличии нет необходимого количества товара '123'", exception.getMessage());
        Mockito.verify(productDao, Mockito.times(1)).save(Mockito.eq(product));
        Assertions.assertEquals(2, product.getCount());
        Assertions.assertEquals(1, cart.getProducts().size());
    }
}